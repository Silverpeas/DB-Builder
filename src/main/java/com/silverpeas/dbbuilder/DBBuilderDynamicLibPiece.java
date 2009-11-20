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

import com.stratelia.dbConnector.DBConnexion;
import com.silverpeas.dbbuilder.dbbuilder_dl.DbBuilderDynamicPart;

public class DBBuilderDynamicLibPiece extends DBBuilderPiece {

  private String className = null;
  private String methodName = null;
  private DbBuilderDynamicPart dynamicPart = null;

  // contructeurs non utilisés
  private DBBuilderDynamicLibPiece(String pieceName, String actionName,
      boolean traceMode) throws Exception {
    super(pieceName, actionName, traceMode);
  }

  private DBBuilderDynamicLibPiece(String pieceName, String actionName,
      String content, boolean traceMode) throws Exception {
    super(pieceName, actionName, content, traceMode);
  }

  // Contructeur utilisé pour une pièce de type fichier
  public DBBuilderDynamicLibPiece(String pieceName, String actionName,
      boolean traceMode, String className, String methodName) throws Exception {
    super(pieceName, actionName, traceMode);
    moreInitialize(className, methodName);
  }

  // Contructeur utilisé pour une pièce de type chaîne en mémoire
  public DBBuilderDynamicLibPiece(String pieceName, String actionName,
      String content, boolean traceMode, String className, String methodName)
      throws Exception {
    super(pieceName, actionName, content, traceMode);
    moreInitialize(className, methodName);
  }

  private void moreInitialize(String className, String methodName)
      throws Exception {

    if (className == null) {
      throw new Exception("Missing <classname> tag for \"pieceName\" item.");
    }

    if (methodName == null) {
      throw new Exception("Missing <methodname> tag for \"pieceName\" item.");
    }

    this.className = className;
    this.methodName = methodName;

    try {
      dynamicPart = (DbBuilderDynamicPart) Class.forName(className)
          .newInstance();

    } catch (Exception e) {
      throw new Exception("Unable to load \"" + className + "\" class.");
    } // try

    dynamicPart.setSILVERPEAS_HOME(DBBuilder.getHome());
    dynamicPart.setSILVERPEAS_DATA(DBBuilder.getData());
    dynamicPart.setConnection(DBConnexion.getInstance().getConnection());

    setInstructions();
  }

  public void setInstructions() {

    instructions = new Instruction[1];
    instructions[0] = new Instruction(Instruction.IN_INVOKEJAVA, methodName,
        dynamicPart);
  }

  public void cacheIntoDB(String _package, int _itemOrder) throws Exception {
    // rien à cacher pour une proc dynamique
  }
}