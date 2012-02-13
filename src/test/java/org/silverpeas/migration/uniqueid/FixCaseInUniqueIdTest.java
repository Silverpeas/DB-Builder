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
package org.silverpeas.migration.uniqueid;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.DatabaseConnection;
import org.springframework.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import org.dbunit.dataset.ITable;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.dbunit.dataset.IDataSet;
import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.migration.contentmanagement.DuplicateContentRemoving;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.dbunit.Assertion.*;

/**
 * Tests on the migration of the sb_contentmanager_content table.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring-uniqueid-datasource.xml")
public class FixCaseInUniqueIdTest {

  public static final String UNIQUEID_TABLE = "uniqueId";
  private IDatabaseTester databaseTester;
  @Inject
  private DataSource dataSource;

  public FixCaseInUniqueIdTest() {
  }

  @Before
  public void prepareDatabase() throws Exception {
    assertThat(dataSource, notNullValue());
    databaseTester = new DataSourceDatabaseTester(dataSource);
    databaseTester.setDataSet(getDataSet());
    databaseTester.onSetup();
  }

  @After
  public void cleanDatabase() throws Exception {
    databaseTester.onTearDown();
  }

  /**
   * Test the migration of the sb_contentmanager_content table.
   */
  @Test
  public void testMigration() throws Exception {
    FixCaseInUniqueId fixCaseInUniqueId = new FixCaseInUniqueId();
    fixCaseInUniqueId.setConnection(databaseTester.getConnection().getConnection());
    fixCaseInUniqueId.migrate();
    ITable actualContentTable = getActualTable(UNIQUEID_TABLE);
    ITable expectedContentTable = getExpectedTable(UNIQUEID_TABLE);
    assertThat(actualContentTable.getRowCount(), is(4));
    assertEquals(expectedContentTable, actualContentTable);
  }

  protected IDataSet getDataSet() throws Exception {
    return new FlatXmlDataSetBuilder().build(getClass().getResourceAsStream("uniqueId-dataset.xml"));
  }

  protected IDataSet getExpectedDataSet() throws Exception {
    return new FlatXmlDataSetBuilder().build(getClass().getResourceAsStream(
        "expected-uniqueId-dataset.xml"));
  }

  protected ITable getActualTable(String tableName) throws Exception {
    Connection connection = DataSourceUtils.getConnection(dataSource);
    IDatabaseConnection databaseConnection = new DatabaseConnection(connection);
    IDataSet dataSet = databaseConnection.createDataSet();
    ITable table = dataSet.getTable(tableName);
    DataSourceUtils.releaseConnection(connection, dataSource);
    return table;
  }

  protected ITable getExpectedTable(String tableName) throws Exception {
    IDataSet dataSet = getExpectedDataSet();
    return dataSet.getTable(tableName);
  }
}
