/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Titre :        dbBuilder
 * Description :  Builder des BDs Silverpeas
 * Copyright :    Copyright (c) 2001
 * Société :      Silverpeas
 * @author ATH
 * @version 1.0
 * Modifications:
 * 11/2004 - DLE - Modification ordre de passage des scripts (init après contraintes)
 */
package com.silverpeas.dbbuilder;

import com.silverpeas.dbbuilder.util.Configuration;
import com.silverpeas.dbbuilder.util.Action;
import com.silverpeas.file.FileUtil;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.jdom.Element;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.silverpeas.dbbuilder.sql.ConnectionFactory;
import com.silverpeas.dbbuilder.sql.FileInformation;
import com.silverpeas.dbbuilder.sql.InstallSQLInstruction;
import com.silverpeas.dbbuilder.sql.MetaInstructions;
import com.silverpeas.dbbuilder.sql.UninstallInformations;
import com.silverpeas.dbbuilder.sql.RemoveSQLInstruction;
import com.silverpeas.dbbuilder.sql.SQLInstruction;
import com.silverpeas.dbbuilder.sql.UninstallSQLInstruction;
import com.silverpeas.dbbuilder.util.CommandLineParameters;
import com.silverpeas.dbbuilder.util.DatabaseType;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.silverpeas.dbbuilder.DBBuilderFileItem.*;
import static com.silverpeas.dbbuilder.util.Action.*;

/**
 * @Description :
 * @Copyright : Copyright (c) 2001
 * @Société : Silverpeas
 * @author STR
 * @version 1.0
 */
public class DBBuilder {

  static final String NEW_LINE = System.getProperty("line.separator");
  public final static Date TODAY = new java.util.Date();
  // Version application
  public static final String DBBuilderAppVersion = "V5";
  // Fichier log
  protected static File fileLog = null;
  protected static PrintWriter bufLog = null;
  static public final String CREATE_TABLE_TAG = "create_table";
  static public final String CREATE_INDEX_TAG = "create_index";
  static public final String CREATE_CONSTRAINT_TAG = "create_constraint";
  static public final String CREATE_DATA_TAG = "init";
  static public final String DROP_TABLE_TAG = "drop_table";
  static public final String DROP_INDEX_TAG = "drop_index";
  static public final String DROP_CONSTRAINT_TAG = "drop_constraint";
  static public final String DROP_DATA_TAG = "clean";
  private static final String[] TAGS_TO_MERGE_4_INSTALL = {
      DBBuilderFileItem.CREATE_TABLE_TAG,
      DBBuilderFileItem.CREATE_INDEX_TAG,
      DBBuilderFileItem.CREATE_CONSTRAINT_TAG,
      DBBuilderFileItem.CREATE_DATA_TAG };
  private static final String[] TAGS_TO_MERGE_4_UNINSTALL = {
      DBBuilderFileItem.DROP_CONSTRAINT_TAG,
      DBBuilderFileItem.DROP_INDEX_TAG,
      DBBuilderFileItem.DROP_DATA_TAG,
      DBBuilderFileItem.DROP_TABLE_TAG };
  private static final String[] TAGS_TO_MERGE_4_ALL = {
      DBBuilderFileItem.DROP_CONSTRAINT_TAG,
      DBBuilderFileItem.DROP_INDEX_TAG,
      DBBuilderFileItem.DROP_DATA_TAG,
      DBBuilderFileItem.DROP_TABLE_TAG,
      DBBuilderFileItem.CREATE_TABLE_TAG,
      DBBuilderFileItem.CREATE_INDEX_TAG,
      DBBuilderFileItem.CREATE_CONSTRAINT_TAG,
      DBBuilderFileItem.CREATE_DATA_TAG };
  private static final String[] TAGS_TO_MERGE_4_OPTIMIZE = {
      DBBuilderFileItem.DROP_INDEX_TAG,
      DBBuilderFileItem.CREATE_INDEX_TAG };
  protected static final String FIRST_DBCONTRIBUTION_FILE = "dbbuilder-contribution.xml";
  protected static final String MASTER_DBCONTRIBUTION_FILE = "master-contribution.xml";
  protected static final String REQUIREMENT_TAG = "requirement"; // pré requis à vérifier pour
  // prise en comptes
  protected static final String DEPENDENCY_TAG = "dependency"; // ordonnancement à vérifier pour
  // prise en comptes
  protected static final String FILE_TAG = "file";
  protected static final String FILENAME_ATTRIB = "name";
  protected static final String PRODUCT_TAG = "product";
  protected static final String PRODUCTNAME_ATTRIB = "name";
  // mes variables rajoutée
  private static Properties dbBuilderResources = new Properties();
  protected static final String DBBUILDER_MODULE = "dbbuilder";
  // Params
  private static CommandLineParameters params = null;

