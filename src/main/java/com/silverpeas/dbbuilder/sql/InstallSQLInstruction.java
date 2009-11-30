/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.dbbuilder.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author ehugonnet
 */
public class InstallSQLInstruction implements SQLInstruction {

  public static final String INSERT_PACKAGE = "insert into SR_PACKAGES(SR_PACKAGE, SR_VERSION) values (?, ?)";
  private String versionFile;
  private String packageName;

  public InstallSQLInstruction(String versionFile, String packageName) {
    this.versionFile = versionFile;
    this.packageName = packageName;
  }

  @Override
  public void execute(Connection connection) throws SQLException {
    PreparedStatement pstmt = connection.prepareStatement(INSERT_PACKAGE);
    pstmt.setString(1, packageName);
    pstmt.setString(2, versionFile);
    pstmt.executeUpdate();
    pstmt.close();
  }
}

