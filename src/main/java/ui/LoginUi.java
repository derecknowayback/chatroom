package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class LoginUi extends JFrame {
    /**
     *
     */
    private static final long serialVersionUID = -6256528270698337060L;
    private JTextField userName; // 用户名
    private JPasswordField password; // 密码
    private JLabel lableUser;
    private JLabel lablePwd;
    private JButton btnLogin; // 按钮
    private JButton btnRegister;
    private int wx, wy;
    private boolean isDraging = false;
    private JPanel contentPane;

    public static void main(String[] args) {
        LoginUi frame = new LoginUi();
        frame.setVisible(true);
    }

    public LoginUi() {

        // 设置无标题栏
        setUndecorated(true);

        // 监听鼠标 确保窗体能够拖拽
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                isDraging = true;
                wx = e.getX();
                wy = e.getY();
            }

            public void mouseReleased(MouseEvent e) {
                isDraging = false;
            }
        });


        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (isDraging) {
                    int left = getLocation().x;
                    int top = getLocation().y;
                    setLocation(left + e.getX() - wx, top + e.getY() - wy);
                }
            }
        });

        setBounds(100, 100, 439, 369);
        setLocationRelativeTo(null);
        contentPane = new JPanel();
        contentPane.setBackground(Color.PINK);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setResizable(false);
        setContentPane(contentPane);
        contentPane.setLayout(null);

        //
        btnLogin = new JButton("登录");
        btnLogin.setBounds(225, 285, 170, 40);
        btnLogin.setBackground(new Color(0xdedef));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBorder(null);
        btnLogin.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 25));
        btnLogin.setPreferredSize(new Dimension(170, 40));
        btnLogin.setFocusPainted(false);
        contentPane.add(btnLogin);
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String name = userName.getText();
                String pwd = String.valueOf(password.getPassword());


            }
        });


        //
        btnRegister = new JButton("注册");
        btnRegister.setBounds(37, 285, 170, 40);
        btnRegister.setFocusPainted(false);
        btnRegister.setBackground(new Color(0xF804));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setBorder(null);
        btnRegister.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 25));
        btnRegister.setPreferredSize(new Dimension(170, 40));
        contentPane.add(btnRegister);
        btnRegister.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            }
        });



        // // 用户号码登录输入框
        userName = new JTextField();
        userName.setBounds(170, 167, 219, 35);
        userName.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 25));
        contentPane.add(userName);

        // 登录输入框旁边的文字
        lableUser = new JLabel("用户名");
        lableUser.setBounds(37, 170, 126, 27);
        lableUser.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 25));
        lableUser.setForeground(Color.WHITE);
        contentPane.add(lableUser);

        // 密码输入框
        password = new JPasswordField();
        password.setBounds(170, 212, 219, 35);
        password.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 30));
        contentPane.add(password);

        // // 密码输入框旁边的文字
        lablePwd = new JLabel("密码");
        lablePwd.setBounds(37, 215, 126, 27);
        lablePwd.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 25));
        lablePwd.setForeground(Color.WHITE);
        contentPane.add(lablePwd);
        //

        JLabel lblv = new JLabel("Welcome");
        lblv.setForeground(Color.WHITE);
        lblv.setBackground(Color.WHITE);
        lblv.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 58));
        lblv.setBounds(37, 60, 357, 80);
        contentPane.add(lblv);

    }
}