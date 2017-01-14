package com.gaocan.train.train;

import com.gaocan.publictransportation.InvalidScheduleDataException;
import com.gaocan.publictransportation.Line;

import java.util.Iterator;

public class TrainLine extends Line {
    // for how long train can travel nonstop BETWEEN_STATIONS
    static final int MAX_NONSTOP_HOURS_TEKUAI = 16;

    static final int MAX_NONSTOP_HOURS_KUAI = 15;

    static final int MAX_NONSTOP_HOURS_PUTONG = 14;

    public static final int MAX_MINUTES_STATION_STOP = 90;

    public static final int NUMBER = 4; // 1xxx

    public static final int CHENGJI = 5; // Cxxx

    public static final int KUAI = 6; // Kxxx

    public static final int YOU = 7; // Yxxx

    public static final int TEKUAI = 8; // Txxx

    public static final int ZHIDATEKUAI = 9; // Zxxx

    public static final int DONG_CHE = 10; // Dxxx

    public static final int GAOSU = 11; // Gxxx

    public static final int LAST_TYPE = 11;

    private boolean alternateDay = false;

    // type
    private int typeCode;

    int total_moving_time = -1;

    private int fullLengthKm = -1;

    private String number2;

    private TrainLineNumber lineNumber;

    public boolean isBidirectional() {
        return false;
    }

