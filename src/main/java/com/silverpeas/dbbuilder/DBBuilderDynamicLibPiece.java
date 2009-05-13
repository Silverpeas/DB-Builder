package com.silverpeas.dbbuilder;

import com.stratelia.dbConnector.DBConnexion;
import com.silverpeas.dbbuilder.dbbuilder_dl.DbBuilderDynamicPart;

public class DBBuilderDynamicLibPiece extends DBBuilderPiece {

  private String className = null;
  private String methodName = null;
  private DbBuilderDynamicPart dynamicPart = null;

  // contructeurs non utilisés
  private DBBuilderDynamicLibPiece(String pieceName, String actionName, boolean traceMode) throws Exception {
    super(pieceName, actionName, traceMode);
  }

  private DBBuilderDynamicLibPiece(String pieceName, String actionName, String content, boolean traceMode) throws Exception {
    super(pieceName, actionName, content, traceMode);
  }

  // Contructeur utilisé pour une pièce de type fichier
  public DBBuilderDynamicLibPiece(String pieceName, String actionName, boolean traceMode, String className, String methodName) throws Exception {
    super(pieceName, actionName, traceMode);
    moreInitialize(className, methodName);
  }

  // Contructeur utilisé pour une pièce de type chaîne en mémoire
  public DBBuilderDynamicLibPiece(String pieceName, String actionName, String content, boolean traceMode, String className, String methodName) throws Exception {
    super(pieceName, actionName, content, traceMode);
    moreInitialize(className, methodName);
  }

  private void moreInitialize(String className, String methodName) throws Exception {

    if (className == null) {
      throw new Exception("Missing <classname> tag for \"pieceName\" item.");
    }

    if (methodName == null) {
      throw new Exception("Missing <methodname> tag for \"pieceName\" item.");
    }

    this.className = className;
    this.methodName = methodName;

    try {
      dynamicPart = (DbBuilderDynamicPart) Class.forName(className).newInstance();

    } catch (Exception e) {
      throw new Exception("Unable to load \"" + className + "\" class.");
    } // try

    dynamicPart.setSILVERPEAS_HOME(DBBuilder.getHome());
    dynamicPart.setSILVERPEAS_DATA(DBBuilder.getData());
    dynamicPart.setConnection(DBConnexion.getInstance().getConnection());

    setInstructions();
  }

  public void setInstructions() {

    instructions = new Instruction[1];
    instructions[0] = new Instruction(Instruction.IN_INVOKEJAVA, methodName, dynamicPart);
  }

  public void cacheIntoDB(String _package, int _itemOrder) throws Exception {
    // rien à cacher pour une proc dynamique
  }
}
