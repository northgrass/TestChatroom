import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
	public static void main(String args[]) throws Exception {
		int port = 12345;
		ServerSocket server = new ServerSocket(port, 70);
		System.out.println(server);
		Map<String, Socket> onlineClients = new ConcurrentHashMap<String, Socket>();
		try {
			while (true) {
				Socket socket = server.accept();
				Task t = new Task(socket, onlineClients);
				t.start();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally {
			server.close();
		}
	}
}

class Task extends Thread {

	private Map<String, Socket> onlineClients;
	private Socket client;
	private BufferedReader br;
	private PrintWriter pw;
	private String userName;
	private Boolean flag = true;
	private Boolean loginSuccessed = true;

	public Task(Socket client, Map<String, Socket> onlineClients)
			throws Exception {
		super();
		this.client = client;
		this.onlineClients = onlineClients;
	}

	public void run() {
		try {
			br = new BufferedReader(new InputStreamReader(
					this.client.getInputStream()));
			pw = new PrintWriter(client.getOutputStream(), true);
			pw.println("please login");
			String login = "";
			login = br.readLine();
			while (flag && login != null) {
				login(login);
				if (flag) {
					login = br.readLine();
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();

		}
		String content;
		try {
			while (loginSuccessed && (userName != null)
					&& (content = br.readLine()) != null) {
				if (content.equals("/quit")) {// 用户正常登陆后使用命令/quit退出
					quit();
					loginSuccessed = false;
					break;
				} else {
					dealWithContact(content);
				}
			}
		} catch (IOException e) {
			loginSuccessed = false;
			remindBroadcast(userName + " has quit!");// 用户登录后强制退出
			onlineClients.remove(userName);
		}
	}

	// 广播消息
	public void broadcast(String content) {
		remindBroadcast(userName + "说:" + content);
		pw.println("你说:" + content);
	}

	// 私聊
	public void privateChat(String content) {
		try {
			String[] str = content.split(" ");
			String receiveName = "";
			String message = "";
			if (str.length == 2) {
				receiveName = str[1];
				if (onlineClients.containsKey(receiveName)) {
					pw.println("发送内容不能为空！");
				} else {
					pw.println("Invalid command!");
				}

			} else if (str.length > 2) {
				receiveName = str[1];
				message = content.replace("/to " + receiveName, userName
						+ " say to you:");
				Socket toClient = (Socket) onlineClients.get(receiveName);
				if ((toClient != null) && (toClient != client)) {
					PrintWriter pwPrivate = new PrintWriter(
							toClient.getOutputStream(), true);
					pwPrivate.println(message);
					String messageToMe = message
							.replace(userName + " say to you:", "you say to "
									+ receiveName + ":");
					pw.println(messageToMe);
				}
				if (toClient == client) {
					pw.println("停止对自己讲话！");
				}
				if (toClient == null) {
					pw.println("该用户暂时不在线！");
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	// 登陆
	public void login(String login) {
		String[] str = login.split(" ");
		if ((login.startsWith("/login ")) && str.length > 1) {
			userName = str[1];
			if (onlineClients.containsKey(userName)) {
				pw.println("Name exist, please choose anthoer name!");
			} else {
				pw.println("You have logined!");
				onlineClients.put(userName, client);
				remindBroadcast(userName + " has logined!");
				flag = false;
				// break;
			}
		} else if (login.equals("/quit")) { // 用户未登录时直接命令/quit退出
			loginSuccessed = false;
			try {
				br.close();
				pw.close();
				client.close();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			flag = false;
			// break;
		} else {
			pw.println("Invalid command!");
		}
	}

	// 查看在线用户及人数
	public void onlineUser() {
		java.util.Iterator<Entry<String, Socket>> iter = onlineClients
				.entrySet().iterator();
		int num = 0;
		try {
			while (iter.hasNext()) {
				Entry entry = (Entry) iter.next();
				Object key = entry.getKey();
				// System.out.println(key);
				pw.println(key);
				num++;
			}
			pw.println("Total online user:" + num);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	// 发送预设命令
	public void preOrder(String content) {
		String[] str = content.split(" ");
		String toWho = "";
		String text = "";
		if (content.equals("//hi")) {
			toWho = "向大家";
			text = "打招呼：“Hi，大家好！我来咯~”";
		} else if ((content.startsWith("//hi")) && str.length == 2) {
			if (str[1].equals(userName)) {
				pw.println("不要同自己打招呼！");
				return;
			} else if (onlineClients.containsKey(str[1])) {
				toWho = "向" + str[1];
				text = "打招呼：“Hi，你好啊~”";
			} else {
				pw.println("你要打招呼的用户暂时不在线！");
				return;
			}
		} else if (content.equals("//smile")) {
			toWho = "";
			text = "脸上泛起无邪的笑容";
		} else {
			pw.println("不存在的预设消息！");
			return;
		}
		pw.println(userName + toWho + text);
		remindBroadcast(userName + toWho + text);
	}

	// 群发消息提示（除了自己）
	public void remindBroadcast(String content) {
		try {
			Collection<Socket> values = onlineClients.values();
			for (Socket value : values) {
				PrintWriter pwBroadcast = new PrintWriter(
						value.getOutputStream(), true);
				if (value != client) {
					pwBroadcast.println(content);
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	// 退出
	public void quit() {
		remindBroadcast(userName + " has quit!");
		onlineClients.remove(userName);
		try {
			br.close();
			pw.close();
			client.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	// 处理消息
	public void dealWithContact(String content) {
		if (!(content.startsWith("/"))) {
			broadcast(content);
		} else if (content.startsWith("/to ")) {
			privateChat(content);
		} else if (content.equals("/who")) {
			onlineUser();
		} else if (content.startsWith("//")) {
			preOrder(content);
		} else if (content.equals("/quit")) {
			quit();
		} else if (content.startsWith("/login ")) {
			pw.println("你已经登录，请不要重复登录！");
		} else {
			pw.println("Invalid command!");
		}
	}
	public void setLoginSuccessed(Boolean loginSuccessed) {
		this.loginSuccessed = loginSuccessed;
	}
	
	public void setFlag(Boolean flag) {
		this.flag = flag;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
