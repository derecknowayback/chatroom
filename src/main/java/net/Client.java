package net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import msg.Message;

public class Client implements Runnable{

    static final int CLIENT_PORT = 8800; // 定义固定端口号

    InetAddress address;

    Scanner scanner;


    public Client() throws UnknownHostException {
        this.address = InetAddress.getByName("localhost"); // 定义服务器的地址、端口号、数据
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run() {

    }

    // 将一条消息发送给某个用户
    public void sendMsg(Message msg,int userId){
        // if 如果用户没有登录
            // sendMsgToS
        // else 如果用户登录了
            // sendMsgToP
    }

    // 当某个用户在线的时候，将一条消息直接发送给某个用户
    private void sendMsgToP(Message msg,int userId){
        // TODO: 孟洲
        /*
            Need socket for communication
            Socket takes two input arguments to initialize, IP address and port number
            IP is "localhost" and port is 8800
            
            Steps:
            1. Intialize socket
            2. Fill in required info in message
            3. Use writeMsg() method to serialize message to output
            4. Send message
            
       
            
        */
        //msg.setSenderID();
        msg.setReceiverID(userID);
        //msg.setTime();                      //?
        Socket socket = new Socket("localhost", 8800);               //initializing socket
        
        OutputStream output = socket.getOutputStream();
        
        msg.writeMsg(msg, output);              //?
        
        ObjectOutputStream o_output = new ObjectOutputStream(output);
        objectOutputStream.writeObject(msg);
      
        socket.close();
      
        
    }

    // 当某个用户不在线的时候，将一条消息发送给Server
    private void sendMsgToS(Message msg,int userId){
        /*
            need server ip, port
            Same steps, just (probably) different ip and port
            
            can't do this yet since server class is not implemented
        */
        
        
    }

    // 我们需要反序列化出对应的Message
    public Message recvMsg(){
        
        
    }
}
