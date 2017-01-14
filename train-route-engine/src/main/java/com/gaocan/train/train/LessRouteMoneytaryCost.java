/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.gaocan.train.train;

import java.util.Comparator;

import org.jpenguin.graph.Path;


/**
 * Class declaration
 *
 *
 * @author
 * @version %I%, %G%
 */
public class LessRouteMoneytaryCost implements Comparator <Path>{
   private TrainPriceImporter train_price_table;
   public LessRouteMoneytaryCost (TrainPriceImporter priceTable) {
     train_price_table = priceTable;  
   }
   
   public int compare(Path path1, Path path2)   {
      TrainTrip  trip_path1 = new TrainTrip(path1);
      TrainTrip  trip_path2 = new TrainTrip(path2);
      int       price1 = trip_path1.getMoneytaryCost(train_price_table).getYzPrice();
      int       price2 = trip_path2.getMoneytaryCost(train_price_table).getYzPrice();

      return price1 - price2;
   }

}



