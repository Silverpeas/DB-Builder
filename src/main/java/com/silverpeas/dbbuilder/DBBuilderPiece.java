/**
 * Titre :        dbBuilderPiece
 * Description :  Superclasse des pièces de contruction de la base de données.
 * Une pièce de construction est un fichier ou une chaîne de caractères contenant une séquence
 * exécutable sur un serveur de données.
 * Cette superclasse offre les services suivants :
 * - chargement du fichier si la pièce est de type fichier
 * - déclaration d'une classe abstraite de transformation de la chaîbe en liste d'instructions
 * interprétables et exécutables par le serveur de données
 * exécution de la séquence d'instructions en relation avec la classe connecteur JDBC com.stratelia.dbConnector.DBConnexion
 * Copyright :    Copyright (c) 2001 - 2002
 * Société :      Stratélia Silverpeas
 * @author ATH
 * @version 1.0
 */
package com.silverpeas.dbbuilder;

import java.io.File;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.*;
import java.lang.reflect.Method;

import com.stratelia.dbConnector.DBConnexion;
import com.stratelia.dbConnector.DbProcParameter;

import com.silverpeas.FileUtil.StringUtil;

public abstract class DBBuilderPiece {

  // identifiant unique pour toute la session
  private static Integer increment = new Integer(0);
  // identifiant de la pièce si elle est stockée en base
  private String actionInternalID = null;
  // nom de la pièce ou du fichier
  private String pieceName = null;
  // contenu initial de la pièce
  private String content = null;
  // nom de l'action
  private String actionName = null;
  // oui ou non fonctionnement en mode trace
  private boolean traceMode = false;
  // contenu interprété en séquence d'instructions
  protected Instruction[] instructions = null;

  // Contructeur utilisé pour une pièce de type fichier
  public DBBuilderPiece(String pieceName, String actionName, boolean traceMode) throws Exception {

    // mémorise le mode trace
    this.traceMode = traceMode;

    // mémorise l'action
    this.actionName = actionName;

    // mémorise le nom de la piece = le nom du fichier
    this.pieceName = pieceName;

    // Charge le contenu sauf pour un package
    if (pieceName.endsWith(".jar")) {
      content = new String("");
    } else {
      // charge son contenu sauf pour un jar qui doit être dans le classpath
      File myFile = new File(pieceName);
      if (!myFile.exists() || !myFile.isFile() || !myFile.canRead()) {
        DBBuilder.displayMessageln("\n\t\t***Unable to load : " + pieceName);
        throw new Exception("Unable to find or load : " + pieceName);
      }
      int fileSize = (int) myFile.length();
      byte[] data = new byte[fileSize];
      DataInputStream in = new DataInputStream(new FileInputStream(pieceName));
      in.readFully(data);
      content = new String(data);
      in.close();
    }

    Properties res = DBBuilder.getdbBuilderResources();
    if (res != null) {
      for (Enumeration e = res.keys(); e.hasMoreElements();) {
        String key = (String) e.nextElement();
        String value = res.getProperty(key);
        content = StringUtil.sReplace("${" + key + "}", value, content);
      }
    }
  }

  // Contructeur utilisé pour une pièce de type chaîne en mémoire
  public DBBuilderPiece(String pieceName, String actionName, String content, boolean traceMode) throws Exception {

    // mémorise le mode trace
    this.traceMode = traceMode;

    // mémorise l'action
    this.actionName = actionName;

    // mémorise le nom du fichier
    this.pieceName = pieceName;

    // mémorise le contenu
    this.content = content;
  }

  // Contructeur utilisé pour une pièce stockée en base de données
  public DBBuilderPiece(String actionInternalID, String pieceName, String actionName, int itemOrder, boolean traceMode) throws Exception {

    // mémorise le mode trace
    this.traceMode = traceMode;

    // mémorise l'action
    this.actionName = actionName;

    // mémorise le nom du fichier
    this.pieceName = pieceName;

    // mémorise l'ID interne de la base
    this.actionInternalID = actionInternalID;

    // charge et mémorise le contenu
    this.content = getContentFromDB(actionInternalID);
  }

  public String getActionInternalID() {

    return actionInternalID;
  }

  public String getPieceName() {

    return pieceName;
  }

  public String getActionName() {

    return actionName;
  }

  /* retourne le contenu du fichier
   */
  public String getContent() {

    // retourne le contenu chargé
    return content;
  }

  /* retourne si oui/non mode trace
   */
  public boolean getTraceMode() {

    // retourne le mode de trace
    return traceMode;
  }

  public abstract void setInstructions();

  public abstract void cacheIntoDB(String _package, int _itemOrder) throws Exception;

  public Instruction[] getInstructions() {

    return instructions;
  }

