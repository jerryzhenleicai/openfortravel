package com.gaocan.train.train;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.gaocan.publictransportation.TripInterval;

/**
 * Read a price file to build a price table
 */
public class TrainPriceImporter {

    /**
     * c 35 10,-1,13,-1 c 40 10,-1,13,-1 c 80 15,-1,22,-1 c 120 19,-1,29,-1 c
     * 160 24,-1,37,-1
     */

    /**
     * key: "c","t","k", "1" ... value: sorted map where key is Integer type:
     * distance (km) and values are TripCost
     */
    Map<String, SortedMap<Integer, TripCost>> typeDistancePriceMap = new HashMap<String, SortedMap<Integer, TripCost>>();

    public TripCost getPrice(TrainLine line, int distance) throws java.lang.IllegalArgumentException {
        Integer dist_integer;
        String type = line.getTypeStr();
        SortedMap<Integer, TripCost> distPriceMap = typeDistancePriceMap.get(type);
        dist_integer = new Integer(distance);
        SortedMap<Integer, TripCost> tail = distPriceMap.tailMap(dist_integer);
        TripCost price;
        if (tail.size() == 0) {
        	price = distPriceMap.get(distPriceMap.lastKey());
        } else {
        	price = tail.get(tail.firstKey());
        }
        return price;
    }

    public TripCost getMoneytaryCost(TripInterval interval) {
        // a in-city station transfer (Beijing East to Beijing West) ?
        if (interval.isIntraAreaTransfer()) {
            return new TripCost(20, 20, 20, 20);
        }

        TrainLine line = (TrainLine) interval.getStartLsp().getLine();

        // get interval distance
        int kms = ((TrainLineStationPair) interval.getEndLsp()).getKmsFromStart()
                - ((TrainLineStationPair) interval.getStartLsp()).getKmsFromStart();

        // If no distance info, then set price according to time
        if (kms == 0) {
            return new TripCost(0, 0, 0, 0);
        } else {
            return getPrice(line, kms);
        }
    }

    public void loadPriceTable(String price_file) throws IOException {
        InputStreamReader reader;
        reader = new InputStreamReader(new FileInputStream(price_file));
        StreamTokenizer tokenizer = new StreamTokenizer(reader);
        tokenizer.eolIsSignificant(true);
        tokenizer.whitespaceChars(',', ',');
        int lineno = 0;
        int t;
        Integer distKm;

        while (true) {
            lineno++;
            t = tokenizer.nextToken();
            if (t == StreamTokenizer.TT_EOF)
                break;
            String type = tokenizer.sval;
            if (t != StreamTokenizer.TT_WORD) {
                if (t == StreamTokenizer.TT_NUMBER && tokenizer.nval == 1) {
                    type =  "1";
                } else {
                    System.err.println(lineno + ": Expect train type in train price table!");
                    break;
                }
            }
            t = tokenizer.nextToken();
            if (t != StreamTokenizer.TT_NUMBER) {
                System.err.println(lineno + ": Expect distance number in train price table!");
                break;
            }
            distKm = new Integer((int) tokenizer.nval);
            // create the entry for this trip dist in the map, if needed
            SortedMap<Integer, TripCost> distPriceMap = typeDistancePriceMap.get(type);
            if (distPriceMap == null) {
                distPriceMap = new TreeMap<Integer, TripCost>();
                this.typeDistancePriceMap.put(type, distPriceMap);
            }
            SortedMap<Integer, TripCost> tail = distPriceMap.tailMap(distKm);
            TripCost price;
            if (tail.isEmpty()) {
                price = new TripCost();
                distPriceMap.put(distKm, price);
            } else {
                throw new RuntimeException("Duplicate entry " + type + ", " + distKm);
            }

            // ----------------------------- pk YZ
            if (tokenizer.nextToken() != StreamTokenizer.TT_NUMBER) {
                System.err.println(lineno + ": Expect  YZ price in train price table!");
                break;
            }
            price.setYzPrice((int) tokenizer.nval);
            if (tokenizer.nextToken() != StreamTokenizer.TT_NUMBER) {
                System.err.println(lineno + ": Expect  RZ price in train price table!");
                break;
            }
            price.setRzPrice((int) tokenizer.nval);

            if (tokenizer.nextToken() != StreamTokenizer.TT_NUMBER) {
                System.err.println(lineno + ": Expect YW price in train price table!");
                break;
            }
            price.setYwPrice((int) tokenizer.nval);
            if (tokenizer.nextToken() != StreamTokenizer.TT_NUMBER) {
                System.err.println(lineno + ": Expect RW price in train price table!");
                break;
            }
            price.setRwPrice((int) tokenizer.nval);

            if (tokenizer.nextToken() != StreamTokenizer.TT_EOL) {
                System.err.println(lineno + ": Expect EOL after kttk rw price in train price table!");
                break;
            }
        }

        reader.close();
    
    }

}
