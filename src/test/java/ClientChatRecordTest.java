import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.List;
import msg.ClientChatRecord;
import msg.Message;
import org.junit.Assert;
import org.junit.Test;

public class ClientChatRecordTest {


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
        Message message = new Message(1, 2, "dedede", new Date().toString());
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
        System.out.println(messages.size());
        messages.forEach(System.out::println);
    }

    @Test
    public void testGetData() {
        ClientChatRecord record = null;
        try {
            record = new ClientChatRecord(15);
        } catch (Exception e) {
            Assert.assertNull(e);
        }
        System.out.println(record.getData());
    }




}
