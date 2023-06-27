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
        users = DBManager.findAllUsers();
    }

    @Override
    public void run() {

    }

    // 接收登录请求，添加用户到登录集合中, 顺便发送所有积压的请求
    // PS: 积压的请求发送完之后需要从文件中删除
    public void acceptUser(String name, String password,InetAddress inetAddress) {
        String msg = DBManager.checkUser(name, password);
        String[] split = msg.split(",");
        if (split[0].equals("true")) {
            // 登录成功
            int userId = Integer.parseInt(split[1]);
            // 添加到登录用户中
            User user = new User(Integer.parseInt(split[1]), name, inetAddress, true);
            LoginStatus.addUser(user);
            // 推送所有的用户
            pubAllUsers(userId);
            // 推送积压的消息
            pubMsg(userId);
        }
        Proto resp = Proto.getRespForLogin(msg);
        byte[] payload = resp.toString().getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet= new DatagramPacket(payload, 0, payload.length, inetAddress, Client.CLIENT_PORT);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 接收注册请求，添加用户到登录集合中
    public void createUser (String name, String password,InetAddress inetAddress) {
        String msg = DBManager.insertUser(name, password);
        String[] split = msg.split(",");
        if (split[0].equals("true")) {
            User user = new User(Integer.parseInt(split[1]), name, inetAddress, true);
            LoginStatus.addUser(user);
            users.add(user);
        }
        Proto resp = Proto.getRespForLogin(msg);
        byte[] payload = resp.toString().getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet= new DatagramPacket(payload, 0, payload.length, inetAddress, Client.CLIENT_PORT);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // 接收暂存消息请求，添加到消息队列中
    public void saveMsg(int sendId,int receiverId, String content,String time) {
        Message message = new Message(sendId, receiverId, content, time);
        serverChatRecord.addMessages(message);
    }

    // 发送暂存消息请求
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
        // todo 没有重新写回磁盘
    }

    public void pubAllUsers (int userId) {
        InetAddress inetAddress = LoginStatus.getIP(userId);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < users.size(); i++) {
            User temp = users.get(i);
            if (LoginStatus.isOnline(temp.getId()))
                temp.setOnline(true);
            builder.append(temp);
            if (i != users.size() - 1) builder.append("|");
        }
        Proto pub = Proto.getRespForAllUsers(builder.toString());
        byte[] payload = pub.toString().getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(payload, 0, payload.length, inetAddress, Client.CLIENT_PORT);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createGroup () {
        // todo 创建群组
    }


    public void removeFromGroup (int userId) {
        // todo 将成员从群组中移除
    }



    public void flushMsg() {
        serverChatRecord.writeRecord();
    }



}
