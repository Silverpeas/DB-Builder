package com.silverpeas.dbbuilder;

/**
 * Titre :        dbBuilder
 * Description :  Builder des BDs Silverpeas
 * Copyright :    Copyright (c) 2001
 * Société :      Stratélia Silverpeas
 * @author ATH
 * @version 1.0
 */

import com.stratelia.dbConnector.DBConnexion;

import org.jdom.*;
import java.util.*;

public abstract class DBBuilderItem {

	static public final String MODULENAME_ATTRIB = "product";
	static public final String CURRENT_TAG = "current";
	static public final String PREVIOUS_TAG = "upgrade";
	static public final String VERSION_ATTRIB = "version";
	static public final String NOTINSTALLED = "xxx";
	static public final String CONTRIBUTION_TAG = "contribution";

	static public final String CREATE_TABLE_TAG = "create_table";
	static public final String CREATE_INDEX_TAG = "create_index";
	static public final String CREATE_CONSTRAINT_TAG = "create_constraint";
	static public final String CREATE_DATA_TAG = "init";
	static public final String DROP_TABLE_TAG = "drop_table";
	static public final String DROP_INDEX_TAG = "drop_index";
	static public final String DROP_CONSTRAINT_TAG = "drop_constraint";
	static public final String DROP_DATA_TAG = "clean";

	static public final String FILE_TAG = "file";
	static public final String ROW_TAG = "row";
	static public final String FILENAME_ATTRIB = "name";
	static public final String FILETYPE_ATTRIB = "type";
	static public final String FILEDELIMITER_ATTRIB = "delimiter";
	static public final String FILEKEEPDELIMITER_ATTRIB = "keepdelimiter";
	static public final String FILEDBPROCNAME_ATTRIB = "dbprocname";
	static public final String FILECLASSNAME_ATTRIB = "classname";
	static public final String FILEMETHODNAME_ATTRIB = "methodname";
	static public final String DBORDER_ATTRIB = "sequence";

	// static public final String FILEATTRIB_ATTRIB = "attrib";

	static public final String FILEATTRIBSTATEMENT_VALUE = "sqlstatement";
	static public final String FILEATTRIBSEQUENCE_VALUE = "sqlstatementlist";
	static public final String FILEATTRIBDBPROC_VALUE = "dbprocedure";
	static public final String FILEATTRIBJAVALIB_VALUE = "javalib";

	private String module = null;
	protected String versionFromFile = null;
	private String versionFromDB = null;

	private DBXmlDocument fileXml;
	private Element root;

	public void setFileXml(DBXmlDocument fileXml) {
		this.fileXml = fileXml;
	}
	public DBXmlDocument getFileXml() {
		return fileXml;
	}

	public void setRoot(Element root) {
		this.root = root;
	}
	public Element getRoot() {
		return root;
	}

	public String getModule() {
		return module;
	}
	protected void setModule(String module) {
		this.module = module;
	}

	public abstract String getVersionFromFile() throws Exception;

	public String getVersionFromDB() throws Exception {
		if (versionFromDB == null) {
			String statement = "select SR_VERSION as version from SR_PACKAGES where SR_PACKAGE = '" + getModule() + "'";
			HashMap h = new HashMap();
			try {
				h = DBConnexion.getInstance().executeQuery(statement);
			} catch (Exception e) {}

			if (h.containsKey("VERSION"))     //ORACLE
                        {
                          versionFromDB = (String) h.get("VERSION");
                        }
                        else if (h.containsKey("version"))    //POSTGRES-MSSQL
                          versionFromDB = (String) h.get("version");

	        else
		        versionFromDB = NOTINSTALLED;
		} // if
		return versionFromDB;
	}

	public Element getUniqueBlock(String b, String v) throws Exception {

        	List listeCurrent = getRoot().getChildren(b);

		if (listeCurrent == null)
        		throw new Exception(getModule() + ": no <" + b + "> tag found for this module into contribution file.");
		if (listeCurrent.size() == 0)
        		throw new Exception(getModule() + ": no <" + b + "> tag found for this module into contribution file.");

		// ici correction ATH t005 05/06/2002
		// il est autorisé qu'un tag apparaisse +sieurs fois : c'est necessaire pour <previous>
		// if (listeCurrent.size() != 1)
        	//	throw new Exception(getModule() + ": tag <" + b + "> appears more than one.");

		Iterator iterCurrent = listeCurrent.iterator();
	        Element myElement = null;

        	while ( iterCurrent.hasNext() ) {

			Element eltCurrent = ( Element ) iterCurrent.next();
        		if (eltCurrent.getAttributeValue(DBBuilderFileItem.VERSION_ATTRIB).equals(v))
				myElement = eltCurrent;
	        } // while

		if (myElement == null)
        		throw new Exception(getModule() + ": no version <" + v + "> for <" + b + "> tag found for this module into contribution file.");

		return myElement;

	}

}
