import java.util.List;
import msg.ClientChatRecord;
import msg.Message;
import org.junit.Assert;
import org.junit.Test;

public class ChatRecordTest {



    @Test
    public void testCreateAndRead () {

    }

    @Test
    public void testWriteRecord() {

    }

    @Test
    public void testGetMessages() {

    }

    @Test
    public void testClientRecord()  {
        ClientChatRecord record = null;
        try {
             record = new ClientChatRecord(15);
        } catch (Exception e) {
            Assert.assertNull(e);
        }
        Message message = new Message(1, 2, "dedede", "2021-2-1");
        record.addMessages(message);
        record.writeRecord();
    }

    @Test
    public void testClientRead() {
        ClientChatRecord record = null;
        try {
            record = new ClientChatRecord(15);
        } catch (Exception e) {
            Assert.assertNull(e);
        }
        List<Message> messages = record.getMessages();
        messages.forEach(System.out::println);
    }

}
