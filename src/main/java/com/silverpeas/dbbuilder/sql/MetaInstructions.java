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
 *
 * @author ehugonnet
 */
public class MetaInstructions {

  private Map<String, List<String>> instructions;

  public MetaInstructions() {
    instructions = new HashMap<String, List<String>>();
  }

  public void addInstruction(String module, String instruction) {
    if (instructions.containsKey(module)) {
      instructions.get(module).add(instruction);
    } else {
      List<String> newInstructions = new ArrayList<String>();
      newInstructions.add(instruction);
      instructions.put(module, newInstructions);
    }
  }

  public void addInstructions(String module, List<String> instructionList) {
    if (instructions.containsKey(module)) {
      instructions.get(module).addAll(instructionList);
    } else {
      List<String> newInstructions = new ArrayList<String>();
      newInstructions.addAll(instructionList);
      instructions.put(module, newInstructions);
    }
  }

  public List<String> getInstructions(String module) {
    if (instructions.containsKey(module)) {
      return instructions.get(module);
    }
    return new ArrayList<String>();
  }
}
