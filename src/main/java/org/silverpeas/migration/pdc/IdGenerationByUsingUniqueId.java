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
package org.silverpeas.migration.pdc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import org.silverpeas.dbbuilder.Console;
import org.silverpeas.dbbuilder.dbbuilder_dl.DbBuilderDynamicPart;

/**
 * A migration dedicated to some SQL tables for the PdC component. This migration tool inserts an
 * entry in the UniqueId table for each of the following SQL tables PdcPosition and
 * PdcClassification (the table PdcAxisValue is just a mapping table between an axis value and a
 * node in the SB_Tree_Tree table). Then it fetches the yet greater existing unique identifier in
 * theses above tables in order to specify the next identifier value to pick for the next tuple
 * insertion.
 */
public class IdGenerationByUsingUniqueId extends DbBuilderDynamicPart {

  private static final String NEW_ENTRY_IN_UNIQUEID =
      "insert into UniqueId(maxId, tableName) values(?, ?)";
  private static final String GREATER_ID_FETCHING = "select max(id) from {0}";
  private static final String[] TABLES_TO_MIGRATE = { "PdcPosition", "PdcClassification" };

  private boolean defaultAutocommitStatus = true;

  public void migrate() throws Exception {
    PreparedStatement preparedStatement = null;
    try {
      Console console = prepareConsole();
      openTransaction();
      preparedStatement = getConnection().prepareStatement(NEW_ENTRY_IN_UNIQUEID);
      for (String aTable : TABLES_TO_MIGRATE) {
        long maxId = getGreaterIdentifier(aTable);
        if (maxId >= 0) {
          preparedStatement.setLong(1, maxId + 1);
          preparedStatement.setString(2, aTable);
          int success = preparedStatement.executeUpdate();
          if (success < 1) {
            console.printError("The entry creation in the UniqueId for table " + aTable
                + " has failed.");
          } else {
            console.printMessageln("The entry creation in UniqueId for table " + aTable
                + " has succeeded");
          }
        } else {
          console.printError("The table " + aTable + " doesn't have any column id!");
        }
      }
      commitTransaction();
    } finally {
      if (preparedStatement != null) {
        preparedStatement.close();
      }
      closeTransaction();
    }
  }

  private void openTransaction() throws SQLException {
    defaultAutocommitStatus = getConnection().getAutoCommit();
    getConnection().setAutoCommit(false);
  }

  private void commitTransaction() throws SQLException {
    getConnection().commit();
  }

  private void closeTransaction() throws SQLException {
    getConnection().setAutoCommit(defaultAutocommitStatus);
  }

  private Console prepareConsole() {
    Console console = getConsole();
    if (console == null) {
      console = new Console();
    }
    return console;
  }

  private long getGreaterIdentifier(String inTable) throws SQLException {
    long maxId = -1;
    Statement statement = null;
    try {
      statement = getConnection().createStatement();
      String sqlQuery = MessageFormat.format(GREATER_ID_FETCHING, inTable);
      ResultSet results = statement.executeQuery(sqlQuery);
      if (results.next()) {
        maxId = results.getLong(1);
      }
    } finally {
      if (statement != null) {
        statement.close();
      }
    }
    return maxId;
  }
}