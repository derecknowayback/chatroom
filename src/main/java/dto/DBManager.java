package dto;


import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

//JDBC工具类
public class DBManager {
    private static String url;
    private static String user;
    private static String password;
    private static String driver;

    private static Connection connection;

    private static final String loginFailedPrefix = "false" + ",";

    private static final String loginSuccessPrefix = "success" + ",";


    //静态代码块
    static {
        //读取资源文件，获取值
        try {
            //1.创建Properties集合类
            Properties pro =new Properties();
            //2.加载文件
            //这里假如找不到配置文件，就用绝对路径
            pro.load(new FileReader("database.properties"));

            //3.获取数据，赋值
            url = pro.getProperty("url");
            user = pro.getProperty("user");
            password = pro.getProperty("password");
            driver = pro.getProperty("driver");

            //4.注册驱动
            Class.forName(driver);

            connection = DriverManager.getConnection(url, user, password);
        } catch (IOException | ClassNotFoundException  | SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void closeRsAndStatement (ResultSet rs, Statement stemt) {
        if(rs != null){
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
    }

    private static int getRows (ResultSet rs) throws SQLException {
        rs.last();
        int rowCount = rs.getRow();
        rs.beforeFirst();
        return rowCount;
    }


    private static final String NameOrPasswordNull = "name and password are required .";
    private static final String NoUserPrefix = "No user: ";
    private static final String WrongPassword = "Wrong Password";


    /**
     * 用于用于判断用户名和密码是否正确
     * @param name 用户名
     * @param password 密码
     * @return
     */
    public static String checkUser(String name,String password) {
        if (name==null || password==null) {
            return loginFailedPrefix +NameOrPasswordNull;
        }
        List<User> users = getByName(name);
        if (users.size() == 0) {
            return loginFailedPrefix +NoUserPrefix + name;
        }
        User user = users.get(0);
        if (! user.getPassword().equals(password)) {
            return loginFailedPrefix + WrongPassword;
        }
        return loginSuccessPrefix +user.getId();
    }


    private static List<User> getByName(String name) {
        Statement stmt=null;
        ResultSet rs=null;
        List<User> users = new ArrayList<>();
        try {
            stmt = connection.createStatement();
            String sql ;
            if (name != null) {
                sql = "select * from user  where username ='"+ name +"' ";
            } else {
                sql = "select * from user ";
            }
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String pwd = rs.getString("password");
                //封装user对象
                User user = new User(id,username,pwd);
                //装载集合
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            closeRsAndStatement(rs,stmt);
        }
        return users;
    }


    /**
     * 用于查询单个用户 输入id 返回对象
     * @param userId
     * @return user 返回对象
     */
    public static User findById(int userId) {
        Statement stmt=null;
        ResultSet rs=null;
        try {
            stmt = connection.createStatement();
            String sql = "select * from user where id = '"+ userId + "'";
            rs = stmt.executeQuery(sql);
            if (! rs.first()) {
                return null;
            }
            int id = rs.getInt("id");
            String username = rs.getString("username");
            String pwd = rs.getString("password");
            User user = new User(id,username,pwd);
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
        }finally{
            closeRsAndStatement(rs,stmt);
        }
        return null;
    }

    /**
     * 用于插入新用户 返回true插入成功
     * @param name
     * @param password
     * @return
     */
    public static String insertUser(String name,String password) {
        if (name == null || password == null){
            return loginFailedPrefix + NameOrPasswordNull;
        }
        List<User> sameName = getByName(name);
        if (sameName.size() > 0) {
            return loginFailedPrefix + "The username is occupied .";
        }
        PreparedStatement stmt = null;
        try {
            String sql = "insert into user (username, password) values (?, ?)";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, password);
            int count = stmt.executeUpdate();
            if(count < 0)
                return null;
            List<User> users = getByName(name);
            return loginSuccessPrefix + users.get(0).getId();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeRsAndStatement(null,stmt);
        }
        return null;
    }

    /**
     * 查询所有用户
     * @return 返回users集合
     */
    public static List<User> findAllUsers() {
        return getByName(null);
    }
}
