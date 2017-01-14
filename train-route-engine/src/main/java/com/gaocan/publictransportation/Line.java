package com.gaocan.publictransportation;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * A scheduled vechile running a certain route, like bus lines, train lines, flights.
 */
@SuppressWarnings("unchecked")
public abstract class Line implements Comparable<Line> {
    static int base_id = 0;
    static String idGeneratorMutex = "lock";
    protected transient ArrayList<LineStationPair> lineStationPairs;
    String interval;
    private transient int id;
    protected String number;
    private String comment;
    private Object auxInfo;
    private Date beginEffectiveDate;
    private Date endEffectiveDate;
    public static final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy/MM/dd");

    protected Line() {
        synchronized (idGeneratorMutex) {
            setId(base_id++);
        }
        lineStationPairs = new ArrayList<LineStationPair>();
        setInterval(null);
    }

    public String getEncryptedIdStr() {
    	return "" + getId();
    }
    
    /**
     * override this for cases where one line is known via several numbers 
     * @param num
     * @return true if this line considered equivalent to a line with the given number
     */
    public boolean matchNumber(String num) {
    	return number.equalsIgnoreCase(num);
    }
    
    /**
     * override this for cases where one line is known via several numbers 
     * @param num
     * @return true if this line considered equivalent to a line with the given number
     */
    public boolean fuzzyMatchNumber(String number2) {
        return number.equalsIgnoreCase(number2);
    }

    /**
     * If true then the line can be traveled in either direction, that is if A and B  
     * are adjacent stations then you can go A to B or B to A, such as a bus line
     * @return
     */
    abstract public boolean isBidirectional() ;
    
    public String getInterval() {
        if (interval == null || interval.equals("")) {
            interval = this.getBeginStation().getName() + "-" + this.getEndStation().getName();
        }
        return interval;
    }

    protected int nubmerCompareTo(String other) {
        return number.compareTo(other);
    }
    
    /**
     * Ordering based on both number and interval
     */
    public int compareTo(Line line2) {
        int r = nubmerCompareTo(line2.getNumber());
        if (r == 0) {
            r = getInterval().compareTo(line2.getInterval());
        } if (r == 0) {
        	// see which has more stations平顶山dfdP
        	int rn = this.getNumPassingStations() - (line2).getNumPassingStations();
        	if (rn != 0)
        		return rn;
        	// a loop line? or other weird use first second station
        	if (lineStationPairs.size() < 2) {
        		String msg = "Line " + this.toString() + " has only one LSP ";
        		throw new IllegalStateException(msg);
        	}
        	if (line2.lineStationPairs.size() < 2) {
        		String msg = "Line " + line2.toString() + " has only one LSP ";
        		throw new IllegalStateException(msg);
        	}
        	// compare 
        	int n = 1;
        	while (n < getNumPassingStations()) {
	        	String s1 = lineStationPairs.get(n).getStation().getName();
	        	String s2 = line2.getLineStationPairs().get(n).getStation().getName();
	        	r =  s1.compareTo(s2);
	        	if (r != 0)
	        		return r;
	        	n++;
        	}
        	return 0; 
        }
        return r;
    }

    /**
     * a line may have to be represented as a string (such as in the precomputed results),
     * this returns whether given a string the line is the one it represents. Overriden by
     *  train line.
     * @param key
     * @return
     */
    public boolean matchesKey(String key) {
        return number.equals(key);
    }

	/**
	 * two lines from two sched may have diff. number but are the same line
	 * @param line2
	 * @return
	 */
    public abstract boolean isEquivalent(Line line2);

