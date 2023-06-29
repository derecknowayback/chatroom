package msg;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
/*
 * Implementing chat record
 * chat record operations:
 * 1. void writeRecord
 * 2. List<Message> getMessage
 * 3. addMessages(Message message)
 *
 */
public class GroupChatRecord extends ChatRecord{
	
	int groupId;
	
	List<Message> messages;
	
	static final String FILE_PREFIX = System.getProperty("user.dir") + "\\" + "group_record_";
	
    RandomAccessFile raf;
	

    public GroupChatRecord (int groupId) throws IOException {
    	
        this.groupId = groupId;
        String fileName = FILE_PREFIX + groupId;
        
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        
        messages = new ArrayList<>();
        raf = new RandomAccessFile(fileName,"rw");
        Message message;
        try {
            while (true) {
                message = Message.readMsg(raf);
                messages.add(message);
            }
        } catch (EOFException e) {
            System.out.println("read done ...");
        }
    }

    public void writeRecord() {
        try {
            raf.setLength(0);
            raf.seek(0);
        } catch (IOException e){
            e.printStackTrace();
            return;
        }
        for (Message msg : messages) {
            Message.writeMsg(msg, raf);
        }
        try {
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<Message> getMessages() {
        return messages;
    }


    @Override
    public void addMessages(Message message) {
        if(!messages.contains(message))
            messages.add(message);
    }

}
