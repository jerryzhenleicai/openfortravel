package com.gaocan.train.route;

public class RouteParam {
    private String schedDirectory ;
    private boolean usePrecompute ;
    private boolean schedEditable ;
    private int maxQueueLength;
    
    public String getSchedDirectory() {
        return schedDirectory;
    }
    public void setSchedDirectory(String schedDirectory) {
        this.schedDirectory = schedDirectory;
    }
    public boolean isUsePrecompute() {
        return usePrecompute;
    }
    public void setUsePrecompute(boolean usePrecompute) {
        this.usePrecompute = usePrecompute;
    }
    public boolean isSchedEditable() {
        return schedEditable;
    }
    public void setSchedEditable(boolean schedEditable) {
        this.schedEditable = schedEditable;
    }
    public int getMaxQueueLength() {
        return maxQueueLength;
    }
    public void setMaxQueueLength(int maxQueueLength) {
        this.maxQueueLength = maxQueueLength;
    }
}
