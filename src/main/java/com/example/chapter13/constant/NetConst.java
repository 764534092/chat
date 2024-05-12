package com.example.chapter13.constant;

public class NetConst {
    // HTTP地址的前缀
//    public final static String HTTP_PREFIX = "http://192.168.1.7:8080/HttpServer/"; //不能为localhost
    public final static String HTTP_PREFIX = "http://192.168.2.10:8080/HttpServer/"; //我自己的回环地址，自己访问自己









    // WebSocket服务的前缀
//    public final static String WEBSOCKET_PREFIX = "ws://192.168.1.7:8080/HttpServer/";
    public final static String WEBSOCKET_PREFIX = "ws://192.168.2.10:8080/HttpServer/";









//    public final static String BASE_IP = "192.168.1.7"; // 基础Socket服务的ip
    public final static String BASE_IP = "192.168.2.10"; // 基础Socket服务的ip
    public final static int BASE_PORT = 9010; // 基础Socket服务的端口








//    public final static String CHAT_IP = "192.168.1.7"; // 聊天Socket服务的ip
    public final static String CHAT_IP = "192.168.2.10"; // 聊天Socket服务的ip

    public final static int CHAT_PORT = 9030; // 聊天Socket服务的端口
}
