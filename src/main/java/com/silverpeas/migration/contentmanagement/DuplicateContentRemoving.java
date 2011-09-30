/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.migration.contentmanagement;

import com.silverpeas.dbbuilder.Console;
import com.silverpeas.dbbuilder.dbbuilder_dl.DbBuilderDynamicPart;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DB migration to remove any duplicate content instances in the database underlying at Silverpeas.
 * 
 * The table sb_contentmanager_content table can contain duplicate rows with the same
 * contentInstanceId and internalContentId columns value but with a different silverContentId
 * column value. This means some contents in Silverpeas are linked with more than one content object
 * and this shouldn't occur.
 */
public class DuplicateContentRemoving extends DbBuilderDynamicPart {

  /**
   * SQL statement for querying the count of duplicated contents that aren't classified on the PdC.
   * The Silverpeas contents are persisted into the sb_contentmanager_content table.
   * A content belongs to a Silverpeas component instance that is refered by the column 
   * contentInstanceId in the sb_contentmanager_content table.
   * The classification of the Silverpeas contents are persisted into the sb_classifyengine_classify
   * table.
   */
  private static final String DUPLICATE_CLASSIFIED_CONTENT_COUNT_QUERY =
          "select count(c1.internalContentId) from sb_contentmanager_content as c1 where "
          + "1 < (select count(c2.internalContentId) from sb_contentmanager_content as c2 where "
          + "c2.contentInstanceId=c1.contentInstanceId and c2.internalContentId = c1.internalContentId) "
          + "and c1.silverContentId in (select objectid from sb_classifyengine_classify)";
  /**
   * SQL statement for querying the count of all duplicated contents.
   * The Silverpeas contents are persisted into the sb_contentmanager_content table.
   * A content belongs to a Silverpeas component instance that is refered by the column 
   * contentInstanceId in the sb_contentmanager_content table.
   */
  private static final String DUPLICATE_CONTENT_COUNT_QUERY =
          "select count(c1.silverContentId) / 2 from sb_contentmanager_content as c1 where "
          + "1 < (select count(c2.internalContentId) from sb_contentmanager_content as c2 where "
          + "c2.contentInstanceId=c1.contentInstanceId and c2.internalContentId = c1.internalContentId) ";
  /**
   * SQL statement for querying the redundant instances of duplicated contents.
   * The Silverpeas contents are persisted into the sb_contentmanager_content table.
   * A content belongs to a Silverpeas component instance that is refered by the column 
   * contentInstanceId in the sb_contentmanager_content table.
   * This statement is used for querying redundant instances that weren't deleted by the two below
   * SQL statements.
   */
  private static final String REDUNDANT_INSTANCE_OF_DUPLICATE_CONTENT_QUERY =
          "select c1.silverContentId, c1.internalContentId, c1.contentInstanceId from sb_contentmanager_content as c1 where "
          + "1 < (select count(c2.internalContentId) from sb_contentmanager_content as c2 where "
          + "c2.contentInstanceId=c1.contentInstanceId and c2.internalContentId = c1.internalContentId) "
          + "and c1.silverContentId < (select c3.silverContentId from sb_contentmanager_content as c3 where C1.contentInstanceId=c3.contentInstanceId and c1.internalContentId=c3.internalContentId and c1.silverContentId != c3.silverContentId)";
  /**
   * SQL statement for deleting in the sb_contentmanager_content table the redundant instances
   * of duplicate content that aren't classificed on the PdC. The redundant unclassified 
   * instance of the classified contents are also deleted by this statement.
   */
  private static final String DELETION_OF_REDUNDANT_INSTANCES_OF_DUPLICATE_UNCLASSIFIED_CONTENT =
          "delete from "
          + "sb_contentmanager_content as c1 where "
          + "1 < (select count(c2.internalContentId) from sb_contentmanager_content as c2 where c2.contentInstanceId=c1.contentInstanceId and c2.internalContentId = c1.internalContentId) "
          + "and c1.silverContentId not in (select objectid from sb_classifyengine_classify)"
          + "and c1.silverContentId < (select c3.silverContentId from sb_contentmanager_content as c3 where C1.contentInstanceId=c3.contentInstanceId and c1.internalContentId=c3.internalContentId and c1.silverContentId != c3.silverContentId)";
  /**
   * SQL statement for deleting in the sb_contentmanager_content table all the redundant and
   * unclassified instances of the duplicate contents. In usual cases, they are deleted by the 
   * above statement; this one is just for ensuring their deletion.
   */
  private static final String DELETION_OF_UNCLASSIFIED_INSTANCES_OF_DUPLICATE_CONTENT = "delete from "
          + "sb_contentmanager_content as c1 where "
          + "1 < (select count(c2.internalContentId) from sb_contentmanager_content as c2 where c2.contentInstanceId=c1.contentInstanceId and c2.internalContentId = c1.internalContentId) "
          + "and c1.silverContentId not in (select objectid from sb_classifyengine_classify)";
  /**
   * SQL statement for deleting explicitly a given content in the sb_contentmanager_content table.
   * This statement will be use in the exceptional case where two instances of a duplicate
   * content are classified; in this case, the redundant instance isn't deleted by the
   * above statement and it is then necessary to delete it explicitly.
   */
  private static final String DELETION_OF_A_GIVEN_CONTENT_INSTANCE = "delete from "
          + "sb_contentmanager_content where silverContentId=";
  /**
   * SQL statement for deleting explicitly the classification of a given content in the
   * sb_classifyengine_classify table.
   * This statement will be use in the exceptional case where two instances of a duplicate
   * content are classified; in this case, the redundant instance isn't deleted by the
   * above statement and it is then necessary to delete its classification before deleting it.
   */
  private static final String DELETION_OF_CLASSIFICATION_OF_A_GIVEN_CONTENT_INSTANCE = "delete from "
          + "sb_classifyengine_classify where objectId=";

