package msg;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ServerChatRecord extends ChatRecord {

    public static final String filename = System.getProperty("user.dir") + "\\" + "server_chat_record";

    List<Message> messages; // 存储积压的消息;

    // 单例
    static ServerChatRecord serverChatRecord;

    ObjectOutputStream writer;
    ObjectInputStream reader;

    // 初始化 serverChatRecord
    static {
        serverChatRecord = new ServerChatRecord();
    }

    public static ServerChatRecord getServerChatRecord() {
        return serverChatRecord;
    }

    // 在server端 加载/创建 记录文件
    private ServerChatRecord() {
        try {
            File file = new File(filename);
            if (! file.exists()) {
                file.createNewFile();
            }
            messages = new ArrayList<>();
            try {
                reader = new ObjectInputStream(new FileInputStream(filename));
                while (reader.available() > 0) {
                    messages.add(Message.readMsg(reader));
                }
                reader.close();
            } catch (EOFException e) {

            }
            writer = new ObjectOutputStream(new FileOutputStream(filename,true));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void writeRecord() {
        for (Message msg : messages) {
            Message.writeMsg(msg, writer);
        }
    }


    public List<Message> getMessages() {
        return messages;
    }

    public void addMessages(Message message) {
        messages.add(message);
    }

    public List<Message> getAllMsgByUserId (int receiverId) {
        List<Message> res = new ArrayList<>();
        for (Message msg : messages) {
            if (msg.getReceiverId() == receiverId) {
                res.add(msg);
            }
        }
        messages.removeAll(res);
        return res;
    }

}