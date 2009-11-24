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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.dbbuilder;

import java.io.File;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.lang.reflect.Method;

import com.silverpeas.FileUtil.StringUtil;
import com.silverpeas.dbbuilder.sql.ConnectionFactory;
import com.silverpeas.dbbuilder.sql.QueryExecutor;
import com.silverpeas.dbbuilder.sql.DbProcParameter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Enumeration;
import java.util.Properties;
import com.silverpeas.dbbuilder.dbbuilder_dl.DbBuilderDynamicPart;

import java.sql.ResultSet;

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
  protected Connection connection = null;

  // Contructeur utilisé pour une pièce de type fichier
  public DBBuilderPiece(String pieceName, String actionName, boolean traceMode)
      throws Exception {
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
        DBBuilder.displayMessageln(DBBuilder.NEW_LINE + "\t\t***Unable to load : " + pieceName);
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
  public DBBuilderPiece(String pieceName, String actionName, String content,
      boolean traceMode) throws Exception {
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
  public DBBuilderPiece(String actionInternalID, String pieceName,
      String actionName, int itemOrder, boolean traceMode) throws Exception {
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

  /*
   * retourne le contenu du fichier
   */
  public String getContent() {

    // retourne le contenu chargé
    return content;
  }

  /*
   * retourne si oui/non mode trace
   */
  public boolean getTraceMode() {

    // retourne le mode de trace
    return traceMode;
  }

  public abstract void setInstructions();

  public abstract void cacheIntoDB(Connection connection,String _package, int _itemOrder)
      throws Exception;

  public Instruction[] getInstructions() {

    return instructions;
  }

  public void traceInstructions() {
    for (int i = 0; i < instructions.length; i++) {
      System.out.println(instructions[i].getInstructionText());
    }
  }

  /**
   * Execute via JDBC la séquence d'instructions élémentaires conservées sur instructions[]
   */
  public void executeInstructions(Connection connection) throws Exception {
    setConnection(connection);
    String currentInstruction = null;
    // try {
    for (int i = 0; i < instructions.length; i++) {
      currentInstruction = instructions[i].getInstructionText();
      if (instructions[i].getInstructionType() == Instruction.IN_UPDATE) {
        executeSingleUpdate(currentInstruction);
      } else if (instructions[i].getInstructionType() == Instruction.IN_CALLDBPROC) {
        executeSingleProcedure(currentInstruction,
            (DbProcParameter[]) instructions[i].getInstructionDetail());
      } else if (instructions[i].getInstructionType() == Instruction.IN_INVOKEJAVA) {
        executeJavaInvoke(currentInstruction, instructions[i].getInstructionDetail());
      }
    }
  }

  // Cache en BD via JDBC une séquence de désinstallation
  // le paramètre est la liste des valeurs à insérer dans la table
  // SR_UNINSTITEMS
  public void cacheIntoDB(Connection connexion, String _package, int _itemOrder, String _pieceType,
      String _delimiter, Integer _keepDelimiter, String _dbProcName)
      throws Exception {
    setConnection(connexion);
    PreparedStatement pstmt = null;
    try {
      // insertion SR_UNINSTITEMS
      Long theLong = new Long(System.currentTimeMillis());
      String itemID = theLong.toString() + "-" + getIncrement().toString();
      pstmt = connexion.prepareStatement("insert into SR_UNINSTITEMS(SR_ITEM_ID, "
          + "SR_PACKAGE, SR_ACTION_TAG, SR_ITEM_ORDER, SR_FILE_NAME, SR_FILE_TYPE, SR_DELIMITER, "
          + "SR_KEEP_DELIMITER, SR_DBPROC_NAME) values ( ?, ?, ?, ?, ?, ?, ?, ?, ?)");
      pstmt.setString(1, itemID);
      pstmt.setString(2, _package);
      pstmt.setString(3, actionName);
      pstmt.setInt(4, _itemOrder);
      pstmt.setString(5, pieceName);
      pstmt.setString(6, _pieceType);
      pstmt.setString(7, _delimiter);
      pstmt.setInt(8, _keepDelimiter);
      pstmt.setString(9, _dbProcName);
      pstmt.executeUpdate();
      // insertion SR_SCRIPTS
      String[] subS = getSubStrings(content);
      pstmt = connexion.prepareStatement("insert into SR_SCRIPTS(SR_ITEM_ID, SR_SEQ_NUM, SR_TEXT) "
          + "values (?, ?, ? )");
      for (int i = 0; i < subS.length; i++) {
        pstmt.setString(1, itemID);
        pstmt.setInt(2, i);
        pstmt.setString(3, subS[i]);
        pstmt.executeUpdate();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new Exception("\n\t\t***ERROR RETURNED BY THE RDBMS : "
          + ex.getMessage() + "\n", ex);
    } finally {
      if (pstmt != null) {
        pstmt.close();
      }      
    }
  }

  public void executeSingleUpdate(String currentInstruction) throws Exception {
    String printableInstruction = null;
    if (traceMode) {
      printableInstruction = StringUtil.sReplace("\r\n", " ", currentInstruction);
      printableInstruction = StringUtil.sReplace("\t", " ", printableInstruction);
      if (printableInstruction.length() > 147) {
        printableInstruction = printableInstruction.substring(0, 146) + "...";
      }
      DBBuilder.displayMessageln("\t\t>" + printableInstruction);
    }
    try {
      java.sql.Statement stmt = connection.createStatement();
      stmt.executeUpdate(currentInstruction);
      stmt.close();
    } catch (Exception e) {
      throw new Exception("\n\t\t***ERROR RETURNED BY THE RDBMS : "
          + e.getMessage() + "\n\t\t***STATEMENT ON ERROR IS : " + "\n"
          + currentInstruction);
    }
  }

  public void executeSingleProcedure(String currentInstruction,
      DbProcParameter[] params) throws Exception {
    String printableInstruction = null;
    if (traceMode) {
      printableInstruction = StringUtil.sReplace("\n", " ", currentInstruction);
      printableInstruction = StringUtil.sReplace("\t", " ",
          printableInstruction);
      if (printableInstruction.length() > 147) {
        printableInstruction = printableInstruction.substring(0, 146) + "...";
      }
      DBBuilder.displayMessageln("\t\t>" + printableInstruction);
    }
    try {
      QueryExecutor.executeProcedure(connection, currentInstruction, params);
    } catch (Exception e) {
      throw new Exception("\n\t\t***ERROR RETURNED BY THE RDBMS : "
          + e.getMessage() + "\n\t\t***STATEMENT ON ERROR IS : " + "\n"
          + currentInstruction);
    }
  }

  public void executeJavaInvoke(String currentInstruction, Object myClass)
      throws Exception {
    if (traceMode) {
      DBBuilder.displayMessageln("\t\t>" + myClass.getClass().getName() + "."
          + currentInstruction + "()");
    }
    ((DbBuilderDynamicPart) myClass).setConnection(connection);
    Method methode = myClass.getClass().getMethod(currentInstruction, new Class[]{});
    if (methode == null) {
      throw new Exception("No method \"" + currentInstruction
          + "\" defined for \"" + myClass.getClass().getName() + "\" class.");
    }
    try {
      methode.invoke(myClass, new Class[]{});
    } catch (Exception e) {
      throw new Exception("\n\t\t***ERROR RETURNED BY THE JVM : "
          + e.getMessage());
    }
  }

  private String getSqlStringValue(String s) {
    if (s == null) {
      return s;
    }
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
    increment = new Integer(i);
    return increment;
  }

  private String getContentFromDB(String itemID) throws Exception {
    Connection connexion = null;
    StringBuilder dbContent = new StringBuilder("");
    try {
      connexion = ConnectionFactory.getConnection();
      PreparedStatement pstmt = connexion.prepareStatement("select SR_SEQ_NUM, SR_TEXT from " +
          "SR_SCRIPTS where SR_ITEM_ID = ? order by 1");
      pstmt.setString(1, itemID);
      ResultSet rs = pstmt.executeQuery();
      while(rs.next()) {
         dbContent = dbContent.append(rs.getString("SR_TEXT"));
      }
      rs.close();
      pstmt.close();
    } catch (Exception e) {
      throw new Exception("\n\t\t***ERROR RETURNED BY THE JVM : "
          + e.getMessage() + "\n\t\t\t(" + "select SR_SEQ_NUM, SR_TEXT from " +
          "SR_SCRIPTS where SR_ITEM_ID = '"+ itemID +"'  order by 1" + ")");
    }
    return dbContent.toString();
  }

  public void setConnection(Connection connection) {
    this.connection = connection;
  }
}
