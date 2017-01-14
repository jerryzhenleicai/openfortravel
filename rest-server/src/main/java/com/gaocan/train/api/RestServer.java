package com.gaocan.train.api;

import com.gaocan.publictransportation.*;
import com.gaocan.train.RouteFinderFactory;
import com.gaocan.train.route.ExceedSearchQuotaException;
import com.gaocan.train.route.RouteFinderTooBusyException;
import com.gaocan.train.route.TrainSearchRequest;
import com.gaocan.train.schedule.FullCityNameSources;
import com.gaocan.train.train.TrainTrip;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;

@SuppressWarnings("serial")
public class RestServer extends HttpServlet {
  static RouteFinderFactory rfp = new RouteFinderFactory(new FullCityNameSources());

  public static String readStringFromStream(LineNumberReader reader) throws Exception {
    StringBuilder stringBuilder = new StringBuilder();
    String ls = System.getProperty("line.separator");
    String line;
    while ((line = reader.readLine()) != null) {
      stringBuilder.append(line);
      stringBuilder.append(ls);
    }
    reader.close();
    return stringBuilder.toString();
  }

  void printEmptyResponse(PrintWriter pw) {
    pw.print("{}");
    pw.close();
  }

  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

    resp.setCharacterEncoding("utf-8");
    resp.setContentType("application/json");
    OutputStream out = resp.getOutputStream();
    PrintWriter pw = new PrintWriter(out);

    // Get schedule last change time
    if (req.getParameter("lastChangeSched") != null) {
      pw.print("{ \"lastChange\" : \"" + rfp.get().getSchedule().getLastChangeDate() + "\" }");
      pw.close();
      return;
    }
    // Get a train line's schedule
    if (req.getParameter("lineSched") != null) {
      Line line = rfp.get().getSchedule().getLineMatchNumber(req.getParameter("lineSched").trim());
      if (line == null) {
        printEmptyResponse(pw);
        return;
      }
      int numStations = line.getLineStationPairs().size();
      if (numStations <= 1) {
        printEmptyResponse(pw);
        return;
      }

      pw.print("{\"lineNumber\": \"" + line.getNumber() + "\", \"start\":\"" + line.getLineStationPairs().get(0)
              .getDepartureTime().getStringWithoutDay() + "\", \"end\":\"" + line.getLineStationPairs()
              .get(numStations - 1).getArrivalTime().getStringWithoutDay() + "\", \"stops\": [");
      int s = 0;
      for (LineStationPair lsp : line.getLineStationPairs()) {
        boolean isLastStation = ((s++) == numStations - 1);
        pw.print("{ \"station\": \"" + lsp.getStation().getName() + "\"");
        pw.print(", \"arriv\": \"" + lsp.getArrivalTime().getStringWithoutDay() + "\"");
        pw.print(", \"dep\": \"" + lsp.getDepartureTime().getStringWithoutDay() + "\"");
        pw.print("} ");
        if (!isLastStation) pw.print(", ");
      }
      pw.print("]}");
      pw.close();
      return;
    }

    if (req.getParameter("apk") != null) {
      try {
        InputStream fis = getClass().getClassLoader().getResourceAsStream("com/gaocan/apkupdate/sample_apk.json");
        String json = readStringFromStream(new LineNumberReader(new InputStreamReader(fis)));
        fis.close();
        pw.print(json);
        pw.close();
        return;
      } catch (Exception e) {
        throw new ServletException(e);
      }

    }

    String[] parameters = URLDecoder.decode(req.getQueryString(), "UTF-8").split("&");
    String src = null;
    String dest = null;
    for (String p : parameters) {
      if (p.startsWith("src=")) {
        src = p.substring(4).trim();
      } else if (p.startsWith("dest=")) {
        dest = p.substring(5).trim();
      }
    }
    TrainSearchRequest searchReq = new TrainSearchRequest();
    searchReq.setDate(new Date());
    searchReq.setSrcStation(src);
    searchReq.setDestStation(dest);

