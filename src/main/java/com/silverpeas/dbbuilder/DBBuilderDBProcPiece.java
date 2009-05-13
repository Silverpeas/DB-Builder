package com.silverpeas.dbbuilder;

/**
 * Titre :        dbBuilder
 * Description :  Builder des BDs Silverpeas
 * Copyright :    Copyright (c) 2001
 * Société :      Stratélia Silverpeas
 * @author ATH
 * @version 1.0
 */

public class DBBuilderDBProcPiece extends DBBuilderPiece {

	private String dbProcName = null;

	// contructeurs non utilisés
        private DBBuilderDBProcPiece(String pieceName, String actionName, boolean traceMode) throws Exception {super(pieceName, actionName, traceMode);}
        private DBBuilderDBProcPiece(String pieceName, String actionName, String content, boolean traceMode) throws Exception {super(pieceName, actionName, content, traceMode);}
        private DBBuilderDBProcPiece(String actionInternalID, String pieceName, String actionName, int itemOrder, boolean traceMode) throws Exception {super(actionInternalID, pieceName, actionName, itemOrder, traceMode);}

	// Contructeur utilisé pour une pièce de type fichier
        public DBBuilderDBProcPiece(String fileName, String actionName, boolean traceMode, String dbProcName) throws Exception {

		super(fileName, actionName, traceMode);
		moreInitialize(dbProcName);
        }

	// Contructeur utilisé pour une pièce de type chaîne en mémoire
        public DBBuilderDBProcPiece(String pieceName, String actionName, String content, boolean traceMode, String dbProcName) throws Exception {

		super(pieceName, actionName, content, traceMode);
		moreInitialize(dbProcName);
        }

	// Contructeur utilisé pour une pièce stockée en base de données
        public DBBuilderDBProcPiece(String actionInternalID, String pieceName, String actionName, int itemOrder, boolean traceMode, String dbProcName) throws Exception {

		super(actionInternalID, pieceName, actionName, itemOrder, traceMode);
		moreInitialize(dbProcName);
        }

	private void moreInitialize(String dbProcName) throws Exception {

		if (dbProcName==null)
			throw new Exception("Missing <dbprocname> tag for \"fileName\" item.");

		this.dbProcName = dbProcName;

		setInstructions();
        }

	public void setInstructions() {

		instructions = new Instruction[3];
		instructions[0] = new Instruction(Instruction.IN_UPDATE, getContent(), null);
		instructions[1] = new Instruction(Instruction.IN_CALLDBPROC, this.dbProcName, null);
		instructions[2] = new Instruction(Instruction.IN_UPDATE, "DROP PROCEDURE " + this.dbProcName, null);
	}

	public void cacheIntoDB(String _package, int _itemOrder) throws Exception {

		cacheIntoDB(_package, _itemOrder, DBBuilderFileItem.FILEATTRIBDBPROC_VALUE, null, null, dbProcName);
	}

}