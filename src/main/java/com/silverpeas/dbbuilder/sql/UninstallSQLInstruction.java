/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.silverpeas.dbbuilder.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static com.silverpeas.dbbuilder.sql.RemoveSQLInstruction.*;

/**
 *
 * @author ehugonnet
 */
public class UninstallSQLInstruction implements SQLInstruction {
  private String versionFile;
  private String packageName;

  private static final String UPDATE_PACKAGE = "update SR_PACKAGES set SR_VERSION= ? where SR_PACKAGE= ?";
  
  public UninstallSQLInstruction(String versionFile, String packageName) {
    this.versionFile = versionFile;
    this.packageName = packageName;
  }


  @Override
  public void execute(Connection connection) throws SQLException {
    PreparedStatement  pstmt = connection.prepareStatement(UPDATE_PACKAGE);
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
