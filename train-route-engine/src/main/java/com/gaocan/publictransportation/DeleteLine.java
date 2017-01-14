package com.gaocan.publictransportation;

public class DeleteLine extends ScheduleCorrection {
    private String lineNumber;
    public DeleteLine(String lineNum) {
        lineNumber = lineNum;
    }
    public void applyTo(Schedule sched) {
        sched.removeLineByNumber(lineNumber);
    }
    
}
