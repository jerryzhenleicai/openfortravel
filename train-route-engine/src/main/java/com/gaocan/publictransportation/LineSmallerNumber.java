package com.gaocan.publictransportation;

import java.util.Comparator;

public class LineSmallerNumber implements  Comparator
{
	static private LineSmallerNumber instance =  new LineSmallerNumber ();
	public static LineSmallerNumber getSingleInstance() {
		return instance;
	}
	
	public int compare(Object p1, Object p2)
   {
      Line Line1 = (Line) p1;
      Line Line2 = (Line) p2;

      return Line1.getNumber().compareTo(Line2.getNumber()) ;
   }

}



