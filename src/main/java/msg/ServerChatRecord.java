package msg;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class ServerChatRecord extends ChatRecord {

    public static final String filename = System.getProperty("user.dir") + "\\" + "server_chat_record" + ".txt";

    List<Message> messages; // 存储积压的消息;

    // 单例
    static ServerChatRecord serverChatRecord;

    RandomAccessFile raf;

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
            raf = new RandomAccessFile(filename,"rw");
            while (true) {
                messages.add(Message.readMsg(raf));
            }
        } catch (IOException e) {
            System.out.println("read done ...");
        }
    }

    public void writeRecord() {
        try {
            raf.setLength(0);
            raf.seek(0);
        } catch (IOException e){
            e.printStackTrace();
            return;
        }
        for (Message msg : messages) {
            Message.writeMsg(msg, raf);
        }
        try {
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
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