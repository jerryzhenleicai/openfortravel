# 简介 (Introduction)

OpenForTravel 包含了开源的高参网火车查询的核心代码，高参网火车查询自从1999年来已经为数百万的网民服务。

OpenForTravel contains source code of the Gaocan.com's train route search code that has serving millions of Chinese travelers since 1999.


# 编译和安装 (Installation)

运行 (Run)：

用Apache Maven来编译:

```mvn install```

结果会生成一个WAR文件： ./rest-server/target/rest-server-1.0.war.这个WAR文件可以直接发布到任何支持Java Servlet标准的J2EE容器，例如Apache Tomcat。下面我们就用Tomcat 8.5.9为例。

This will create a WAR file under ./rest-server/target/rest-server-1.0.war, which can be deployed to any Java servlet
container. We will use Apache Tomcat 8.5.9 as an example.

运行 (Run)：

```cp ./rest-server/target/rest-server-1.0.war  /opt/apache-tomcat-8.5.9/webapps/rest.war```

启动 Tomcat ， 然后在浏览器中打开如下的URL: (start Tomcat and open this URL)

http://localhost:8080/rest/trest?src=成都&dest=深圳

注意app启动时由于初始化一次性的需要大概两分钟,最后结果会返回如下JSON。
Note the app may take a couple minutes to initialize. Finally you will see this JSON response:

```
[{ "segs": [ {"line" : "G1315/G1318", "dep":"7:55", "arriv":"21:50","from":"成都东","to":"广州南"},{"line" : "G6225", "dep":"22:25", "arriv":"23:01","from":"广州南","to":"深圳北"}],"shifa":true,"travel_minutes":906,"price":{"yzPrice":159,"rzPrice":250,"ywPrice":250,"rwPrice":-2}, "start":"7:55", "end":"23:01"},{ "segs": [ {"line" : "G1315/G1318", "dep":"7:55", "arriv":"19:01","from":"成都东","to":"长沙南"},{"line" : "G6027", "dep":"19:29", "arriv":"22:55","from":"长沙南","to":"深圳北"}],"shifa":true,"travel_minutes":900,"price":{"yzPrice":253,"rzPrice":399,"ywPrice":399,"rwPrice":-2}, "start":"7:55", "end":"22:55"},{ "segs": [ {"line" : "D2241/D2244", "dep":"6:45", "arriv":"16:32","from":"成都东","to":"武汉"},{"line" : "G1021", "dep":"16:58", "arriv":"22:02","from":"武汉","to":"深圳北"}],"shifa":true,"travel_minutes":917,"price":{"yzPrice":292,"rzPrice":462,"ywPrice":462,"rwPrice":-2}, "start":"6:45", "end":"22:02"},{ "segs": [ {"line" : "G1315/G1318", "dep":"7:55", "arriv":"20:17","from":"成都东","to":"郴州西"},{"line" : "G9685", "dep":"21:57", "arriv":"23:58","from":"郴州西","to":"深圳北"}],"shifa":true,"travel_minutes":963,"price":{"yzPrice":210,"rzPrice":329,"ywPrice":329,"rwPrice":-2}, "start":"7:55", "end":"23:58"},{ "segs": [ {"line" : "G1315/G1318", "dep":"7:55", "arriv":"21:50","from":"成都东","to":"广州南"},{"line" : "G6141/G6144", "dep":"23:08", "arriv":"23:37","from":"广州南","to":"深圳北"}],"shifa":true,"travel_minutes":942,"price":{"yzPrice":159,"rzPrice":250,"ywPrice":250,"rwPrice":-2}, "start":"7:55", "end":"23:37"},
...
,{ "segs": [ {"line" : "D351/D354", "dep":"8:01", "arriv":"16:54","from":"成都东","to":"汉口"},{"line" : "T95", "dep":"18:30", "arriv":"8:50","from":"汉口","to":"深圳"}],"shifa":true,"travel_minutes":1489,"price":{"yzPrice":304,"rzPrice":509,"ywPrice":666,"rwPrice":428}, "start":"8:01", "end":"8:50"}]

```

其中的每个 segs 代表一个旅行方案，例如 (each segs stands for one travel plan)：

```
{ "segs": [ {"line" : "G1315/G1318", "dep":"7:55", "arriv":"21:50","from":"成都东","to":"广州南"},{"line" : "G6225", "dep":"22:25", "arriv":"23:01","from":"广州南","to":"深圳北"}],"shifa":true,"travel_minutes":906,"price":{"yzPrice":159,"rzPrice":250,"ywPrice":250,"rwPrice":-2}, "start":"7:55", "end":"23:01"},
```

代表从成都东坐G1315/G1318次车7:55出发，21:50到达广州南, 然后在广州南22:25转G6225次，最后于23:01到达深圳。shifa:true 表示G1315成都东是始发站。
travel_minutes是旅途总共花费时间。price是各种等级座位的价格。

# LICENSE

MIT License. See LICENSE.md

# 作者 Author

蔡杰瑞 Jerry Cai

http://www.caijerry.com/

jerrycai@gaocan.com
