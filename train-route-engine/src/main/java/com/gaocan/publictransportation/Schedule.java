/*
 * Created on Apr 30, 2003
 *
 */
package com.gaocan.publictransportation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.jpenguin.util.StringFuncs;


/**
 * A schedule has a name and comprises of a set of stations and lines and a set
 * of general areas where each area has a number of stations associated with it.
 * 
 * @author zcai
 */
public abstract class Schedule {
    private AreaServedByStations[] areasArray = null;

    private int nextLineId = 1;

    /** Ex, Aug2003 */
    private String name;

    protected String lastChangeDate = "";

    public String getLastChangeDate() {
        return lastChangeDate;
    }

    public void setLastChangeDate(String lastChangeDate) {
        this.lastChangeDate = lastChangeDate;
    }

    /** mapping from station name to Station */
    private HashMap<String, Station> stationNameObjMap = new HashMap<String, Station>();

    /** mapping from general areas' name to objs */
    private TreeMap<String, AreaServedByStations> generalAreaNameObjMap = new TreeMap<String, AreaServedByStations>();

    /** mapping from line ID to line obj */
    private HashMap<Integer, Line> lineIdObjMap = new HashMap<Integer, Line>();

    /** file from which sched data is read */
    private String textFilePath;

    private boolean isUsedLive = true;

    protected String cvsVision = "$Revision: 1.37 $";

    /**
     * Create a schedule with no station or line in it
     * 
     * @param name
     *            name name , such as Sept2002
     */
    protected Schedule(String _name) {
        name = _name;
    }

    public abstract void writeHeader(PrintWriter pw) throws IOException;

    public abstract void readHeader(StreamTokenizer tokenStream) throws IOException;

    /**
     * modify this through the corrections
     * 
     * @param corrs
     * @return list of errors found
     */
    public List<String> applyCorrections(Collection<ScheduleCorrection> corrs) {
        if (this.isUsedLive()) {
            throw new IllegalStateException("This schedule is used live, not a copy for edit");
        }
        List<String> errs = new ArrayList<String>();
        for (ScheduleCorrection corr : corrs) {
            String err = corr.applyToSched(this);
            if (err != null) {
                errs.add(err);
            }
        }
        return errs;
    }

    /**
     * stations of all schedules created are shared and managed by schedule
     * factory
     * 
     * @return
     */
    public Station getExistingStation(String name) {
        // make name has no '|' reserver char
        if (name.indexOf('|') != -1) {
            name = name.replaceAll("|", "");
        }
        Station st = (Station) stationNameObjMap.get(name);
        return st;
    }

    /**
     * Add a new line to this schedule, the line may be partially constructed
     * 
     * @param line
     * 
     * @throws DuplicateLineException
     *             DOCUMENT ME!
     */
    public void addLine(Line line) {
        // make sure line ID unique
        Integer idKey = nextLineId++;
        line.setId(idKey);

        // do not allow insertion of any line that's equivalent to an existing
        // line
        Line existing = getLineEquivalentTo(line);
        if (existing != null) {
            throw new RuntimeException("Existing line " + existing.toString() + ", conflict with inserted  "
                    + line.toString());
        }
        lineIdObjMap.put(idKey, line);
    }

    public void updateLine(Line newLine) {
        Integer idKey = new Integer(newLine.getId());

        if (!lineIdObjMap.containsKey(idKey)) {
            throw new RuntimeException("Update line: the line " + newLine.getNumber() + " not exist");
        }

        lineIdObjMap.put(idKey, newLine);
    }

    /**
     * Add a new station to this schedule, check existing areas to identify if
     * the new station actually belongs to any of them, if not add a new area
     * with the same name of the station
     * 
     * @param station
     * 
     * @throws DuplicateStationException
     *             DOCUMENT ME!
     */
    public void addStation(Station station) throws DuplicateStationException {
        String name = station.getName();

        if (stationNameObjMap.get(name) != null) {
            String msg = "Station with name " + name + " already in schdule";
            throw new DuplicateStationException(msg);
        }

        // for each area in schedule
        boolean stationBelongToExistingArea = false;

        for (AreaServedByStations area : this.getAreas()) {
            if (station.isAreaServedByStation(area)) {
                stationBelongToExistingArea = true;
                station.associateWithArea(area);
            }
        }

        // new station not in any area ?
        if (!stationBelongToExistingArea) {
            // create a new area with the same as this station
            AreaServedByStations area = station.createAreaFromStation();
            station.associateWithArea(area);

            // add new area to sched
            this.addAreaServedByStations(area);
        }

        // add to map
        stationNameObjMap.put(name, station);
    }

