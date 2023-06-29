package net;

import dto.DBManager;
import dto.Group;
import dto.User;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import msg.GroupChatRecord;
import msg.Message;
import msg.ServerChatRecord;

public class Server implements Runnable {

    HashMap<Integer,User> users; // 全部的用户

    ServerChatRecord serverChatRecord;

    HashMap<String, GroupChatRecord> groupChatRecordMap;

   HashMap<String, Group> groupHashMap;

   DatagramSocket socket;

   public static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws SocketException {
        Server server = new Server();
        Thread thread = new Thread(server);
        thread.start();
    }

    public Server () throws SocketException {
        serverChatRecord = ServerChatRecord.getServerChatRecord();
        groupChatRecordMap = new HashMap<>();
        groupHashMap = new HashMap<>();
        socket = new DatagramSocket(DEFAULT_PORT);
        users = new HashMap<>();
        List<User> allUsers = DBManager.findAllUsers();
        for (int i = 0; i < allUsers.size(); i++) {
            users.put(allUsers.get(i).getId(),allUsers.get(i));
        }
        List<Group> groups = DBManager.getAllGroups();
        for (int i = 0; i < groups.size(); i++) {
            Group group = groups.get(i);
            System.out.println(group);
            groupHashMap.put(group.getName(),group);
        }
    }

