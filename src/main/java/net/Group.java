package net;

import dto.User;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class Group{

/*
 * process：
 * 1. client working on run() method
 * 2. send a request to create/join/leave group
 * 2.1 send a message to a group
 * 		message with group ID
 * 
 * 3. if landed in the case of group responses and group ID isn't -1
 * 		3.1 granted new group:
 * 			new group object:
 * 				received group ID
 * 				list of group member
 * 			save to local file
 * 		3.2 granted join group:
 * 			same as above(message sync)
 * 		3.3 leave group success:
 * 			delete this record from local
 * 		
 * 		3.5 received message:
 * 			every once in a while, sent all message records from the group in chronic order, include the ones this user sent
 * 			
 * 
 * 4. refresh group member whenever server broadcast messages
 * 
 * 
 * no need for online user list(for now), the info is kept at server
 * 
 * 
 * 
 * how does server manage group?
 * server should have a group ID table to keep track of ID taken and returned
 * 
 * 
 * group chat message record format:
 * 
 * 
 * group ID
 * time1 | userID1 | text string ,
 * time2 | userID2 | text string ,
 * ...
 * 
 * update: Implementing chat record
 * chat record operations:
 * 1. void writeRecord
 * 2. List<Message> getMessage
 * 3. addMessages(Message message)
 * 
 * Class group:
 * make group messages but don't send? leave the job to sendMsgToS()
 * 
 */
	
	

	int groupID;	
	private int limit;				
	
	List<User> groupMember = new ArrayList<>();	
	
	
	Group(int groupID){					//constructor at server side
		this.groupID = groupID;
		
	}
	
	
	//req_ section: in conjunction with proto, messages for the server
	void req_newGroup(User[] newMembers) {		
	//input: a list of friends(Users)
	//send a request to server
		
		
	}
	
	void req_joinGroup(User newMember) {				//join this group
		
	}
	
	void req_leaveGroup(Group g) {	
		
	}
	
	 
	//response from the server
	//assume these are granted
	
	void newGroup(int groupID, List<Message> messages) {				//if server granted creation of new group
	//save at local, display at front end
	//message: user ID : text, need to split
		GroupChatRecord newGroupRecord = new GroupChatRecord(groupID, messages);
		
	}
	
	
	
	void leaveGroup(int groupID) {
	//delete local record for this group
	
		
	}
	

	void sendMessageToGroup(int groupID, String content, int userID, int serverID) {
	//plan A: to keep a copy at local
	//plan B: take the server response
	//
		Message newMessage = new Message();
		
		newMessage.setSenderId(userID);			
		newMessage.setReceiverId(serverID);			//to server, some number
		newMessage.setContent(content);
		
		String msg = newMessage.toString();	
		
		//add proto field to msg before sending, proto is currently undefined
    
		sendMsgToS(msg);
		
		//make it into string and call "sendMsgToS"
		
		
		
		
		
		
	}
	
	void receiveMessageFromGroup(int groupID, List<Message> messages) {
	//each time server broadcast the messages, it should also update the user list
	//update the local chat record, insert by chronological order
	//write to local disc
		GroupChatRecord newGroupRecord = new GroupChatRecord(groupID, messages);
		
	}
	
}	
