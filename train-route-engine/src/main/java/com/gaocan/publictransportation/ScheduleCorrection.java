package com.gaocan.publictransportation;

/**
 * one user submitted correction to schedule
 * @author zcai
 *
 */
public abstract class ScheduleCorrection {
    
    /** 
     * @return error message if apply failed
     */
    public String applyToSched(Schedule sched) {
        try {
            applyTo(sched);
        } catch (ApplyCorrectionException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return null;
    }
    
    /**
     * modify the sched
     * @param sched schedule to be modified by this
     */
    public abstract void applyTo(Schedule sched) throws ApplyCorrectionException;
    
    /**
     * 
     * @param sched
     * @param force if true then any conflicts are ignored and the correction is forcely applied, for example,
     * even if the schedule may already have N1, we wll remove that and add the new N1
     * @return error found during applying
     */
    //public abstract void applyTo(Schedule sched, boolean force) ;
}
