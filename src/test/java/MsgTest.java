import java.io.IOException;
import java.util.List;
import msg.Message;
import msg.ServerChatRecord;
import org.junit.Assert;
import org.junit.Test;

public class MsgTest {


    @Test
    public void testServerRecord() throws IOException {
        ServerChatRecord record = ServerChatRecord.getServerChatRecord();
        Message msg1 = new Message(1, 2, "Hello", "2020-01-01");
        record.addMessages(msg1);
        record.writeRecord();
        List<Message> messages = record.getMessages();
        Assert.assertEquals(messages.get(0),msg1);
    }
}