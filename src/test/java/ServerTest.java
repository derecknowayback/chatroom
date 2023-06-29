import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.SocketException;
import java.util.Date;
import msg.Message;
import net.Server;
import org.junit.Assert;
import org.junit.Test;

public class ServerTest {

	@Test
	public void testHandleSynMsg () throws IOException {
		Message message = new Message(1, 2, "test data", new Date().toString());
		StringBuilder builder = new StringBuilder();
		int senderId = 1;
		//  版本号 | 用户id | msg1
		builder.append(0).append("|").append(2).append("|").append(message);
		Server server = new Server();
		server.handleSynMsg(senderId, builder.toString());
	}


	@Test
	public void testGetRecordVersionId () throws SocketException {
		Server server = new Server();
		int versionId = server.getRecordVersionId(1, 2);
		Assert.assertEquals(0,versionId);
	}


	@Test
	public void testLoadRecord () throws IOException {
		// 先获取
		String workDir = System.getProperty("user.dir");
		File file = new File(workDir);
		File[] files = file.listFiles();
		int userId = 1;
		for (int i = 0; i < files.length; i++) {
			File temp = files[i];
			String fileName = temp.getName();
			if (fileName.startsWith(String.valueOf(userId))) {
				RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
				StringBuilder sb = new StringBuilder();
				int receiverId = Integer.parseInt(fileName.substring(fileName.indexOf("_") + 1));
				int versionId = raf.readInt();
				sb.append(versionId).append("|").append(receiverId).append("|");
				int cnt = raf.readInt();
				Assert.assertEquals(1,cnt);
				for (int j = 0; j < cnt; j++) {
						Message msg = Message.readMsg(raf);
						sb.append(msg);
						if (j != cnt - 1) sb.append("|");
				}
				raf.close();
			}
		}
	}

}
