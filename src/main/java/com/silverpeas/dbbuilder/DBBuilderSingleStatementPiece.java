package com.silverpeas.dbbuilder;

/**
 * Titre :        dbBuilder
 * Description :  Builder des BDs Silverpeas
 * Copyright :    Copyright (c) 2001
 * Société :      Stratélia Silverpeas
 * @author ATH
 * @version 1.0
 */

public class DBBuilderSingleStatementPiece extends DBBuilderPiece {


	// Contructeur utilisé pour une pièce de type fichier
        public DBBuilderSingleStatementPiece(String pieceName, String actionName, boolean traceMode) throws Exception {

		super(pieceName, actionName, traceMode);
		setInstructions();
        }

	// Contructeur utilisé pour une pièce de type chaîne en mémoire
        public DBBuilderSingleStatementPiece(String pieceName, String actionName, String content, boolean traceMode) throws Exception {

		super(pieceName, actionName, content, traceMode);
		setInstructions();
        }

	// Contructeur utilisé pour une pièce stockée en base de données
        public DBBuilderSingleStatementPiece(String actionInternalID, String pieceName, String actionName, int itemOrder, boolean traceMode) throws Exception {
		super(actionInternalID, pieceName, actionName, itemOrder, traceMode);
		setInstructions();
        }

	public void setInstructions() {

		instructions = new Instruction[1];
		instructions[0].setInstructionType(Instruction.IN_UPDATE);
		instructions[0].setInstructionText(getContent());
	}

	public void cacheIntoDB(String _package, int _itemOrder) throws Exception {

		cacheIntoDB(_package, _itemOrder, DBBuilderFileItem.FILEATTRIBSTATEMENT_VALUE, null, null, null);
	}

}
