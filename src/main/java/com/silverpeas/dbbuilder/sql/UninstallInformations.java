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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.dbbuilder.sql;

import com.silverpeas.dbbuilder.DBXmlDocument;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ehugonnet
 */
public class UninstallInformations {

  private Map<String, List<FileInformation>> informations;

  public UninstallInformations() {
    informations = new HashMap<String, List<FileInformation>>();
  }

  public void addInformation(String module, FileInformation information) {
    if (informations.containsKey(module)) {
      informations.get(module).add(information);
    } else {
      List<FileInformation> fileInformations = new ArrayList<FileInformation>();
      fileInformations.add(information);
      informations.put(module, fileInformations);
    }
  }

  public void addInformation(String module, String srPackage, DBXmlDocument document) {
    this.addInformation(module, new FileInformation(srPackage, document));
  }

  public void addInstructions(String module, List<FileInformation> informationList) {
    if (informations.containsKey(module)) {
      informations.get(module).addAll(informationList);
    } else {
      List<FileInformation> fileInformations = new ArrayList<FileInformation>();
      fileInformations.addAll(informationList);
      informations.put(module, fileInformations);
    }
  }

  public List<FileInformation> getInformations(String module) {
    if (informations.containsKey(module)) {
      return informations.get(module);
    }
    return new ArrayList<FileInformation>();
  }
}
