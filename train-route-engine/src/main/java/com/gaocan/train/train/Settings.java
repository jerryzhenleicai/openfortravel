package com.gaocan.train.train;

import java.util.HashMap;


public class Settings {
    // Fees
    public static final int fee_station_search = 5;
    public static final int fee_line_search = 0;
    public static final int fee_line_view = 2;
    public static final int fee_path_search = 10;
    public static final boolean needs_fee = false;

    public static final int MAX_ROUTES_SHOWN_TO_USER = 45;
    public static final int NUM_ROUTES_PREFILTER = 2 * MAX_ROUTES_SHOWN_TO_USER;
    static public final int max_num_users_in_queue = 4;
    static public final String top_city_file_name = "bigcities.txt";
    static public final String astar_top_city_pair_file_name = "big_city_routes.txt";
    static public final String astar_from_top_city_file_name = "as_from_top.txt";
    static public final String astar_to_top_city_file_name = "as_to_top.txt";
    static public final String price_table_file_name = "price.txt";
    public static TrainPriceImporter train_price_importer;

    /** same name in two provinces,  mapping linen numbe to station name, so only that lsp is skipped */
    static public HashMap<String,String> duplicateCities = new HashMap<String,String>();

    // stations whose names are <city>East/North only but really not serves that city
    static public java.util.Vector falseCitySecondaryStations = new java.util.Vector();

    // some train lines are wrong and they are not considered in planning graph
    static {
        // 清水河西  is in YunNan  清水河 is in LiaoNing
        falseCitySecondaryStations.addElement("清水河西");

        // 河口南 is near Lan Zhou, but a 河口 is in Yun Nan
        falseCitySecondaryStations.addElement("河口南");

        // 石门 南 is near LHuanan , but a 石门  in hebei
        falseCitySecondaryStations.addElement("石门南");

        // ignore cities which are same name
        // too manu 水库
        duplicateCities.put("*","水库");

        // one in FuJian, one in LiaoNing     , dont do liaoning
        duplicateCities.put("6751","永安");
        duplicateCities.put("6755","永安");
        duplicateCities.put("6756","永安");
        duplicateCities.put("6757","永安");
        duplicateCities.put("6775","永安");
        // in dong bei and  sichuan, kill dong bai
        duplicateCities.put("6051","团结村");

        // in Heilong jiang and  hubei

        /*  BanRuo has no dups for this
        duplicate_cities.add("襄河", "*");
        */

        // in zhejiang and liaoning

        /*  BanRuo has no dups for this
        duplicate_cities.add("海城", "2207");
        // in sichuan nd   annhui
        duplicate_cities.add("大通", "*");
        // in sichuan nd   annhui
        duplicate_cities.add("凤州", "5024");

        // in sichuan nd   annhui
        duplicate_cities.add("桐梓", "1403");
        duplicate_cities.add("桐梓", "1404");

        // one in henan, one in Hunan (not found in sched)
        duplicate_cities.add("新安县", "null");

        // one in  Jilin, one in Shanxi (del)
        duplicate_cities.add("朝阳镇", "7083");
        duplicate_cities.add("朝阳镇", "7084");

        // a Yunan 2059/2058镇江
        duplicate_cities.add("镇江", "2059");

        // 新化  in huanan and liaoning
        duplicate_cities.add("新化", "K606");

        // drop gansu keep hubei
        duplicate_cities.add("红卫", "8773");
        duplicate_cities.add("红卫", "8774");
        // not found
        duplicate_cities.add("武山", "null");
        // a 宣城 in Yuannan
        duplicate_cities.add ("宣城", "1337");
        duplicate_cities.add ("宣城", "1338");

        // 松树  in YuNan and LiaoNing, get rid of YunNan
        duplicate_cities.add ("松树", "8973");
        duplicate_cities.add ("松树", "8974");
        */
    }
}
