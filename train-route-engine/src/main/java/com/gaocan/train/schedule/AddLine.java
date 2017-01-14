package com.gaocan.train.schedule;

import com.gaocan.publictransportation.Line;
import com.gaocan.publictransportation.Schedule;
import com.gaocan.publictransportation.ScheduleCorrection;
import com.gaocan.publictransportation.ScheduleFactory;
import com.gaocan.train.train.TrainScheduleFactory;

public class AddLine extends ScheduleCorrection {
    private String                      lspText;
    private static ScheduleFactory trainScheduleFactory = new TrainScheduleFactory();
    private String 						lineNumber ; 
    /**
     * 
     * @param lineNum
     *            line number
     * @param lineText
     *            string representation of the line data
     */
    public AddLine(String lineNum, String lineText) {
        lspText = lineText;
        lineNumber = lineNum;
    }

    public void applyTo(Schedule schedule) {
        schedule.addLine(trainScheduleFactory.createLineFromLspText(lineNumber, lspText));
    }
}