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
import java.util.Arrays;
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
		Client cli = new Client();
		cli.setSelf(new User("ccc","11"));
		ClientUi ui = new ClientUi(cli);
		Thread thread = new Thread(ui);
		thread.start();
	}

	/**
	 *
	 */
	// 变量声明
	private static final long serialVersionUID = 6704231622520334518L;

	private JFrame frame;
	// private JTextArea textShow;
	static private JTextPane textShow;
	private JTextField txtMsg;
	private JLabel infoName;
	private JButton btnSend;

	private final static String BtnTextSend = "Send";
	private final static String BtnTextCreate = "Create";
	private final static String BtnTextInvite = "Invite";
	private final static String BtnTextLeave = "Leave";

	private JButton createGroup;

	private JButton addFriendToGroup;

	private JButton leaveGroup;

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

	private String talkingTo;

	@Override
	public void run() {
		while (true) {

		}
	}

	class MyBut extends JButton {

		public MyBut(String text) {
			super(text);
			addActionListener(a -> {
				talkingTo = text;
				List<Message> messages = client.getRecordByName(talkingTo);
				Document docs = textShow.getDocument();
				try {
					int offset = 0;
					docs.remove(0,docs.getLength());
					for (Message msg : messages) {
						String toDisplay = msg.toDisplay();
						offset = toDisplayString(toDisplay,offset);
					}
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


	private int toDisplayString(String s,int offset) {
		Document document = textShow.getDocument();
		try {
			s += "\n";
			document.insertString(offset,s,attrset);
			return offset + s.length();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return -1;
	}

	private void truncateAndDisplay (String s) {
		Document document = textShow.getDocument();
		try {
			document.remove(0, document.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		toDisplayString(s,0);
	}

	// 构造方法
	public ClientUi(Client client) {
		this.user = client.getSelf();
		this.client = client;


		frame = new JFrame(user.getName());
		frame.setVisible(true); // 可见
		frame.setBackground(Color.PINK);
		frame.setResizable(false); // 大小不可变

		infoName = new JLabel(user.getName());
		textShow = new JTextPane();
		textShow.setEditable(false);
		attrset = new SimpleAttributeSet();
		StyleConstants.setFontSize(attrset, 15);
		txtMsg = new JTextField();
		btnSend = new JButton("Send");


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
		infoName.setForeground(Color.WHITE);
		infoName.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		northPanel.add(infoName);
		TitledBorder info_b = new TitledBorder("My Info");
		info_b.setTitleColor(Color.WHITE);
		info_b.setTitleFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		northPanel.setBorder(info_b);

		rightScroll = new JScrollPane(textShow);
		TitledBorder info_c = new TitledBorder("Message");
		info_c.setTitleColor(Color.DARK_GRAY);
		info_c.setTitleFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		rightScroll.setBorder(info_c);


        leftScroll = new JScrollPane(leftPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		TitledBorder info_d = new TitledBorder("Online User");
		info_d.setTitleColor(Color.DARK_GRAY);
		info_d.setTitleFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		leftScroll.setBorder(info_d);


		southPanel = new JPanel(new BorderLayout());
		southPanel.setLayout(null);
		txtMsg.setBounds(0, 31, 500, 100);
		txtMsg.setBackground(Color.PINK);
		btnSend.setBounds(501, 31, 80, 100);
		btnSend.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 15));
		btnSend.setForeground(Color.GREEN);

		createGroup = new JButton("Create Group");
		createGroup.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		createGroup.setBounds(0,0,180,30);
		createGroup.addActionListener(e -> btnSend.setText(BtnTextCreate));

		addFriendToGroup = new JButton("Invite");
		addFriendToGroup.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		addFriendToGroup.setBounds(181,0,180,30);
		addFriendToGroup.addActionListener(e -> btnSend.setText(BtnTextInvite));


		leaveGroup = new JButton("Leave");
		leaveGroup.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		leaveGroup.setBounds(362,0,180,30);
		leaveGroup.addActionListener(e -> btnSend.setText(BtnTextLeave));


		southPanel.add(txtMsg);
		southPanel.add(btnSend);
		southPanel.add(createGroup);
		southPanel.add(addFriendToGroup);
		southPanel.add(leaveGroup);

		centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
		centerSplit.setDividerLocation(200);


		frame.setLayout(null);
		northPanel.setBounds(0, 0, 600, 80);
		northPanel.setBackground(Color.pink);
		centerSplit.setBounds(0, 80, 600, 500);
		southPanel.setBounds(0, 580, 600, 200);
		frame.add(northPanel);
		frame.add(centerSplit);
		frame.add(southPanel);
		frame.setBounds(0, 0, 600, 800);
		int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
		frame.setLocation((screen_width - frame.getWidth()) / 2, (screen_height - frame.getHeight()) / 2);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // todo 为了同步消息记录


		// btn_send单击发送按钮时事件
		btnSend.addActionListener (e -> {
			String text = txtMsg.getText();
			switch (btnSend.getText()) {
				case BtnTextSend : {
					int userId = client.getUserIdByName(talkingTo);
					Proto message = Proto.getNewMessage(text);
					int type = 0;
					try {
						client.sendMsgToP(message.toString(),userId,type);
					} catch (IOException ex) {
						System.out.println("发送消息失败: " + ex);
					}
				} break;
				case BtnTextCreate: {
					String[] split = text.split("\n");
					// 第一行GroupName, 第二行level, 第三行是你要邀请的用户名
					List<String> usernames = Arrays.asList(split[2].split(","));
					String errMsg = client.askForCreateGroup(split[0], Integer.parseInt(split[1]),usernames);
					if(errMsg != null && errMsg.length() != 0)
						truncateAndDisplay(errMsg);
				} break;
				case BtnTextInvite : {
					String[] split = text.split(",");
					List<String> usernames = Arrays.asList(split);
					String errMsg = client.askForJoinGroup(talkingTo, usernames);
					if(errMsg != null && errMsg.length() != 0)
						truncateAndDisplay(errMsg);
				} break;
				case BtnTextLeave: {
					client.askToLeaveGroup(talkingTo);
				} break;
			}
		});


		leftPanel.setLayout(null);
		leftPanel.setPreferredSize(new Dimension(200,2000));
	}

}