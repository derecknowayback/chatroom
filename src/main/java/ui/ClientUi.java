package ui;

import dto.User;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
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
import msg.ClientChatRecord;
import msg.Message;
import net.Client;
import net.Proto;

public class ClientUi implements Runnable{


    public static void main(String[] args) throws SocketException, InterruptedException {
//		Client cli = new Client();
//		cli.setSelf(new User("ccc","11"));
//		ClientUi ui = new ClientUi(cli);
//		int k = 0, preY = 0;
//		while (k < 12) {
//			String name = k+ "";
//			System.out.println(name);
//				MyBut but = new MyBut(name);
//				but.setBounds(0, preY , 180, 50);
//				but.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
//				but.setForeground(Color.GREEN);
//				ui.leftPanel.add(but);
//				preY += 51;
////				map.put(name,k);
//				k++;
//			Thread.sleep(1000);
//		}
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

	static private HashMap<String,MyBut> map = new HashMap<>();

	private JPanel leftPanel = new JPanel();

	private User user;
	private Client client;

	private HashMap<String,ClientChatRecord> chatRecordMap;

	private String talkingTo;

	@Override
	public void run() {
		while (true) {
			// todo 刷新用户列表

		}
	}

	class MyBut extends JButton {

		public MyBut(String text) {
			super(text);
			addActionListener(a -> {
				talkingTo = text;
				Document docs = text_show.getDocument();
				try {
					docs.remove(0, docs.getLength());
					ClientChatRecord record = chatRecordMap.get(text);
					if (record == null) {
						record = new ClientChatRecord(client.getUserIdByName(text));
					}
					chatRecordMap.put(text,record);
					List<Message> messages = record.getMessages();
					int offset = 0;
					for (Message msg : messages) {
						String toDisplay = msg.toDisplay();
						if (msg.getSenderId() == client.getSelfId()) {
							// todo 如果是自己的话，移到右边
						}
						docs.insertString(offset, toDisplay , attrset);// 对文本进行追加
						offset += toDisplay.length();
					}
				} catch (BadLocationException e) {
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("Error on new ClientChatRecord: " + e);
				}
			});
		}

		@Override
		public String toString(){
			return super.getText();
		}
	}


	// 构造方法
	public ClientUi(Client client) {
		this.user = client.getSelf();
		this.client = client;
		this.chatRecordMap = new HashMap<>();

		frame = new JFrame(user.getName());
		frame.setVisible(true); // 可见
		frame.setBackground(Color.PINK);
		frame.setResizable(false); // 大小不可变

		info_name = new JLabel(user.getName());
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
		txt_msg.setBackground(Color.PINK);
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


		// btn_send单击发送按钮时事件
		btn_send.addActionListener(e -> {
			// todo 检查是不是好友

			String text = txt_msg.getText();
			int userId = client.getUserIdByName(talkingTo);
			Proto message = Proto.getNewMessage(text);
			try {
				client.sendMsgToP(message.toString(),userId);
			} catch (IOException ex) {
				System.out.println("发送消息失败: " + ex);
			}
		});


		leftPanel.setLayout(null);
		leftPanel.setPreferredSize(new Dimension(200,2000));
	}

}