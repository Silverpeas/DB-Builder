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

package org.silverpeas.dbbuilder;

public class Instruction {

  static final public int IN_CALLDBPROC = 0;
  static final public int IN_UPDATE = 1;
  static final public int IN_INVOKEJAVA = 2;

  private int instructionType = -1;
  private String instructionText = "";
  private Object instructionDetail = null;

  public Instruction() {
  }

  public Instruction(int instructionType, String instructionText,
      Object instructionDetail) {

    this.instructionType = instructionType;
    this.instructionText = instructionText;
    this.instructionDetail = instructionDetail;
  }

  public void setInstructionType(int instructionType) {
    this.instructionType = instructionType;
  }

  public void setInstructionText(String instructionText) {
    this.instructionText = instructionText;
  }

  public void setInstructionDetail(Object instructionDetail) {
    this.instructionDetail = instructionDetail;
  }

  public int getInstructionType() {
    return instructionType;
  }

  public String getInstructionText() {
    return instructionText;
  }

  public Object getInstructionDetail() {
    return instructionDetail;
  }
}