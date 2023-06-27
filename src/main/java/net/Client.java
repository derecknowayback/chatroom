package net;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import dto.Group;
import dto.User;
import java.io.File;
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
import java.util.Set;
import msg.ClientChatRecord;
import msg.Message;

public class Client implements Runnable {

    public static final int CLIENT_PORT = 8800; // 定义固定端口号
    private static final int REFRESH_ONLINE_USERS_FREQUENCY = 10;

    InetAddress serverIP;
    int serverPort;

    HashSet<Integer> friends;

    HashMap<String,User> allUsers;

    HashMap<String,Group> allGroups;

    User self;

    HashMap<String,ClientChatRecord> chatRecordMap;

    private boolean isLogin;
    private String loginFailedMsg;

    /*存储好友消息的磁盘上的文件名*/
    static final String friendRecordDocumentName = System.getProperty("user.dir") + "\\"+ "friend_record";


    DatagramSocket socket;

    /*将磁盘文件读取到friends集合*/
    public void readFriendRecord () throws IOException {
        File friendFile = new File(friendRecordDocumentName);
        if (!friendFile.exists()) {
            friendFile.createNewFile();
        }
        RandomAccessFile raf = new RandomAccessFile(friendFile, "rw");
        int num = raf.readInt();
        for (int i = 0; i < num; i++) {
            friends.add(raf.readInt());
        }
        raf.close();
    }

    /*将friends信息写入磁盘文件*/
    public void writeFriendRecord () throws IOException {
        File friendFile = new File(friendRecordDocumentName);
        if (!friendFile.exists()) {
            friendFile.createNewFile();
        }
        RandomAccessFile raf = new RandomAccessFile(friendFile, "rw");
        raf.writeInt(friends.size());
        for (Integer friend : friends) {
            raf.writeInt(friend);
        }
        raf.close();
    }


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
            // type部分和message部分用‘,’分隔
            byte[] data = packet.getData();
            String payload = new String(data);
            String[] dataString = payload.split(",", 2);
            String typeString = dataString[0];
            String messageString = dataString[1];

            // 接下来开始解析: type + content
            int type = Integer.parseInt(typeString);
            switch (type) {
                case Proto.RespForAllUsers: {
                    String[] blocks = messageString.split("\\|");
                    for (String block : blocks) {
                        String index[] = block.split(",");
//                        User user = new User(Integer.parseInt(index[0]), index[1], Boolean.parseBoolean(index[2]));
//                        allUsers.put(index[1], user);
                    }
                    break;
                }

                case Proto.RespForSaveMsg : {
                    // do nothing


                }
                case Proto.RespForLogin: {
                    //boolean和string之间用‘,’分隔
                    String index[] = messageString.split(",");
                    boolean isLoginSuccess = Boolean.parseBoolean(index[0]);
                    if (isLoginSuccess) {
                        isLogin = true;
                    } else {
                        isLogin = false;
                        loginFailedMsg = index[1];
                    }
                    break;
                }
                case Proto.SendForMsg: {
                    String[] blocks = messageString.split("\\|");
                    for (String msgStr : blocks) {

                    }
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
//            User user = new User(fileds[1], fileds[2], id);
//            res.add(user);
        }
        return res;
    }


    // 将一条消息发送给某个用户
    public void sendMsgToP(String msg,int userId,int type) throws IOException {


        // 登录了没有 + 是不是好友
        // 不是好友要发送make friend的消息，
        // 在线的话要发给对方，不在线的话要发给服务器
        // 服务器可以接收 make friend / resp make friend / save for msg 3种消息
        // 客户端在前端来选择要不要接受消息
        // 是好友的话同上

        boolean isFriend;
        if (friends.contains(userId)) {
            isFriend = true;
        } else {
            isFriend = false;
        }

        Proto p = new Proto();

        if (!allUsers.containsKey(userId)) {
            // if 如果用户没有登录
            if (isFriend){
                p = Proto.getNewMessage(msg);
            } else {
                if (type == 0) {
                    p = Proto.getAskForMakeFriend(msg);
                } else if (type == 1) {
                    p = Proto.getRespForMakeFriend(true);
                } else  {
                    p = Proto.getRespForMakeFriend(false);
                }
            }
            byte[] payload = p.toString().getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(payload, 0, payload.length, serverIP, serverPort);
            socket.send(packet);
        } else {
            // else 如果用户登录了
            if (isFriend){
                p = Proto.getNewMessage(msg);
            } else {
                if (type == 0) {
                    p = Proto.getAskForMakeFriend(msg);
                } else if (type == 1) {
                    p = Proto.getRespForMakeFriend(true);
                } else {
                    p = Proto.getRespForMakeFriend(false);
                }
            }
            String ip = "11";
            InetAddress inetAddress = InetAddress.getByName(ip);
            byte[] payload = p.toString().getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(payload, 0, payload.length, inetAddress, CLIENT_PORT);
            socket.send(packet);
        }
        // todo 如果需要从server发送积压消息切换到实时沟通，需要同步一下
        // sendMsgToS

        // sendMsgToP
//        String ip = allUsers.get(userId);
    }

