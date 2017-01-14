package com.gaocan.train.route;

import java.util.HashMap;

public class RouteTestData {
    public static HashMap<String, String[]> cityPairRoutes = new HashMap<String, String[]>();

    public static String SRC_DEST_DELIM = "-";

    private static void populateData(String src, String dest, String[] routes) {
        cityPairRoutes.put(src + SRC_DEST_DELIM + dest, routes);
    }

    public static void init() {
        // BJ XA
        String[] bjxa = { "Z19", "G87", "T7" };
        populateData("北京", "西安", bjxa);

        // testChengduShanghai()
        String[] cdsh = { "D2205", "K1158" };
        populateData("成都", "上海", cdsh);

        String[] cdwh = { "D2241", "D2260" };
        populateData("成都", "武汉", cdwh);

        // NaningHarbin()
        String[] nnhb = { "G422", "Z6", "Z286" };
        populateData("南宁", "北京", nnhb);

        String[] shwh = { "D3002", "G576", "G676"};
        populateData("上海", "武汉", shwh);

        String[] hbnn = { "G382" };
        populateData("哈尔滨", "南宁", hbnn);

        String[] fzwlmq = { "K30" };
        populateData("福州", "乌鲁木齐", fzwlmq);

        String[] hbwlmq = { "D28" };
        populateData("哈尔滨", "乌鲁木齐", hbwlmq);

        String[] wlmqhb = { "Z180", "Z70" };
        populateData("乌鲁木齐", "哈尔滨", wlmqhb);

        String[] myjn = { "K206" };
        populateData("绵阳", "济南", myjn);

        String[] xash = { "T137", };
        populateData("西安", "上海", xash);

    }
}