  public void traceInstructions() {

    for (int i = 0; i < instructions.length; i++) {
      System.out.println(instructions[i].getInstructionText());
    }
  }

  // Execute via JDBC la séquence d'instructions élémentaires conservées sur instructions[]
  public void executeInstructions() throws Exception {

    String currentInstruction = null;

    // try {
    for (int i = 0; i < instructions.length; i++) {
      currentInstruction = instructions[i].getInstructionText();
      if (instructions[i].getInstructionType() == Instruction.IN_UPDATE) // DBConnexion.getInstance().executeUpdate(currentInstruction);
      {
        executeSingleUpdate(currentInstruction);
      } else if (instructions[i].getInstructionType() == Instruction.IN_CALLDBPROC) // DBConnexion.getInstance().executeProcedure(currentInstruction, (DbProcParameter[]) instructions[i].getInstructionDetail());
      {
        executeSingleProcedure(currentInstruction, (DbProcParameter[]) instructions[i].getInstructionDetail());
      } else if (instructions[i].getInstructionType() == Instruction.IN_INVOKEJAVA) {
        executeJavaInvoke(currentInstruction, instructions[i].getInstructionDetail());
      }

    } // for
    // } catch (Exception e) {
    //	System.out.println("DBBuiderPiece.executeInstructions():ERROR:" + currentInstruction);
    //	throw e;
    // } // try
  }

  // Cache en BD via JDBC une séquence de désinstallation
  // le paramètre est la liste des valeurs à insérer dans la table SR_UNINSTITEMS
  public void cacheIntoDB(String _package, int _itemOrder, String _pieceType, String _delimiter, Integer _keepDelimiter, String _dbProcName) throws Exception {

    String currentInstruction = "";

    // try {
    // insertion SR_UNINSTITEMS
    Long theLong = new Long(System.currentTimeMillis());
// System.out.println(theLong);
    String itemID = theLong.toString() + "-" + getIncrement().toString();
// System.out.println(itemID);

    currentInstruction = "insert into SR_UNINSTITEMS(SR_ITEM_ID, SR_PACKAGE, SR_ACTION_TAG, SR_ITEM_ORDER, SR_FILE_NAME, SR_FILE_TYPE, SR_DELIMITER, SR_KEEP_DELIMITER, SR_DBPROC_NAME) " +
        "values (" + getSqlStringValue(itemID) + ", " +
        getSqlStringValue(_package) + ", " +
        getSqlStringValue(actionName) + ", " +
        new Integer(_itemOrder) + ", " +
        getSqlStringValue(pieceName) + ", " +
        getSqlStringValue(_pieceType) + ", " +
        getSqlStringValue(_delimiter) + ", " +
        _keepDelimiter + ", " +
        getSqlStringValue(_dbProcName) + ")";

    // DBConnexion.getInstance().executeUpdate(currentInstruction);
    executeSingleUpdate(currentInstruction);

    // insertion SR_SCRIPTS
    String[] subS = getSubStrings(content);
    for (int i = 0; i < subS.length; i++) {
      currentInstruction = "insert into SR_SCRIPTS(SR_ITEM_ID, SR_SEQ_NUM, SR_TEXT) " +
          "values (" + getSqlStringValue(itemID) + ", " +
          new Integer(i) + ", " +
          getSqlStringValue(subS[i]) + ")";

      // DBConnexion.getInstance().executeUpdate(currentInstruction);
      executeSingleUpdate(currentInstruction);
    } // for

    // } catch (Exception e) {
    //	System.out.println("DBBuiderPiece.cacheIntoDB():ERROR:" + currentInstruction);
    //	throw e;
    // } // try
  }

  public void executeSingleUpdate(String currentInstruction) throws Exception {

    String printableInstruction = null;

    if (traceMode) {
      // printableInstruction = replaceAll(currentInstruction, "\n", " ");
      // printableInstruction = replaceAll(printableInstruction, "\t", " ");

      printableInstruction = StringUtil.sReplace("\r\n", " ", currentInstruction);
      printableInstruction = StringUtil.sReplace("\t", " ", printableInstruction);

      /*
      int i = currentInstruction.indexOf("\n");
      if (i==-1)
      printableInstruction = currentInstruction;
      else
      printableInstruction = currentInstruction.substring(0, i) + "...";
       */

      if (printableInstruction.length() > 147) {
        printableInstruction = printableInstruction.substring(0, 146) + "...";
      }
      DBBuilder.displayMessageln("\t\t>" + printableInstruction);
    }

    try {
      DBConnexion.getInstance().executeUpdate(currentInstruction);
    } catch (Exception e) {

      // DBBuilder.displayMessageln("\n\t\t***ERROR RETURNED BY THE RDBMS : " + e.getMessage());
      // DBBuilder.displayMessageln("\t\t***STATEMENT ON ERROR IS : ");
      // DBBuilder.displayMessageln(currentInstruction);
      throw new Exception("\n\t\t***ERROR RETURNED BY THE RDBMS : " + e.getMessage() +
          "\n\t\t***STATEMENT ON ERROR IS : " +
          "\n" + currentInstruction);
    } // try
  }

