package com.socketio.server;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.socketio.bean.ImageMessage;
import com.socketio.bean.JoinInfo;
import com.socketio.bean.MessageInfo;

public class WeChatServer {//先连接 再上线

    // 客户端映射
    private static Map<String, SocketIOClient> clientMap = new HashMap<>();//{sessionId,client}
    // 人员名字映射
    private static Map<String, String> nameMap = new HashMap<>();//{sessionId, name}
    // 群名称与群成员映射
    private static Map<String, Map<String, String>> groupMap = new HashMap<>();//{grop_name,{sessionId,User_name}}
    
    public static void main(String[] args) {
        Configuration config = new Configuration();
        // 如果调用了setHostname方法，就只能通过主机名访问，不能通过IP访问
        //config.setHostname("localhost");
        config.setPort(9030); // 设置监听端口
        final SocketIOServer server = new SocketIOServer(config);



        // 添加连接连通的监听事件
        server.addConnectListener(client -> {
            String sessionId = client.getSessionId().toString();
            System.out.println("getRemoteAddress "+client.getRemoteAddress().toString());
            System.out.println(sessionId+"已连接");
            clientMap.put(sessionId, client);
        });


        // 添加连接断开的监听事件
        server.addDisconnectListener(client -> { //addDisconnectListener就是执行的断开
            String sessionId = client.getSessionId().toString();
            System.out.println(sessionId+"已断开");
            for (Map.Entry<String, SocketIOClient> item : clientMap.entrySet()) {
                if (!sessionId.equals(item.getKey())) {
                    item.getValue().sendEvent("friend_offline", nameMap.get(sessionId));//返回该条目的值
                }
            }
            for (Map.Entry<String, SocketIOClient> item : clientMap.entrySet()) {
                if (sessionId.equals(item.getKey())) {
                    clientMap.remove(item.getKey());//1 remove
                    break;
                }
            }
            for (Map.Entry<String, Map<String, String>> group : groupMap.entrySet()) {
                group.getValue().remove(sessionId);// 2 remove
            }
            nameMap.remove(sessionId);//3 remove
        });






        // mSocket.emit("self_online", content);    self_online 是客户端那边emit的tag，
        // 添加我已上线的监听事件
        server.addEventListener("self_online", String.class, (client, name, ackSender) -> { //(client, name, ackSender)== DataListener<T> listener
            String sessionId = client.getSessionId().toString();
            System.out.println(sessionId+"已上线："+name);
            for (Map.Entry<String, SocketIOClient> item : clientMap.entrySet()) {
                item.getValue().sendEvent("friend_online", name);//向客户端发送name，name是客户端编辑发过来的string
                client.sendEvent("friend_online", nameMap.get(item.getKey()));//向客户端发送信息SessionID。 mSocket.on("friend_online"）friend_online是客户单那边on的tag
            }
            nameMap.put(sessionId, name);
        });
        // 添加我已下线的监听事件
        server.addEventListener("self_offline", String.class, (client, name, ackSender) -> {
            String sessionId = client.getSessionId().toString();
            System.out.println(sessionId+"已下线："+name);
            for (Map.Entry<String, SocketIOClient> item : clientMap.entrySet()) {
                if (!sessionId.equals(item.getKey())) {
                    item.getValue().sendEvent("friend_offline", name);
                }
            }
            nameMap.remove(sessionId);
        });



        // 添加文本发送的事件监听器
        server.addEventListener("send_friend_message", JSONObject.class, (client, json, ackSender) -> { //私聊发文本
            String sessionId = client.getSessionId().toString();
            System.out.println(sessionId+"发送消息："+json.toString());
            MessageInfo message = (MessageInfo) JSONObject.toJavaObject(json, MessageInfo.class); // 属性有from(name) to(name) 内容
            for (Map.Entry<String, String> item : nameMap.entrySet()) { //   item={sessionid,name}
                if (message.getTo().equals(item.getValue())) {
                    clientMap.get(item.getKey()).sendEvent("receive_friend_message", message);//to这个人的客户端收到了一个object的对象，属性有from(name) to(name) 内容。
                    break;
                }
            }
        });
        // 添加图像发送的事件监听器
        server.addEventListener("send_friend_image", JSONObject.class, (client, json, ackSender) -> {//私聊发图片
            String sessionId = client.getSessionId().toString();
            System.out.println(sessionId+"发送图片："+json.toString());
            ImageMessage message = (ImageMessage) JSONObject.toJavaObject(json, ImageMessage.class);
            System.out.println("getFrom="+message.getFrom()+",getTo="+message.getTo()+",getName="+message.getPart().getName());// 图像的分段名称
            for (Map.Entry<String, String> item : nameMap.entrySet()) {
                if (message.getTo().equals(item.getValue())) {
                    System.out.println(item.getKey()+"receive_friend_image");
                    clientMap.get(item.getKey()).sendEvent("receive_friend_image", message);// to这个人的客户端收到了一个object的对象，属性有from(name) to(name) 内容。
                    break;
                }
            }
        });






        // 添加入群的事件监听器
        server.addEventListener("join_group", JSONObject.class, (client, json, ackSender) -> {
            String sessionId = client.getSessionId().toString();
            System.out.println(sessionId+"已入群："+json.toString());
            JoinInfo info = (JoinInfo) JSONObject.toJavaObject(json, JoinInfo.class);//阿里的json到object 得到客户端编辑的输入


            if (!groupMap.containsKey(info.getGroup_name())) {//第一个入群的，创建该群
                System.out.println("groupMap.put "+info.getGroup_name());
                groupMap.put(info.getGroup_name(), new HashMap<String, String>());
            }
            for (Map.Entry<String, Map<String, String>> group : groupMap.entrySet()) {//遍历所有的群 看要加入的是那个群
                System.out.println("群名称为"+group.getKey());
                if (info.getGroup_name().equals(group.getKey())) {
                    group.getValue().put(sessionId, info.getUser_name());// 把这个人的SessionID和name添加到指定的群中
                    for (Map.Entry<String, String> user : group.getValue().entrySet()) { //user:{sessionId,user_name} 输出加入的这个群的所有人的SessionID 和 向客户端发送count次tag为person_in_group的这个人的名字
                        System.out.println("群成员为"+user.getKey());
                        clientMap.get(user.getKey()).sendEvent("person_in_group", info.getUser_name());//clientMap:{sessionId,client}，
                        System.out.println(user.getKey()+" person_in_group");
                    }
                    System.out.println("person_count="+group.getValue().size());
                    client.sendEvent("person_count", group.getValue().size());
                }
            }
        });
        // 添加退群的事件监听器
        server.addEventListener("leave_group", JSONObject.class, (client, json, ackSender) -> {
            String sessionId = client.getSessionId().toString();
            System.out.println(sessionId+"已退群："+json.toString());
            JoinInfo info = (JoinInfo) JSONObject.toJavaObject(json, JoinInfo.class);

            for (Map.Entry<String, Map<String, String>> group : groupMap.entrySet()) {
                if (info.getGroup_name().equals(group.getKey())) {
                    group.getValue().remove(sessionId);
                    for (Map.Entry<String, String> user : group.getValue().entrySet()) {
                        clientMap.get(user.getKey()).sendEvent("person_out_group", info.getUser_name());//向客户端发送count次tag为person_out_groupd的user_name
                        System.out.println(user.getKey()+" person_out_group");
                    }
                }
            }
        });




        // 添加群消息发送的事件监听器
        server.addEventListener("send_group_message", JSONObject.class, (client, json, ackSender) -> {// 群聊发文本
            String sessionId = client.getSessionId().toString();
            System.out.println(sessionId+"发送消息："+json.toString());
            MessageInfo message = (MessageInfo) JSONObject.toJavaObject(json, MessageInfo.class);// 属性有from(个人name) to(群聊名字name) 内容
            for (Map.Entry<String, Map<String, String>> group : groupMap.entrySet()) { // {grop_name,{sessionId,User_name}}
                if (message.getTo().equals(group.getKey())) {
                    for (Map.Entry<String, String> user : group.getValue().entrySet()) { //user{ssessionID,user_name} 上面的值
                        if (!user.getValue().equals(message.getFrom())) { //不能自己给自己发
                            clientMap.get(user.getKey()).sendEvent("receive_group_message", message);//该群聊的其他人收到对象（属性有from(个人name) to(群聊名字name) 内容）。。
                            System.out.println("receive_group_message 接收方为"+user.getKey());// 打印N次ssessionID
                        }
                    }
                    break;
                }
            }
        });
        // 添加群图片发送的事件监听器
        server.addEventListener("send_group_image", JSONObject.class, (client, json, ackSender) -> {// 群聊发图片
            String sessionId = client.getSessionId().toString();
            System.out.println(sessionId+"发送图片："+json.toString());
            ImageMessage message = (ImageMessage) JSONObject.toJavaObject(json, ImageMessage.class);
            for (Map.Entry<String, Map<String, String>> group : groupMap.entrySet()) {
                if (message.getTo().equals(group.getKey())) {
                    for (Map.Entry<String, String> user : group.getValue().entrySet()) {
                        if (!user.getValue().equals(message.getFrom())) {
                            clientMap.get(user.getKey()).sendEvent("receive_group_image", message);
                            System.out.println("receive_group_image 接收方为"+user.getKey());
                        }
                    }
                    break;
                }
            }
        });

        server.start(); // 启动Socket服务
    }

}