    @Override
    public void run() {
        byte[] bytes = new byte[1024];
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
                try {
                    socket.receive(packet);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                InetAddress address = packet.getAddress();
                String payload = new String(bytes,0, packet.getLength());
                System.out.println(payload);
                String[] dataString = payload.split(";", 2);
                int type = Integer.parseInt(dataString[0]);
                String message= dataString[1];
                switch (type) {
                    case Proto.AskForLogin: {
                        System.out.println("收到了 AskForLogin " + message);
                        String[] split = message.split(",");
                        acceptUser(split[0],split[1],address);
                    } break;
                    case Proto.AskForRegister: {
                        System.out.println("收到了 AskForRegister " + message);
                        String[] split = message.split(",");
                        createUser(split[0],split[1],address );
                    } break;
                    case Proto.AskForSaveMsg: {
                        System.out.println("收到了 AskForSaveMsgr " + message);
                        String[] split = message.split("\\|");
                        Message msg = new Message(Integer.parseInt(split[0]), Integer.parseInt(split[1]), split[2], split[3]);
                        serverChatRecord.addMessages(msg);
                    } break;
                    case Proto.SynMsg: {
                        System.out.println("收到了 SynMsg " + message);
                        String[] split = message.split("\\|", 2);
                        handleSynMsg(Integer.parseInt(split[0]),split[1]);
                    } break;
                    case Proto.AskForMakeFriend:
                    case Proto.RespForMakeFriend: {
                        System.out.println("收到了 AskForMakeFriend | RespForMakeFriend  " + message);
                        String[] split = message.split("\\|",4);
                        Message msg = new Message(Integer.parseInt(split[0]), Integer.parseInt(split[1]), split[2], split[3]);
                        serverChatRecord.addMessages(msg);
                    } break;
                    case Proto.AskForNewGroup: {
                        System.out.println("收到了 AskForNewGroup  " + message);
                        String[] split = message.split("\\|", 3);
                        String[] idStrs = split[2].split(",");
                        List<Integer> ids = new ArrayList<>();
                        for (String str : idStrs)
                            ids.add(Integer.parseInt(str));
                        createGroup(split[0],Integer.parseInt(split[1]),ids);
                        broadcastGroupsInfo();
                    } break;
                    case Proto.AskToJoinGroup: {
                        System.out.println("收到了 AskToJoinGroup  " + message);
                        String[] split = message.split("\\|", 2);
                        String[] idStrs = split[1].split(",");
                        for (String str : idStrs){
                            int id = Integer.parseInt(str);
                            System.out.println(id +" join " + split[0]);
                            boolean b = joinGroup(split[0], id);
                            System.out.println("Join res" + b);
                            Group group = groupHashMap.get(split[0]);
                            group.addMember(id);
                            broadcastGroupsInfo();
                            broadcastGroupMessage(split[0]);
                        }
                    } break;
                    case Proto.AskToLeave: {
                        System.out.println("收到了 AskToLeave  " + message);
                        String[] split = message.split("\\|", 2);
                        //message: sender_id | group_name
                        removeFromGroup(split[1],Integer.parseInt(split[0]));
                        Group group = groupHashMap.get(split[1]);
                        group.removeUser(Integer.parseInt(split[0]));
                        broadcastGroupsInfo();
                    } break;
                    case Proto.NewGroupMessage: {
                        System.out.println("收到了 NewGroupMessage  " + message);
                        String[] split = message.split("\\|", 4);
                        GroupChatRecord record = groupChatRecordMap.get(split[1]);
                        Group group = groupHashMap.get(split[1]);
                        if (record == null) {
                            try {
                                record = new GroupChatRecord(group.getGroupID());
                            } catch (IOException e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                        Message msg = new Message(Integer.parseInt(split[0]), 0, split[2], split[3]);
                        record.addMessages(msg);
                        groupChatRecordMap.put(split[1],record);
                        broadcastGroupMessage(split[1]);
                    } break;
                    case Proto.AskToLogOut: {
                        System.out.println("收到了 AskToLogOut  " + message);
                        int userId = Integer.parseInt(message);
                        User user = users.get(userId);
                        user.setOnline(false);
                        LoginStatus.removeUser(userId);
                        broadcastAllUsers();
                    } break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void broadcastGroupsInfo() {
        for (int userId : users.keySet()) {
            User user = users.get(userId);
            if (user.isOnline()) {
                pubGroupInfo(user.getIp());
            }
        }
    }

    public void pubGroupInfo (InetAddress address) {
        StringBuilder sb = new StringBuilder();
        for (String name : groupHashMap.keySet()) {
            Group group = groupHashMap.get(name);
            System.out.println("pub group " + group);
            sb.append(group).append("|");
        }
        Proto groups = Proto.getRespForAllGroups(sb.toString());
        byte[] payload = groups.toString().getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(payload, 0, payload.length, address, Client.CLIENT_PORT);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastAllGroupMessage () {
        for (String groupName : groupHashMap.keySet()) {
            broadcastGroupMessage(groupName);
        }
    }

    public void broadcastGroupMessage (String groupName) {
        Group group = groupHashMap.get(groupName);
        for (int userId : users.keySet()) {
            User user = users.get(userId);
            // 用户在线 && 用户是小组成员
            if (user.isOnline() && group.isMember(userId)) {
                pubGroupMessage(groupName,user.getIp());
            }
        }
    }

    public void pubGroupMessage (String name,InetAddress address) {
        Group group = groupHashMap.get(name);
        GroupChatRecord record = groupChatRecordMap.get(name);
        if (record == null) {
            try {
                record = new GroupChatRecord(group.getGroupID());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        StringBuilder sb = new StringBuilder();
        List<Message> messages = record.getMessages();
        System.out.println(name + "  has " + messages.size() + "  records");
        sb.append(name).append("|");
        for (int i = 0; i < messages.size(); i++) {
            sb.append(messages.get(i)).append("|");
        }
        Proto synGroupMessages = Proto.getSynGroupMessages(sb.toString());
        byte[] payload = synGroupMessages.toString().getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(payload, 0, payload.length, address, Client.CLIENT_PORT);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // 接收登录请求，添加用户到登录集合中, 顺便发送所有积压的请求
    // PS: 积压的请求发送完之后需要从文件中删除
    public void acceptUser(String name, String password,InetAddress inetAddress) throws UnknownHostException {
        String msg = DBManager.checkUser(name, password);
        String[] split = msg.split(",");
        if (split[0].equals("true")) {
            // 登录成功
            int userId = Integer.parseInt(split[1]);
            // 添加到登录用户中
            if (name.equals("tim") || name.equals("dim")) {
                inetAddress = InetAddress.getByName("10.28.245.221");
            }
            User user = new User(Integer.parseInt(split[1]), name, inetAddress, true);
            LoginStatus.addUser(user);
            users.put(user.getId(),user);
            // 推送所有的用户
            broadcastAllUsers();
            // 推送所有群组
            broadcastGroupsInfo();
            // 推送积压的消息
            pubMsg(userId);
            // 发送消息记录

            // 发送全部的群组消息
            broadcastAllGroupMessage();
            try {
                loadRecord(userId);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
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
            users.put(user.getId(), user);
            // 推送所有的用户
            broadcastAllUsers();
            // 推送所有的群组信息
            broadcastGroupsInfo();
        }
        Proto resp = Proto.getRespForLogin(msg);
        byte[] payload = resp.toString().getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(payload, 0, payload.length, inetAddress, Client.CLIENT_PORT);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 发送暂存消息请求
    public void pubMsg(int userId) {
        InetAddress ip = LoginStatus.getIP(userId);
        List<Message> toPub = serverChatRecord.getAllMsgByUserId(userId);
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < toPub.size(); i++) {
            content.append(toPub.get(i)).append("|");
        }
//        System.out.println("准备发送给" + userId + "  :" + content);
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


    public void broadcastAllUsers () {
        for (int userId : users.keySet()) {
            User user = users.get(userId);
            if (user.isOnline()) {
                System.out.println(user.getName() + "在线");
                pubAllUsersToOne(userId);
            }
        }
    }

    public void pubAllUsersToOne (int userId) {
        InetAddress inetAddress = LoginStatus.getIP(userId);
        StringBuilder builder = new StringBuilder();
        for (int uid : users.keySet()) {
            User temp = users.get(uid);
            if (LoginStatus.isOnline(temp.getId()))
                temp.setOnline(true);
            builder.append(temp).append("|");
        }
        System.out.println(builder);
        Proto pub = Proto.getRespForAllUsers(builder.toString());
        byte[] payload = pub.toString().getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(payload, 0, payload.length, inetAddress, Client.CLIENT_PORT);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createGroup (String name,int level,List<Integer> memberIds) {
        int groupId = DBManager.createGroup(Group.getLimitByLevel(level), name, memberIds);
        Group group = new Group(groupId, name,level, memberIds);
        groupHashMap.put(name,group);
    }

    public boolean joinGroup(String groupName,int userId) {
        Group group = groupHashMap.get(groupName);
        if (group == null)  return false;
        group.addMember(userId);
        return DBManager.addUserToGroup(group.getGroupID(),userId);
    }



    public boolean removeFromGroup (String groupName,int userId) {
        Group group = groupHashMap.get(groupName);
        if (group == null)  return false;
        group.removeUser(userId);
        return DBManager.removeUserFromGroup(group.getGroupID(),userId);
    }



    public void flushMsg() {
        serverChatRecord.writeRecord();
    }


    // 现在开始处理一下用户记录的事情
    public void handleSynMsg (int senderId,String data) throws IOException {
        // 文件名: sendId_recvId
        // 格式: 版本号 | 消息记录数 | 消息记录
        String[] split = data.split("\\|");
        int versionId = Integer.parseInt(split[0]);
        int receiverId = Integer.parseInt(split[1]);
        int oldId = getRecordVersionId(senderId, receiverId);
        // 如果Server的版本大于现有的版本，不存
        if (oldId > versionId) return;
        if (split.length <= 2 || split[2].length() == 0) return;
        // todo 这里不确定是否有消息，这样做是否会触发空指针
        String fileName = System.getProperty("user.dir") + "/"+ senderId + "_" + receiverId;
        RandomAccessFile raf = new RandomAccessFile(fileName,"rw");
        try {
            raf.writeInt(versionId); // 写版本号
            raf.writeInt(split.length - 2); // 写长度
            for (int i = 2; i < split.length; i++) {
                Message message = Message.marshall(split[i]);
                Message.writeMsg(message,raf);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            raf.close();
        }
    }

    public int getRecordVersionId (int sendId,int recvId) {
        String fileName = System.getProperty("user.dir") + "/" + sendId + "_" + recvId;
        File f = new File(fileName);
        if (!f.exists()) {
            try {
                f.createNewFile();
                return -1;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(f, "r");
            int versionId = randomAccessFile.readInt();
            return versionId;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (randomAccessFile != null)
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return -1;
    }

    // 从磁盘上加载记录文件, 做好了
    public void loadRecord(int userId) throws FileNotFoundException {
        // 先获取
        String workDir = System.getProperty("user.dir");
        File file = new File(workDir);
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File temp = files[i];
            String fileName = temp.getName();
            if (fileName.startsWith(String.valueOf(userId))) {
                RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
                try {
                    StringBuilder sb = new StringBuilder();
                    int receiverId = Integer.parseInt(fileName.substring(fileName.indexOf("_") + 1));
                    int versionId = raf.readInt();
                    sb.append(versionId).append("|").append(receiverId).append("|");
                    int cnt = raf.readInt();
                    for (int j = 0; j < cnt; j++) {
                        Message msg = Message.readMsg(raf);
                        sb.append(msg);
                        if (j != cnt - 1) sb.append("|");
                    }
                    raf.close();
                    // 开始发送
                    Proto pubSynMsg = Proto.getPubSynMsg(sb.toString());
                    byte[] payload = pubSynMsg.toString().getBytes(StandardCharsets.UTF_8);
                    DatagramPacket packet = new DatagramPacket(payload, 0, payload.length, LoginStatus.getIP(userId), Client.CLIENT_PORT);
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
