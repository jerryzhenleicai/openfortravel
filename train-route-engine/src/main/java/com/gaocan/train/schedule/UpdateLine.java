package com.gaocan.train.schedule;

import com.gaocan.publictransportation.DeleteLine;
import com.gaocan.publictransportation.Schedule;
import com.gaocan.publictransportation.ScheduleCorrection;

public class UpdateLine extends ScheduleCorrection {
    private String                      lspText;
    private String 						lineNumber ; 

    /**
     * 
     * @param lineNum
     *            line number
     * @param lineText
     *            string representation of the line data
     */
    public UpdateLine(String lineNum, String lineText) {
        lineNumber = lineNum;
        lspText = lineText;
    }

    public void applyTo(Schedule schedule) {
    	// first delete then add line
        (new DeleteLine(lineNumber)).applyTo(schedule);
        (new AddLine(lineNumber, lspText)).applyTo(schedule);
    }
}