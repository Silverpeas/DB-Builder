package com.silverpeas.dbbuilder_ep;

// import obligatoire pour la superclasse
import com.silverpeas.dbbuilder.dbbuilder_dl.DbBuilderDynamicPart;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.PropertyResourceBundle;
import java.util.Locale;

public class DbBuilder_ep extends DbBuilderDynamicPart {

	private static boolean needEncryption = false;
	private String m_PasswordEncryption = "";

        public DbBuilder_ep() {

		// recherche de la propriété spécifiant l'encryptage
		// -> si pb de lecture, on considère qu'on n'a pas à encrypter
		try {
		        PropertyResourceBundle propFile = (PropertyResourceBundle) PropertyResourceBundle.getBundle("com.stratelia.silverpeas.domains.domainSP", Locale.getDefault());
		        m_PasswordEncryption = propFile.getString("database.SQLPasswordEncryption");
		} catch (Exception e) {
			m_PasswordEncryption = "";
		}
        }

	public void run() throws Exception {

		Connection m_Connection = this.getConnection();

		// System.out.println("Data:"+this.getSILVERPEAS_DATA());
		// System.out.println("Connection:"+this.getConnection());

		ResultSet   rs = null;
		Statement   stmt = null;
		PreparedStatement   stmtUpdate = null;
		String      sUpdateStart = "UPDATE DomainSP_User SET password = '";
		String      sUpdateMiddle = "' WHERE id=";
		String      sClearPass;

		try {
		        if (m_PasswordEncryption.equals("CryptUnix")) {

			        stmt = m_Connection.createStatement();
	        	        rs = stmt.executeQuery("SELECT * FROM DomainSP_User");

			        while (rs.next()) {
	        			sClearPass = rs.getString("password");
		        		if (sClearPass == null)
			        	        sClearPass = "";
				        stmtUpdate = m_Connection.prepareStatement(sUpdateStart + jcrypt.crypt("SP",sClearPass) + sUpdateMiddle + rs.getString("id"));
					stmtUpdate.executeUpdate();
	        			stmtUpdate.close();
		        		stmtUpdate = null;
		                } // while
	            } // if

		} catch(SQLException ex) {
		        throw new Exception("Error during password Crypting : " + ex.getMessage());

		} finally {
		        try {
				if (rs != null) {
				        rs.close();
				        rs = null;
				} // if
				if (stmt != null) {
				        stmt.close();
				        stmt = null;
				} // if
				if (stmtUpdate != null) {
				        stmtUpdate.close();
				        stmtUpdate = null;
				} // if
		        } catch (SQLException ex) {}
		} // try
	}
}