    // 做好了
    public void askForRegister(String name, String password) {
        // 发请求，注册用户
        User user = new User(name, password);
        Proto askForRegister = Proto.getAskForRegister(user.loginOrRegisterStr());
        byte[] payload = askForRegister.toString().getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(payload, 0, payload.length, serverIP, serverPort);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 做好了
    public void askForLogin(String name, String password) {
        User user = new User(name, password);
        Proto askForLogin = Proto.getAskForLogin(user.loginOrRegisterStr());
        byte[] payload = askForLogin.toString().getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(payload, 0, payload.length, serverIP, serverPort);
        try {
            socket.send(packet);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public boolean isFriend(User user) {
        return friends.contains(user.getId());
    }

    public int getUserIdByName (String name) {
        User user = allUsers.get(name);
        if (user == null) return -1;
        return user.getId();
    }

    public int getSelfId () {
        return self.getId();
    }

    // 做好了
    public String askForCreateGroup(String groupName,int groupLevel,List<String> toInvites) {
        StringBuilder errMsg = new StringBuilder();
        List<Integer> userIds = new ArrayList<>();
        userIds.add(self.getId());
        for (String str : toInvites) {
            User user = allUsers.get(str);
            if (! isFriend(user)) {
                errMsg.append(user.getName()).append(" is not your friend, you can't invite he/she.");
            } else {
                userIds.add(user.getId());
            }
        }
        Group group = new Group(groupName, groupLevel, userIds);
        Proto askForNewGroup = Proto.getAskForNewGroup(group.askCreateStr());
        byte[] payload = askForNewGroup.toString().getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(payload, 0, payload.length, serverIP, serverPort);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return errMsg.toString();
    }

    // 做好了
    public String askForJoinGroup(String groupName,List<String> usernames) {
        StringBuilder errMsg = new StringBuilder(), toInvite = new StringBuilder();
        toInvite.append(getGroupIdByName(groupName)).append("|");
        boolean isFirst = true;
        for (int i = 0; i < usernames.size(); i++) {
            User user = allUsers.get(usernames.get(i));
            if (! isFriend(user)) {
                errMsg.append(user.getName()).append(" is not your friend, you can't invite he/she.\n");
            } else {
                toInvite.append(user.getId());
                if (!isFirst) toInvite.append(",");
                isFirst = false;
            }
        }
        Proto join = Proto.getAskToJoin(toInvite.toString());
        byte[] payload = join.toString().getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(payload, 0, payload.length, serverIP, serverPort);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return errMsg.toString();
    }

    // 做好了
    public String askToLeaveGroup(String groupName) {
        Group group = allGroups.get(groupName);
        if (group.isMember(getSelfId())) {
            return "You are not in the group";
        }
        StringBuilder msg = new StringBuilder();
        msg.append(getSelfId()).append("|").append(getGroupIdByName(groupName));
        Proto leave = Proto.getNotifyToLeave(msg.toString());
        byte[] payload = leave.toString().getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(payload, 0, payload.length, serverIP, serverPort);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public int getGroupIdByName (String name) {
        Group group = allGroups.get(name);
        if (group == null) return -1;
        return group.getGroupID();
    }

    // 做好了
    public void askForSynChatRecord () {
        for (String userName : chatRecordMap.keySet()) {
            ClientChatRecord record = chatRecordMap.get(userName);
            String data = record.getData();
            Proto synMsg = Proto.getSynMsg(data);
            byte[] payload = synMsg.toString().getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(payload, 0, payload.length, serverIP, serverPort);
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 做好了
    public List<Message> getRecordByName(String username) {
        ClientChatRecord chatRecord = chatRecordMap.get(username);
        if (chatRecord == null) {
            try {
                chatRecord = new ClientChatRecord(getUserIdByName(username));
                chatRecordMap.put(username,chatRecord);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return chatRecord.getMessages();
    }


}
