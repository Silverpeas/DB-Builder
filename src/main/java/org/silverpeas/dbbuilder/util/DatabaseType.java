/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.silverpeas.dbbuilder.util;

import java.io.File;

/**
 * @author ehugonnet
 */
public enum DatabaseType {
  POSTGRES("postgres"), MSSQL("mssql"), ORACLE("oracle"), H2("postgres");

  private String contributionDir;

  public String getDBContributionDir() {
    return Configuration.getContributionFilesDir() + File.separatorChar + contributionDir;
  }

  private DatabaseType(String contributionDir) {
    this.contributionDir = contributionDir;
  }
}
