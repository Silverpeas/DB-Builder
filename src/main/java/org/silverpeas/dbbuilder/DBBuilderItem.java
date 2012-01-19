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

package org.silverpeas.dbbuilder;

/**
 * Titre :        dbBuilder
 * Description :  Builder des BDs Silverpeas
 * Copyright :    Copyright (c) 2001
 * Société :      Stratélia Silverpeas
 * @author ATH
 * @version 1.0
 */
import org.silverpeas.dbbuilder.sql.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;

public abstract class DBBuilderItem {

  static public final String MODULENAME_ATTRIB = "product";
  static public final String CURRENT_TAG = "current";
  static public final String PREVIOUS_TAG = "upgrade";
  static public final String VERSION_ATTRIB = "version";
  static public final String NOTINSTALLED = "xxx";
  static public final String CONTRIBUTION_TAG = "contribution";
  static public final String CREATE_TABLE_TAG = "create_table";
  static public final String CREATE_INDEX_TAG = "create_index";
  static public final String CREATE_CONSTRAINT_TAG = "create_constraint";
  static public final String CREATE_DATA_TAG = "init";
  static public final String DROP_TABLE_TAG = "drop_table";
  static public final String DROP_INDEX_TAG = "drop_index";
  static public final String DROP_CONSTRAINT_TAG = "drop_constraint";
  static public final String DROP_DATA_TAG = "clean";
  static public final String FILE_TAG = "file";
  static public final String ROW_TAG = "row";
  static public final String FILENAME_ATTRIB = "name";
  static public final String FILETYPE_ATTRIB = "type";
  static public final String FILEDELIMITER_ATTRIB = "delimiter";
  static public final String FILEKEEPDELIMITER_ATTRIB = "keepdelimiter";
  static public final String FILEDBPROCNAME_ATTRIB = "dbprocname";
  static public final String FILECLASSNAME_ATTRIB = "classname";
  static public final String FILEMETHODNAME_ATTRIB = "methodname";
  static public final String DBORDER_ATTRIB = "sequence";
  // static public final String FILEATTRIB_ATTRIB = "attrib";
  static public final String FILEATTRIBSTATEMENT_VALUE = "sqlstatement";
  static public final String FILEATTRIBSEQUENCE_VALUE = "sqlstatementlist";
  static public final String FILEATTRIBDBPROC_VALUE = "dbprocedure";
  static public final String FILEATTRIBJAVALIB_VALUE = "javalib";
  private String module = null;
  protected String versionFromFile = null;
  private String versionFromDB = null;
  private DBXmlDocument fileXml;
  private Element root;

  public void setFileXml(DBXmlDocument fileXml) {
    this.fileXml = fileXml;
  }

  public DBXmlDocument getFileXml() {
    return fileXml;
  }

  public void setRoot(Element root) {
    this.root = root;
  }

  public Element getRoot() {
    return root;
  }

  public String getModule() {
    return module;
  }

  protected void setModule(String module) {
    this.module = module;
  }

  public abstract String getVersionFromFile() throws Exception;

  public String getVersionFromDB() throws Exception {
    if (versionFromDB == null) {
      versionFromDB = extractVersionFromDatabase();
    }
    return versionFromDB;
  }

  private String extractVersionFromDatabase() throws SQLException {
    Connection connection = null;
    String version = NOTINSTALLED;
    try {
      connection = ConnectionFactory.getConnection();
      PreparedStatement pstmt = connection.prepareStatement(
          "SELECT SR_VERSION FROM SR_PACKAGES where SR_PACKAGE = ?");
      pstmt.setString(1, module);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        version = rs.getString("SR_VERSION");
      }
      rs.close();
      pstmt.close();
    } catch (SQLException sqlex) {
    } finally {
      if (connection != null) {
        connection.close();
      }
    }
    return version;
  }

  public Element getUniqueBlock(String b, String v) throws Exception {
    List listeCurrent = getRoot().getChildren(b);
    if (listeCurrent == null) {
      throw new Exception(getModule() + ": no <" + b
          + "> tag found for this module into contribution file.");
    }
    if (listeCurrent.size() == 0) {
      throw new Exception(getModule() + ": no <" + b
          + "> tag found for this module into contribution file.");
    }
    Iterator iterCurrent = listeCurrent.iterator();
    Element myElement = null;
    while (iterCurrent.hasNext()) {
      Element eltCurrent = (Element) iterCurrent.next();
      if (eltCurrent.getAttributeValue(DBBuilderFileItem.VERSION_ATTRIB).equals(v)) {
        myElement = eltCurrent;
      }
    } // while
    if (myElement == null) {
      throw new Exception(getModule() + ": no version <" + v + "> for <" + b
          + "> tag found for this module into contribution file.");
    }
    return myElement;
  }
}
