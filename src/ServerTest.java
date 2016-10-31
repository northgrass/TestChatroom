import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ServerTest {

	private Socket client1;
	private Socket client2;
	private Socket client3;
	private ByteArrayOutputStream byteArrayOutputStream;
	private ByteArrayOutputStream byteArrayOutputStream2;
	private ByteArrayOutputStream byteArrayOutputStream3;
	private Task task1;
	private Task task2;
	private Map<String, Socket> onlineClients;

	@Before
	public void setUp() throws Exception {
		client1 = Mockito.mock(Socket.class);
		client2 = Mockito.mock(Socket.class);
		client3 = Mockito.mock(Socket.class);
		onlineClients = new ConcurrentHashMap<String, Socket>();
		onlineClients.put("b", client2);
		onlineClients.put("c", client3);
		byteArrayOutputStream = new ByteArrayOutputStream();
		Mockito.when(client1.getOutputStream()).thenReturn(
				byteArrayOutputStream);
		byteArrayOutputStream2 = new ByteArrayOutputStream();
		Mockito.when(client2.getOutputStream()).thenReturn(
				byteArrayOutputStream2);
		byteArrayOutputStream3 = new ByteArrayOutputStream();
		Mockito.when(client3.getOutputStream()).thenReturn(
				byteArrayOutputStream3);
		task1 = new Task(client1, onlineClients);
		task2 = new Task(client2, onlineClients);
		task2.setLoginSuccessed(true);
		task2.setFlag(false);
		task2.setUserName("b");
	}

	@Test
	public void corrLoginTest() throws IOException {

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				"/login a".getBytes());
		Mockito.when(client1.getInputStream()).thenReturn(byteArrayInputStream);
		task1.run();
		// System.out.println(byteArrayOutputStream.toString());
		// System.out.println(byteArrayOutputStream2.toString());
		Assert.assertEquals("please login\r\nYou have logined!\r\n",
				byteArrayOutputStream.toString());
		Assert.assertEquals("a has logined!\r\n",
				byteArrayOutputStream2.toString());
	}

	@Test
	public void nameExistLoginTest() throws IOException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				"/login b\r\n".getBytes());
		Mockito.when(client1.getInputStream()).thenReturn(byteArrayInputStream);
		task1.run();
		// System.out.println(byteArrayOutputStream.toString());
		Assert.assertEquals(
				"please login\r\nName exist, please choose anthoer name!\r\n",
				byteArrayOutputStream.toString());
	}

	@Test
	public void invalidTest() throws IOException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				"/logina\r\n//smile ads".getBytes());
		Mockito.when(client1.getInputStream()).thenReturn(byteArrayInputStream);
		task1.run();
		// System.out.println(byteArrayOutputStream.toString());
		Assert.assertEquals("please login\r\nInvalid command!\r\nInvalid command!\r\n",
				byteArrayOutputStream.toString());
	}
	
	

	@Test
	public void privateChatTest() throws IOException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				"/to c hello xixi!".getBytes());
		Mockito.when(client2.getInputStream()).thenReturn(byteArrayInputStream);
		task2.run();
		// System.out.println(byteArrayOutputStream.toString());
		// System.out.println(byteArrayOutputStream2.toString());
		Assert.assertEquals(
				"please login\r\nyou say to b: hello xixi!\r\n",
				byteArrayOutputStream2.toString());
		Assert.assertEquals("b say to you: hello xixi!\r\n",
				byteArrayOutputStream3.toString());
	}

	@Test
	public void sayToSelfTest() throws IOException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				"/login a\r\n/to a hello xixi!".getBytes());
		Mockito.when(client1.getInputStream()).thenReturn(byteArrayInputStream);
		task1.run();
		// System.out.println(byteArrayOutputStream.toString());
		Assert.assertEquals(
				"please login\r\nYou have logined!\r\n停止对自己讲话！\r\n",
				byteArrayOutputStream.toString());
	}

	@Test
	public void sayToNotOnlineTest() throws IOException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				"/login a\r\n/to mary hello xixi!".getBytes());
		Mockito.when(client1.getInputStream()).thenReturn(byteArrayInputStream);
		task1.run();
		// System.out.println(byteArrayOutputStream.toString());
		Assert.assertEquals(
				"please login\r\nYou have logined!\r\n该用户暂时不在线！\r\n",
				byteArrayOutputStream.toString());
	}

	@Test
	public void broadcastTest() throws IOException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				"/login a\r\nhello everyone!".getBytes());
		Mockito.when(client1.getInputStream()).thenReturn(byteArrayInputStream);
		task1.run();
		// System.out.println(byteArrayOutputStream.toString());
		// System.out.println(byteArrayOutputStream2.toString());
		// System.out.println(byteArrayOutputStream3.toString());
		Assert.assertEquals(
				"please login\r\nYou have logined!\r\n你说:hello everyone!\r\n",
				byteArrayOutputStream.toString());
		Assert.assertEquals("a has logined!\r\na说:hello everyone!\r\n",
				byteArrayOutputStream2.toString());
		Assert.assertEquals("a has logined!\r\na说:hello everyone!\r\n",
				byteArrayOutputStream3.toString());
	}

	@Test
	public void onlineUserTest() throws IOException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				"/login a\r\n/who".getBytes());
		Mockito.when(client1.getInputStream()).thenReturn(byteArrayInputStream);
		task1.run();
		// System.out.println(byteArrayOutputStream.toString());
		Assert.assertEquals(
				"please login\r\nYou have logined!\r\na\r\nb\r\nc\r\nTotal online user:3\r\n",
				byteArrayOutputStream.toString());
	}

	@Test
	public void sayHiTest() throws IOException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				"/login a\r\n//hi".getBytes());
		Mockito.when(client1.getInputStream()).thenReturn(byteArrayInputStream);
		task1.run();
		// System.out.println(byteArrayOutputStream.toString());
		Assert.assertEquals(
				"please login\r\nYou have logined!\r\na向大家打招呼：“Hi，大家好！我来咯~”\r\n",
				byteArrayOutputStream.toString());
	}

	@Test
	public void smileTest() throws IOException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				"/login a\r\n//smile".getBytes());
		Mockito.when(client1.getInputStream()).thenReturn(byteArrayInputStream);
		task1.run();
		// System.out.println(byteArrayOutputStream.toString());
		Assert.assertEquals(
				"please login\r\nYou have logined!\r\na脸上泛起无邪的笑容\r\n",
				byteArrayOutputStream.toString());
	}

	@Test
	public void quitTest() throws IOException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				"/login a\r\n/quit".getBytes());
		Mockito.when(client1.getInputStream()).thenReturn(byteArrayInputStream);
		task1.run();
		// System.out.println(byteArrayOutputStream2.toString());
		Assert.assertEquals("a has logined!\r\na has quit!\r\n",
				byteArrayOutputStream2.toString());
	}
}
