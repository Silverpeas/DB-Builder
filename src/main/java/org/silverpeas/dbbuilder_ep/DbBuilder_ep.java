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
package org.silverpeas.dbbuilder_ep;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.silverpeas.dbbuilder.dbbuilder_dl.DbBuilderDynamicPart;
import org.silverpeas.dbbuilder.util.Configuration;

public class DbBuilder_ep extends DbBuilderDynamicPart {

  private static boolean needEncryption = false;
  private static String m_PasswordEncryption = "";

  static {
    try {
      Properties propFile =
          Configuration.loadResource("/org/silverpeas/domains/domainSP.properties");
      m_PasswordEncryption = propFile.getProperty("database.SQLPasswordEncryption", "");
      needEncryption = ("CryptUnix").equalsIgnoreCase(m_PasswordEncryption);
    } catch (Exception e) {
      e.printStackTrace();
      m_PasswordEncryption = "";
    }
  }

  public DbBuilder_ep() {
    // recherche de la propriété spécifiant l'encryptage
    // -> si pb de lecture, on considère qu'on n'a pas à encrypter
  }

  public void run() throws Exception {
    Connection m_Connection = this.getConnection();
    ResultSet rs = null;
    Statement stmt = null;
    PreparedStatement stmtUpdate = null;
    String sUpdateStart = "UPDATE DomainSP_User SET password = '";
    String sUpdateMiddle = "' WHERE id=";
    String sClearPass;

    try {
      if (needEncryption) {
        stmt = m_Connection.createStatement();
        rs = stmt.executeQuery("SELECT * FROM DomainSP_User");
        while (rs.next()) {
          sClearPass = rs.getString("password");
          if (sClearPass == null) {
            sClearPass = "";
          }
          stmtUpdate = m_Connection.prepareStatement(sUpdateStart
              + jcrypt.crypt("SP", sClearPass) + sUpdateMiddle
              + rs.getString("id"));
          stmtUpdate.executeUpdate();
          stmtUpdate.close();
          stmtUpdate = null;
        } // while
      } // if
    } catch (SQLException ex) {
      throw new Exception("Error during password Crypting : " + ex.getMessage());

    } finally {
      try {
        if (rs != null) {
          rs.close();
          rs = null;
        } // if
        if (stmt != null) {
          stmt.close();
          stmt = null;
        } // if
        if (stmtUpdate != null) {
          stmtUpdate.close();
          stmtUpdate = null;
        } // if
      } catch (SQLException ex) {
      }
    } // try
  }
}
