package msg;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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
    RandomAccessFile raf;

    // 根据UserId选择对应的文件, 然后从磁盘上读取对应的文件
    // Tips: 如果没有文件就创建一个文件
    public ClientChatRecord (int userId) throws IOException {
        this.userId = userId;
        String fileName = FILE_PREFIX + userId + ".txt";
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        messages = new ArrayList<>();
        raf = new RandomAccessFile(fileName,"rw");
        versionId = 0;
        try {
            versionId = raf.readInt() + 1;
            while (true)  {
                Message message = Message.readMsg(raf);
                messages.add(message);
            }
            } catch (EOFException e) {
                System.out.println("read done ...");
        }
    }

    public void writeRecord() {
        try {
            raf.setLength(0);
            raf.seek(0);
            raf.writeInt(1);
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


    @Override
    public void addMessages(Message message) {
        if(!messages.contains(message))
            messages.add(message);
    }

    public String getData() {
        // 版本号 | 用户id | msg1 | msg2 | msg3
        StringBuilder sb = new StringBuilder();
        sb.append(versionId).append("|");
        sb.append(userId).append("|");
        for (int i = 0; i < messages.size(); i++) {
            Message msg = messages.get(i);
            sb.append(msg);
            if (i != messages.size() - 1) sb.append("|");
        }
        return sb.toString();
    }


    public void  toReplace(int versionId, List<Message> messages) {
            this.versionId = versionId;
            this.messages = messages;
    }

    public void clear() {
        messages.clear();
        try {
          raf.setLength(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean didISentFirst() {
        if (messages.isEmpty()) return false;
        Message message = messages.get(0);
        return message.getSenderId() != userId;
    }

}

