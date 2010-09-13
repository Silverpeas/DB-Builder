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

import java.util.Iterator;
import java.util.List;
import org.jdom.Element;

/**
 * Titre : dbBuilder Description : Builder des BDs Silverpeas Copyright : Copyright (c) 2001 Société
 * : Stratélia Silverpeas
 * @author ATH
 * @version 1.0
 */

public class DBBuilderFileItem extends DBBuilderItem {

  public DBBuilderFileItem(DBXmlDocument fileXml) throws Exception {
    setFileXml(fileXml);
    setRoot(((org.jdom.Document) fileXml.getDocument().clone()).getRootElement()); // Get the root
    // element
    // récupère le nom du module une fois pour toutes
    super.setModule(getRoot().getAttributeValue(MODULENAME_ATTRIB));
  }

  @Override
  public String getVersionFromFile() throws Exception {
    if (versionFromFile == null) {
      List listeCurrent = getRoot().getChildren(CURRENT_TAG);
      if (listeCurrent == null || listeCurrent.size() == 0) {
        throw new Exception(
            getModule() + ": no <" + CURRENT_TAG +
            "> tag found for this module into contribution file.");
      }
      if (listeCurrent.size() != 1) {
        throw new Exception(getModule() + ": tag <" + CURRENT_TAG + "> appears more than one.");
      }
      Iterator iterCurrent = listeCurrent.iterator();
      while (iterCurrent.hasNext()) {

        Element eltCurrent = (Element) iterCurrent.next();
        versionFromFile = eltCurrent.getAttributeValue(VERSION_ATTRIB);
      } // while
    } // if
    return versionFromFile;
  }
}