  /**
   * @param args
   * @see
   */
  public static void main(String[] args) {
    Logger.getLogger("org.springframework").setLevel(Level.SEVERE);
    new ClassPathXmlApplicationContext("classpath:/spring-jdbc-datasource.xml");
    try {
      // Ouverture des traces
      System.out.println("Start Database build using Silverpeas DBBuilder v. "
          + DBBuilderAppVersion + " (" + TODAY + ").");
      fileLog =
          new File(Configuration.getLogDir() + File.separator + "DBBuilder.log");
      fileLog.getParentFile().mkdirs();
      bufLog = new PrintWriter(new BufferedWriter(new FileWriter(fileLog.getAbsolutePath(), true)));
      displayMessageln(NEW_LINE + "*************************************************************");
      displayMessageln("Start Database Build using Silverpeas DBBuilder v. " + DBBuilderAppVersion
          + " (" + TODAY + ").");
      // Lecture des variables d'environnement à partir de dbBuilderSettings
      dbBuilderResources = Configuration.loadResource(
          "/com/stratelia/silverpeas/dbBuilder/settings/dbBuilderSettings.properties");
      // Lecture des paramètres d'entrée
      params = new CommandLineParameters(args);

      if (params.isSimulate() && DatabaseType.ORACLE == params.getDbType()) {
        throw new Exception("Simulate mode is not allowed for Oracle target databases.");
      }

      displayMessageln(NEW_LINE);
      displayMessageln("Parameters are :");
      displayMessage(ConnectionFactory.getConnectionInfo());
      displayMessageln(NEW_LINE);
      displayMessageln("\tAction        : " + params.getAction());
      displayMessageln("\tVerbose mode  : " + params.isVerbose());
      displayMessageln("\tSimulate mode : " + params.isSimulate());
      if (Action.ACTION_CONNECT == params.getAction()) {
        // un petit message et puis c'est tout
        displayMessageln(NEW_LINE);
        displayMessageln("Connection to database successfull.");
        System.out.println(NEW_LINE + "Connection to database successfull.");
      } else {
        // Modules en place sur la BD avant install
        displayMessageln(NEW_LINE + "DB Status before build :");
        List<String> packagesIntoDB = checkDBStatus();
        // initialisation d'un vecteur des instructions SQL à passer en fin d'upgrade
        // pour mettre à niveau les versions de modules en base
        MetaInstructions sqlMetaInstructions = new MetaInstructions();
        File dirXml = new File(params.getDbType().getDBContributionDir());
        DBXmlDocument destXml = new DBXmlDocument(dirXml, MASTER_DBCONTRIBUTION_FILE);
        if (!destXml.getPath().exists()) {
          destXml.getPath().createNewFile();
          BufferedWriter destXmlOut =
              new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destXml.getPath(),
              false), "UTF-8"));
          destXmlOut.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
          destXmlOut.newLine();
          destXmlOut.write("<allcontributions>");
          destXmlOut.newLine();
          destXmlOut.write("</allcontributions>");
          destXmlOut.newLine();
          destXmlOut.flush();
          destXmlOut.close();
        }
        destXml.load();
        UninstallInformations processesToCacheIntoDB = new UninstallInformations();

        File[] listeFileXml = dirXml.listFiles();
        Arrays.sort(listeFileXml);

        List<DBXmlDocument> listeDBXmlDocument = new ArrayList<DBXmlDocument>(listeFileXml.length);

        // Ouverture de tous les fichiers de configurations
        displayMessageln(NEW_LINE);
        displayMessageln("Ignored contribution files are :");
        int ignoredFiles = 0;

        for (File f : listeFileXml) {
          if (f.isFile() && "xml".equals(FileUtil.getExtension(f))
              && !(FIRST_DBCONTRIBUTION_FILE.equalsIgnoreCase(f.getName()))
              && !(MASTER_DBCONTRIBUTION_FILE.equalsIgnoreCase(f.getName()))) {
            DBXmlDocument fXml = new DBXmlDocument(dirXml, f.getName());
            fXml.load();
            // vérification des dépendances
            // & prise en compte uniquement si dependences OK
            if (!checkRequired(listeFileXml, fXml)) {
              displayMessageln("\t" + f.getName() + " (because of unresolved requirements).");
              ignoredFiles++;
            } else if (ACTION_ENFORCE_UNINSTALL == params.getAction()) {
              displayMessageln("\t" + f.getName() + " (because of " + ACTION_ENFORCE_UNINSTALL
                  + " mode).");
              ignoredFiles++;
            } else {
              listeDBXmlDocument.add(fXml);
            }
          }
        }
        if (ignoredFiles == 0) {
          displayMessageln("\t(none)");
        }

