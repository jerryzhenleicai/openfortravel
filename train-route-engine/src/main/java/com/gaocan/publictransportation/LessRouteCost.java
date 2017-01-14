package com.gaocan.publictransportation;

import java.util.Comparator;

import org.jpenguin.graph.Path;


public class LessRouteCost implements Comparator<Path>
{
   static private LessRouteCost instance = new LessRouteCost ();
   public static LessRouteCost getSingleInstance() {
       return instance;
   }
   public int compare(Path path1, Path path2)   {
      return  (int) (path1.getCost() - path2.getCost());
   }

}



