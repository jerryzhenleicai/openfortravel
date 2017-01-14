package com.gaocan.publictransportation;

public class InvalidScheduleDataException extends RuntimeException {
    String err_str;

	public InvalidScheduleDataException (Exception e) {
		super(e);
	}
    public InvalidScheduleDataException (String errstr, String msg) {
        super(errstr + msg);

        err_str = new String(errstr);
    }

    public String getOffendingString () {
        return err_str;
    }
}
