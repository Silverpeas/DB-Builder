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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.dbbuilder;

import java.util.Vector;
import com.silverpeas.file.StringUtil;
import java.sql.Connection;

/**
 * Titre : dbBuilder Description : Builder des BDs Silverpeas Copyright : Copyright (c) 2001
 * Société : Stratélia Silverpeas
 * @author ATH
 * @version 1.0
 */

public class DBBuilderMultipleStatementPiece extends DBBuilderPiece {

  private String delimiter = null;
  private boolean keepDelimiter = false;

  // contructeurs non utilisés
  private DBBuilderMultipleStatementPiece(String pieceName, String actionName,
      boolean traceMode) throws Exception {
    super(pieceName, actionName, traceMode);
  }

  private DBBuilderMultipleStatementPiece(String pieceName, String actionName,
      String content, boolean traceMode) throws Exception {
    super(pieceName, actionName, content, traceMode);
  }

  private DBBuilderMultipleStatementPiece(String actionInternalID,
      String pieceName, String actionName, int itemOrder, boolean traceMode)
      throws Exception {
    super(actionInternalID, pieceName, actionName, itemOrder, traceMode);
  }

  // Contructeur utilisé pour une pièce de type fichier
  public DBBuilderMultipleStatementPiece(String pieceName, String actionName,
      boolean traceMode, String delimiter, boolean keepDelimiter)
      throws Exception {
    super(pieceName, actionName, traceMode);
    moreInitialize(delimiter, keepDelimiter);
  }

  // Contructeur utilisé pour une pièce de type chaîne en mémoire
  public DBBuilderMultipleStatementPiece(String pieceName, String actionName,
      String content, boolean traceMode, String delimiter, boolean keepDelimiter)
      throws Exception {
    super(pieceName, actionName, content, traceMode);
    moreInitialize(delimiter, keepDelimiter);
  }

  // Contructeur utilisé pour une pièce stockée en base de données
  public DBBuilderMultipleStatementPiece(String actionInternalID,
      String pieceName, String actionName, int itemOrder, boolean traceMode,
      String delimiter, boolean keepDelimiter) throws Exception {
    super(actionInternalID, pieceName, actionName, itemOrder, traceMode);
    moreInitialize(delimiter, keepDelimiter);
  }

  private void moreInitialize(String delimiter, boolean keepDelimiter)
      throws Exception {

    if (delimiter == null)
      throw new Exception("Missing <delimiter> tag for \"pieceName\" item.");

    String d = new String();
    // d = replaceAll(delimiter, "\\n", "\n");
    // d = replaceAll(d, "\\t", "\t");
    d = StringUtil.sReplace("\\n", "\n", delimiter);
    d = StringUtil.sReplace("\\t", "\t", d);

    this.delimiter = d;
    this.keepDelimiter = keepDelimiter;

    setInstructions();
  }

  public void setInstructions() {

    if (getContent() != null && delimiter != null) {

      Vector v = tokenizeAll(getContent(), delimiter, keepDelimiter);

      instructions = new Instruction[v.size()];

      for (int i = 0; i < v.size(); i++) {
        instructions[i] = new Instruction(Instruction.IN_UPDATE, (String) v
            .get(i), null);
      }
    }
  }

  public void cacheIntoDB(Connection connection, String _package, int _itemOrder) throws Exception {

    Integer kd;
    if (keepDelimiter)
      kd = new Integer(1);
    else
      kd = new Integer(0);

    cacheIntoDB(connection, _package, _itemOrder,
        DBBuilderFileItem.FILEATTRIBSEQUENCE_VALUE, delimiter, kd, null);
  }

  private Vector tokenizeAll(String str, String delimiter, boolean keepDelimiter) {

    Vector c = new Vector();
    int previ = 0;
    int curi = 0;
    while (curi < str.length() && curi >= 0) {
      previ = curi;
      curi = str.indexOf(delimiter, curi);
      if (curi < str.length() && curi >= 0) {
        int endIndex = curi;
        if (keepDelimiter) {
          endIndex = curi + delimiter.length();
        }
        String instruction = str.substring(previ, endIndex).trim();
        if (!" ".equals(instruction) && !"".equals(instruction)) {
          c.add(instruction);
        }
        curi += delimiter.length();
      } else if (str.length() - previ > delimiter.length()) {
        String instruction = str.substring(previ, str.length()).trim();
        if (!" ".equals(instruction) && !"".equals(instruction)) {
          if (keepDelimiter) {
            instruction = instruction + delimiter;
          }
          c.add(instruction);
        }
      }// if
    } // while
    return c;
  }
}