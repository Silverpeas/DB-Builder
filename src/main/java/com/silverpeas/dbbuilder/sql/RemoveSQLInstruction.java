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
public class RemoveSQLInstruction implements SQLInstruction {

  public static final String DELETE_DEPENDENCIES = "delete from SR_DEPENDENCIES where SR_PACKAGE = ?";
  public static final String DELETE_SCRIPTS = "delete from SR_SCRIPTS where SR_ITEM_ID IN "
      + "(SELECT SRU.SR_ITEM_ID from SR_UNINSTITEMS SRU where SRU.SR_PACKAGE = ?)";
  public static final String DELETE_UNINSTITEMS = "delete from SR_UNINSTITEMS where SR_PACKAGE = ?";
  public static final String DELETE_PACKAGE = "delete from SR_PACKAGES where SR_PACKAGE= ?";
  private String packageName;

  public RemoveSQLInstruction(String packageName) {
    this.packageName = packageName;
  }

  @Override
  public void execute(Connection connection) throws SQLException {
    PreparedStatement pstmt = connection.prepareStatement(DELETE_DEPENDENCIES);
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
    pstmt = connection.prepareStatement(DELETE_PACKAGE);
    pstmt.setString(1, packageName);
    pstmt.executeUpdate();
    pstmt.close();
  }
}
