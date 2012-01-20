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

package org.silverpeas.dbbuilder.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.io.IOUtils;

/**
 * @author ehugonnet
 */
public class Configuration {

  private static String dbbuilderHome = null;
  private static String dbbuilderData = null;

  private static final String DATA_KEY = "dbbuilder.data";
  private static final String HOME_KEY = "dbbuilder.home";
  private static final String DBREPOSITORY_SUBDIR = "dbRepository";
  private static final String CONTRIB_FILES_SUBDIR = "data";
  private static final String LOG_FILES_SUBDIR = "log";
  private static final String TEMP_FILES_SUBDIR = "temp";
  private static final String DIR_CONTRIBUTIONFILESROOT = Configuration.getHome()
      + File.separator + DBREPOSITORY_SUBDIR + File.separator + CONTRIB_FILES_SUBDIR;
  // Répertoire racine des DB Pieces Contribution File
  private static final String DIR_DBPIECESFILESROOT =
      getHome() + File.separator + DBREPOSITORY_SUBDIR;
  // Répertoire temp
  private static final String DIR_TEMP = getHome() + File.separator + TEMP_FILES_SUBDIR;

  /**
   * Load a properties file from the classpath then from $SILVERPEAS_HOME/properties
   * @param propertyName
   * @return a java.util.Properties
   * @throws IOException
   */
  public static Properties loadResource(String propertyName) throws IOException {
    Properties properties = new Properties();
    InputStream in = Configuration.class.getClassLoader().getResourceAsStream(propertyName);
    try {
      if (in == null) {
        String path = propertyName.replace('/', File.separatorChar);
        path = path.replace('\\', File.separatorChar);
        if (!path.startsWith(File.separator)) {
          path = File.separatorChar + path;
        }
        File configurationFile = new File(getHome() + File.separatorChar + "properties" + path);
        if (configurationFile.exists()) {
          in = new FileInputStream(configurationFile);
        }
      }
      if (in != null) {
        properties.load(in);
      }
    } finally {
      IOUtils.closeQuietly(in);
    }
    return properties;
  }

  // Récupère le répertoire racine d'installation
  public static String getHome() {
    if (dbbuilderHome == null) {
      if (!System.getProperties().containsKey(HOME_KEY)) {
        System.err.println("### CANNOT FIND DBBUILDER INSTALL LOCATION ###");
        System.err.println("please use \"-D" + HOME_KEY
            + "=<install location>\" on the command line");
        System.exit(1);
      }
      dbbuilderHome = System.getProperty(HOME_KEY);
    }
    return dbbuilderHome;
  }

  public static String getContributionFilesDir() {
    return DIR_CONTRIBUTIONFILESROOT;
  }

  public static String getPiecesFilesDir() {
    return DIR_DBPIECESFILESROOT;
  }

  // Récupère le répertoire data d'installation
  public static String getData() {
    if (dbbuilderData == null) {
      if (System.getProperties().containsKey(DATA_KEY)) {
        dbbuilderData = System.getProperty(DATA_KEY);
      }
    }
    return dbbuilderData;
  }

  /**
   * Return the temporary directory path.
   * @return the temporary directory path.
   */
  public static String getTemp() {
    return DIR_TEMP;
  }

  public static String getLogDir() {
    return getHome() + File.separator + LOG_FILES_SUBDIR;
  }

  private Configuration() {
  }
}