  public void executeSingleProcedure(String currentInstruction, DbProcParameter[] params) throws Exception {

    String printableInstruction = null;

    if (traceMode) {
      // printableInstruction = replaceAll(currentInstruction, "\n", " ");
      // printableInstruction = replaceAll(printableInstruction, "\t", " ");
      printableInstruction = StringUtil.sReplace("\n", " ", currentInstruction);
      printableInstruction = StringUtil.sReplace("\t", " ", printableInstruction);
      if (printableInstruction.length() > 147) {
        printableInstruction = printableInstruction.substring(0, 146) + "...";
      }
      DBBuilder.displayMessageln("\t\t>" + printableInstruction);
    }

    try {
      DBConnexion.getInstance().executeProcedure(currentInstruction, params);
    } catch (Exception e) {

      // DBBuilder.displayMessageln("\n\t\t***ERROR RETURNED BY THE RDBMS : " + e.getMessage());
      // DBBuilder.displayMessageln("\t\t***STATEMENT ON ERROR IS : ");
      // DBBuilder.displayMessageln(currentInstruction);
      throw new Exception("\n\t\t***ERROR RETURNED BY THE RDBMS : " + e.getMessage() +
          "\n\t\t***STATEMENT ON ERROR IS : " +
          "\n" + currentInstruction);
    } // try
  }

  public void executeJavaInvoke(String currentInstruction, Object myClass) throws Exception {

    if (traceMode) {
      DBBuilder.displayMessageln("\t\t>" + myClass.getClass().getName() + "." + currentInstruction + "()");
    }

    Method[] methodes = myClass.getClass().getMethods();
    Method m = null;

    for (int i = 0; i < methodes.length; i++) {
      if (methodes[i].getName().equals(currentInstruction)) {
        m = methodes[i];
      }
    }

    if (m == null) {
      // DBBuilder.displayMessageln("\n\t\nNo method \"" + currentInstruction + "\" defined for \"" + myClass.getClass().getName() + "\" class.");
      throw new Exception("No method \"" + currentInstruction + "\" defined for \"" + myClass.getClass().getName() + "\" class.");
    } // if

    try {
      m.invoke(myClass, null);

    } catch (Exception e) {

      // DBBuilder.displayMessageln("\n\t\t***ERROR RETURNED BY THE JVM : " + e.getMessage());
      throw new Exception("\n\t\t***ERROR RETURNED BY THE JVM : " + e.getMessage());
    } // try
  }

  private String getSqlStringValue(String s) {

    if (s == null) {
      return s;
    }
    // return "'" + replaceAll(s, "'", "''") + "'";
    return "'" + StringUtil.sReplace("'", "''", s) + "'";
  }

  private String[] getSubStrings(String str) {

    int maxl = 1100;

    int nbS = str.length() / maxl;
    if ((str.length() - nbS * maxl) > 0) {
      nbS++;
    }

    String tmpS = new String(str);
    String[] retS = new String[nbS];

    for (int i = 0; i < nbS; i++) {
      if (i == nbS - 1) {
        retS[i] = tmpS;
      } else {
        retS[i] = tmpS.substring(0, maxl - 1);
        tmpS = tmpS.substring(maxl - 1);
      }
    }

    return retS;
  }

  private synchronized Integer getIncrement() {

    int i = increment.intValue();
    i++;
    this.increment = new Integer(i);
    return increment;
  }

  private String getContentFromDB(String itemID) throws Exception {

    String selectContentFromDB = "select SR_SEQ_NUM, SR_TEXT from SR_SCRIPTS where SR_ITEM_ID = '" + itemID + "' order by 1";
    String content = "";
    ArrayList textList = null;

    try {
      textList = DBConnexion.getInstance().executeLoopQuery(selectContentFromDB);
    } catch (Exception e) {
      // displayMessageln( "\tIgnore this unfatal error due to empty database." );
      throw new Exception("\n\t\t***ERROR RETURNED BY THE JVM : " + e.getMessage() + "\n\t\t\t(" +
          selectContentFromDB + ")");
    }

    if (textList != null) {

      int nbValues = textList.size();
      if (nbValues > 0) {
        for (int i = 0; i < nbValues; i++) {
          HashMap h = (HashMap) textList.get(i);
          if (h.containsKey("SR_TEXT")) {
            content = content.concat((String) h.get("SR_TEXT"));
          }
        } // for
      }
    } // if

    return content;
  }
}