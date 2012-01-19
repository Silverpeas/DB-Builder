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

/**
 * All tha available acions for the DBBuilder.
 * @author ehugonnet
 */
public enum Action {

  ACTION_CONNECT("-C"), ACTION_INSTALL("-I"), ACTION_UNINSTALL("-U"), ACTION_OPTIMIZE("-O"), ACTION_ALL(
      "-A"), ACTION_STATUS("-S"), ACTION_CONSTRAINTS_INSTALL("-CI"), ACTION_CONSTRAINTS_UNINSTALL(
      "-CU"), ACTION_ENFORCE_UNINSTALL("-FU"), ACTION_NONE("");
  private String argument;

  Action(String argument) {
    this.argument = argument;
  }

  public static Action getAction(String arg) {
    if (ACTION_CONNECT.argument.equals(arg)) {
      return ACTION_CONNECT;
    }
    if (ACTION_INSTALL.argument.equals(arg)) {
      return ACTION_INSTALL;
    }
    if (ACTION_UNINSTALL.argument.equals(arg)) {
      return ACTION_UNINSTALL;
    }
    if (ACTION_OPTIMIZE.argument.equals(arg)) {
      return ACTION_OPTIMIZE;
    }
    if (ACTION_ALL.argument.equals(arg)) {
      return ACTION_ALL;
    }
    if (ACTION_STATUS.argument.equals(arg)) {
      return ACTION_STATUS;
    }
    if (ACTION_CONSTRAINTS_INSTALL.argument.equals(arg)) {
      return ACTION_CONSTRAINTS_INSTALL;
    }
    if (ACTION_CONSTRAINTS_UNINSTALL.argument.equals(arg)) {
      return ACTION_CONSTRAINTS_UNINSTALL;
    }
    if (ACTION_ENFORCE_UNINSTALL.argument.equals(arg)) {
      return ACTION_ENFORCE_UNINSTALL;
    }
    return ACTION_NONE;
  }
}
