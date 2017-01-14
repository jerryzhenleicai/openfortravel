/*
 * Created on Nov 11, 2003
 * Copyright (c) Gaocan Inc., Oak Hill, VA, USA, All rights reserverd.
 */
package com.gaocan.train.train;

import com.gaocan.publictransportation.AbstractLineFactory;
import com.gaocan.publictransportation.Line;

/**
 * @author zcai
 * 
 */
public class TrainLineFactory implements AbstractLineFactory {
    public Line createLine(String name, String extraData) {
        TrainLine line = new TrainLine(name);
        // extra string : <type:公里[(effectiveDates)];comment>
        // sample comment is 隔日开行 or summer onyl
        int alt = extraData.indexOf("[隔日开行]");
        if (alt != -1) {
            extraData = extraData.substring(0, alt);
            line.setAlternateDay(true);
        }
        String[] ds = extraData.split("[:,;]");
        line.setInterval(ds[0]);
        if (ds.length < 3) {
            throw new IllegalStateException("缺少公里数:" + name + "," + extraData);
        }
        int kmIdx = ds[2].indexOf("公里");
        if (kmIdx == -1) {
            throw new IllegalStateException("缺少公里数:" + name + "," + extraData);
        }
        line.setFullLengthKm(Integer.parseInt(ds[2].substring(0, kmIdx)));
        if (ds.length > 3) {
            String effecDates = ds[3];
            if (!effecDates.isEmpty()) {
                line.parseEffectiveDates(effecDates);
            }
            // anything after 3 is comment
            if (ds.length > 4) {
                StringBuilder sb = new StringBuilder();
                for (int c = 4; c < ds.length; c++) {
                    sb.append(ds[c]);
                }
                line.setComment(sb.toString());
            }
        }
        return line;
    }

    public boolean lineHasExtraData() {
        return true;
    }
}
