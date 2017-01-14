/*
 * Created on Oct 13, 2003
 * Copyright (c) Gaocan Inc., Oak Hill, VA, USA, All rights reserverd.
 */
package com.gaocan.train.train;

import com.gaocan.publictransportation.Line;
import com.gaocan.publictransportation.TripInterval;
import com.gaocan.publictransportation.TripPath;

/**
 * @author zcai
 */
public class TrainTrip extends TripPath {

  private static final long serialVersionUID = 1216077319399494426L;

  public TrainTrip(org.jpenguin.graph.Path path) {
    super(path);
  }

  public int getMoneytaryCostInCents() {
    return 0;
  }

  public TripCost getMoneytaryCost(TrainPriceImporter train_price_importer) {
    TripInterval interval;
    int kms;
    Line line;
    TripCost cost = new TripCost();
    TripCost intv_cost;

    for (int i = 0; i < intervals.size(); i++) {
      interval = (TripInterval) intervals.get(i);
      intv_cost = train_price_importer.getMoneytaryCost(interval);
      cost.setYzPrice(cost.getYzPrice() + intv_cost.getYzPrice());
      cost.setRzPrice(cost.getRzPrice() + intv_cost.getRzPrice());
      cost.setYwPrice(cost.getYwPrice() + intv_cost.getYwPrice());
      cost.setRwPrice(cost.getRwPrice() + intv_cost.getRwPrice());
    }

    return cost;
  }

}

