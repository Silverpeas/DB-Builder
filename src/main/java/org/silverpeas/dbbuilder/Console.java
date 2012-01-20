/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.dbbuilder;

import org.silverpeas.dbbuilder.util.Configuration;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Console into which messages are displayed. It wraps the source into which messages are printed
 * out.
 */
public final class Console {

  public static final String NEW_LINE = System.getProperty("line.separator");

  private File logFile;
  private PrintWriter logBuffer;

  /**
   * Creates and open a console upon the specified file. All messages will be printed into the file.
   * The file will be created in the directory provided by the Configuration.getLogDir() method.
   * @param fileName the name of the file into which the messages will be printed.
   * @throws IOException if an error occurs while creating the console.
   */
  public Console(final String fileName) throws IOException {
    logFile = new File(Configuration.getLogDir() + File.separator
        + fileName);
    logFile.getParentFile().mkdirs();
    logBuffer =
        new PrintWriter(new BufferedWriter(new FileWriter(logFile.getAbsolutePath(), true)));
  }

  /**
   * Creates and open a console upon the standard system output.
   */
  public Console() {

  }

  public void printError(String errMsg, Exception ex) {
    printError(errMsg);
    if (logBuffer != null) {
      ex.printStackTrace(logBuffer);
      logBuffer.close();
    }
  }

  public void printError(String errMsg) {
    if (logBuffer != null) {
      printMessageln(NEW_LINE);
      printMessageln(errMsg);
      logBuffer.close();
    }
    System.out.println(NEW_LINE + errMsg + NEW_LINE);
  }

  public void printMessageln(String msg) {
    printMessage(msg);
    printMessage(NEW_LINE);
  }

  public void printMessage(String msg) {
    if (logBuffer != null) {
      logBuffer.print(msg);
      System.out.print(".");
    } else {
      System.out.print(msg);
    }
  }

  public void close() {
    logBuffer.close();
  }
}
