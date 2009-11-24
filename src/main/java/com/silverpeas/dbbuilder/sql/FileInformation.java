/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.dbbuilder.sql;

import com.silverpeas.dbbuilder.DBXmlDocument;

/**
 *
 * @author ehugonnet
 */
public class FileInformation {

  private String srPackage;
  private DBXmlDocument document;

  public FileInformation(String srPackage, DBXmlDocument document) {
    this.srPackage = srPackage;
    this.document = document;
  }

  public DBXmlDocument getDocument() {
    return document;
  }

  public String getSrPackage() {
    return srPackage;
  }
}
