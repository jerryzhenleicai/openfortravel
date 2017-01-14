package com.gaocan.publictransportation;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Build a schedule from a text file. The format of the text file is:
 * 
 * <PRE>
 * 
 * [Number of Stations] [Number of Lines] [station1Name] [station2Name] ...
 * [stationNName] [Line1Name] [Number of Lsps] [1stLspStation][|AuxInfo]
 * [2ndLspStation][|AuxInfo] (e.g. Hefei|12:00,12:14 means this line's 2nd stop
 * is Hefei) ... [Line2Name] .. [LineNName] ..
 * 
 * </PRE>
 */
public abstract class ScheduleFactory {
    private static Logger logger = LoggerFactory.getLogger(ScheduleFactory.class);
    private static final int END_OF_LINE = 1;

    private static final int END_OF_SCHEDULE = 2;

    private static final int END_OF_FILE = 3;

    protected AbstractStationFactory stationFactory;

    protected AbstractLineFactory lineFactory;

    protected AbstractLineStationPairFactory lspFactory;

    private String lineName;

    private InputStream ins;

    private EndOfSchedFileListener eofListener;

    /**
     * a file can contain multiple schedules such as a bus
     * 
     * @param line
     * @return true if line represents end of a schedule such as one city's
     *         buses
     */
    protected abstract boolean isLineEndOfSchedule(String line);

    /**
     * is it possible for same station appear in one line twice
     * 
     * @return
     */
    protected abstract boolean isDupStationInSameLine();

    protected ScheduleFactory(AbstractStationFactory stationFac, AbstractLineFactory lineFac,
            AbstractLineStationPairFactory lspF) {
        stationFactory = stationFac;
        lineFactory = lineFac;
        lspFactory = lspF;
    }

    /**
     * Build a Schedule object from reading the input files
     * 
     * @param schedule
     *            schedule with inital areas populated (more areas may need to
     *            be added because cities for some train stations whose province
     *            is unknown are not in the database).
     * @param fileName
     *            the version name to be used for the schedule built
     */
    public final void buildSchedule(Schedule schedule, String fileName) {
        try {
            logger.info("Building schedule " + schedule.getName() + " from file " + fileName, "");
            ins = new FileInputStream(fileName);
            StreamTokenizer st = getStream(ins);
            buildSchedule(schedule, null, st);
        } catch (IOException e) {
            logger.error( schedule.getName() + "Error reading file " + fileName, e);
            throw new IllegalStateException(e);
        }
    }

    public final void buildSchedule(Schedule schedule, List<String> errors, InputStream fis) {
        try {
            // skip version # (v2, v3)
            StreamTokenizer st = getStream(fis);
            st.nextToken();
            buildSchedule(schedule, errors, st);
        } catch (IOException e) {
            logger.error( "Error reading file ", e);
            throw new IllegalStateException(e);
        }
    }

    private StreamTokenizer getStream(InputStream strm) throws IOException {
        // Now READ the schedule data in UTF8
        StreamTokenizer tokens = new StreamTokenizer(new InputStreamReader(strm, "utf-8"));
        tokens.resetSyntax();
        tokens.wordChars(0, 255);
        tokens.eolIsSignificant(false);
        // space is not
        tokens.whitespaceChars('\n', '\n');
        tokens.whitespaceChars('\t', '\t');

        // this must be there or will not work on Windows!
        tokens.whitespaceChars('\r', '\r');
        return tokens;
    }

