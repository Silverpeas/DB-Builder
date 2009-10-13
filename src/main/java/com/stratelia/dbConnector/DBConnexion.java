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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
/**
 * La classe DBConnexion réalise une connection base de données
 * au travers de la classe de connection JDBC Connection.
 * Elle implémente par ailleurs une méthode générique de lecture
 */
package com.stratelia.dbConnector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.CallableStatement;
import java.util.Properties;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DBConnexion {

  private static DBConnexion con = null;
  private Connection connection = null;
  private Properties props = new Properties();
  private String username = "";

  private DBConnexion() {
  }

  public static DBConnexion getInstance() {

    if (con == null) {
      con = new DBConnexion();
    }

    return con;
  }

  /**
   * Crée une DBConnexion en utilisant l'URL et les propriétés
   * spécifiées.
   * @param url l'URL JDBC pour cette DatabaseConnection
   * @param p les propriétés, généralement contenant le nom de login et le mot de passe
   * @exception Exception Une erreur est survenue lors de la connexion à l'URL
   */
  public void dbConnexionInitialize(String u, Properties p) throws Exception {

    // Tentative de connexion
    try {
      this.username = u;
      this.props = p;
      connection = DriverManager.getConnection(u, p);
    } catch (SQLException e) {
      throw (Exception) e;
    }

  }

  /**
   * @return Le JDBC Connection pour cette DBConnexion
   */
  public Connection getConnection() {
    try {
      if (connection == null || connection.isClosed()) {
        connection = DriverManager.getConnection(username, props);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return connection;
  }

  public HashMap executeQuery(String query) throws Exception {
    Statement stmt = null;
    HashMap h = new HashMap();
    try {
      // Crée l'instruction JDBC à partir de la connexion
      stmt = connection.createStatement();
      // Exécute le SQL
      ResultSet results = stmt.executeQuery(query);
      // Récupère le meta data
      ResultSetMetaData meta = results.getMetaData();
      // Vérification qu'on a un enregistrement!
      if (!results.next()) {
        results.close();
        stmt.close();
      } // if
      // Pour chaque colonne du result set
      for (int i = 1; i
              <= meta.getColumnCount(); i++) {
        Object ob = results.getObject(i);
        // Met la valeur dans la HashMap en utilisant le nom
        // de la colonne comme clé
        h.put(meta.getColumnLabel(i), ob);
      } // for
      results.close();
      stmt.close();
    } catch (SQLException e) {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e2) {
        }
      } // if

      try {
        abort();
      } catch (SQLException e2) {
      }
      throw (Exception) e;
    } // try
    return h;
  }

  public List<Map<String, Object>> executeLoopQuery(String query) throws Exception {
    return executeLoopQuery(query, null);
  }

  public List<Map<String, Object>> executeLoopQuery(String query, Object[] parameters) throws Exception {
    Statement stmt = null;
    PreparedStatement pstmt = null;
    ArrayList array = new ArrayList();
    try {
      ResultSet results;
      if (parameters == null) {
        // Crée l'instruction JDBC à partir de la connexion
        stmt = connection.createStatement();
        // Exécute le SQL
        results = stmt.executeQuery(query);
      } else {
        // Crée l'instruction JDBC à partir de la connexion
        pstmt = connection.prepareStatement(query);
        int nbparameters = parameters.length;
        for (int i = 0; i
                < nbparameters; i++) {
          pstmt.setObject(i + 1, parameters[i]);
        } // for
        // Exécute le SQL
        results = pstmt.executeQuery();
      } // if
      // Récupère le meta data
      ResultSetMetaData meta = results.getMetaData();
      // Tant qu'on a des enregistrements dans le result set
      while (results.next()) {

        // Stockage d'un enregistrement
        HashMap<String, Object> h = new HashMap<String, Object>(meta.getColumnCount());
        // Pour chaque colonne du result set
        for (int i = 1; i
                <= meta.getColumnCount(); i++) {
          Object ob = results.getObject(i);
          h.put(meta.getColumnLabel(i).toUpperCase(), ob);
        } // for
        // Ajoute le résultat dans le vecteur
        array.add(h);
      } // while
      // Fermeture de l'ensemble résultat
      results.close();
      if (stmt != null) {
        stmt.close();
      }
      if (pstmt != null) {
        pstmt.close();
      }
    } catch (SQLException e) {

      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e2) {
        }
      } // if
      if (pstmt != null) {
        try {
          pstmt.close();
        } catch (SQLException e2) {
        }
      } // if
      try {
        abort();
      } catch (SQLException e2) {
      }
      throw (Exception) e;
    }
    return array;
  }

  public void executeUpdate(String query) throws Exception {

    // ignore les instructions vides
    if (query.trim().length() == 0) {
      return;
    }
    Statement stmt = null;
    try {
      //System.out.println(query);

      // Crée l'instruction JDBC à partir de la connexion

      stmt = connection.createStatement();

      // Exécute le SQL
      stmt.executeUpdate(query);

      // Ferme le statement
      stmt.close();





    } catch (SQLException e) {
      if (stmt != null) {
        try {
          stmt.close();




        } catch (SQLException e2) {
        }
      } // if

      try {
        abort();




      } catch (SQLException e2) {
      }

      throw (Exception) e;




    }
  }

  public void executeProcedure(String _procedureName, DbProcParameter[] _dbProcParameters) throws Exception {

    CallableStatement call = null;
    DbProcParameter dbPP;
    String preparedStatement = null;




    int i;





    try {

      // Prépare l'appel JDBC à la procédure sur la connection,
      // cette procédure admet des paramètres en entrée et en sortie mais ne doit rien retourner
      if (_dbProcParameters == null) {
        preparedStatement = "{call " + _procedureName + "}";




      } else if (_dbProcParameters.length == 0) {
        preparedStatement = "{call " + _procedureName + "}";




      } else {
        preparedStatement = "{call " + _procedureName + "(";





        if (_dbProcParameters != null) {

          for (i = 0; i
                  < _dbProcParameters.length; i++) {

            preparedStatement += "?";




            if (i != (_dbProcParameters.length - 1)) {
              preparedStatement += ",";




            }
          } // for
        } // if

        preparedStatement += ")}";




      } // if

      call = connection.prepareCall(preparedStatement);





      if (_dbProcParameters != null) {

        for (i = 0; i
                < _dbProcParameters.length; i++) {

          dbPP = _dbProcParameters[i];

          // PCT 28/8 Ajout reformatage du paramètre
          //call.setObject(i + 1, DatabasePeer.getSQLValue(dbPP.parameterValue),

          call.setObject(i + 1, dbPP.getParameterValue(), dbPP.getParameterType());





          if (dbPP.getIsOutParameter() == true) {
            call.registerOutParameter(i + 1, dbPP.getParameterType());




          }

        } // for
      } // if

      // Exécute la procédure
      call.execute();

      // Initialise le tableau des paramètres en sortie




      if (_dbProcParameters != null) {

        for (i = 0; i
                < _dbProcParameters.length; i++) {

          if (_dbProcParameters[i].getIsOutParameter() == true) {
            /***
            System.out.println("Valeur de retour (Object) en " + i + ":" + call.getObject(i + 1));
            System.out.println("Valeur de retour (String) en " + i + ":" + call.getString(i + 1));
             ***/
            _dbProcParameters[i].setParameterValue(call.getObject(i + 1));




          } // if
        } // for

      } // if
    } catch (SQLException e) {

      throw (Exception) e;




    } // try

  }

  public synchronized void freeConnection() throws Exception {

    try {
      connection.close();
      connection = null;




    } catch (SQLException e) {
      throw (Exception) e;




    }
  }

  public synchronized void startTransaction() throws Exception {

    if (connection.getAutoCommit()) {

      try {
        connection.setAutoCommit(false);




      } catch (SQLException e) {
        throw (Exception) e;




      }
    } // if
  }

  public void abort() throws Exception {

    if (!connection.getAutoCommit()) {
      try {
        connection.rollback();





      } catch (SQLException e) {
        throw (Exception) e;




      }
    } // if
  }

  public synchronized void commit() throws Exception {

    if (!connection.getAutoCommit()) {
      try {
        connection.commit();





      } catch (SQLException e) {
        throw (Exception) e;


      }
    }
  }
}
