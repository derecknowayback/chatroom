package msg;

import java.io.RandomAccessFile;
import java.util.List;

public class ServerChatRecord extends ChatRecord{

    RandomAccessFile file; // 一个file，用来持久化消息

    List<Message> messages; // 存储积压的消息;

    // 单例
    static ServerChatRecord serverChatRecord;

    // 初始化 serverChatRecord
    static {
        serverChatRecord = new ServerChatRecord();
    }

    public static ServerChatRecord getServerChatRecord(){
        return serverChatRecord;
    }


    // 在server端 加载/创建 记录文件
    private ServerChatRecord(){
        // TODO: 俞李文澜
    }

    @Override
    public void writeRecord() {
        // TODO: 俞李文澜
    }

    @Override
    public List<Message> getMessages() {
        // TODO: 俞李文澜
    }
}