    public void addAreaServedByStations(AreaServedByStations area) {
        String name = area.getName();

        if (generalAreaNameObjMap.get(name) != null) {
            throw new RuntimeException("Area " + name + "  already in schedule");
        }
        generalAreaNameObjMap.put(name, area);
    }

    public void removeAreaServedByStations(AreaServedByStations area) {
        String name = area.getName();
        if (generalAreaNameObjMap.get(name) != null) {
            generalAreaNameObjMap.remove(name);
        }

    }

    /**
     * Get the list of candidate areas whose names start with a given prefix
     * 
     * @param prefix
     * @param numAreas
     *            number of areas to return
     * @return
     */
    public List<AreaServedByStations> getAreaSuggestionsFromNamePrefix(String prefix, int numAreas) {
        SortedMap<String, AreaServedByStations> map = generalAreaNameObjMap.tailMap(prefix);
        ArrayList<AreaServedByStations> result = new ArrayList<AreaServedByStations>();
        // get area until its name no longer has that prefix
        if (prefix != null && prefix.length() > 0) {
            Iterator<AreaServedByStations> areas = map.values().iterator();
            while (areas.hasNext() && result.size() < numAreas) {
                AreaServedByStations area = areas.next();
                if (!area.getName().startsWith(prefix))
                    break;
                else
                    result.add(area);
            }
        }
        return result;
    }

    /**
     * return areas whose names fuzzy matches a keyword
     * 
     * user typed 翡翠城北区 , he meant 翡翠楼北区 , 清华大学 we only have 清华园
     * 
     * @param keyword
     * @param numAreas
     * @return sorted based on how may matches and where the matches occur, the
     *         earlier each character match occur within the name the better
     * 
     */
    public List<AreaServedByStations> getAreasNameMatchString(String keyword, int numAreas, double minMatchScore) {
        ArrayList<AreaServedByStations> result = new ArrayList<AreaServedByStations>();
        if (keyword.isEmpty()) {
            return result;
        }

        Set<AreaNameMatchRecord> matchList = getAreaNameMatchRecord(keyword, numAreas, minMatchScore);

        // get top N
        for (AreaNameMatchRecord amr : matchList) {
            result.add(amr.area);
        }

        return result;
    }

    protected Set<AreaNameMatchRecord> getAreaNameMatchRecord(String keyword, int numAreas, double minMatchScore) {
        /**
         * keep a top numArea list of top matches sorted by match score max to
         * min
         */
        SortedSet<AreaNameMatchRecord> matchList = new TreeSet<AreaNameMatchRecord>();
        for (AreaServedByStations area : generalAreaNameObjMap.values()) {
            double score = StringFuncs.getMatchScore(area.getName(), keyword);
            if (matchList.size() < numAreas) {
                if (score > minMatchScore)
                    matchList.add(new AreaNameMatchRecord(area, score));
            } else {
                // replace the area with the currently weakest match with this
                // one if this one
                // area has a better match
                if (score > matchList.last().matchScore) {
                    matchList.remove(matchList.last());
                    matchList.add(new AreaNameMatchRecord(area, score));
                }
            }
        }

        return matchList;
    }

    final protected class AreaNameMatchRecord implements Comparable<AreaNameMatchRecord> {
        AreaServedByStations area;

        double matchScore;

        AreaNameMatchRecord(AreaServedByStations a, double score) {
            area = a;
            matchScore = score;
        }

        public double getMatchScore() {
            return matchScore;
        }

        public int hashCode() {
            return area.getName().hashCode();
        }

        public boolean equals(Object o) {
            AreaNameMatchRecord m = (AreaNameMatchRecord) o;
            return area.equals(m.area) && matchScore == m.matchScore;
        }

        public int compareTo(AreaNameMatchRecord o) {
            double score2 = o.matchScore;
            if (matchScore > score2)
                return -1;
            if (matchScore == score2) {
                // cannot return 0 here as TreeMap use compareTo() , not equals
                // to determine equality, here favor shorter names
                String name1 = area.getName();
                String name2 = ((AreaNameMatchRecord) o).area.getName();
                if (name1.length() != name2.length()) {
                    return name1.length() - name2.length();
                } else {
                    return name1.compareTo(name2);
                }
            }
            return 1;
        }

