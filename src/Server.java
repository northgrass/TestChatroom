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
				if (content.equals("/quit")) {// �û�������½��ʹ������/quit�˳�
					quit();
					loginSuccessed = false;
					break;
				} else {
					dealWithContact(content);
				}
			}
		} catch (IOException e) {
			loginSuccessed = false;
			remindBroadcast(userName + " has quit!");// �û���¼��ǿ���˳�
			onlineClients.remove(userName);
		}
	}

	// �㲥��Ϣ
	public void broadcast(String content) {
		remindBroadcast(userName + "˵:" + content);
		pw.println("��˵:" + content);
	}

	// ˽��
	public void privateChat(String content) {
		try {
			String[] str = content.split(" ");
			String receiveName = "";
			String message = "";
			if (str.length == 2) {
				receiveName = str[1];
				if (onlineClients.containsKey(receiveName)) {
					pw.println("�������ݲ���Ϊ�գ�");
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
					pw.println("ֹͣ���Լ�������");
				}
				if (toClient == null) {
					pw.println("���û���ʱ�����ߣ�");
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	// ��½
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
		} else if (login.equals("/quit")) { // �û�δ��¼ʱֱ������/quit�˳�
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

	// �鿴�����û�������
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

	// ����Ԥ������
	public void preOrder(String content) {
		String[] str = content.split(" ");
		String toWho = "";
		String text = "";
		if (content.equals("//hi")) {
			toWho = "����";
			text = "���к�����Hi����Һã�������~��";
		} else if ((content.startsWith("//hi")) && str.length == 2) {
			if (str[1].equals(userName)) {
				pw.println("��Ҫͬ�Լ����к���");
				return;
			} else if (onlineClients.containsKey(str[1])) {
				toWho = "��" + str[1];
				text = "���к�����Hi����ð�~��";
			} else {
				pw.println("��Ҫ���к����û���ʱ�����ߣ�");
				return;
			}
		} else if (content.equals("//smile")) {
			toWho = "";
			text = "���Ϸ�����а��Ц��";
		} else {
			pw.println("�����ڵ�Ԥ����Ϣ��");
			return;
		}
		pw.println(userName + toWho + text);
		remindBroadcast(userName + toWho + text);
	}

	// Ⱥ����Ϣ��ʾ�������Լ���
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

	// �˳�
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

	// ������Ϣ
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
			pw.println("���Ѿ���¼���벻Ҫ�ظ���¼��");
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
