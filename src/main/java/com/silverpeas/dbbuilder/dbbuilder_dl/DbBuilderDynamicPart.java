package com.silverpeas.dbbuilder.dbbuilder_dl;

import java.sql.Connection;
/**
 * Titre :
 * Description :
 * Copyright :    Copyright (c) 2002
 * Société :
 * @author
 * @version 1.0
 */

public abstract class DbBuilderDynamicPart {

	private String SILVERPEAS_HOME = null;
	private String SILVERPEAS_DATA = null;
	private Connection con = null;

        public DbBuilderDynamicPart() {
        }

	public void setSILVERPEAS_HOME(String sh) throws Exception {
		if (SILVERPEAS_HOME!=null)
			throw new Exception("DbBuilderDynamicPart.setSILVERPEAS_HOME() fatal error : SILVERPEAS_HOME is already set.");
		SILVERPEAS_HOME = sh;
	}

	public void setSILVERPEAS_DATA(String sh) throws Exception  {
		if (SILVERPEAS_DATA!=null)
			throw new Exception("DbBuilderDynamicPart.setSILVERPEAS_DATA() fatal error : SILVERPEAS_DATA is already set.");
		SILVERPEAS_DATA = sh;
	}

	public void setConnection(Connection con) throws Exception {
		if (this.con!=null)
			throw new Exception("DbBuilderDynamicPart.setConnection() fatal error : Connection is already set.");
		this.con = con;
	}

	public String getSILVERPEAS_HOME() {
		return SILVERPEAS_HOME;
	}

	public String getSILVERPEAS_DATA() {
		return SILVERPEAS_DATA;
	}

	public Connection getConnection() {
		return con;
	}
}
