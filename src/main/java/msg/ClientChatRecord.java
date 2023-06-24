package msg;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

/**
 *  一个ChatRecord是 "我"和另外一个用户的聊天记录
 */
public class ClientChatRecord extends ChatRecord {

    int userId;  // 对方的id

    List<Message> messages; // 一个消息记录的集合，包含了 "我"和对方的所有消息

    static final String FILE_PREFIX = System.getProperty("user.dir") + "\\"+ "chat_record_"; // 磁盘上的文件名的前缀

    // 一个file，用来持久化消息记录, 文件名是 "chat_record_{userId}"，这里userId就是对方的id
    ObjectOutputStream writer;
    ObjectInputStream reader;


    // 根据UserId选择对应的文件, 然后从磁盘上读取对应的文件
    // Tips: 如果没有文件就创建一个文件
    public ClientChatRecord (int userId) throws IOException {
        this.userId = userId;
        String fileName = FILE_PREFIX + userId;
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        messages = new ArrayList<>();
        FileInputStream stream = new FileInputStream(fileName);
        try {
                reader = new ObjectInputStream(stream);
                Message message;
                while ( (message = Message.readMsg(reader)) != null) {
                    messages.add(message);
                }
                reader.close();
            } catch (EOFException e) {
                System.out.println("creat file...");
            }
            writer = new ObjectOutputStream(new FileOutputStream(fileName,true));
    }

    public void writeRecord() {
        for (Message msg : messages) {
            Message.writeMsg(msg, writer);
        }
    }


    public List<Message> getMessages() {
        return messages;
    }


    @Override
    public void addMessages(Message message) {
        messages.add(message);
    }
}

