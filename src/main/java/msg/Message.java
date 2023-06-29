package msg;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 *  Message 一条消息
 */
public class Message {

    private int senderId; // 发送人的id
    private int receiverId; // 接收者的id
    private String content; // 消息的内容

    private String time; // 消息发送的时间

    public Message(int senderId, int receiverId, String content, String time) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    // 将一条消息写入到指定文件中
    public static void writeMsg(Message msg, RandomAccessFile raf) {
        try {
            raf.writeInt(msg.senderId);
            raf.writeInt(msg.receiverId);
            byte[] bytes = msg.getContent().getBytes(StandardCharsets.UTF_8);
            raf.writeInt(bytes.length);
            raf.write(bytes);
            byte[] bytes1 = msg.getTime().getBytes(StandardCharsets.UTF_8);
            raf.writeInt(bytes1.length);
            raf.write(bytes1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Message readMsg(RandomAccessFile raf) throws IOException {
            int senderId = raf.readInt();
            int receiverId = raf.readInt();
            int contentLen = raf.readInt();
            byte[] contentBytes = new byte[contentLen];
            raf.read(contentBytes);
            String content = new String(contentBytes);
            int timeLen = raf.readInt();
            byte[] timeBytes = new byte[timeLen];
            raf.read(timeBytes);
            String time = new String(timeBytes);
            return new Message(senderId, receiverId, content, time);
    }

    @Override
    public String toString() {
        return senderId + "," + receiverId + "," + content + "," + time;
    }

    public String toDisplay () {
        return senderId + "  " + time + "\n" + content + "\n";
    }

    public static Message marshall (String json) {
        String[] split = json.split(",");
        return new Message(Integer.parseInt(split[0]),Integer.parseInt(split[1]),split[2],split[3]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Message message = (Message) o;
        return senderId == message.senderId && receiverId == message.receiverId && Objects.equals(content, message.content) && Objects.equals(time, message.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderId, receiverId, content, time);
    }
}
