package msg;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *  一个ChatRecord是 "我"和另外一个用户的聊天记录
 */
public class ClientChatRecord extends ChatRecord {

    int versionId;

    int userId;  // 对方的id

    List<Message> messages; // 一个消息记录的集合，包含了 "我"和对方的所有消息

    static final String FILE_PREFIX = System.getProperty("user.dir") + "\\"+ "chat_record_"; // 磁盘上的文件名的前缀

    // 一个file，用来持久化消息记录, 文件名是 "chat_record_{userId}"，这里userId就是对方的id
    ObjectOutputStream writer;
    ObjectInputStream reader;

    String data;

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
            versionId = reader.readInt();
            Message message;
                while ( (message = Message.readMsg(reader)) != null) {
                    messages.add(message);
                }
                reader.close();
            } catch (EOFException e) {
                System.out.println("creat file...");
            }
            writer = new ObjectOutputStream(new FileOutputStream(fileName,false));
    }

    public void writeRecord() {
        try {
            writer.writeInt(versionId);
        } catch (IOException e){
            e.printStackTrace();
            return;
        }
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

    public String getData() {
        // 版本号 | 用户id | msg1 | msg2 | msg3
        StringBuilder sb = new StringBuilder();
        sb.append(versionId).append("|");
        sb.append(userId).append("|");
        for (int i = 0; i < messages.size(); i++) {
            sb.append(messages);
            if (i != messages.size() - 1) sb.append("|");
        }
        return sb.toString();
    }


    public ClientChatRecord (String data) {
        // todo 从二进制数据创建一个Record

    }

}

