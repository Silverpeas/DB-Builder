/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.dbbuilder;

/**
 * Titre : dbBuilder Description : Builder des BDs Silverpeas Copyright : Copyright (c) 2001 Société
 * : Stratélia Silverpeas
 *
 * @author ATH
 * @version 1.0
 */
import org.silverpeas.dbbuilder.sql.ConnectionFactory;
import org.silverpeas.dbbuilder.sql.QueryExecutor;
import org.silverpeas.dbbuilder.util.Configuration;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class DBBuilderDBItem extends DBBuilderItem {

  private static final String SELECT_CONTENT_FROM_DB =
      "select SR_ITEM_ID, SR_ACTION_TAG, SR_ITEM_ORDER, SR_FILE_NAME, SR_FILE_TYPE, "
      + "SR_DELIMITER, SR_KEEP_DELIMITER, SR_DBPROC_NAME from SR_UNINSTITEMS where SR_PACKAGE = ?"
      + " order by SR_ACTION_TAG, SR_ITEM_ORDER ";
  private List<Map<String, Object>> dbInfos;
  protected static final String TEMP_DBCONTRIBUTION_FILE = "temp-contribution.xml";

  public DBBuilderDBItem(String module) throws Exception {

    super.setModule(module);
    // lecture from base des items
    dbInfos = getContentFromDB();
    // construit un fichier xml temporaire avec toutes les infos nécessaires
    File f = new File(Configuration.getTemp() + File.separator + TEMP_DBCONTRIBUTION_FILE);
    if (!f.getParentFile().exists()) {
      f.getParentFile().mkdirs();
    }
    OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
    try {
      out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      out.write("<" + CONTRIBUTION_TAG + " " + MODULENAME_ATTRIB + "=\"" + module + "\">\n");

      if (dbInfos != null) {
        int nbValues = dbInfos.size();
        if (nbValues > 0) {
          for (int i = 0; i < nbValues; i++) {
            Map<String, Object> h = dbInfos.get(i);
            String tag = (String) h.get("SR_ACTION_TAG");
            out.write("        <" + tag + ">\n");
            out.write("            <" + ROW_TAG + " " + FILENAME_ATTRIB + "=\"" + (String) h.get(
                "SR_ITEM_ID") + "\" ");
            String valueHash = h.get("SR_ITEM_ORDER").toString();

            out.write(DBORDER_ATTRIB + "=\"" + new Integer(valueHash) + "\" ");
            out.write(FILETYPE_ATTRIB + "=\"" + (String) h.get("SR_FILE_TYPE") + "\" ");

            if (h.containsKey("SR_DELIMITER")) {
              if (h.get("SR_DELIMITER") != null) {
                out.write(FILEDELIMITER_ATTRIB + "=\"" + (String) h.get("SR_DELIMITER") + "\" ");
              }
            }
            if (h.containsKey("SR_KEEP_DELIMITER")) {
              if (h.get("SR_KEEP_DELIMITER") != null) {
                valueHash = h.get("SR_KEEP_DELIMITER").toString();
              }
            }
            if ("0".equals(valueHash)) {
              out.write(FILEKEEPDELIMITER_ATTRIB + "=\"NO\" ");
            } else {
              out.write(FILEKEEPDELIMITER_ATTRIB + "=\"YES\" ");
            }
            if (h.containsKey("SR_DBPROC_NAME")) {
              if (h.get("SR_DBPROC_NAME") != null) {
                out.write(FILEDBPROCNAME_ATTRIB + "=\"" + (String) h.get("SR_DBPROC_NAME") + "\" ");
              }
            }
            out.write("/>\n");
            out.write("        </" + tag + ">\n");
          } // for
        }
      } // if

      out.write("</contribution>\n");
    } finally {
      out.close();
    }
    DBXmlDocument destXml = new DBXmlDocument(new File(Configuration.getTemp()),
        TEMP_DBCONTRIBUTION_FILE);
    destXml.load();

    setFileXml(destXml);
    setRoot(((org.jdom.Document) destXml.getDocument().clone()).getRootElement()); // Get the root
    // element
  }

  @Override
  public String getVersionFromFile() throws Exception {
    if (versionFromFile == null) {
      versionFromFile = DBBuilderItem.NOTINSTALLED;
    } // if
    return versionFromFile;
  }

  private List<Map<String, Object>> getContentFromDB() throws Exception {
    Connection connexion = null;
    List<Map<String, Object>> infos = null;
    try {
      connexion = ConnectionFactory.getConnection();
      infos = QueryExecutor.executeLoopQuery(connexion, SELECT_CONTENT_FROM_DB,
          new Object[] { getModule() });
    } catch (Exception e) {
      throw new Exception("\n\t\t***ERROR RETURNED BY THE JVM : " + e.getMessage() + "\n\t\t\t("
          + SELECT_CONTENT_FROM_DB + ")");
    } finally {
      if (connexion != null) {
        connexion.close();
      }
    }
    return infos;
  }
}