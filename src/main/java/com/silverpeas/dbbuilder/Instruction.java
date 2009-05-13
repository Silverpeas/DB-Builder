package com.silverpeas.dbbuilder;

public class Instruction {

	static final public int IN_CALLDBPROC = 0;
	static final public int IN_UPDATE = 1;
	static final public int IN_INVOKEJAVA = 2;

	private int instructionType = -1;
	private String instructionText = "";
	private Object instructionDetail = null;

        public Instruction() {}

        public Instruction(int instructionType, String instructionText, Object instructionDetail) {

		this.instructionType = instructionType;
		this.instructionText = instructionText;
		this.instructionDetail = instructionDetail;
        }

	public void setInstructionType(int instructionType) {
		this.instructionType = instructionType;
	}
	public void setInstructionText(String instructionText) {
		this.instructionText = instructionText;
	}
	public void setInstructionDetail(Object instructionDetail) {
		this.instructionDetail = instructionDetail;
	}

	public int getInstructionType() {
		return instructionType;
	}
	public String getInstructionText() {
		return instructionText;
	}
	public Object getInstructionDetail() {
		return instructionDetail;
	}
}