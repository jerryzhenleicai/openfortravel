package com.gaocan.publictransportation;

public abstract class LineScheduleError {
	private String lineNumber;
	
	protected LineScheduleError(String lineNum) {
		lineNumber = lineNum;
	}
	
	public abstract boolean isFatal();
	
	public String getErrorMessage() {
		String template = getErrorMessageTemplate();
		String msg = template.replaceAll("%l", this.getLineNumber());
		msg = msg.replaceAll("%s1", getStation1Name());
		msg = msg.replaceAll("%s2", getStation2Name());
		return msg;
	}

    public String toString() {
        return getErrorMessage();
    }
	/**
	 * %l has an error with stations %s1 %s2
	 * @return
	 */
	public abstract String getErrorMessageTemplate();

	public abstract String getStation1Name();
	public String getStation2Name() {
		return null;
	}

	public String getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}
}
