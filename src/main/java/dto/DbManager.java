package com.main/java.dto;

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

//JDBC工具类
public class DbManager {
    private static String url;
    private static String user;
    private static String password;
    private static String driver;
    //静态代码块
    static {
        //读取资源文件，获取值
        try {
            //1.创建Properties集合类
            Properties pro =new Properties();
            //2.加载文件
            //这里一直找不到配置文件，就用绝对路径了
            pro.load(new FileReader("C:\\Users\\86186\\IdeaProjects\\jdbc\\jdbc-demo\\src\\com\\jdbc/database.properties"));

            //3.获取数据，赋值
            url = pro.getProperty("url");
            user = pro.getProperty("user");
            password = pro.getProperty("password");
            driver = pro.getProperty("driver");

            //4.注册驱动
            Class.forName(driver);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //获取连接的工具方法
    //返回连接对象
    public static Connection getConnection() throws SQLException {

        return DriverManager.getConnection(url,user,password);
    }

    //释放资源
    public static void close(Statement stemt, Connection conn){
        if(stemt!=null){
            try {
                stemt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(conn!=null){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(ResultSet rs, Statement stemt, Connection conn){
        if(rs!=null){
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if(stemt!=null){
            try {
                stemt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if(conn!=null){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 用于用于判断用户名和密码是否正确
     * @param name 用户名
     * @param password 密码
     * @return
     */
    public static boolean checkUser(String name,String password)
    {
        if (name==null||password==null){
            return false;
        }
        Connection conn=null;
        Statement stmt=null;
        ResultSet rs=null;
        try {
            conn = DbManager.getConnection();//工具类连接
            stmt = conn.createStatement();
            String sql = "select * from tb_user where username='"+name+"' and password='"+password+"'";
            rs = stmt.executeQuery(sql);
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            DbManager.close(rs,stmt,conn);//工具类释放资源
        }
        return false;
    }

    /**
     * 用于查询单个用户 输入id 返回对象
     * @param findid
     * @return user 返回对象
     */
    public static List<User> findById(int findid)
    {
        Connection conn=null;
        Statement stmt=null;
        ResultSet rs=null;
        try {
            conn = DbManager.getConnection();//工具类连接
            stmt = conn.createStatement();
            String sql = "select * from tb_user";
            rs = stmt.executeQuery(sql);
            int i, isfind = 1;
            for(i = 0; i < findid ; i++)
            {
                if(!rs.next())
                {
                    isfind = 0;
                    break;
                }
            }
            if(isfind == 1 && i > 0)
            {
                String name = rs.getString("username");
                System.out.println(name);
                User user = null;
                List<User> users = new ArrayList<>();
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                //封装user对象
                user = new User();
                user.setId(id);
                user.setUsername(username);
                user.setPassword(password);
                //装载集合
                users.add(user);
                return users;
            }else{
                System.out.println("不存在该用户！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            DbManager.close(rs,stmt,conn);//工具类释放资源
        }
        return null;
    }

    /**
     * 用于插入新用户 返回true插入成功
     * @param name
     * @param password
     * @return
     */
    public static boolean insertUser(String name,String password)
    {
        if (name==null||password==null){
            return false;
        }
        Connection conn=null;
        ResultSet rs=null;
        try {
            conn = DbManager.getConnection();//工具类连接
            String sql = "insert into tb_user(username, password) values (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, password);
            int count = stmt.executeUpdate();
            if(count > 0)
                return true;
            else
                return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            DbManager.close(rs, null,conn);//工具类释放资源
        }
        return false;
    }

    /**
     * 查询所有用户
     * @return 返回users集合
     */
    public static List<User> findAllUsers()
    {
        Connection conn=null;
        Statement stmt=null;
        ResultSet rs=null;
        try {
            conn = DbManager.getConnection();//工具类连接
            stmt = conn.createStatement();
            String sql = "select * from tb_user";
            rs = stmt.executeQuery(sql);
            //封装user对象 装载List集合
            User user = null;
            List<User> users = new ArrayList<>();
            while (rs.next())
            {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                //封装user对象
                user = new User();
                user.setId(id);
                user.setUsername(username);
                user.setPassword(password);
                //装载集合
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            DbManager.close(rs,stmt,conn);//工具类释放资源

        }
        return null;
    }
}
