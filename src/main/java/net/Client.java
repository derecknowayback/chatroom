package net;

import dto.User;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Client implements Runnable {

    private static final int CLIENT_PORT = 8800; // 定义固定端口号
    private static final int REFRESH_ONLINE_USERS_FREQUENCY = 10;

    InetAddress serverIP;
    int serverPort;

    HashSet<Integer> friends;

    HashMap<String,User> allUsers;

    User self;

    private boolean isLogin;
    private String loginFailedMsg;


    DatagramSocket socket;

    public void getServerAddrByConfig() throws IOException {
        RandomAccessFile file = new RandomAccessFile("serverip.txt","r");
        String ip = file.readLine();
        int port = file.readInt();
        serverIP = InetAddress.getByName(ip);
        serverPort = port;
    }

    public Client() throws SocketException {
        this.socket = new DatagramSocket(CLIENT_PORT);
        this.isLogin = false;
        this.loginFailedMsg = null;
    }

    public boolean getIsLogin() {
        return isLogin;
    }

    public String getLoginFailedMsg() {
        return loginFailedMsg;
    }

    public User getSelf() {
        return self;
    }

    public void setSelf (User u) {
        this.self = u;
    }

    @Override
    public void run() {
        // 先获取服务端ip
        try {
            getServerAddrByConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//        开始循环
        int refreshOnlineUser = 0;
        byte[] bytes = new byte[1024];
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
        while (true) {
            // 尝试获取包
            try {
                socket.receive(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // 对包进行分类
            byte[] data = packet.getData();
            int length = packet.getLength();
            String reply = new String(data, 0, length);

            // 接下来开始解析: type + content
            int type = 0;
            switch (type) {
                case  Proto.RespForSaveMsg : {
                    String msg = ...;
                    // do nothing
                }

            }


            // 定期获取在线用户
            if (refreshOnlineUser == REFRESH_ONLINE_USERS_FREQUENCY) {
                try {
                    askForAllUsers();
                    refreshOnlineUser = 0;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            refreshOnlineUser ++;
        }
    }


    public void askForAllUsers() throws IOException {
        Proto askForOnlineUsers = Proto.getAskForAllUsers();
        byte[] payload = askForOnlineUsers.toString().getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(payload,0,payload.length,serverIP,serverPort);
        socket.send(packet);
    }

    public List<User> resolveAllUsers(String s) {
        String[] split = s.split(";");
        List<User> res = new ArrayList<>();
        for (String userStr : split) {
            String[] fileds = userStr.split(",");
            // id name ip
            int id = Integer.parseInt(fileds[0]);
            User user = new User(fileds[1], fileds[2], id);
            res.add(user);
        }
        return res;
    }


    // 将一条消息发送给某个用户
    public void sendMsgToP(String msg,int userId) throws IOException {

        if (!allUsers.containsKey(userId)) {
            // if 如果用户没有登录

        } else {
            // else 如果用户登录了

        }
        // todo 如果需要从server发送积压消息切换到实时沟通，需要同步一下
        // sendMsgToS

        // sendMsgToP
//        String ip = allUsers.get(userId);
        String ip = "11";
        InetAddress inetAddress = InetAddress.getByName(ip);
        byte[] payload = Proto.getNewMessage(msg.toString()).toString().getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(payload, 0, payload.length, inetAddress, CLIENT_PORT);
        socket.send(packet);
    }


    // 当某个用户在线的时候，将一条消息直接发送给某个用户
//    private void sendMsgToP(Message msg,int userId) throws IOException {

//    }

    // 和Server端打交道
    // 当某个用户不在线的时候，将一条消息发送给Server
    public void sendMsgToS(String msg) throws IOException {
        byte[] payload = msg.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(payload, 0, payload.length, serverIP, serverPort);
        socket.send(packet);
    }

    public boolean isFriend(int userId) {
        return friends.contains(userId);
    }

    public int getUserIdByName (String name) {
        User user = allUsers.get(name);
        if (user == null) return -1;
        return user.getId();
    }

    public int getSelfId () {
        return self.getId();
    }

}
