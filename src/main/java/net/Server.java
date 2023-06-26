package net;

import dto.DBManager;
import dto.User;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import msg.GroupChatRecord;
import msg.Message;
import msg.ServerChatRecord;

public class Server implements Runnable {

    List<User> users; // 全部的用户

    ServerChatRecord serverChatRecord;

    HashMap<Integer, GroupChatRecord> groupChatRecordMap;

   HashMap<Integer, GroupManager> groupHashMap;


   DatagramSocket socket;

    private Server () throws SocketException {
        serverChatRecord = ServerChatRecord.getServerChatRecord();
        groupChatRecordMap = new HashMap<>();
        groupHashMap = new HashMap<>();
        socket = new DatagramSocket();
    }

    @Override
    public void run() {

    }

    // 接收请求，返回所有用户,并且判断用户是否登录
    public void getAllUsers(){
        // todo 调用数据库接口
    }

    // 接收登录请求，添加用户到登录集合中, 顺便发送所有积压的请求
    // PS: 积压的请求发送完之后需要从文件中删除
    public void acceptUser(String name, String password) {
        String msg = DBManager.checkUser(name, password);
        String[] split = msg.split(",");
        // 登录成功
        if (split[0].equals("true")) {
            int userId = Integer.parseInt(split[1]);
            // todo 发送响应

            // 推送积压的消息
            pubMsg(userId);
        }
        // 登录失败
        else {

        }
    }

    // 接收注册请求，添加用户到登录集合中
    public void createUser (String name, String password) {
        User user = DBManager.insertUser(name, password);
        // 插入失败, 可能是因为同名 ?
        if (user == null) {

        }

    }


    // 接收暂存消息请求，添加到消息队列中
    public void saveMsg(int sendId,int receiverId, String content,String time) {
        Message message = new Message(sendId, receiverId, content, time);
        serverChatRecord.addMessages(message);
    }

    // 发送暂存消息请求，发送给每个
    public void pubMsg(int userId) {
        InetAddress ip = LoginStatus.getIP(userId);
        List<Message> toPub = serverChatRecord.getAllMsgByUserId(userId);
        StringBuilder content = new StringBuilder();
        toPub.forEach(content::append);
        Proto proto = Proto.getBacklogMsg(content.toString());
        byte[] bytes = proto.toString().getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(bytes, 0, bytes.length, ip, Client.CLIENT_PORT);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pubAllUsers () {

    }


    public void flushMsg() {
        serverChatRecord.writeRecord();
    }

}