    /**
     * Build a Schedule object from reading the input files
     * 
     * @param schedule
     *            schedule with initial areas populated (more areas may need to
     *            be added because cities for some train stations whose province
     *            is unknown are not in the database).
     * @param ins
     *            the input stream
     * @param errors
     *            list of errors in sched
     * @return true if stream is at the end of file and no more data in stream,
     *         false if there's still more data in the file for the next
     *         schedule (such as bus)
     */
    public final boolean buildSchedule(Schedule schedule, List<String> errors, StreamTokenizer tokens) {
        // parser
        boolean endOfFileSeen = false;
        try {
            // header
            schedule.readHeader(tokens);
            tokens.whitespaceChars('|', '|');

            // read lines
            readlines: while (true) {
                lineName = "First line";
                int result = readOneLine(schedule, tokens, errors);
                switch (result) {
                case END_OF_FILE:
                    endOfFileSeen = true;
                    break readlines;
                case END_OF_SCHEDULE:
                    break readlines;
                case END_OF_LINE:
                    break;
                }
            }
            return endOfFileSeen;
        } catch (IOException e) {
            logger.error( "Error in  line " + lineName, e);
            throw new IllegalStateException(e);
        } finally {
            if (endOfFileSeen) {
                if (ins != null) {
                    try {
                        ins.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else if (eofListener != null) {
                    eofListener.endOfFileReached();
                }
            }
        }
    }

    /**
     * 
     * @param tokens
     * @return END_OF_SCHEDULE if at end of a schedule (e.g. a city's bus),
     *         END_OF_FILE if at end of stream
     */
    private int readOneLine(Schedule schedule, StreamTokenizer tokens, List<String> errors) throws IOException {
        int c = tokens.nextToken();

        if (c != StreamTokenizer.TT_WORD) {
            if (c == StreamTokenizer.TT_EOF) {
                return END_OF_FILE;
            } else {
                throw new RuntimeException("expecting line name, got non word after line: " + lineName);
            }
        }
        lineName = tokens.sval.toUpperCase().trim();

        if (isLineEndOfSchedule(lineName)) {
            return END_OF_SCHEDULE;
        }

        String lineExtraData = null;
        // read extra data if any
        if (lineFactory.lineHasExtraData()) {
            c = tokens.nextToken();
            lineExtraData = tokens.sval.trim();
        }
        // create the new line
        Line line = null;
        try {
            line = lineFactory.createLine(lineName, lineExtraData);
        } catch (Exception e) {
            String s = schedule.getName() + ": Line " + lineName + " has error:" + e.getMessage();
            if (errors != null)
                errors.add(s);
            logger.error( s, e);
            throw new RuntimeException(e);
        }

        // read comment of a line, which start with #
        c = tokens.nextToken();
        boolean gotComment = false;
        if (c == StreamTokenizer.TT_WORD) {
            String strLine = tokens.sval.trim();
            if (strLine.startsWith("#") || strLine.startsWith("＃")) {
                line.setComment(strLine.substring(1).trim());
                gotComment = true;
            }
        }

        // zcai, bug here, comment may be set by .createLine, has to use a flag
        if (!gotComment) {
            tokens.pushBack();
        }

        while (true) {
            // get LSP's station name
            c = tokens.nextToken();
            if (c != StreamTokenizer.TT_WORD) {
                throw new RuntimeException("expecting station name or EOL, got non word at line: " + lineName);
            }
            String str = tokens.sval.trim();
            if (str.equalsIgnoreCase("EOL")) {
                break;
            }
            Station station = (Station) schedule.getStation(str);
            if (station == null) {
                station = stationFactory.createStation(tokens.sval.trim());
                schedule.addStation(station);
            }
            boolean dupStation = false;
            // make sure this station is not already in line
            if (line.getStationIndex(station.getName()) != -1 && !isDupStationInSameLine()) {
                logger.warn("Line wrong : " + lineName);
                String err = "Station " + station.getName() + " appeared twice in line, ignored! " + line.getNumber() + " : "  +  line.toString();
                logger.warn(err);
                dupStation = true;
                if (errors != null)
                    errors.add(err);
            }

            String extraLspData = null;
            if (lspFactory.lspHasExtraData()) {
                c = tokens.nextToken();
                extraLspData = tokens.sval.replaceAll(" ", "");
            }
            // this will append lsp into line or station as well
            if (lspFactory.isLspInvalid(line, station) == false && !dupStation) {
                try {
                    lspFactory.createLsp(schedule, line, station, extraLspData);
                } catch (InvalidScheduleDataException ide) {
                    logger.error( schedule.getName() + "read line" + line.getNumber(), ide);
                    if (errors != null)
                        errors.add(ide.getMessage());
                }
            }
        }
        // validate the line data
        try {
            line.validateData();
            // add line to sched
            schedule.addLine(line);
        } catch (Exception se) {
            // remove all the lines's jsp from station too
            for (LineStationPair lsp : line.getLineStationPairs()) {
                lsp.getStation().removeLineStationPair(lsp);
            }

            logger.error( "Skipped reading line " + line.getNumber(), se);
            if (errors != null)
                errors.add(se.getMessage());
        }
        return END_OF_LINE;
    }

    /**
     * purely create a line without it being in any schedule, used outside of a
     * schedule context
     * 
     * @param lineNumber
     * @param lspText
     * @return
     */
    public Line createLineFromLspText(String lineNumber, String lspText) {
        // allow space
        StringTokenizer tokens = new StringTokenizer(lspText, "|\t\n\r\f");
        // get version number
        String lineName = tokens.nextToken().toUpperCase().trim();

        String lineExtraData = null;
        // read extra data if any
        if (lineFactory.lineHasExtraData()) {
            lineExtraData = tokens.nextToken();
        }
        if (lineExtraData != null) {
            lineExtraData = lineExtraData.replaceAll(" ", "");
        }

        Line line = lineFactory.createLine(lineName, lineExtraData);

        String s = tokens.nextToken().trim();
        // if lsp start with #， read comment
        if (!s.equalsIgnoreCase("EOL") && s.startsWith("#")) {
            line.setComment(s.substring(1));
        } else {
            createStation(tokens, line, s);
        }

        // since version 2 last line is "eol"
        while (true) {
            // get LSP's station name
            s = tokens.nextToken().trim();
            if (s.equalsIgnoreCase("EOL")) {
                break;
            }

            createStation(tokens, line, s);
        }

        return line;
    }

    private void createStation(StringTokenizer tokens, Line line, String s) {
        Station station = stationFactory.createStation(s);
        String extraLspData = null;
        if (lspFactory.lspHasExtraData()) {
            extraLspData = tokens.nextToken();
        }
        // this will append lsp into line or station as well
        if (lspFactory.isLspInvalid(line, station) == false) {
            lspFactory.createLsp(line, station, extraLspData);
        }
    }

    public EndOfSchedFileListener getEofListener() {
        return eofListener;
    }

    public void setEofListener(EndOfSchedFileListener eofListener) {
        this.eofListener = eofListener;
    }
}
