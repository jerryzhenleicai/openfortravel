package com.gaocan.train.train;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gaocan.publictransportation.RoutePlanner;
import com.gaocan.publictransportation.Schedule;

public class TrainSchedule extends Schedule {
    private static Logger logger = LoggerFactory.getLogger(TrainSchedule.class);
    static final String[] big_city_missing = { "黄山","合肥","蚌埠","芜湖","马鞍山","安庆",
        "北京",
        "重庆",
        "福州","邵武","厦门","漳州",
        "兰州","嘉峪关","武威","酒泉","天水",
        "广州","东莞","汕头","惠州","深圳","湛江","茂名","肇庆","佛山",
        "南宁","北海","玉林","桂林","柳州",
        "贵阳","六盘水","凯里",
        "石家庄","邢台","北戴河","唐山","秦皇岛","承德","保定",
        "哈尔滨","绥化","伊春","佳木斯","牡丹江","齐齐哈尔","大庆",
        "郑州","新乡","安阳","洛阳",
        "武汉","黄石","襄樊","宜昌",
        "长沙","张家界","衡阳","岳阳","怀化","湘潭","株洲",
        "长春","吉林","延吉","通化",
        "南京","镇江","常州","无锡","苏州","徐州",
        "庐山","南昌","九江","景德镇","鹰潭",
        "沈阳","辽阳","兴城","抚顺","鞍山","大连","丹东","锦州",
        "呼和浩特","包头","通辽",
        "银川",
        "西宁","格尔木",
        "济南","威海","德州","淄博","东营","潍坊","烟台","青岛","泰山","菏泽","聊城",
        "西安","咸阳","宝鸡",
        "太原","大同","运城",
        "上海",
        "成都","乐山","西昌","攀枝花","德阳","绵阳","广元","内江","自贡",
        "天津",
        "乌鲁木齐","石河子","吐鲁番","哈密","库尔勒",
        "昆明",
        "杭州","绍兴","嘉兴","宁波","义乌","温州","金华","萧山"
    };
	public static class AdjacentStationDistanceStat {
		int kmSum;
		int numPairs;
		public AdjacentStationDistanceStat() {
		}
	}
	
	/** key is station1->station2,  value is the Km between them */
	private HashMap<String, AdjacentStationDistanceStat> stationDistanceMap = new HashMap<String, AdjacentStationDistanceStat > ();
	
    public TrainSchedule (String  _cityName) {
		super(_cityName);
	}
    
    public void writeHeader(PrintWriter pw) throws IOException {
        pw.write(cvsVision + "\r\n");
    }
    
    public void readHeader(StreamTokenizer tokenStream) throws IOException {
        tokenStream.nextToken(); // Revision
        tokenStream.nextToken(); // 
        String dateLastChange = tokenStream.sval;
        if (dateLastChange != null && dateLastChange.startsWith("$LastChangedDate")) {
            lastChangeDate = dateLastChange.split(" ")[1];
        }
        
        // there is only "漳州East" in sched.txt
        for (String bigc : big_city_missing) {
            if (this.getAreaServedByStationsByName(bigc) == null) {
                //logger.warn("City " + bigc + " has no central station in sched, add it as a area " );
                this.addAreaServedByStations(new City(bigc));
            }
        }
    }

    public HashMap<String, AdjacentStationDistanceStat> getStationDistanceMap() {
		return stationDistanceMap;
	}
    
	public void setStationDistanceMap(
			HashMap<String, AdjacentStationDistanceStat> stationDistanceMap) {
		this.stationDistanceMap = stationDistanceMap;
	}
    
	public int getAvgDistanceKmBetweenStations(String from, String to) {
    	String key = from + "-" + to;
    	AdjacentStationDistanceStat stat = this.stationDistanceMap.get(key);
    	if (stat == null) {
    		return -1;
    	} 
    	return stat.kmSum / stat.numPairs;
	}
	
    @Override
    public void write(PrintWriter pw) throws IOException {
        pw.println("v2");
        super.write(pw);
    }
    
	/**
	 * clone a live sched and return a copy used for editing etc
	 * @return
	 */
	public Schedule getClone() {
		if (this.getTextFilePath() == null) {
			throw new IllegalStateException("Unknown text file for sched");
		}
	    Schedule schedCpy = new TrainSchedule("trainschedCopy");
	    (new TrainScheduleFactory()).buildSchedule(schedCpy, getTextFilePath());
	    schedCpy.setUsedLive(false);
	    return schedCpy;
	}
}
