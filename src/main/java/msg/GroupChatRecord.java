package msg;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
/*
 * Implementing chat record
 * chat record operations:
 * 1. void writeRecord
 * 2. List<Message> getMessage
 * 3. addMessages(Message message)
 * 

 */
public class GroupChatRecord extends ChatRecord{
	
	int groupID;
	
	List<Message> messages;
	
	static final String FILE_PREFIX = System.getProperty("user.dir") + "\\" + "group_record_";
	
	ObjectOutputStream writer;
	ObjectInputStream reader;
	
	
	
	
    public GroupChatRecord (int groupId) throws IOException {
    	
        this.groupId = groupId;
        
        String fileName = FILE_PREFIX + groupId;
        
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        
        messages = new ArrayList<>();
        FileInputStream stream = new FileInputStream(fileName);
        try {
                reader = new ObjectInputStream(stream);
                Message message;
                while ( (message = Message.readMsg(reader)) != null) {
                    messages.add(message);
                }
                reader.close();
            } catch (EOFException e) {
                System.out.println("create file...");
            }
            writer = new ObjectOutputStream(new FileOutputStream(fileName,true));
    }

    public void writeRecord() {
        for (Message msg : messages) {
            Message.writeMsg(msg, writer);
        }
    }


    public List<Message> getMessages() {
        return messages;
    }


    @Override
    public void addMessages(Message message) {
        messages.add(message);
    }

}
