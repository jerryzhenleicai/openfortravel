package com.gaocan.train.schedule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.jpenguin.ICityNameSource;


public class ArrayCityNameSource implements ICityNameSource {
    // qingdao is replaced with sifang in sched, 上海Nan first appear in sched, 沈阳北, 金华Xi, 连云港dong

    public final static String[] topCities = { "北京", "上海", "广州", "杭州", "西安", "桂林", "成都", "金华", "武汉", "昆明", "香港", "重庆", "南京",
            "深圳", "青岛", "大连", "天津", "苏州", "宁波","连云港", "三亚", "沈阳", "长沙", "黄山", "拉萨", "厦门", "哈尔滨", "张家界", "济南" };

    Collection<String> names = Arrays.asList(topCities);
    /**
     * if no explicit big cities inited, use own
     *
     */
    public ArrayCityNameSource() {
    }
    
    public ArrayCityNameSource(Collection<String> cnames) {
        names = Collections.unmodifiableCollection(cnames);
    }
    public Collection<String> getCityNames() {
        return names;
    }
}
