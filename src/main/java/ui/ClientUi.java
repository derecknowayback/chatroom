package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class ClientUi {

    public static void main(String[] args) throws InterruptedException {
        ClientUi xx = new ClientUi("xx");
		xx.leftPanel.setLayout(null);
		xx.leftPanel.setPreferredSize(new Dimension(200,2000));
		int k = 0, preY = 0;
		while (k < 12) {
			String name = k+ "";
			System.out.println(name);
				MyBut but = new MyBut(name);
				but.setBounds(0, preY , 180, 50);
				but.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
				but.setForeground(Color.GREEN);
				xx.leftPanel.add(but);
				preY += 51;
				map.put(name,k);
				k++;
			Thread.sleep(1000);
		}
    }

	/**
	 *
	 */
	// 变量声明
	private static final long serialVersionUID = 6704231622520334518L;

	private JFrame frame;
	// private JTextArea text_show;
	static private JTextPane text_show;
	private JTextField txt_msg;
	private JLabel info_name;
	private JButton btn_send;

	private JPanel northPanel;
	private JPanel southPanel;
	private JScrollPane rightScroll;
	private JScrollPane leftScroll;
	private JSplitPane centerSplit;

	static private SimpleAttributeSet attrset;

	private DefaultListModel<MyBut> listModel;
	private JList<MyBut> userList;

	static private HashMap<String,Integer> map = new HashMap<>();

	private String name;

	private JPanel leftPanel = new JPanel();

	static class MyBut extends JButton{

		public MyBut(String text) {
			super(text);
			addActionListener(a -> {
//				System.out.println();
				int id = map.get(super.getText());
				Document docs = text_show.getDocument();
				try {
					docs.remove(0, docs.getLength());
					docs.insertString(0, "This is a message from"+id+"\n", attrset);// 对文本进行追加
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			});
		}

		@Override
		public String toString(){
			return super.getText();
		}
	}


	// 构造方法
	public ClientUi(String n) {

		this.name = n;
		frame = new JFrame(name);
		frame.setVisible(true); // 可见
		frame.setBackground(Color.PINK);
		frame.setResizable(false); // 大小不可变

		info_name = new JLabel(name);
		text_show = new JTextPane();
		text_show.setEditable(false);
		attrset = new SimpleAttributeSet();
		StyleConstants.setFontSize(attrset, 15);
		txt_msg = new JTextField();
		btn_send = new JButton("Send");


		listModel = new DefaultListModel<>();
		userList = new JList<>(listModel);

		leftPanel.setLayout(new FlowLayout(FlowLayout.LEADING));

        // 最上面的本人信息
		northPanel = new JPanel();
		northPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		JLabel info_a = new JLabel("UserName : ");
		info_a.setForeground(Color.WHITE);
		info_a.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		northPanel.add(info_a);

        // 最上面的边框
		info_name.setForeground(Color.WHITE);
		info_name.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		northPanel.add(info_name);
		TitledBorder info_b = new TitledBorder("My Info");
		info_b.setTitleColor(Color.WHITE);
		info_b.setTitleFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		northPanel.setBorder(info_b);

		rightScroll = new JScrollPane(text_show);
		TitledBorder info_c = new TitledBorder("Message");
		info_c.setTitleColor(Color.DARK_GRAY);
		info_c.setTitleFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		rightScroll.setBorder(info_c);


        leftScroll = new JScrollPane(leftPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		TitledBorder info_d = new TitledBorder("Online dto.User");
		info_d.setTitleColor(Color.DARK_GRAY);
		info_d.setTitleFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		leftScroll.setBorder(info_d);



		southPanel = new JPanel(new BorderLayout());
		southPanel.setLayout(null);
		txt_msg.setBounds(0, 0, 500, 100);
		txt_msg.setBackground(Color.pink);
		btn_send.setBounds(501, 0, 80, 100);
		btn_send.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		btn_send.setForeground(Color.GREEN);
		southPanel.add(txt_msg);
		southPanel.add(btn_send);



		centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
		centerSplit.setDividerLocation(200);

		frame.setLayout(null);
		northPanel.setBounds(0, 0, 600, 80);
		northPanel.setBackground(Color.pink);
		centerSplit.setBounds(0, 90, 600, 500);
		southPanel.setBounds(0, 600, 600, 200);
		frame.add(northPanel);
		frame.add(centerSplit);
		frame.add(southPanel);
		frame.setBounds(0, 0, 600, 800);
		int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
		frame.setLocation((screen_width - frame.getWidth()) / 2, (screen_height - frame.getHeight()) / 2);

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);



		// txt_msg回车键时事件
		txt_msg.addActionListener(arg0 -> {

		});

		// btn_send单击发送按钮时事件
		btn_send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			}

		});





		// 关闭窗口时事件
//		frame.addWindowListener(new WindowAdapter() {
//			public void windowClosing(WindowEvent e) {
//				if (isConnected) {
//					try {
//						// 断开连接
//						boolean flag = false;
//						if (flag == false) {
//							throw new Exception("断开连接发生异常！");
//						} else {
//							JOptionPane.showMessageDialog(frame, "成功断开!");
//							txt_msg.setEnabled(false);
//							btn_send.setEnabled(false);
//						}
//					} catch (Exception e4) {
//						JOptionPane.showMessageDialog(frame, "断开连接服务器异常：" + e4.getMessage(), "错误",
//								JOptionPane.ERROR_MESSAGE);
//					}
//				} else if (!isConnected) {
//					txt_msg.setEnabled(true);
//					btn_send.setEnabled(true);
//				}
//			}
//		});
	}



}