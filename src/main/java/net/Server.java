package net;

import dto.User;
import java.io.RandomAccessFile;
import java.util.List;
import msg.ServerChatRecord;

public class Server {

    List<User> users; // 全部的用户

    ServerChatRecord serverChatRecord;

    RandomAccessFile chatFiles;

    // 接收请求，返回所有用户,并且判断用户是否登录
    public void getAllUsers(){
        // todo 调用数据库接口
    }

    // 接收登录请求，添加用户到登录集合中, 顺便发送所有积压的请求
    // PS: 积压的请求发送完之后需要从文件中删除
    public void acceptUser() {
        // todo 调用数据库接口
    }

    // 接收注册请求，添加用户到登录集合中
    public void createUser() {
        // todo 调用数据库请求
    }


    // 接收暂存消息请求，添加到消息队列中
    public void saveMsg() {

    }

    // 发送暂存消息请求，发送给每个
    public void pubMsg() {

    }

    public void flushMsg() {

    }

}
