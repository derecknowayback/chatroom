package dto;

import java.util.ArrayList;
import java.util.List;

public class Group {

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
 * 
 * 
 * 
 * 
 */
	int groupID;	
	boolean granted;
	private int limit;
	List<User> groupMember = new ArrayList<>();
	
	
	Group(){					//constructor at server side
		
		
	}
	
	//return values?
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
	
	void newGroup(int groupID, String message) {				//if server granted creation of new group
	//save at local, display at front end
	//message: user ID : text, need to split
		
	}
	
	
	
	void leaveGroup(int groupID) {
		
	}
	
	void sendMessageToGroup(int groupID, String message) {
		
	}
	
	void receiveMessageFromGroup(int groupID, String message) {
	//each time server broadcast the messages, it should also update the user list
		
		
	}
	
}	
