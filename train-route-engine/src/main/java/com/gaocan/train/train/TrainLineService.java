package com.gaocan.train.train;

public class TrainLineService {

    /**
     * turn into a standard format
     * 
     * @param lineNum
     * @return
     */
    public static TrainLineNumber getNormalizeNumber(String lineNum) {
        TrainLineNumber result = new TrainLineNumber();
        int slash_pos;
        lineNum = lineNum.trim().toUpperCase();
        // xxA remove A
        if (lineNum.endsWith("A") || lineNum.endsWith("B")) {
        	lineNum = lineNum.substring(0, lineNum.length() - 1);
        }
        slash_pos = lineNum.indexOf('/');
        // /K343 ? turn to K343
        if (slash_pos == 0) {
            lineNum = lineNum.substring(1);
            slash_pos = lineNum.indexOf('/');
        }
        if (slash_pos != -1) {
            int order;
            result.setNumber1(lineNum.substring(0, slash_pos));
            // in case of 382/379/375 find second slash
            result.setNumber2(lineNum.substring(slash_pos + 1));
            slash_pos = result.getNumber2().indexOf('/');
            if (slash_pos != -1) {
                result.setNumber2(result.getNumber2().substring(0, slash_pos));
            }

            // if K533/532 make it K532/K533
            if (result.getNumber2().length() > 0 && !Character.isDigit(result.getNumber1().charAt(0))
                    && Character.isDigit(result.getNumber2().charAt(0))) {
                result.setNumber2(result.getNumber1().charAt(0) + result.getNumber2());
            }
            // make sure number is smaller than number2
            // this is to help detect redundant trainline files that differ only
            // on order of two numbers like 814/811 and 811/814
            order = result.getNumber1().compareTo(result.getNumber2());

            if (order == 0) { // two numbers same ,merge them
                result.setNumber2(null);
            } else if (order > 0) {
                String temp = result.getNumber2();
                result.setNumber2(result.getNumber1());
                result.setNumber1(temp);
            }
        } else {
            result.setNumber1(new String(lineNum));
            result.setNumber2(null);
        }
        return result;
    }

    public static boolean numberPartialMatch(String number1, String number2) {
        return numberPartialMatch(getNormalizeNumber(number1), getNormalizeNumber(number2)) ;
    }
    
    /**
     * EXample, T298/T297 will match T297/T298 and T298 
     * @param number1
     * @param number2
     * @return
     */
    public static boolean numberPartialMatch(TrainLineNumber number1, TrainLineNumber number2) {
        String coreNum11 = toCoreNumber(number1.getNumber1());
        String coreNum12 = null;
        if (number1.getNumber2() != null) {
            coreNum12 = toCoreNumber(number1.getNumber2());
        }

        String coreNum21 = toCoreNumber(number2.getNumber1());
        String coreNum22 = null;
        if (number2.getNumber2() != null) {
            coreNum22 = toCoreNumber(number2.getNumber2());
        }


        if (coreNum11.equalsIgnoreCase(coreNum21) || coreNum11.equalsIgnoreCase(coreNum22)) {
            return true;
        }

        // check second if any
        if (coreNum12 != null) {
            return coreNum12.equalsIgnoreCase(coreNum21) || coreNum12.equalsIgnoreCase(coreNum22);
        }
        return false;        
    }
    
    /**
     * EXample, T298/T297 will match T297/T298 but not T298 
     * @param number1
     * @param number2
     * @return
     */
    public static boolean isTwoNumbersSameLine(TrainLineNumber number1, TrainLineNumber number2) {
        String coreNum11 = toCoreNumber(number1.getNumber1());
        String coreNum12 = null;
        if (number1.getNumber2() != null) {
            coreNum12 = toCoreNumber(number1.getNumber2());
        }

        String coreNum21 = toCoreNumber(number2.getNumber1());
        String coreNum22 = null;
        if (number2.getNumber2() != null) {
            coreNum22 = toCoreNumber(number2.getNumber2());
        }

        // check second if any
        if (coreNum12 != null) {
            return coreNum11.equalsIgnoreCase(coreNum21) && coreNum12.equalsIgnoreCase(coreNum22);
        } else {
            return coreNum22 == null && coreNum11.equalsIgnoreCase(coreNum21);
        }

    }

    /**
     * return true if two numbers represent the same train line
     * 
     * @param num1
     *            full number
     * @param num2
     * @return
     */
    public static boolean isTwoNumbersSameLine(String num1, String num2) {
        if (num1.equalsIgnoreCase(num2)) {
            return true;
        }
        
        TrainLineNumber number1 = TrainLineService.getNormalizeNumber(num1.trim());
        // need to normalize both DB line and input number to number1/number2
        // then compare number1/number1 , number2 /nu
        TrainLineNumber number2 = TrainLineService.getNormalizeNumber(num2.trim());
        return isTwoNumbersSameLine(number1, number2);
    }

    /**
     * remove seasonal train's single character suffix such as C in xxxC, S in
     * xxxS and return the core number
     * 
     * @return
     */
    public static String toCoreNumber(String number) {
        // for T25C T25A T25B etc
        if (TrainLine.isNumberSeasonal(number) || Character.isLetter(number.charAt(number.length() - 1))) {
            return number.substring(0, number.length() - 1);
        } else {
            return number;
        }

    }

}
