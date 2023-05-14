package net;

import java.net.InetAddress;
import java.util.HashMap;

/**
 *  全局类，只有一个实例，负责记录有哪些用户已经登录了，用户登录了会有ip记录
 */
public class LoginStatus {

    // 一个哈希表，记录每个用户对应的ip地址
    private static final HashMap<Integer, InetAddress> userId2Ipaddr = new HashMap<>();

    // 用户登录的时候，添加一条映射
    public static void addUser(int userId,InetAddress inetAddress){
        // TODO :
    }

    // 用户下线后，移除映射
    public static void removeUser(int userId){
        // TODO :
    }

    // 根据userId获取用户的ip地址，如果没有登录或者没有用户返回null
    public static InetAddress getIP(int userId){
        // TODO :
    }

    // 返回某个用户是否登录
    public static boolean isOnline(int userId){
        // TODO :
    }

}