    private static boolean isValidNumber(String num) {
        // strip the B, C, S at the end
        if (!Character.isDigit(num.charAt(num.length() - 1))) {
            num = num.substring(0, num.length() - 1);
        }
        // and A,B
        if (!Character.isDigit(num.charAt(num.length() - 1))) {
            num = num.substring(0, num.length() - 1);
        }

        // K123, or 1234
        if (Character.isDigit(num.charAt(0))) {
            try {
                Integer.parseInt(num);
            } catch (NumberFormatException fe) {
                return false;
            }
        } else {
            try {
                Integer.parseInt(num.substring(1));
            } catch (NumberFormatException fe) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidFullNumber(String fullNum) {
        String[] nums = fullNum.split("/");
        for (String num : nums) {
            if (!isValidNumber(num)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumberShuYun(String n) {
        return n.endsWith("S");
    }

    public static boolean isNumberChunYun(String n) {
        return n.endsWith("C");
    }

    public static boolean isNumberPaused(String n) {
        return n.endsWith("P");
    }

    /**
     * start running in the future
     * 
     * @param n
     * @return
     */
    public static boolean isNumberFuture(String n) {
        return n.endsWith("F");
    }

    /**
     * a line is restored after it was suspended seasonally
     */
    public void restoreFromNoRunState() {
        assert (isNumberPaused(number));
        number = number.substring(0, number.length() - 1);
        if (number2 != null) {
            number2 = number2.substring(0, number2.length() - 1);
        }
        this.setEndEffectiveDate(null);
    }

    public TrainLine(String n) {
        super();
        n = n.trim();
        if (!isValidFullNumber(n)) {
            throw new InvalidScheduleDataException("invalid train number ", n);
        }
        lineNumber = TrainLineService.getNormalizeNumber(n);
        setNumber(lineNumber.getNumber1());
        setNumber2(lineNumber.getNumber2());
        // set default train type
        setTypeOnNumber();
    }

    /**
     * train is only running for a period such as Chun Jie or Shu Jia
     * 
     * @return
     */
    public boolean isSeasonal() {
        return isNumberSeasonal(number);
    }

    public TrainLine(String num, int type, String intv) {
        this(num);
        typeCode = type;
        setInterval(intv);
    }

    /**
     * Important for Astar
     */
    public int compareTo(Line o) {
        int c = super.compareTo(o);
        if (c != 0) {
            return c;
        } else {
            TrainLine line2 = (TrainLine) o;
            if (number2 != null && line2.number2 == null) {
                return 1;
            } else if (number2 == null && line2.number2 != null) {
                return -1;
            } else if (number2 == null && line2.number2 == null) {
                return 0;
            } else {
                return number2.compareTo(line2.number2);
            }
        }
    }

    /**
     * override parent's so the ordering of two numbers have no effect on Lsp
     * node's order in graph (critical for A star results matching up)
     */
    public int hashCode() {
        if (number2 == null) {
            return getNumber().hashCode();
        }
        return getNumber().hashCode() + getNumber2().hashCode();
    }

    /**
     * a line may have to be represented as a string (such as in the precomputed
     * results), this returns whether given a string the line is the one it
     * represents. Line 1234/1235 is considered to be NOT matching both "1234"
     * and "1235"
     * 
     * @param key
     * @return
     */
    public boolean matchesKey(String key) {
        return this.matchNumber(key);
    }

    /**
     * a line can be have different numbers ... in different schedules (e.g.
     * some always use one number for a train)
     * 
     * @return true if two lines are same numbers
     */
    public boolean isEquivalent(Line line2) {
        boolean sameLine = TrainLineService.isTwoNumbersSameLine(this.lineNumber, ((TrainLine) line2).lineNumber);
        if (sameLine) {
            // we allow two version of same line if their effective dates are
            // different
            if (this.hasEffectiveDates() && line2.hasEffectiveDates()) {
                return false;
            }
            // or they run on alternate days (T20A and T20B)
            if (this.isAlternateDay() && ((TrainLine) line2).isAlternateDay())
                return !this.lineNumber.equals(((TrainLine)line2).lineNumber);
        }
        return sameLine;
    }

    /**
     * @return false if train line
     */
    public boolean isNumberInvalid() {
        boolean valid = false;

        // make sure no K1111 or 456 are there
        if (this.isNumberStartingWithLetter()) {
            // make sure len is <=4 chars (Kxxx)
            valid = (getNumber().length() <= 4) && ((getNumber2() == null) || (getNumber2().length() <= 4));
        } else {
            valid = (getNumber().length() == 4) && ((getNumber2() == null) || (getNumber2().length() == 4));
        }

        return !valid;
    }

    public static boolean isNumberSeasonal(String num) {
        return isNumberChunYun(num) || isNumberShuYun(num) || isNumberPaused(num) || isNumberFuture(num);
    }

    public boolean matchNumber(String number2) {
        return matchNumber(TrainLineService.getNormalizeNumber(number2));
    }

    /**
     * in DB train # can be T297/T298 or T297/298 User may input T298/T297,
     * T298/297, T297/298, all these combinations are considered matched, but
     * not for T297 or T298 those match T298B or T297C.
     * 
     * @param number2
     *            full number that can identify the line
     * @return true if matched, a seasonal line will match its core number
     */
    public boolean matchNumber(TrainLineNumber number2) {
        // need to normalize both DB line and input number to number1/number2
        // then compare number1/number1 , number2 /nu
        return TrainLineService.isTwoNumbersSameLine(lineNumber, number2);
    }

    /**
     * in DB train # can be T297/T298 or T297/298 User may input T297, T298,
     * T298/T297, T298/297, T297/298, all these combinations are considered
     * matched
     * 
     * @param number2
     *            full or partial number that can identify the line
     * @return true if matched, a seasonal line will match its core number
     */
    public boolean fuzzyMatchNumber(String number2) {
        // need to normalize both DB line and input number to number1/number2
        // then compare number1/number1 , number2 /nu
        return TrainLineService.numberPartialMatch(lineNumber, TrainLineService.getNormalizeNumber(number2));
    }

    /**
     * Getter for property typeCode.
     * 
     * @return Value of property typeCode.
     * 
     */
    public int getTypeCode() {
        return typeCode;
    }
    
    public String getTypeStr() {
        switch (typeCode) {
        case TrainLine.KUAI:
            return "k";

        case TrainLine.NUMBER:
            return "1";

        case TrainLine.CHENGJI:
            return "c";

        case TrainLine.TEKUAI:
            return "t";

        case TrainLine.DONG_CHE:
            return "d";

        case TrainLine.ZHIDATEKUAI:
            return "z";

        case TrainLine.GAOSU:
            return "g";
        }

        return null;
    }

    /**
     * Setter for property typeCode.
     * 
     * @param typeCode
     *            New value of property typeCode.
     * 
     */
    public void setTypeCode(int typeCode) {
        this.typeCode = typeCode;
    }

    /**
     * Get the full name/number. No two train lines can have this method
     * returning same value
     */
    public String getFullNumber() {
        if (getNumber2() != null) {
            return (getNumber() + '/' + getNumber2());
        } else {
            return new String(getNumber());
        }
    }

    /**
     * remove seasonal train's single character suffix such as C in xxxC, S in
     * xxxS and return the core number
     * 
     * @return
     */
    public String getCoreNumber() {
        return TrainLineService.toCoreNumber(number);
    }

    /**
     * Get the full core name/number. It's possible two train lines can have
     * this method returning same value
     */
    public String getFullCoreNumber() {
        if (getNumber2() != null) {
            return (getCoreNumber() + '/' + TrainLineService.toCoreNumber(getNumber2()));
        } else {
            return getCoreNumber();
        }
    }

    /**
     * full number shown to user
     * 
     * @return
     */
    public String getNumberForDisplay() {
        String s = getFullNumber();
        if (isSeasonal()) {
            if (isChunYun()) {
                s = this.getFullCoreNumber() + "(春运)";
            } else if (isShuYun()) {
                s = this.getFullCoreNumber() + "(暑运)";
            } else if (isPaused()) {
                // annotate at UI layer (暂时停开);
                s = this.getFullCoreNumber();
            } else if (isFuture()) {
                s = this.getFullCoreNumber() + "(未来)";
            }
        }
        return s;
    }

    /**
     * used for writing to txt file, must match parser in TrainLineFactory
     */
    public String toString() {
        String s = this.getFullNumber().concat("|").concat(getInterval()) + "," + this.getTypeString() + ":"
                + this.fullLengthKm + "公里" + (!this.hasEffectiveDates() ? ";" : ';' + effectiveDatesToStr());

        // remove last ; if no comment
        if (s.endsWith(";")) {
            s = s.substring(0, s.length() - 1);
        }
        if (this.isAlternateDay())
            s += "[隔日开行]";

        return s;
    }

    public String getTypeString() {
        return getTypeStringForCode(typeCode);
    }

    static public String getTypeStringForCode(int code) {
        switch (code) {
        case TrainLine.KUAI:
            return "快速";

        case TrainLine.NUMBER:
            return "普通";

        case TrainLine.CHENGJI:
            return "城际";

        case TrainLine.TEKUAI:
            return "特快";

        case TrainLine.ZHIDATEKUAI:
            return "直达特快";

        case TrainLine.DONG_CHE:
            return "动车组";

        case TrainLine.GAOSU:
            return "高速列车";
        }

        return null;
    }

    /**
     * based on train number guess its type
     * 
     */
    private void setTypeOnNumber() {
        setTypeCode(guessTypeCodeFromNumber(number));
        // xxxA means alternate running
        char lastChar = number.charAt(number.length() - 1);
        if (lastChar == 'A' || lastChar == 'B') {
            this.setAlternateDay(true);
        }

    }

    public static int guessTypeCodeFromNumber(String number) {
        try {
            // numberic 1xxx-9xxxx
            int num = Integer.parseInt(number);
            return TrainLine.NUMBER;
        } catch (NumberFormatException e) {
            if (number.charAt(0) == 'T') {
                return TrainLine.TEKUAI;
            } else if (number.charAt(0) == 'Q') {
                return TrainLine.TEKUAI;
            } else if (number.charAt(0) == 'L') {
                return TrainLine.NUMBER;
            } else if (number.charAt(0) == 'D') {
                return TrainLine.DONG_CHE;
            } else if (number.charAt(0) == 'G') {
                return TrainLine.GAOSU;
            } else if (number.charAt(0) == 'Z') {
                return TrainLine.ZHIDATEKUAI;
            } else if (number.charAt(0) == 'K') {
                return TrainLine.KUAI;
            } else if (number.charAt(0) == 'S') {
                return TrainLine.KUAI;
            } else if (number.charAt(0) == 'C') {
                return TrainLine.CHENGJI;
            } else if (number.charAt(0) == 'Q') {
                return TrainLine.TEKUAI;
            } else if (number.charAt(0) == 'Y') {
                return TrainLine.NUMBER;
            } else if (number.charAt(0) == 'W') {
                return TrainLine.NUMBER;
            } else {
                throw new RuntimeException("Invalid train num " + number);
            }
        }
    }

    public void setFullLengthKm(int fullLengthKm) {
        this.fullLengthKm = fullLengthKm;
    }

    public int getFullLengthKm() {
        return fullLengthKm;
    }

    public void setNumber2(String number2) {
        this.number2 = number2;
    }

    public String getNumber2() {
        return number2;
    }

    /**
     *
     */
    public void validateData() {
        // check speed
        Iterator it = getLspIterator();
        TrainLineStationPair prevLsp = null;

        while (it.hasNext()) {
            TrainLineStationPair lsp = (TrainLineStationPair) it.next();
            if (prevLsp != null) {
                if (!prevLsp.getDepartureTime().isEarlierThan(lsp.getArrivalTime())) {
                    throw new IllegalStateException("This station's arrival time: " + lsp.toString()
                            + " is earlier than previous stations's departure time: " + prevLsp.toString());
                }
                if (lsp.getArrivalTime().minutesEarlierThan(lsp.getDepartureTime()) > 120) {
                    throw new IllegalStateException("Arrival time " + lsp.getArrivalTime()
                            + "  more than 2 hours early than depart: " + lsp.getDepartureTime().toString()
                            + " at LSP :" + lsp.toString());
                }
                int kmsTraveled = lsp.getKmsFromStart() - prevLsp.getKmsFromStart();
                if (kmsTraveled < 0) {
                    throw new IllegalStateException("This station's distance (KM): " + lsp.toString()
                            + " is less than previous stations's distance: " + prevLsp.toString());
                }
            }
            prevLsp = lsp;
        }
    }

    public boolean isAlternateDay() {
        return alternateDay;
    }

    public void setAlternateDay(boolean alternateDay) {
        this.alternateDay = alternateDay;
    }

    public boolean isChunYun() {
        return isNumberChunYun(number);
    }

    public boolean isShuYun() {
        return isNumberShuYun(number);
    }

    public boolean isPaused() {
        return isNumberPaused(number);
    }

    public boolean isFuture() {
        return isNumberFuture(number);
    }

}
