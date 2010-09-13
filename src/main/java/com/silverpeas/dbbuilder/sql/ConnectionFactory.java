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

package com.silverpeas.dbbuilder.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * Utility class for obtaining a connection to the database.
 * @author ehugonnet
 */
public class ConnectionFactory {

  private DataSource datasource;
  private static ConnectionFactory instance;

  private ConnectionFactory() {
  }

  public static final ConnectionFactory getInstance() {
    synchronized (ConnectionFactory.class) {
      if (instance == null) {
        instance = new ConnectionFactory();
      }
    }
    return instance;
  }

  /**
   * @param datasource the datasource to set
   */
  public void setDatasource(DataSource datasource) {
    this.datasource = datasource;
  }

  public static Connection getConnection() throws SQLException {
    return instance.datasource.getConnection();
  }

  public static String getConnectionInfo() throws SQLException {
    StringBuilder builder = new StringBuilder();
    Connection connection = null;
    try {
      connection = getConnection();
      DatabaseMetaData metaData = connection.getMetaData();
      String newLine = System.getProperty("line.separator");
      builder.append(newLine).append("\tRDBMS         : ")
          .append(metaData.getDatabaseProductName());
      builder.append(newLine).append("\tJdbcUrl       : ").append(metaData.getURL());
      builder.append(newLine).append("\tJdbcDriver    : ").append(metaData.getDriverName());
      builder.append(newLine).append("\tUserName      : ").append(metaData.getUserName());
    } finally {
      if (connection != null) {
        connection.close();
      }
    }

    return builder.toString();
  }
}
