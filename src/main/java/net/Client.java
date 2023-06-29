package net;

import dto.Group;
import dto.User;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import msg.ClientChatRecord;
import msg.GroupChatRecord;
import msg.Message;

public class Client implements Runnable {

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        Thread thread = new Thread(client);
        thread.start();
    }

    public static final int CLIENT_PORT = 8800; // 定义固定端口号

    InetAddress serverIP;
    int serverPort;

    HashSet<Integer> friends;

    HashMap<String,User> allUsers;

    HashMap<String,Group> allGroups;

    User self;

    HashMap<String,ClientChatRecord> chatRecordMap;

    HashMap<String, GroupChatRecord> groupChatRecordMap;

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
            return;
        }
        RandomAccessFile raf = new RandomAccessFile(friendFile, "rw");
        try {
            int num = raf.readInt();
            for (int i = 0; i < num; i++) {
                friends.add(raf.readInt());
            }
        } catch (IOException e){
            System.out.println("read friend ...");
        } finally {
            raf.close();
        }
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
        int port = Integer.parseInt(file.readLine());
        serverIP = InetAddress.getByName(ip);
        serverPort = port;
        System.out.println(serverPort);
    }

    public Client() throws IOException {
        this.socket = new DatagramSocket(CLIENT_PORT);
        this.isLogin = false;
        this.loginFailedMsg = null;
        friends = new HashSet<>();
        allGroups = new HashMap<>();
        allUsers = new HashMap<>();
        groupChatRecordMap = new HashMap<>();
        chatRecordMap = new HashMap<>();
        getServerAddrByConfig();
        readFriendRecord();
    }

    public HashSet<Integer> getFriends() {
        return friends;
    }

    public HashMap<String, User> getAllUsers() {
        return allUsers;
    }

    public HashMap<String, Group> getAllGroups() {
        return allGroups;
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

//        开始循环
        byte[] bytes = new byte[1024];
        while (true) {
            // 尝试获取包
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // 对包进行分类
            // type部分和message部分用‘,’分隔
            String payload = new String(bytes,0, packet.getLength());
            String[] dataString = payload.split(";", 2);
            String typeString = dataString[0];
            String messageString = dataString[1];

            // 接下来开始解析: type + content
            int type = Integer.parseInt(typeString);
            try {
                switch (type) {
                    case Proto.RespForAllUsers: {
                        System.out.println("收到了 RespForAllUsers " + messageString);
                        String[] blocks = messageString.split("\\|");
                        for (String block : blocks) {
                            String index[] = block.split(",");
                            // id , name , isOnline , ip
                            String ipStr = index[3].substring(index[3].indexOf("/") + 1);
                            User user = null;
                            try {
                                boolean isOnline = Boolean.parseBoolean(index[2]);
                                InetAddress ip = null;
                                if (isOnline) ip = InetAddress.getByName(ipStr);
                                user = new User(Integer.parseInt(index[0]),index[1],ip,isOnline);
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                            if (user != null)
                                allUsers.put(user.getName(), user);
                        }
                    }break;
                    case Proto.RespForLogin: {
                        System.out.println("收到了 RespForLogin " + messageString);
                        //boolean和string之间用‘,’分隔
                        String index[] = messageString.split(",");
                        boolean isLoginSuccess = Boolean.parseBoolean(index[0]);
                        if (isLoginSuccess) {
                            isLogin = true;
                            self.setId(Integer.parseInt(index[1]));
                        } else {
                            isLogin = false;
                            loginFailedMsg = index[1];
                        }
                    } break;
                    case Proto.PubSynMsg: {
                        System.out.println("收到了 PubSynMsg " + messageString);
                        String[] blocks = messageString.split("\\|");
                        int versionId = Integer.parseInt(blocks[0]);
                        int receiverId = Integer.parseInt(blocks[1]);
                        List<Message> msgs = new ArrayList<>();
                        for (int i = 2; i < blocks.length; i++)
                            msgs.add(Message.marshall(blocks[i]));
                        ClientChatRecord record = getRecordById(receiverId);
                        record.toReplace(versionId,msgs);
                    }break;
                    case Proto.AskForMakeFriend: {
                        System.out.println("收到了 AskForMakeFriend " + messageString);
                        String[] split = messageString.split("\\|");
                        int senderId = Integer.parseInt(split[0]);
                        Message msg = new Message(Integer.parseInt(split[0]), Integer.parseInt(split[1]), split[2], split[3]);
                        // todo 这里用户不一定会存在
                        ClientChatRecord record = chatRecordMap.get(getUserNameById(senderId));
                        if (record == null) {
                            try {
                                record = new ClientChatRecord(senderId);
                                record.addMessages(msg);
                                chatRecordMap.put(getUserNameById(senderId),record);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            record.addMessages(msg);
                        }
                    } break;
                    case Proto.RespForMakeFriend: {
                        System.out.println("收到了 RespForMakeFriend " + messageString);
                        String[] split = messageString.split("\\|");
                        int senderId = Integer.parseInt(split[0]);
                        boolean isAccepted = split[2].equals("Yes");
                        ClientChatRecord record = getRecordById(senderId);
                        // 如果已经同意了
                        if (isAccepted) {
                            friends.add(senderId);
                        }
                        Message message = new Message(senderId, getSelfId(), split[2], split[3]);
                        record.addMessages(message);
                    } break;
                    case Proto.NewMessage: {
                        System.out.println("收到了 NewMessage " + messageString);
                        String[] split = messageString.split("\\|");
                        int senderId = Integer.parseInt(split[0]);
                        Message msg = new Message(senderId, Integer.parseInt(split[1]), split[2], split[3]);
                        ClientChatRecord record = getRecordById(senderId);
                        record.addMessages(msg);
                    } break;
                    case Proto.BacklogMsg: {
                        System.out.println("收到了 BacklogMsg " + messageString);
                        String[] split = messageString.split("\\|");
                        for (int i = 0; i < split.length; i++) {
                            String temp = split[i];
                            if (temp.length() == 0) continue;
                            Message msg = Message.marshall(temp);
                            int senderId = msg.getSenderId();
                            // 如果我们之间是好友的话，直接走正常路径就好
                            ClientChatRecord record = getRecordById(senderId);
                            record.addMessages(msg);
                            // backLogMsg之中也可能有MakeFriend和RespMakeFriend的消息
                            String senderName = getUserNameById(senderId);
                            if (!isFriend(senderName)) {
                                // 不是好友 + record的size应该是
                                System.out.println("在 BacklogMsg 收到了 "+senderId+"的 MakeFriend相关的信息" + msg);
                                boolean iSentFirst = record.didISentFirst();
                                // 如果是我主动的话，那么这个msg是一个resp
                                if (iSentFirst) {
                                    if (msg.getContent().equals("Yes")) {
                                        friends.add(senderId);
                                    }
                                }
                                // 如果不是我主动的话，这个msg是一个req, 留给前端去处理了
                            }
                        }
                    } break;
                    case Proto.SynGroupMessages: {
                        System.out.println("收到了 SynGroupMessages:" + messageString);
                        String[] split = messageString.split("\\|");
                        GroupChatRecord record = groupChatRecordMap.get(split[0]);
                        Group group = allGroups.get(split[0]);
                        if (record == null){
                            try {
                                if (group == null) break;
                                record = new GroupChatRecord(group.getGroupID());
                                groupChatRecordMap.put(split[0],record);
                            } catch (IOException e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                        for (int i = 1; i < split.length; i++) {
                            Message marshall = Message.marshall(split[i]);
                            System.out.println("marshall msg   " + marshall);
                            record.addMessages(marshall);
                        }
                    } break;
                    case Proto.RespForAllGroups: {
                        System.out.println("收到了 RespForAllGroups:" + messageString);
                        String[] split = messageString.split("\\|");
                        //  group1 | group2 | group3 ...
                        // groupId | name | level | 1,2,3,4,5...
                        for (int i = 0; i < split.length; i += 4) {
                            String[] idsStr = split[i + 3].split(",");
                            List<Integer> ids = new ArrayList<>();
                            for (int j = 0; j < idsStr.length; j++) ids.add(Integer.parseInt(idsStr[j]));
                            Group group = new Group(Integer.parseInt(split[i]), split[i + 1], Group.getLevelByLimit(Integer.parseInt(split[i + 2])), ids);
                            System.out.println("resolved:  "+group);
                            allGroups.put(group.getName(),group);
                        }
                    } break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void synRecord () {
        askForSynChatRecord();
        for (String name : chatRecordMap.keySet()) {
            chatRecordMap.get(name).writeRecord();
        }
    }

    public ClientChatRecord getRecordById(int senderId) {
        ClientChatRecord record = chatRecordMap.get(getUserNameById(senderId));
        if (record == null) {
            try {
                record = new ClientChatRecord(senderId);
//                System.out.println(senderId + "has " +record.getMessages().size() + " records with you");
                chatRecordMap.put(getUserNameById(senderId),record);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return record;
    }

    public String getUserNameById(int id) {
        for (String name : allUsers.keySet()) {
            User user = allUsers.get(name);
            if (user.getId() == id) return name;
        }
        return null;
    }


    // 将一条消息发送给某个用户
    public void sendMsgToP(String msg,String name,boolean isRespForMakeFriend) throws IOException {
        // 登录了没有 + 是不是好友
        // 不是好友要发送make friend的消息，
        // 在线的话要发给对方，不在线的话要发给服务器
        // 服务器可以接收 make friend / resp make friend / save for msg 3种消息
        // 客户端在前端来选择要不要接受消息
        // 是好友的话同上
        User user = allUsers.get(name);
        Proto p;
        StringBuilder sb = new StringBuilder();
        Date date = new Date();
        if (isRespForMakeFriend) {
            sb.append(getSelfId()).append("|").append(user.getId()).append("|");
            if(msg.equals("Yes"))
                sb.append("Yes");
            else
                sb.append("No");
            sb.append("|").append(new Date());
            p = Proto.getRespForMakeFriend(sb.toString());
            byte[] payload = p.toString().getBytes(StandardCharsets.UTF_8);
            // 判断该往哪里发
            DatagramPacket packet ;
            if (user.isOnline()) {
                packet = new DatagramPacket(payload, 0, payload.length, user.getIp(), CLIENT_PORT);
            } else {
                packet = new DatagramPacket(payload, 0, payload.length, serverIP, serverPort);
            }
            socket.send(packet);
        } else {
            // 如果是好友的话:
            if (friends.contains(user.getId())) {
                sb.append(getSelfId()).append("|").append(user.getId()).append("|").append(msg).append("|").append(date);
                if (user.isOnline()){
                    p = Proto.getNewMessage(msg);
                }
                else{
                    p = Proto.getAskForSaveMsg(sb.toString());
                }
            } else {
                sb.append(getSelfId()).append("|").append(user.getId()).append("|").append(msg).append("|").append(date);
                p = Proto.getAskForMakeFriend(sb.toString());
            }
            byte[] payload = p.toString().getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet ;
            if (user.isOnline()) {
                packet = new DatagramPacket(payload,0,payload.length,user.getIp(),CLIENT_PORT);
            } else {
                packet = new DatagramPacket(payload,0,payload.length,serverIP,serverPort);
            }
            socket.send(packet);
        }
        // todo 积压消息实现了吗
    }


    // 将一条消息发送给群组
    public void sendMsgToG(String msg,String name) {
        Group group = allGroups.get(name);
        boolean isMember = group.isMember(getSelfId());
        if (!isMember) {
            return ;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getSelfId()).append("|").append(name).append("|").append(msg).append("|").append(new Date());
        System.out.println("Send Group Msg   " + sb);
        Proto groupMessage = Proto.getNewGroupMessage(sb.toString());
        byte[] payload = groupMessage.toString().getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(payload, 0, payload.length, serverIP, serverPort);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void askToLogOut () {
        Proto groupMessage = Proto.getAskForLogout(String.valueOf(getSelfId()));
        byte[] payload = groupMessage.toString().getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(payload, 0, payload.length, serverIP, serverPort);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addSelfMessageToRecord(String msg,String receiver) {
        User user = allUsers.get(receiver);
        ClientChatRecord record = getRecordById(user.getId());
        Message message = new Message(getSelfId(), user.getId(), msg, new Date().toString());
        record.addMessages(message);
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


    public void clearRecord(String name) {
        ClientChatRecord record = chatRecordMap.get(name);
        if (record == null) return;
        record.clear();
    }

    public void addFriend (String name) {
        User user = allUsers.get(name);
        friends.add(user.getId());
    }

    public boolean isFriend(User user) {
        return friends.contains(user.getId());
    }

    public boolean isFriend(String userName) {
        User user = allUsers.get(userName);
        if (user == null) return false;
        return friends.contains(user.getId());
    }

    public boolean isMember(String groupName) {
        Group group = allGroups.get(groupName);
        if (group == null) return false;
        return group.isMember(getSelfId());
    }

    public int getUserIdByName (String name) {
        User user = allUsers.get(name);
        if (user == null) {
//            System.out.println("There is no user named : " + name);
            return -1;
        }
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
                if (!isFirst) toInvite.append(",");
                toInvite.append(user.getId());
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
        if (!group.isMember(getSelfId())) {
            return "You are not in the group";
        }
        StringBuilder msg = new StringBuilder();
        msg.append(getSelfId()).append("|").append(groupName);
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
            String data = getSelfId() + "|"  +record.getData();
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
                int id = getUserIdByName(username);
                if (id == -1) return null;
                chatRecord = new ClientChatRecord(id);
                chatRecordMap.put(username,chatRecord);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return chatRecord.getMessages();
    }

    public boolean didISentFirst(String username) {
        ClientChatRecord chatRecord = chatRecordMap.get(username);
        if (chatRecord == null) {
            try {
                int id = getUserIdByName(username);
                if (id == -1) return false;
                chatRecord = new ClientChatRecord(id);
                chatRecordMap.put(username,chatRecord);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return chatRecord.didISentFirst();
    }

    public List<Message> getGroupRecordByName (String groupName) {
        GroupChatRecord record = groupChatRecordMap.get(groupName);
        if (record == null) {
            try {
                Group group = allGroups.get(groupName);
                record = new GroupChatRecord(group.getGroupID());
                groupChatRecordMap.put(groupName,record);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return record.getMessages();
    }


}
