/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.migration.uniqueid;

import com.silverpeas.dbbuilder.dbbuilder_dl.DbBuilderDynamicPart;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ehugonnet
 */
public class FixCaseInUniqueId extends DbBuilderDynamicPart {

  public void migrate() throws SQLException {
    Map<String, Long> uniqueIds = listCurrentIds();
    cleanTable();
    insertNewIds(uniqueIds);
  }

  public void cleanTable() throws SQLException {
    Statement stmt = null;
    try {
      stmt = getConnection().createStatement();
      stmt.executeUpdate("DELETE FROM uniqueId");
    } finally {
      if (stmt != null) {
        stmt.close();
      }
    }
  }

  public Map<String, Long> listCurrentIds() throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;
    Map<String, Long> uniqueIds = new HashMap<String, Long>(222);
    try {
      stmt = getConnection().createStatement();
      rs = stmt.executeQuery("SELECT tablename, maxid FROM uniqueId");
      while (rs.next()) {
        String tableName = rs.getString("tablename");
        long maxId = rs.getLong("maxid");
        if (tableName != null) {
          tableName = tableName.toLowerCase();
          if (uniqueIds.containsKey(tableName)) {
            if (uniqueIds.get(tableName).compareTo(maxId) < 0) {
              uniqueIds.put(tableName, maxId);
            }
          } else {
            uniqueIds.put(tableName, maxId);
          }
        }
      }
      return uniqueIds;
    } finally {
      if (rs != null) {
        rs.close();
      }
      if (stmt != null) {
        stmt.close();
      }
    }
  }

  public void insertNewIds(Map<String, Long> uniqueIds) throws SQLException {
    PreparedStatement pstmt = null;
    try {
      pstmt = getConnection().prepareStatement(
          "INSERT INTO uniqueId (tablename, maxId) VALUES(?, ?)");
      for (Map.Entry<String, Long> uniqueId : uniqueIds.entrySet()) {
        pstmt.setString(1, uniqueId.getKey());
        pstmt.setLong(2, uniqueId.getValue());
        pstmt.executeUpdate();
      }
    } finally {
      if (pstmt != null) {
        pstmt.close();
      }
    }
  }
}
