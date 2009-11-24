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
 *
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
