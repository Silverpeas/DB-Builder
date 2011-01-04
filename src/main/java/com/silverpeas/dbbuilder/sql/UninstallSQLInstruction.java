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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static com.silverpeas.dbbuilder.sql.RemoveSQLInstruction.*;

/**
 * @author ehugonnet
 */
public class UninstallSQLInstruction implements SQLInstruction {
  private String versionFile;
  private String packageName;

  private static final String UPDATE_PACKAGE =
      "update SR_PACKAGES set SR_VERSION= ? where SR_PACKAGE= ?";

  public UninstallSQLInstruction(String versionFile, String packageName) {
    this.versionFile = versionFile;
    this.packageName = packageName;
  }

  @Override
  public void execute(Connection connection) throws SQLException {
    PreparedStatement pstmt = connection.prepareStatement(UPDATE_PACKAGE);
    pstmt.setString(1, versionFile);
    pstmt.setString(2, packageName);
    pstmt.executeUpdate();
    pstmt.close();
    pstmt = connection.prepareStatement(DELETE_DEPENDENCIES);
    pstmt.setString(1, packageName);
    pstmt.executeUpdate();
    pstmt.close();
    pstmt = connection.prepareStatement(DELETE_SCRIPTS);
    pstmt.setString(1, packageName);
    pstmt.executeUpdate();
    pstmt.close();
    pstmt = connection.prepareStatement(DELETE_UNINSTITEMS);
    pstmt.setString(1, packageName);
    pstmt.executeUpdate();
    pstmt.close();

  }

}
