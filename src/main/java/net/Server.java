package net;

import dto.DBManager;
import dto.Group;
import dto.User;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import msg.ClientChatRecord;
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

        int refreshOnlineUser = 0;
        byte[] bytes = new byte[1024];
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
        while (true) {
            try {
                socket.receive(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            byte[] data = packet.getData();
            String payload = new String(data);
            String[] dataString = payload.split(",", 2);
            String typeString = dataString[0];
            String messageString = dataString[1];





        }
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

    public void createGroup (String name,int level,List<Integer> memberIds) {
        DBManager.createGroup(Group.getLimitByLevel(level), name, memberIds);

    }


    public void removeFromGroup (int userId) {
        // todo 将成员从群组中移除

    }



    public void flushMsg() {
        serverChatRecord.writeRecord();
    }


    // 现在开始处理一下用户记录的事情

    public void handleClientChatRecord (int senderId,String data)  {
        // 文件名: sendId_recvId
        // 格式: 版本号 + 消息记录数 + 消息记录
        String[] split = data.split("\\|");
        int versionId = Integer.parseInt(split[0]);
        int receiverId = Integer.parseInt(split[1]);
        int oldId = getRecordVersionId(senderId, receiverId);
        // 如果Server的版本大于现有的版本，不存
        if (oldId > versionId) return;
        // todo 这里不确定是否有消息，这样做是否会触发空指针
        String fileName = System.getProperty("user.dir") + "/"+ senderId + "_" + receiverId;
        ObjectOutputStream outputStream = null;
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(fileName, false));
            outputStream.writeInt(versionId); // 写版本号
            outputStream.writeInt(split.length - 2); // 写长度
            for (int i = 2; i < split.length; i++) {
                Message message = Message.marshall(split[i]);
                outputStream.writeObject(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
    public void loadRecord(int userId) {
        // 先获取
        String workDir = System.getProperty("user.dir");
        File file = new File(workDir);
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File temp = files[i];
            String fileName = temp.getName();
            if (fileName.startsWith(String.valueOf(userId))) {
                ObjectInputStream stream = null;
                try {
                    StringBuilder sb = new StringBuilder();
                    int receiverId = Integer.parseInt(fileName.substring(fileName.indexOf("_") + 1));
                    stream = new ObjectInputStream(new FileInputStream(fileName));
                    int versionId = stream.readInt();
                    // todo 这里最后也append一个 | , 可能会制空
                    sb.append(versionId).append("|").append(receiverId).append("|");
                    int cnt = stream.readInt();
                    for (int j = 0; j < cnt; j++) {
                        Message msg = (Message)stream.readObject();
                        sb.append(msg);
                        if (j != cnt - 1) sb.append("|");
                    }
                    // 开始发送
                    Proto pubSynMsg = Proto.getPubSynMsg(sb.toString());
                    byte[] payload = pubSynMsg.toString().getBytes(StandardCharsets.UTF_8);
                    DatagramPacket packet = new DatagramPacket(payload, 0, payload.length, LoginStatus.getIP(userId), Client.CLIENT_PORT);
                    socket.send(packet);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


}