    /**
     * for merging sched, if two equivalent lines have totally different data, means one has incorrect sched data
     * @return
     */
    public boolean isVastlyDifferent(Line line2) {
        if (!this.isEquivalent(line2)) {
            throw new IllegalArgumentException("Two lines must be equivalent to be tested for difference");
        }

        // the two lines must have same end stations
        if (!this.getEndStation().equals(line2.getEndStation())) {
        	System.out.println("line, vast diff due to end station diff");
            return true;
        }
        if (!this.getBeginStation().equals(line2.getBeginStation())) {
			System.out.println("line, vast diff due to beg station diff");
            return true;
        }

        // and similar # of LSPs, disabled this because sometimes a line no longer 
        // passes a lot of stations when its speed is increased
        /*
        int lspNumDiff = this.getNumPassingStations() - line2.getNumPassingStations();
        if (Math.abs(lspNumDiff) > (getNumPassingStations() / 6)) {
			System.out.println("line, vast diff due to  LSPs, lsp1: " + getNumPassingStations() + 
			 " lsp2:" + line2.getNumPassingStations() );
            return true;
        }*/
        return false;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int hashCode() {
        return getNumber().hashCode();
    }

    /**
     *  return true if st1 appears after st2 on this line
     */
    public boolean isStationAfter(Station st1, Station st2) {
        boolean seen_st2 = false;
        Station station;
        if (st1 == st2) {
            return false;
        }
        for (int i = 0; i < lineStationPairs.size(); i++) {
            station = (lineStationPairs.get(i)).getStation();
            if (station == st2) {
                seen_st2 = true;
            } else if ((station == st1) && seen_st2) {
                return true;
            }
        }
        return false;
    }

    /**
     *  return true if st2 appears right after st1 on this line
     */
    public boolean areConsecutiveStations(Station st1, Station st2) {
        Station station;
        for (int i = 0; i < (lineStationPairs.size() - 1); i++) {
            station = (lineStationPairs.get(i)).getStation();
            if (station == st1) {
                // check next station, note we do not return false at first mismatch because 
                // a station could errorneously appear twice in a line 
                if (st2 == (lineStationPairs.get(i + 1)).getStation()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * get LSP enum
     */
    public Iterator<LineStationPair>  getLspIterator() {
        return lineStationPairs.iterator();
    }

    public void printStations() {
        for (int i = 0; i < lineStationPairs.size(); i++) {
            System.out.println(lineStationPairs.get(i));
        }
    }

    /**
     * can be overriden by subclass to check for invalid sched data
     * @param lsp
     * @throws InvalidScheduleDataException
     */
    public void addLineStationPair(LineStationPair lsp)
        throws InvalidScheduleDataException {
        lineStationPairs.add(lsp);
    }

    public int getNumPassingStations() {
        return lineStationPairs.size();
    }

    public LineStationPair getLspAtStation(String stationName) {
        LineStationPair lsp;
        for (int i = 0; i < lineStationPairs.size(); i++) {
            lsp = lineStationPairs.get(i);
            if (lsp.getStation().getName().equals(stationName)) {
                return lsp;
            }
        }
        return null;
    }

    public boolean equals(Object l) {
        // same number !
    	if (l.getClass() != getClass()) {
    		throw new IllegalStateException("different types of lines cannot be compared");
    	}
        return getNumber().equals(((Line) l).number) && this.interval.equals(((Line) l).interval);
    }

    public boolean isNumberStartingWithLetter() {
        return Character.isLetter(getNumber().charAt(0));
    }

    public void setInterval() {
        interval = this.getBeginStation() + "-" + this.getEndStation();
    }

    /**
     * Method declaration
     *
     *
     * @param intv
     *
     * @see
     */
    public void setInterval(String intv) {
        interval = intv;
    }

    /** Getter for property comment.
     * @return Value of property comment.
     *
     */
    public java.lang.String getComment() {
        return comment;
    }

    /** Setter for property comment.
     * @param comment New value of property comment.
     *
     */
    public void setComment(java.lang.String comment) {
        this.comment = comment;
    }

    /** Getter for property auxInfo.
     * @return Value of property auxInfo.
     *
     */
    public java.lang.Object getAuxInfo() {
        return auxInfo;
    }

    /** Setter for property auxInfo.
     * @param auxInfo New value of property auxInfo.
     *
     */
    public void setAuxInfo(java.lang.Object auxInfo) {
        this.auxInfo = auxInfo;
    }

    public Station getBeginStation() {
        return (lineStationPairs.get(0)).getStation();
    }

    public Station getEndStation() {
        return (lineStationPairs.get(lineStationPairs.size() - 1)).getStation();
    }

    public LineStationPair getEndLsp() {
        return lineStationPairs.get(lineStationPairs.size() - 1);
    }

    public void setNumber(String number) {
        this.number = number;
    }

    /**
     * Identifying primary number like T560
     * @return
     */
    public String getNumber() {
        return number;
    }
    
    /**
     * Fully qualified number like T560/T561 for trains
     * @return
     */
    public abstract String getFullNumber();

    /**
     * Return the station index of the lsp
     * @param lsp
     * @return -1 if not found
     */
	public int getLspIndex(LineStationPair lsp) {
		for (int i = 0 ; i < lineStationPairs.size() ;i++) {
			if (lineStationPairs.get(i) == lsp) {
				return i;
			}
		}	
		return -1;
	}
    
    /**
     * Return the station index of the lsp
     * @param lsp
     * @return -1 if not found
     */

    public int getEndLspIndex(LineStationPair lsp) {
        for (int i = lineStationPairs.size() - 1; i >= 0; i--) {
            if (lineStationPairs.get(i) == lsp) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Return the station index of the lsp
     * @param lsp
     * @return -1 if not found
     */

	public int getStationIndex(String stationName) {
		for (int i = 0 ; i < lineStationPairs.size() ;i++) {
			if (lineStationPairs.get(i).getStation().getName().equals(stationName) ) {
				return i;
			}
		}	
		return -1;
	}

	public List<LineStationPair> getLineStationPairs() {
		return Collections.unmodifiableList(lineStationPairs);
	}
	
	public void printOut(PrintWriter pw) {
        pw.println(toString());
        // comment must not be blank to write
        if (!comment.isEmpty()) {
            pw.print("#");
            pw.println(comment);
        }
        
        Iterator lspIt = getLspIterator();

        // for each LSP write station name and then aux info
        while (lspIt.hasNext()) {
            LineStationPair lsp = (LineStationPair) lspIt.next();
            pw.print(lsp.getStation().getName());
            String ext = lsp.getExtraDataString();
            if (!ext.isEmpty()) {
            	pw.print('|' + ext);
            }
            pw.println();
        }
        pw.println("eol");
	}
	public Date getBeginEffectiveDate() {
		return beginEffectiveDate;
	}

	public void setBeginEffectiveDate(Date beginEffectiveDate) {
		this.beginEffectiveDate = beginEffectiveDate;
	}

	public Date getEndEffectiveDate() {
		return endEffectiveDate;
	}

	public void setEndEffectiveDate(Date endEffectiveDate) {
		this.endEffectiveDate = endEffectiveDate;
	}

    public boolean hasEffectiveDates() {
       	return !(beginEffectiveDate == null && endEffectiveDate == null);
    }
    /**
     * used for writing to txt file
     */
    protected String effectiveDatesToStr() {
    	if (beginEffectiveDate == null && endEffectiveDate == null) {
    		return "";
    	}
    	if (beginEffectiveDate == null) {
    		return "-" + dateFmt.format(endEffectiveDate);
    	}
    	if (endEffectiveDate == null) {
    		return dateFmt.format(beginEffectiveDate) + "-";
    	} 
		return dateFmt.format(beginEffectiveDate) + "-" + dateFmt.format(endEffectiveDate) ;
    }

    
    public void parseEffectiveDates(String str) {
		String[] ds = str.split("-");
		try {
			if (ds.length == 1) {
				if (str.charAt(0) != '-')  {
					this.setBeginEffectiveDate(dateFmt.parse(ds[0]));
				} else {
					throw new IllegalStateException("invalid dates " + str);
				}
			}  else if (ds.length == 2) {
				if (!ds[0].isEmpty()) {
					this.setBeginEffectiveDate(dateFmt.parse(ds[0]));
				}
				this.setEndEffectiveDate(dateFmt.parse(ds[1]));
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
    }
    
    public LineStationPair getLsp(int i) {
    	return lineStationPairs.get(i);
    }
    /**
     * 
     * @param line2
     * @return the differences from this line to line2
     */
    public Collection<LineDiff> getDifferencesWith(Line line2) {
    	List<LineDiff> results = new ArrayList<LineDiff>();
        // scan both lines (move p1 and p2) until we find a common station
    	
       	int p1 = 0;
       	int p2 = 0;
        while (p1 < this.getNumPassingStations() && p2 < line2.getNumPassingStations()) {
            LineStationPair lsp1 = this.getLsp(p1); 
           	LineStationPair lsp2 = line2.getLsp(p2);
        	String s1Name =lsp1.getStation().getName();
        	// advance both p1 and p2 as long as *p1 and *p2 are the same station,
        	if (s1Name.equals(lsp2.getStation().getName())) {
        		// see if even though the stations are the same, its properties has changed
        		if (!lsp1.getExtraDataString().equals(lsp2.getExtraDataString())) {
        			results.add(new StationPropertiesChanged(s1Name, lsp1.getExtraDataString() , lsp2.getExtraDataString()));
        		}
        		p1 ++;
        		p2 ++;
        		continue;
        	} 
            // if mismatch between p1_a and p2_b, search p2 forward until *p2_c ==*p1_a,
        	boolean foundS1InL2 = false;
        	int p2p = p2 + 1;
            while (p2p < line2.getNumPassingStations()) {
               	lsp2 = line2.getLsp(p2p);
            	if (s1Name.equals(lsp2.getStation().getName())) {
            		foundS1InL2 = true;
            		break;
            	}
            	p2p ++;
        	}
        	// (1) if not found
            // then means p1_a is deleted in line2, so add L1 deletions while advancing p1 and repeat the steps.
        	if (!foundS1InL2) {
        		results.add(new DeletedStation(s1Name));
                p1 ++;
                continue;
        	} else {
        		// (2) if found then  line2 inserted the stations from p2_b until just before p2_c
        		for (int j = p2 ; j < p2p; j++) {
            		results.add(new InsertedStation(line2.getLsp(j).getStation().getName()));
        		}
        		// move up p2 so it's same station as p1
        		p2 = p2p;
        	}
        }
        if (p1 == this.getNumPassingStations() && p2 < line2.getNumPassingStations()) {
        	// means l2 has more stations at end
    		for (int j = p2 ; j < line2.getNumPassingStations(); j++) {
        		results.add(new InsertedStation(line2.getLsp(j).getStation().getName()));
    		}
       } else if (p1 < this.getNumPassingStations() && p2 == line2.getNumPassingStations()) {
    	   // means l1 has more stations at end
    	   for (int j = p1 ; j < getNumPassingStations(); j++) {
    		  results.add(new DeletedStation(getLsp(j).getStation().getName()));
       	   }
       }
     	return results; 
    }
    
    public abstract void validateData() ;
}