        // prépare une HashMap des modules présents en fichiers de contribution
        Map packagesIntoFile = new HashMap();
        // ATH à compléter ici algo de traitement de l'ordonnancement
        try {
          DBXmlDocument[] bidon = checkDependencies(listeDBXmlDocument);
        } catch (Exception e) {
          e.printStackTrace();
        }
        List<DBXmlDocument> orderedlisteDBXmlDocument = listeDBXmlDocument;

        int j = 0;
        displayMessageln(NEW_LINE);
        displayMessageln("Merged contribution files are :");
        displayMessageln(params.getAction().toString());
        if (ACTION_ENFORCE_UNINSTALL != params.getAction()) {
          displayMessageln("\t" + FIRST_DBCONTRIBUTION_FILE);
          j++;
        }
        for (DBXmlDocument currentDoc : orderedlisteDBXmlDocument) {
          displayMessageln("\t" + currentDoc.getName());
          j++;
        }
        if (j == 0) {
          displayMessageln("\t(none)");
        }
        // merge des diffrents fichiers de contribution éligibles :
        displayMessageln(NEW_LINE);
        displayMessageln("Build decisions are :");
        // d'abord le fichier dbbuilder-contribution ...
        DBXmlDocument fileXml = null;
        if (ACTION_ENFORCE_UNINSTALL != params.getAction()) {
          try {
            fileXml = new DBXmlDocument(dirXml, FIRST_DBCONTRIBUTION_FILE);
            fileXml.load();
          } catch (Exception e) {
            // contribution de dbbuilder non trouve -> on continue, on est certainement en train
            // de desinstaller la totale
            fileXml = null;
          }
          if (fileXml != null) {
            DBBuilderFileItem dbbuilderItem = new DBBuilderFileItem(fileXml);
            packagesIntoFile.put(dbbuilderItem.getModule(), null);
            mergeActionsToDo(dbbuilderItem, destXml, processesToCacheIntoDB, sqlMetaInstructions);
          }
        }

        // ... puis les autres
        for (DBXmlDocument currentDoc : orderedlisteDBXmlDocument) {
          DBBuilderFileItem tmpdbbuilderItem = new DBBuilderFileItem(currentDoc);
          packagesIntoFile.put(tmpdbbuilderItem.getModule(), null);
          mergeActionsToDo(tmpdbbuilderItem, destXml, processesToCacheIntoDB, sqlMetaInstructions);
        }

        // ... et enfin les pièces BD à désinstaller
        // ... attention, l'ordonnancement n'étant pas dispo, on les traite dans
        // l'ordre inverse pour faire passer busCore a la fin, de nombreuses contraintes
        // des autres modules referencant les PK de busCore
        List<String> itemsList = new ArrayList<String>();

        boolean foundDBBuilder = false;
        for (String p : packagesIntoDB) {
          if (!packagesIntoFile.containsKey(p)) {
            // Package en base et non en contribution -> candidat à desinstallation
            if (DBBUILDER_MODULE.equalsIgnoreCase(p)) // le module a desinstaller est dbbuilder, on
            // le garde sous le coude pour le traiter en dernier
            {
              foundDBBuilder = true;
            } else if (ACTION_ENFORCE_UNINSTALL == params.getAction()) {
              if (p.equals(params.getModuleName())) {
                itemsList.add(0, p);
              }
            } else {
              itemsList.add(0, p);
            }
          }
        }

        if (foundDBBuilder) {
          if (ACTION_ENFORCE_UNINSTALL == params.getAction()) {
            if (DBBUILDER_MODULE.equals(params.getModuleName())) {
              itemsList.add(itemsList.size(), DBBUILDER_MODULE);
            }
          } else {
            itemsList.add(itemsList.size(), DBBUILDER_MODULE);
          }
        }
        for (String p : itemsList) {
          displayMessageln("**** Treating " + p + " ****");
          DBBuilderDBItem tmpdbbuilderItem = new DBBuilderDBItem(p);
          mergeActionsToDo(tmpdbbuilderItem, destXml, processesToCacheIntoDB, sqlMetaInstructions);
        }

        // Trace
        destXml.setName("res.txt");
        destXml.save();

        displayMessageln(NEW_LINE + "Build parts are :");

