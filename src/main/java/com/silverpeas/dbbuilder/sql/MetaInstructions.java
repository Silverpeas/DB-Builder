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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.dbbuilder.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ehugonnet
 */
public class MetaInstructions {

  private Map<String, List<SQLInstruction>> instructions;

  public MetaInstructions() {
    instructions = new HashMap<String, List<SQLInstruction>>();
  }

  public void addInstruction(String module, SQLInstruction instruction) {
    if (instructions.containsKey(module)) {
      instructions.get(module).add(instruction);
    } else {
      List<SQLInstruction> newInstructions = new ArrayList<SQLInstruction>();
      newInstructions.add(instruction);
      instructions.put(module, newInstructions);
    }
  }

  public void addInstructions(String module, List<SQLInstruction> instructionList) {
    if (instructions.containsKey(module)) {
      instructions.get(module).addAll(instructionList);
    } else {
      List<SQLInstruction> newInstructions = new ArrayList<SQLInstruction>();
      newInstructions.addAll(instructionList);
      instructions.put(module, newInstructions);
    }
  }

  public List<SQLInstruction> getInstructions(String module) {
    if (instructions.containsKey(module)) {
      return instructions.get(module);
    }
    return new ArrayList<SQLInstruction>();
  }
}
