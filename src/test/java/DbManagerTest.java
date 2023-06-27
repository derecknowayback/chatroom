import java.util.List;
import java.util.Scanner;
import org.junit.Test;

import static dto.DBManager.checkUser;
import static dto.DBManager.findAllUsers;
import static dto.DBManager.findById;
import static dto.DBManager.insertUser;

public class DbManagerTest {


    @Test
    public void testDbManager() {
        Scanner sc = new Scanner(System.in);
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入一个整数（1/2/3/4） 1：check 2：insert 3：find allusers 4: find userbyid");
        int num = scanner.nextInt();
        switch (num) {
            case 1:
                System.out.println("请输入用户名：");
                String name = sc.nextLine();
                System.out.println("请输入密码：");
                String password = sc.nextLine();
                if (checkUser(name, password)) {
                    System.out.println("登录成功！");
                } else {
                    System.out.println("用户名或密码错误！");
                }
                break;
            case 2:
                System.out.println("请输入姓名：");
                String pname = sc.nextLine();
                System.out.println("请输入密码：");
                String ppassword = sc.nextLine();
                if (insertUser(pname, ppassword))
                    System.out.println("添加成功");
                else
                    System.out.println("添加失败");
                break;
            case 3:
                List users = findAllUsers();
                System.out.println(users);
                break;
            case 4:
                System.out.println("请输入id：");
                int findid = Integer.parseInt(sc.nextLine());
                List user = findById(findid);
                System.out.println(user);
                break;
            default:
                System.out.println("无效的输入！");
        }
    }

}


