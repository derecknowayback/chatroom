import java.io.IOException;
import java.util.List;
import msg.GroupChatRecord;
import msg.Message;
import org.junit.Assert;
import org.junit.Test;

public class GroupChatRecordTest {

	@Test
	public void testCreate() throws IOException {
		GroupChatRecord record = new GroupChatRecord(10);
		Message message = new Message(1, 2, "dede", "2021-2-1");
		record.addMessages(message);
		record.writeRecord();
	}


	@Test
	public void testRead() throws IOException {
		GroupChatRecord record = new GroupChatRecord(10);
		List<Message> messages = record.getMessages();
		Assert.assertEquals(messages.size(),1);
		messages.forEach(System.out::println);
	}

}
