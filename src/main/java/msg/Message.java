package msg;


import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 *  Message 一条消息
 */
public class Message implements Serializable {

    public static final byte[] globalIdLock = new byte[0];
    public static int globalId = 0;

    private int id; // 消息的id，对于一个Client自己来说是全局唯一的

    private int senderId; // 发送人的id
    private int receiverId; // 接收者的id
    private String content; // 消息的内容

    private String time; // 消息发送的时间

    public Message(int senderId, int receiverId, String content,String time) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.time = time;
        synchronized (globalIdLock) {
            globalId ++;
            this.id = globalId;
        }
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
    public static void writeMsg(Message msg, OutputStream outputStream){
        // TODO: 俞李文澜
    }

    // 从指定文件中读出一条消息
    public static Message readMsg(InputStream inputStream){
        // TODO: 俞李文澜
    }

}
