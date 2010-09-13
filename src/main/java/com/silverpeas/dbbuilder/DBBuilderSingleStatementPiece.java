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

package com.silverpeas.dbbuilder;

import java.sql.Connection;

/**
 * Titre : dbBuilder Description : Builder des BDs Silverpeas Copyright : Copyright (c) 2001 Société
 * : Stratélia Silverpeas
 * @author ATH
 * @version 1.0
 */

public class DBBuilderSingleStatementPiece extends DBBuilderPiece {

  // Contructeur utilisé pour une pièce de type fichier
  public DBBuilderSingleStatementPiece(String pieceName, String actionName,
      boolean traceMode) throws Exception {

    super(pieceName, actionName, traceMode);
    setInstructions();
  }

  // Contructeur utilisé pour une pièce de type chaîne en mémoire
  public DBBuilderSingleStatementPiece(String pieceName, String actionName,
      String content, boolean traceMode) throws Exception {

    super(pieceName, actionName, content, traceMode);
    setInstructions();
  }

  // Contructeur utilisé pour une pièce stockée en base de données
  public DBBuilderSingleStatementPiece(String actionInternalID,
      String pieceName, String actionName, int itemOrder, boolean traceMode)
      throws Exception {
    super(actionInternalID, pieceName, actionName, itemOrder, traceMode);
    setInstructions();
  }

  public void setInstructions() {
    instructions = new Instruction[1];
    instructions[0].setInstructionType(Instruction.IN_UPDATE);
    instructions[0].setInstructionText(getContent());
  }

  public void cacheIntoDB(Connection connection, String _package, int _itemOrder) throws Exception {
    cacheIntoDB(connection, _package, _itemOrder,
        DBBuilderFileItem.FILEATTRIBSTATEMENT_VALUE, null, null, null);
  }

}