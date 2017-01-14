package com.gaocan.train.schedule;

import java.util.Iterator;
import java.util.LinkedList;

public class LineNameFactory {

	public static Iterator<String> getPossibleLineNames() {
		LinkedList<String> lineNames = new LinkedList<String>();

		// type: 0 - Txxx, 1 - Kxxx, 2: Zxxx, 3:Nxx, 4 - normal (1001-8998)
		for (int type = 0; type <= 4; type++) {
			String letter = "";
			int numberMax = -1; 
			int numberMin = -1;
			if (type == 0) {
				letter = "";
				numberMin = 1001;
				numberMax = 8998;
			}
			else if (type == 1) {
				letter = "K";
				numberMin = 1;
				numberMax = 999;
			} else if (type == 2) {
				letter = "N";
				numberMin = 1;
				numberMax = 999;
			} else if (type == 3) {
				letter = "T";
				numberMin = 1;
				numberMax = 999;
			} else if (type == 4) {
				letter = "Z";
				numberMin = 1;
				numberMax = 99;
			} 

			for (int number = numberMin ; number <= numberMax; number++) {
				lineNames.add(letter + number);
			}
		}
		return lineNames.iterator();
	}
}
