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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.dbbuilder.sql;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ehugonnet
 */
public class QueryExecutor {

  public static void executeUpdate(Connection connection, String query) throws SQLException {
    Statement stmt = null;
    try {
      // Crée l'instruction JDBC à partir de la connexion
      stmt = connection.createStatement();
      // Exécute le SQL
      stmt.executeUpdate(query);
      stmt.close();
    } catch (SQLException e) {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e2) {
        }
      }
      throw e;
    }
  }

  public static void executeProcedure(Connection connection, String procedureName,
      DbProcParameter[] dbProcParameters) throws
      Exception {
    CallableStatement call = null;
    String preparedStatement = null;
    int i;
    try {
      // Prépare l'appel JDBC à la procédure sur la connection,
      // cette procédure admet des paramètres en entrée et en sortie mais ne doit rien retourner
      if (dbProcParameters == null || dbProcParameters.length == 0) {
        preparedStatement = "{call " + procedureName + "}";
      } else {
        preparedStatement = "{call " + procedureName + "(";
        for (i = 0; i < dbProcParameters.length; i++) {
          preparedStatement += "?";
          if (i != (dbProcParameters.length - 1)) {
            preparedStatement += ",";
          }
        }
        preparedStatement += ")}";
      }
      call = connection.prepareCall(preparedStatement);
      if (dbProcParameters != null) {
        for (i = 0; i < dbProcParameters.length; i++) {
          DbProcParameter dbPP = dbProcParameters[i];
          call.setObject(i + 1, dbPP.getParameterValue(), dbPP.getParameterType());
          if (dbPP.getIsOutParameter() == true) {
            call.registerOutParameter(i + 1, dbPP.getParameterType());
          }
        }
      }
      // Execute la procédure
      call.execute();
      // Initialise le tableau des paramètres en sortie
      if (dbProcParameters != null) {
        for (i = 0; i < dbProcParameters.length; i++) {
          if (dbProcParameters[i].getIsOutParameter() == true) {
            dbProcParameters[i].setParameterValue(call.getObject(i + 1));
          }
        }
      }
      call.close();
    } catch (SQLException e) {
      throw (Exception) e;
    }
  }

  public static List<Map<String, Object>> executeLoopQuery(Connection connection, String query,
      Object[] parameters) throws Exception {
    Statement stmt = null;
    PreparedStatement pstmt = null;
    ArrayList array = new ArrayList();
    ResultSet results;
    if (parameters == null) {
      stmt = connection.createStatement();
      results = stmt.executeQuery(query);
    } else {
      pstmt = connection.prepareStatement(query);
      for (int i = 0; i < parameters.length; i++) {
        pstmt.setObject(i + 1, parameters[i]);
      }
      results = pstmt.executeQuery();
    }
    ResultSetMetaData meta = results.getMetaData();
    // Tant qu'on a des enregistrements dans le result set
    while (results.next()) {
      // Stockage d'un enregistrement
      HashMap<String, Object> h = new HashMap<String, Object>(meta.getColumnCount());
      // Pour chaque colonne du result set
      for (int i = 1; i <= meta.getColumnCount(); i++) {
        Object ob = results.getObject(i);
        h.put(meta.getColumnLabel(i).toUpperCase(), ob);
      }
      array.add(h);
    }
    results.close();
    if (stmt != null) {
      stmt.close();
    }
    if (pstmt != null) {
      pstmt.close();
    }
    return array;
  }
}
