package msg;

import java.util.List;

public abstract class ChatRecord {
    // 将所有的消息记录写到文件
    public abstract void writeRecord();

    public abstract List<Message> getMessages();

    public abstract void addMessages(Message message);
}