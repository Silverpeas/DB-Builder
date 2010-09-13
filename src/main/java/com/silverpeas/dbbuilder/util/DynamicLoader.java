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

package com.silverpeas.dbbuilder.util;

import com.silverpeas.dbbuilder.DBBuilder;
import com.silverpeas.dbbuilder.dbbuilder_dl.DbBuilderDynamicPart;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 * Load the classes for dynamic Databases Operations
 * @author ehugonnet
 */
public class DynamicLoader {

  private URLClassLoader loader;
  private static final String JAR_DIRECTORY = "dynamic";

  public DynamicLoader() {
    File jarDirectory = new File(Configuration.getPiecesFilesDir(), JAR_DIRECTORY);
    URL[] classpath = new URL[] {};
    if (jarDirectory.exists() && jarDirectory.isDirectory()) {
      @SuppressWarnings("unchecked")
      Collection<File> jars = FileUtils.listFiles(jarDirectory, new String[] { "jar" }, true);
      List<URL> urls = new ArrayList<URL>(jars.size());
      DBBuilder.displayMessage("We have found " + jars.size() + " jars files");
      for (File jar : jars) {
        try {
          urls.add(jar.toURI().toURL());
          for (URL url : urls) {
            DBBuilder.printError(url.toString());
          }
        } catch (MalformedURLException ex) {
          Logger.getLogger(DynamicLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      classpath = urls.toArray(new URL[urls.size()]);
    }
    ClassLoader parent = Thread.currentThread().getContextClassLoader();
    if (parent == null) {
      parent = getClass().getClassLoader();
    }
    loader = new URLClassLoader(classpath, parent);
  }

  public DbBuilderDynamicPart loadDynamicPart(String className) throws InstantiationException,
      IllegalAccessException, ClassNotFoundException {
    @SuppressWarnings("unchecked")
    Class<DbBuilderDynamicPart> dynamicPart =
        (Class<DbBuilderDynamicPart>) Class.forName(className,
            true, loader);
    return dynamicPart.newInstance();
  }
}
