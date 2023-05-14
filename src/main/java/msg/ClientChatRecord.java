package msg;

import java.io.RandomAccessFile;
import java.util.List;

/**
 *  一个ChatRecord是 "我"和另外一个用户的聊天记录
 */
public class ClientChatRecord extends ChatRecord{
    int userId;  // 对方的id
    RandomAccessFile chatFile; // 一个file，用来持久化消息记录, 文件名是 "chat_record_{userId}"，这里userId就是对方的id
    List<Message> messages; // 一个消息记录的集合，包含了 "我"和对方的所有消息

    static final String FILE_PREFIX = "chat_record_"; // 磁盘上的文件名的前缀


    // 根据UserId选择对应的文件, 然后从磁盘上读取对应的文件
    // Tips: 如果没有文件就创建一个文件
    public ClientChatRecord (int userId){
        // TODO: 俞李文澜

    }

    // 将所有的消息记录写到文件
    @Override
    public void writeRecord(){
        // TODO: 俞李文澜
    }

    @Override
    public List<Message> getMessages() {
        return messages;
    }
}