  /**
   * Migrates the sb_contentmanager_content table by removing all duplicated Silverpeas contents
   * @throws Exception if an error occurs while migrating the sb_contentmanager_content table.
   */
  public void migrate() throws Exception {
    Console console = getConsole();
    if (console == null) {
      console = new Console();
    }
    Connection connection = getConnection();
    boolean autocommit = connection.getAutoCommit();
    if (autocommit) {
      connection.setAutoCommit(false);
    }

    int duplicateContentCount = executeQuery(DUPLICATE_CONTENT_COUNT_QUERY);
    String duplicateContents = "Number of duplicate content: " + duplicateContentCount;
    console.printMessageln(duplicateContents);
    System.out.println();
    System.out.println(duplicateContents);

    int classifiedContents = executeQuery(DUPLICATE_CLASSIFIED_CONTENT_COUNT_QUERY);
    console.printMessageln("Number of duplicate content that are classified: " + classifiedContents);

    console.printMessageln(
            "Delete the redundant instances of contents that aren't classified on the PdC");
    int deletedContents1 = executeDeletion(
            DELETION_OF_REDUNDANT_INSTANCES_OF_DUPLICATE_UNCLASSIFIED_CONTENT);
    console.printMessageln("-> number of redundant instances deleted: " + deletedContents1);

    console.printMessageln(
            "Delete all the unclassified redundant instances of the contents that are classified on the PdC");
    int deletedContents2 =
            executeDeletion(DELETION_OF_UNCLASSIFIED_INSTANCES_OF_DUPLICATE_CONTENT);
    console.printMessageln("-> number of redundant instances deleted: " + deletedContents2);

    console.printMessageln(
            "Delete all the redundant instance of the content for which all the instances are exceptionally classified on the PdC");
    int deletedContents3 = deleteRedundantClassifiedInstances();
    console.printMessageln("-> number of redundant instances deleted: " + deletedContents3);

    String deletedContents = "Number of cleaned up contents: " + (deletedContents1 + deletedContents2
            + deletedContents3);
    console.printMessageln(deletedContents);
    System.out.println();
    System.out.println(deletedContents);

    connection.commit();
    connection.setAutoCommit(autocommit);
  }
  
  private int executeQuery(String query) throws SQLException {
    Connection connection = getConnection();
    Statement statement = connection.createStatement();
    ResultSet resultSet = statement.executeQuery(query);
    resultSet.next();
    return resultSet.getInt(1);
  }

  private int executeDeletion(String query) throws SQLException {
    Connection connection = getConnection();
    Statement statement = connection.createStatement();
    return statement.executeUpdate(query);
  }

  private int deleteRedundantClassifiedInstances() throws SQLException {
    int deletedCount = 0;
    Connection connection = getConnection();
    Statement statement = connection.createStatement();
    ResultSet rs = statement.executeQuery(REDUNDANT_INSTANCE_OF_DUPLICATE_CONTENT_QUERY);
    // As it should have only a few (or no) results from the query above, we can execute the deletion
    // for each of the retrieved result.
    while (rs.next()) {
      int silverContentId = rs.getInt("silverContentId");
      deletedCount += executeDeletion(DELETION_OF_CLASSIFICATION_OF_A_GIVEN_CONTENT_INSTANCE
              + silverContentId);
      executeDeletion(DELETION_OF_A_GIVEN_CONTENT_INSTANCE + silverContentId);
    }
    return deletedCount;
  }
}