    if (req.getParameter("sb") != null) {
      searchReq.setSrcTimeContraint(true);
      searchReq.setEarliestDepartHour(Integer.parseInt(req.getParameter("sb")));
    }
    if (req.getParameter("se") != null) {
      searchReq.setSrcTimeContraint(true);
      searchReq.setLatestDepartHour(Integer.parseInt(req.getParameter("se")));
    }

    if (req.getParameter("db") != null) {
      searchReq.setEarliestArriveHour(Integer.parseInt(req.getParameter("db")));
      searchReq.setDestTimeContraint(true);
    }
    if (req.getParameter("de") != null) {
      searchReq.setDestTimeContraint(true);
      searchReq.setLatestArriveHour(Integer.parseInt(req.getParameter("de")));
    }

    String sortBy = req.getParameter("sortBy");
    if (sortBy != null) {
      if ("P".equalsIgnoreCase(sortBy)) {
        searchReq.setSortOrder(TrainSearchRequest.SORT_BY_PRICE);
      } else if ("D".equalsIgnoreCase(sortBy)) {
        searchReq.setSortOrder(TrainSearchRequest.SORT_BY_DEPART_TIME);
      } else if ("A".equalsIgnoreCase(sortBy)) {
        searchReq.setSortOrder(TrainSearchRequest.SORT_BY_ARRIVE_TIME);
      } else {
        searchReq.setSortOrder(TrainSearchRequest.SORT_BY_TRIP_TIME);
      }
    }

    String res = doRouteSearchByRequest(searchReq);
    pw.write(res);
    pw.close();
  }

  private String doRouteSearchByRequest(TrainSearchRequest req) {
    try {
      List<TrainTrip> rides = rfp.get().findTrainRidesBetweenCityPair(req);
      StringBuffer sb = new StringBuffer(10000);
      sb.append("[");
      HourMinTime dep = null, arriv = null;
      Gson gson = new Gson();
      for (int r = 0; r < rides.size(); r++) {
        TrainTrip ride = rides.get(r);
        boolean isLastRide = r == rides.size() - 1;
        sb.append("{ \"segs\": [ ");
        dep = null;

        for (int i = 0; i < ride.getIntervals().size(); i++) {
          TripInterval intv = ride.getIntervals().get(i);
          boolean isLastSeg = i == ride.getIntervals().size() - 1;
          Line line = intv.getLine();
          HourMinTime start = null, end = null;
          start = intv.getStartLsp().getDepartureTime();
          if (dep == null) dep = start;

          end = intv.getEndLsp().getArrivalTime();
          arriv = end;

          sb.append("{\"line\" : \"" + line.getFullNumber() + "\", \"dep\":\"" + start.toString() + "\", \"arriv\":\""
                  + end.toString() + "\",\"from\":\"" + intv.getStartLsp().getStation().getName() + "\",\"to\":\""
                  + intv.getEndLsp().getStation().getName() + "\"}" + (isLastSeg ? "" : ","));
        }
        sb.append("],\"shifa\":" + ride.isSrcStationFirstInLine());
        sb.append(",\"travel_minutes\":" + ride.getTripTimeInMinutes());
        sb.append(
                ",\"price\":" + gson.toJson(ride.getMoneytaryCost(rfp.get().getTrainPricer())) + ", \"start\":\"" + dep
                        .toString() + "\", \"end\":\"" + arriv.toString() + "\"}" + (isLastRide ? "" : ","));
      }
      sb.append("]");
      return sb.toString();
    } catch (RouteFinderTooBusyException rbe) {
      return ("服务器太忙，请稍后再试。");
    } catch (NoSuchStationException e) {
      return ("库中没有此车站：" + e.getMessage());
    } catch (ExceedSearchQuotaException xe) {
      return ("您不能同时执行两次查询，请稍后再试。");
    }
  }
}