        // Traitement des pièces sélectionnées
        // remarque : durant cette phase, les erreurs sont traitées -> on les catche en
        // retour sans les retraiter
        if (ACTION_INSTALL == params.getAction()) {
          processDB(destXml, processesToCacheIntoDB, sqlMetaInstructions, TAGS_TO_MERGE_4_INSTALL);
        } else if (ACTION_UNINSTALL == params.getAction()
            || ACTION_ENFORCE_UNINSTALL == params.getAction()) {
          processDB(destXml, processesToCacheIntoDB, sqlMetaInstructions, TAGS_TO_MERGE_4_UNINSTALL);
        } else if (ACTION_OPTIMIZE == params.getAction()) {
          processDB(destXml, processesToCacheIntoDB, sqlMetaInstructions, TAGS_TO_MERGE_4_OPTIMIZE);
        } else if (ACTION_ALL == params.getAction()) {
          processDB(destXml, processesToCacheIntoDB, sqlMetaInstructions, TAGS_TO_MERGE_4_ALL);
        } else if (ACTION_STATUS == params.getAction()) {
          // nothing to do
        } else if (ACTION_CONSTRAINTS_INSTALL == params.getAction()) {
          // nothing to do
        } else if (ACTION_CONSTRAINTS_UNINSTALL == params.getAction()) {
          // nothing to do
        }
        // Modules en place sur la BD en final
        displayMessageln(NEW_LINE + "Finally DB Status :");
        checkDBStatus();

      }

      displayMessageln(NEW_LINE);
      displayMessageln("Database build SUCCESSFULL (" + TODAY + ").");
      System.out.println(NEW_LINE + "Database Build SUCCESSFULL (" + TODAY + ").");

    } catch (Exception e) {
      e.printStackTrace();
      printError(e.getMessage(), e);
      displayMessageln(e.getMessage());
      // e.printStackTrace();
      displayMessageln(NEW_LINE);
      displayMessageln("Database Build FAILED (" + TODAY + ").");
      System.out.println(NEW_LINE + "Database Build FAILED (" + TODAY + ").");
    } finally {
      bufLog.close();
    }
  } // main

  // ---------------------------------------------------------------------
  public static void printError(String errMsg, Exception ex) {
    printError(errMsg);
    if (bufLog != null) {
      ex.printStackTrace(bufLog);
      bufLog.close();
    }
  }

  public static void printError(String errMsg) {
    if (bufLog != null) {
      displayMessageln(NEW_LINE);
      displayMessageln(errMsg);
      bufLog.close();
    }
    System.out.println(NEW_LINE + errMsg + NEW_LINE);
  }

  public static void displayMessageln(String msg) {
    displayMessage(msg);
    displayMessage(NEW_LINE);
  }

  public static void displayMessage(String msg) {
    if (bufLog != null) {
      bufLog.print(msg);
      System.out.print(".");
    } else {
      System.out.print(msg);
    }
  }

  // ---------------------------------------------------------------------
  private static boolean checkRequired(File[] listeFileXml, DBXmlDocument fXml) {

    // liste des dépendences
    Element root = fXml.getDocument().getRootElement(); // Get the root element
    @SuppressWarnings("unchecked")
    List<Element> listeDependencies = root.getChildren(REQUIREMENT_TAG);
    if (listeDependencies != null) {
      for (Element eltDependencies : listeDependencies) {
        @SuppressWarnings("unchecked")
        List<Element> listeDependencyFiles = eltDependencies.getChildren(FILE_TAG);
        for (Element eltDependencyFile : listeDependencyFiles) {
          String name = eltDependencyFile.getAttributeValue(FILENAME_ATTRIB);
          boolean found = false;
          for (int i = 0; i < listeFileXml.length; i++) {
            File f = listeFileXml[i];
            if (f.getName().equals(name)) {
              found = true;
              i = listeFileXml.length;
            }
          }
          if (!found) {
            return false;
          }
        }
      }
    }
    return true;
  }

  // ---------------------------------------------------------------------

  /*
   * Construit une hashmap des dépendances (hDep) : pour chaque item, la clé est le nom du produit
   * (ie module), et la valeur un vecteur de string, chacun étant le nom du produit en dépendance
   */
  private static DBXmlDocument[] checkDependencies(List<DBXmlDocument> tfXml) {
    Map<String, List<String>> hDep = new HashMap<String, List<String>>();
    for (DBXmlDocument fXml : tfXml) {
      if (fXml != null) {
        // liste des dépendences
        Element root = fXml.getDocument().getRootElement(); // Get the root element
        String moduleNameAtt = root.getAttributeValue(DBBuilderFileItem.MODULENAME_ATTRIB);
        @SuppressWarnings("unchecked")
        List<Element> listeDependencies = (List<Element>) root.getChildren(DEPENDENCY_TAG);
        List<String> aDependencies = new ArrayList<String>();
        if (listeDependencies != null) {
          int j = 0;
          for (Element eltDependencies : listeDependencies) {
            @SuppressWarnings("unchecked")
            List<Element> listeDependencyFiles =
                (List<Element>) eltDependencies.getChildren(PRODUCT_TAG);
            for (Element eltDependencyFile : listeDependencyFiles) {
              String name = eltDependencyFile.getAttributeValue(PRODUCTNAME_ATTRIB);
              aDependencies.add(name);
              j++;
            }
          }
        }
        hDep.put(moduleNameAtt, aDependencies);
      }
    }
    return null;
  }

  // ---------------------------------------------------------------------
  // Accesseurs
  // ---------------------------------------------------------------------
  public static Properties getdbBuilderResources() {
    return dbBuilderResources;
  }

  private static void mergeActionsToDo(DBBuilderItem pdbbuilderItem, DBXmlDocument xmlFile,
      UninstallInformations processesToCacheIntoDB, MetaInstructions sqlMetaInstructions) {

    String package_name = pdbbuilderItem.getModule();
    String versionDB = null;
    String versionFile = null;
    try {
      versionDB = pdbbuilderItem.getVersionFromDB();
      versionFile = pdbbuilderItem.getVersionFromFile();
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
    String[] tags_to_merge = null;
    VersionTag[] blocks_merge = null;

    if (pdbbuilderItem instanceof com.silverpeas.dbbuilder.DBBuilderFileItem) {
      DBBuilderFileItem dbbuilderItem = (DBBuilderFileItem) pdbbuilderItem;
      int iversionDB = -1;
      if (!versionDB.equals(DBBuilderFileItem.NOTINSTALLED)) {
        iversionDB = new Integer(versionDB).intValue();
      }
      int iversionFile = new Integer(versionFile).intValue();
      if (iversionDB == iversionFile) {
        if (ACTION_INSTALL == params.getAction() || ACTION_UNINSTALL == params.getAction()
            || ACTION_STATUS == params.getAction()
            || ACTION_CONSTRAINTS_INSTALL == params.getAction()
            || ACTION_CONSTRAINTS_UNINSTALL == params.getAction()) {
          displayMessageln("\t" + package_name + " is up to date with version " + versionFile + ".");
        } else {
          displayMessageln("\t" + package_name + " is up to date with version " + versionFile
              + " and will be optimized.");
          tags_to_merge = TAGS_TO_MERGE_4_OPTIMIZE;
          blocks_merge = new VersionTag[1];
          blocks_merge[0] = new VersionTag(CURRENT_TAG, versionFile);
        }
      } else if (iversionDB > iversionFile) {
        displayMessageln("\t" + package_name
            + " will be ignored because this package is newer into DB than installed files.");
      } else {
        if (ACTION_INSTALL == params.getAction() || ACTION_ALL == params.getAction()
            || ACTION_STATUS == params.getAction()
            || ACTION_CONSTRAINTS_INSTALL == params.getAction()
            || ACTION_CONSTRAINTS_UNINSTALL == params.getAction()) {
          if (iversionDB == -1) {
            displayMessageln("\t" + package_name + " will be installed with version "
                + versionFile + ".");
            tags_to_merge = TAGS_TO_MERGE_4_INSTALL;
            blocks_merge = new VersionTag[1];
            blocks_merge[0] = new VersionTag(CURRENT_TAG, versionFile);
            // module nouvellement installé -> il faut stocker en base sa procedure de uninstall
            processesToCacheIntoDB.addInformation(dbbuilderItem.getModule(), package_name,
                dbbuilderItem.getFileXml());
            // inscription du module en base
            sqlMetaInstructions.addInstruction(dbbuilderItem.getModule(),
                new InstallSQLInstruction(
                versionFile, package_name));
          } else {
            displayMessageln("\t" + package_name + " will be upgraded from " + versionDB + " to "
                + versionFile + ".");
            tags_to_merge = TAGS_TO_MERGE_4_INSTALL;

            blocks_merge = new VersionTag[iversionFile - iversionDB];
            for (int i = 0; i < iversionFile - iversionDB; i++) {
              String sversionFile = "000" + (iversionDB + i);
              sversionFile = sversionFile.substring(sversionFile.length() - 3);
              blocks_merge[i] = new VersionTag(DBBuilderFileItem.PREVIOUS_TAG, sversionFile);
            }
            // module upgradé -> il faut stocker en base sa nouvelle procedure de uninstall
            processesToCacheIntoDB.addInformation(dbbuilderItem.getModule(), package_name,
                dbbuilderItem.getFileXml());

            // desinscription du module en base
            sqlMetaInstructions.addInstruction(dbbuilderItem.getModule(),
                new UninstallSQLInstruction(
                versionFile, package_name));
          }
        } else if (ACTION_OPTIMIZE == params.getAction()) {
          displayMessageln("\t" + package_name + " will be optimized.");
          tags_to_merge = TAGS_TO_MERGE_4_OPTIMIZE;
          blocks_merge = new VersionTag[1];
          blocks_merge[0] = new VersionTag(DBBuilderFileItem.CURRENT_TAG, versionFile);
        }

        // construction du xml global des actions d'upgrade de la base
        if (blocks_merge != null && tags_to_merge != null) {
          try {
            xmlFile.mergeWith(pdbbuilderItem, tags_to_merge, blocks_merge);
          } catch (Exception e) {
            displayMessage("Error with " + pdbbuilderItem.getModule() + " " + e.getMessage());
            e.printStackTrace();
          }
        }
      }

    } else if (pdbbuilderItem instanceof com.silverpeas.dbbuilder.DBBuilderDBItem) {

      if (ACTION_UNINSTALL == params.getAction() || ACTION_ALL == params.getAction()
          || ACTION_ENFORCE_UNINSTALL == params.getAction()) {
        displayMessageln("\t" + package_name + " will be uninstalled.");
        tags_to_merge = TAGS_TO_MERGE_4_UNINSTALL;
        // desinscription du module de la base
        if (!DBBUILDER_MODULE.equalsIgnoreCase(package_name)) {
          System.out.println("delete from SR_");
          sqlMetaInstructions.addInstruction(pdbbuilderItem.getModule(), new RemoveSQLInstruction(
              package_name));
        }
        // construction du xml global des actions d'upgrade de la base
        if (tags_to_merge != null) {
          try {
            xmlFile.mergeWith(pdbbuilderItem, tags_to_merge, null);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      displayMessageln("");
      displayMessageln("*** AVERTISSEMENT ***");
      displayMessageln("\t Le Module " + package_name
          + " est présent en BD mais n'a pas de scripts SQL fichiers");
      displayMessageln("");
      System.out.println("");
      System.out.println("*** AVERTISSEMENT ***");
      System.out.println("Le Module " + package_name
          + " est présent en BD mais n'a pas de scripts SQL fichiers");
    }

  }

  private static void processDB(DBXmlDocument xmlFile,
      UninstallInformations processesToCacheIntoDB,
      MetaInstructions sqlMetaInstructions, String[] tagsToProcess) throws Exception {
    // ------------------------------------------
    // ETAPE 1 : TRAITEMENT DES ACTIONS D'UPGRADE
    // ------------------------------------------
    // Get the root element
    Element root = xmlFile.getDocument().getRootElement();
    @SuppressWarnings("unchecked")
    List<Element> modules = root.getChildren(DBXmlDocument.ELT_MODULE);
    for (Element module : modules) {
      Connection connection = null;
      try {
        connection = ConnectionFactory.getConnection();
        connection.setAutoCommit(false);
        processSQLFiles(connection, module, tagsToProcess, sqlMetaInstructions);
        cacheIntoDb(connection, processesToCacheIntoDB.getInformations(module.getAttributeValue(
            DBXmlDocument.ATT_MODULE_ID)));
        if (params.isSimulate()) {
          connection.rollback();
        } else {
          connection.commit();
        }
      } catch (Exception e) {
        try {
          if (connection != null) {
            connection.rollback();
          }
        } catch (SQLException sqlex) {
        }
        throw e;
      } finally {
        try {
          if (connection != null) {
            connection.close();
          }
        } catch (SQLException sqlex) {
        }
      }
    }
    displayMessageln("DB Status after build :");
    checkDBStatus();
  }

  // liste des packages en base
  private static List<String> checkDBStatus() {
    List<String> packagesIntoDB = new ArrayList<String>();
    Connection connection = null;
    try {
      connection = ConnectionFactory.getConnection();
      Statement stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(
          "select SR_PACKAGE, SR_VERSION from SR_PACKAGES order by SR_PACKAGE");
      while (rs.next()) {
        String srPackage = rs.getString("SR_PACKAGE");
        String srVersion = rs.getString("SR_VERSION");
        displayMessageln("\t" + srPackage + " v. " + srVersion);
        packagesIntoDB.add(srPackage);
      }
      rs.close();
      stmt.close();
    } catch (SQLException sqlex) {
    } finally {
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException sqlex) {
        }
      }
    }
    return packagesIntoDB;
  }

  private static void processSQLFiles(final Connection connection, final Element moduleRoot,
      final String[] tagsToProcess, final MetaInstructions metaInstructions) throws Exception {
    // piece de DB Builder
    DBBuilderPiece dbBuilderPiece = null;
    for (int i = 0; i < tagsToProcess.length; i++) {
      int nbFiles = 0;
      // liste des pièces correspondant au i-eme tag a traiter
      final List<Element> listeTags = (List<Element>) moduleRoot.getChildren(tagsToProcess[i]);
      for (Element eltTag : listeTags) {
        final String nomTag = eltTag.getName();
        // -------------------------------------
        // TRAITEMENT DES PIECES DE TYPE DB
        // -------------------------------------
        final List<Element> listeRowFiles = (List<Element>) eltTag.getChildren(ROW_TAG);
        for (Element eltFile : listeRowFiles) {
          String name = eltFile.getAttributeValue(FILENAME_ATTRIB);
          String value = eltFile.getAttributeValue(FILETYPE_ATTRIB);
          Integer order = new Integer(eltFile.getAttributeValue(DBORDER_ATTRIB));
          String delimiter = eltFile.getAttributeValue(FILEDELIMITER_ATTRIB);
          String skeepdelimiter = eltFile.getAttributeValue(FILEKEEPDELIMITER_ATTRIB);
          String dbprocname = eltFile.getAttributeValue(FILEDBPROCNAME_ATTRIB);
          boolean keepdelimiter = "YES".equals(skeepdelimiter);
          displayMessageln("\t" + tagsToProcess[i] + " : internal-id : " + name + "\t type : "
              + value);
          nbFiles++;
          if (FILEATTRIBSTATEMENT_VALUE.equals(value)) {
            // piece de type Single Statement
            dbBuilderPiece =
                new DBBuilderSingleStatementPiece(name, name + "(" + order + ")", nomTag, order.
                intValue(), params.isVerbose());
          } else if (FILEATTRIBSEQUENCE_VALUE.equals(value)) {
            // piece de type Single Statement
            dbBuilderPiece =
                new DBBuilderMultipleStatementPiece(name, name + "(" + order + ")", nomTag, order.
                intValue(), params.isVerbose(), delimiter, keepdelimiter);
          } else if (FILEATTRIBDBPROC_VALUE.equals(value)) {
            // piece de type Database Procedure
            dbBuilderPiece =
                new DBBuilderDBProcPiece(name, name + "(" + order + ")", nomTag, order.intValue(),
                params.isVerbose(), dbprocname);

          }
          if (dbBuilderPiece != null) {
            dbBuilderPiece.executeInstructions(connection);
          }
        }

        // -------------------------------------
        // TRAITEMENT DES PIECES DE TYPE FICHIER
        // -------------------------------------
        final List<Element> listeFiles = (List<Element>) eltTag.getChildren(FILE_TAG);
        for (Element eltFile : listeFiles) {
          String name = getCleanPath(eltFile.getAttributeValue(FILENAME_ATTRIB));
          String value = eltFile.getAttributeValue(FILETYPE_ATTRIB);
          String delimiter = eltFile.getAttributeValue(FILEDELIMITER_ATTRIB);
          String skeepdelimiter =
              eltFile.getAttributeValue(FILEKEEPDELIMITER_ATTRIB);
          String dbprocname = eltFile.getAttributeValue(FILEDBPROCNAME_ATTRIB);
          boolean keepdelimiter = (skeepdelimiter != null && skeepdelimiter.equals("YES"));
          String classname = eltFile.getAttributeValue(FILECLASSNAME_ATTRIB);
          String methodname = eltFile.getAttributeValue(FILEMETHODNAME_ATTRIB);
          displayMessageln("\t" + tagsToProcess[i] + " : name : " + name + "\t type : " + value);
          nbFiles++;
          if (FILEATTRIBSTATEMENT_VALUE.equals(value)) {
            // piece de type Single Statement
            dbBuilderPiece =
                new DBBuilderSingleStatementPiece(
                Configuration.getPiecesFilesDir() + File.separatorChar + name,
                nomTag, params.isVerbose());
          } else if (FILEATTRIBSEQUENCE_VALUE.equals(value)) {
            dbBuilderPiece =
                new DBBuilderMultipleStatementPiece(
                Configuration.getPiecesFilesDir() + File.separatorChar + name,
                nomTag, params.isVerbose(), delimiter, keepdelimiter);
          } else if (FILEATTRIBDBPROC_VALUE.equals(value)) {
            // piece de type Database Procedure
            dbBuilderPiece =
                new DBBuilderDBProcPiece(
                Configuration.getPiecesFilesDir() + File.separatorChar + name, nomTag,
                params.isVerbose(), dbprocname);
          } else if (FILEATTRIBJAVALIB_VALUE.equals(value)) {
            // piece de type Java invoke
            dbBuilderPiece =
                new DBBuilderDynamicLibPiece(
                Configuration.getPiecesFilesDir() + File.separatorChar + name,
                nomTag, params.isVerbose(), classname, methodname);
          }
          if (dbBuilderPiece != null) {
            dbBuilderPiece.executeInstructions(connection);
          }
        }
      }
      if (nbFiles == 0) {
        displayMessageln("\t" + tagsToProcess[i] + " : (none)");
      }
    }
    final List<SQLInstruction> sqlMetaInstructions =
        metaInstructions.getInstructions(moduleRoot.getAttributeValue(
        DBXmlDocument.ATT_MODULE_ID));
    // Mise à jour des versions en base
    if (sqlMetaInstructions.isEmpty()) {
      displayMessageln("\tdbbuilder meta base maintenance : (none)");
    } else {
      displayMessageln("\tdbbuilder meta base maintenance :");
      for (SQLInstruction instruction : sqlMetaInstructions) {
        instruction.execute(connection);
      }
    }
  }

  protected static void cacheIntoDb(Connection connection, List<FileInformation> informations)
      throws Exception {
    // ------------------------------------------------------
    // ETAPE 2 : CACHE EN BASE DES PROCESS DE DESINSTALLATION
    // ------------------------------------------------------
    displayMessageln(System.getProperty("line.separator") + "Uninstall stored parts are :");
    String[] tagsToProcessU = TAGS_TO_MERGE_4_UNINSTALL;
    for (FileInformation information : informations) {
      String pName = information.getSrPackage();
      DBXmlDocument xFile = information.getDocument();
      // Get the root element
      Element rootU = xFile.getDocument().getRootElement();
      int nbFilesU = 0;
      // piece de DB Builder
      DBBuilderPiece pU;
      for (int i = 0; i < tagsToProcessU.length; i++) {
        // liste des pièces correspondant au i-eme tag a traiter
        List<Element> listeTagsCU = rootU.getChildren(DBBuilderFileItem.CURRENT_TAG);
        for (Element eltTagCU : listeTagsCU) {
          List listeTagsU = eltTagCU.getChildren(tagsToProcessU[i]);
          Iterator iterTagsU = listeTagsU.iterator();
          while (iterTagsU.hasNext()) {
            Element eltTagU = (Element) iterTagsU.next();
            List listeFilesU = eltTagU.getChildren(DBBuilderFileItem.FILE_TAG);
            Iterator iterFilesU = listeFilesU.iterator();
            int iFile = 1;
            while (iterFilesU.hasNext()) {
              Element eltFileU = (Element) iterFilesU.next();
              String nameU = getCleanPath(eltFileU.getAttributeValue(
                  DBBuilderFileItem.FILENAME_ATTRIB));
              String valueU = eltFileU.getAttributeValue(DBBuilderFileItem.FILETYPE_ATTRIB);
              String delimiterU =
                  eltFileU.getAttributeValue(DBBuilderFileItem.FILEDELIMITER_ATTRIB);
              String skeepdelimiterU =
                  eltFileU.getAttributeValue(DBBuilderFileItem.FILEKEEPDELIMITER_ATTRIB);
              String dbprocnameU =
                  eltFileU.getAttributeValue(DBBuilderFileItem.FILEDBPROCNAME_ATTRIB);
              boolean keepdelimiterU = (skeepdelimiterU != null && skeepdelimiterU.equals("YES"));
              displayMessageln("\t" + tagsToProcessU[i] + " : name : " + nameU + "\t type : "
                  + valueU);
              if (valueU.equals(FILEATTRIBSTATEMENT_VALUE)) {
                // piece de type Single Statement
                pU =
                    new DBBuilderSingleStatementPiece(Configuration.getPiecesFilesDir() +
                        File.separatorChar
                        + nameU, tagsToProcessU[i], params.isVerbose());
                pU.cacheIntoDB(connection, pName, iFile);
              } else if (valueU.equals(FILEATTRIBSEQUENCE_VALUE)) {
                // piece de type Single Statement
                pU =
                    new DBBuilderMultipleStatementPiece(Configuration.getPiecesFilesDir() +
                        File.separatorChar
                        + nameU, tagsToProcessU[i], params.isVerbose(), delimiterU,
                        keepdelimiterU);
                pU.cacheIntoDB(connection, pName, iFile);
              } else if (valueU.equals(FILEATTRIBDBPROC_VALUE)) {
                // piece de type Database Procedure
                pU = new DBBuilderDBProcPiece(
                    Configuration.getPiecesFilesDir() + File.separatorChar + nameU,
                    tagsToProcessU[i], params.isVerbose(), dbprocnameU);
                pU.cacheIntoDB(connection, pName, iFile);
              }
              iFile++;
              nbFilesU++;
            }
          }
        }
        if (nbFilesU == 0) {
          displayMessageln("\t" + tagsToProcessU[i] + " : (none)");
        }
      }
    }
  }

  private static String getCleanPath(String name) {
    String path = name.replace('/', File.separatorChar);
    return path.replace('\\', File.separatorChar);
  }

  private DBBuilder() {
  }
}
