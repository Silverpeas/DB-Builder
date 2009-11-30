/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.silverpeas.dbbuilder.sql;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author ehugonnet
 */
public interface SQLInstruction {
  public void execute(Connection connection) throws SQLException;
}
