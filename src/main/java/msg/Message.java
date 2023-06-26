package msg;


import java.io.*;
import java.util.Objects;

/**
 *  Message 一条消息
 */
public class Message implements Serializable {

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
    public static void writeMsg(Message msg, ObjectOutputStream outputStream) {
        try {
            outputStream.writeObject(msg);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Message readMsg(ObjectInputStream inputStream) {
        try {
            return (Message) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return senderId + "," + receiverId + "," + content + "," + time;
    }

    public String toDisplay () {
        return senderId + "  " + time + "\n" + content + "\n";
    }

    public Message marshall (String json) {
        String[] split = json.split(",");
        return null;
//        new Message(Integer.parseInt(split[0]),Integer.parseInt(split[1]),split[2],)
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