        public AreaServedByStations getArea() {
            return area;
        }
    }

    /**
     * in DB train # can be T297/T298 or T297/298 or T298 or T297, User may
     * input T298/T297, T298/297, T297 or T298 or T297/298, all these
     * combinations are considered matched, except that if DB is T298 but user
     * is T297 then no match is found.
     * 
     * @param number
     *            full or partial number that can identify the line
     * @return the first line matches the number, null if not found
     */
    public Line   getLineMatchNumber(String number) {
        for (Line l : getLines()) {
            if (l.matchNumber(number)) {
                return l;
            }
        }
        return null;
    }

    public List<Line> getLinesFuzzyMatchNumber(String number) {
        List<Line> res = new ArrayList<Line>();
        for (Line l : getLines()) {
            if (l.fuzzyMatchNumber(number)) {
                res.add(l);
            }
        }
        return res;
    }

    public Collection<Line> getLines() {
        return lineIdObjMap.values();
    }

    public Collection<Station> getStations() {
        return stationNameObjMap.values();
    }

    /**
     * Find the station with given name
     * 
     * @param name
     * 
     * @return null if no station with such name
     */
    public Station getStation(String name) {
        Object st = stationNameObjMap.get(name);
        return (Station) st;
    }

    /**
     * Find the line with given id
     * 
     * @param id
     * @return null if not found
     */
    public Line getLineById(Integer id) {
        Object ln = lineIdObjMap.get(id);
        return (Line) ln;
    }

    /**
     * Get the area with given name
     * 
     * @param name
     * 
     * @return
     */
    public AreaServedByStations getAreaServedByStationsByName(String name) {
        return (AreaServedByStations) generalAreaNameObjMap.get(name);
    }

    public Collection<AreaServedByStations> getAreas() {
        return Collections.unmodifiableCollection(generalAreaNameObjMap.values());
    }

    public synchronized AreaServedByStations[] getAreasArray() {
        if (areasArray == null) {
            Collection<AreaServedByStations> areas = generalAreaNameObjMap.values();
            areasArray = areas.toArray(new AreaServedByStations[areas.size()]);
            Arrays.sort(areasArray);
        }
        return areasArray;
    }

    public int getNumStations() {
        return stationNameObjMap.size();
    }

    /**
     * Write out to a standard text format
     * 
     * @see ScheduleFactory
     */
    @SuppressWarnings("unchecked")
    public void write(PrintWriter pw) throws IOException {
        // write header;
        this.writeHeader(pw);

        // sort lines
        Vector<Line> lines = new Vector<Line>(lineIdObjMap.values());

        // remove lines with zero stations
        for (int i = 0; i < lines.size(); i++) {
            Line line = (Line) lines.get(i);
            if (line.getNumPassingStations() == 0) {
                lines.remove(i);
                i = i - 1;
            }
        }

        Collections.sort(lines);

        // print each line
        for (int i = 0; i < lines.size(); i++) {
            Line line = (Line) lines.get(i);
            line.printOut(pw);
        }
    }

    public void writeToFile(String filename) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"), true);
            write(pw);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                pw.close();
            } catch (Exception ex) {
            }
        }
    }

    /**
     * 
     * @param lineId
     */
    protected void removeLine(int lineId) {
        lineIdObjMap.remove(lineId);
    }

    public void removeLineByNumber(String lineNumber) {
        Line line = getLineMatchNumber(lineNumber);
        if (line == null) {
            throw new IllegalStateException("Line to be deleted: " + lineNumber + " not found in schedule.");
        }
        removeLine(line.getId());
    }

    /**
     * search all lines in this sched to find a line that's equivalent to the
     * input line, used for comparing two schedules
     * 
     * @param line
     * @return null if not founde
     */
    public Line getLineEquivalentTo(Line line) {
        for (Line myLine : getLines()) {

            if (myLine.isEquivalent(line)) {
                return myLine;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTextFilePath() {
        return textFilePath;
    }

    public void setTextFilePath(String textFilePath) {
        this.textFilePath = textFilePath;
    }

    public boolean isUsedLive() {
        return isUsedLive;
    }

    public void setUsedLive(boolean isUsedLive) {
        this.isUsedLive = isUsedLive;
    }

    public String getCvsVision() {
        return cvsVision;
    }

    public void setCvsVision(String cvsVision) {
        this.cvsVision = cvsVision;
    }

}
