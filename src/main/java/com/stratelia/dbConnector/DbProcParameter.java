package com.stratelia.dbConnector;

public class DbProcParameter {

	private boolean isOutParameter = false;  // vrai pour un paramètre de sortie
	private int parameterType; // Type du paramètre si paramètre de sortie
	private Object parameterValue; // valeur du paramètre à utiliser pour un paramètre en entrée

	public DbProcParameter(boolean _isOutParameter, int _parameterType, Object _parameterValue) {

		isOutParameter = _isOutParameter;
		parameterType = _parameterType;
		parameterValue = _parameterValue;
	}

	public boolean getIsOutParameter() {

		return isOutParameter;
	}
	public void setIsOutParameter(boolean _isOutParameter) {

		isOutParameter = _isOutParameter;
	}

	public int getParameterType() {

		return parameterType;
	}
	public void setParameterType(int _parameterType) {

		parameterType = _parameterType;
	}

	public Object getParameterValue() {

		return parameterValue;
	}
	public void setParameterValue(Object _parameterValue) {

		parameterValue = _parameterValue;
	}

}
