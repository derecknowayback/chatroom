import java.util.List;
import msg.Message;
import msg.ServerChatRecord;
import org.junit.Assert;
import org.junit.Test;

public class ServerChatRecordTest {
	@Test
	public void testGetAllMsgByUserId () {
		ServerChatRecord record = ServerChatRecord.getServerChatRecord();
		for (int i = 0; i < 100; i++) {
			Message message = new Message(i, 2, "dedede", "2023-6-28");
			record.addMessages(message);
		}
		List<Message> id = record.getAllMsgByUserId(2);
		Assert.assertEquals(id.size(),100);
		int size = record.getMessages().size();
		Assert.assertEquals(size,0);
	}

	@Test
	public void testWriteRecord () {
		ServerChatRecord record = ServerChatRecord.getServerChatRecord();
		Message message1 = new Message(1, 2, "dedede", "2023-6-28");
		Message message2 = new Message(2, 2, "cdcdcdc", "2023-6-28");
		record.addMessages(message1);
		record.addMessages(message2);
		record.writeRecord();
	}

	@Test
	public void testReadRecord() {
		ServerChatRecord record = ServerChatRecord.getServerChatRecord();
		List<Message> msgs = record.getAllMsgByUserId(2);
		Assert.assertEquals(msgs.size(),2);
	}


}
