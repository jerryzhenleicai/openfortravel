package com.gaocan.train.train;

public class TrainLineNumber {
	private String number1 = null;
	private String number2 = null;
    
    /**
     * if XXX/YYY
     * @return
     */
    public boolean isCompoundNumber() {
        return number2 != null;
    }
    
	public String getNumber1() {
		return number1;
	}
	public void setNumber1(String number1) {
		this.number1 = number1;
	}
	public String getNumber2() {
		return number2;
	}
	public void setNumber2(String number2) {
		this.number2 = number2;
	}
	
	public String getFullNumber() {
	    if (number2 == null) {
	        return number1;    
        }
        return number1 + "/" + number2;
    }

}
