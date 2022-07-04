import java.io.*;

public class ServerDriver {
	public static void main(String[] args) throws Exception {
		int port = 10000;
		ThreadServer ts = new ThreadServer(port);
		ts.start();
		Server server = ts.server;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in), 1);


		int TEST_CLIENT_NUM  = 4;
		SimpleClient2[] sc = new SimpleClient2[TEST_CLIENT_NUM];

		for (int i=0; i < TEST_CLIENT_NUM; i++) {
			sc[i] = new SimpleClient2();
			String name, mode;
			do {
				Thread.sleep(100);
				System.out.println("********************************");
				System.out.println("player" + i + "‚Ì–¼‘O‚ð“ü—Í‚µ‚Ä‚­‚¾‚³‚¢");
				name = br.readLine();
				System.out.println("‘Îíƒ‚[ƒh[normal, time]‚ð“ü—Í‚µ‚Ä‚­‚¾‚³‚¢");
				mode = br.readLine();
				sc[i].connectServer("localhost", 10000);
				sc[i].sendMessage(name + ":" + mode);
				Thread.sleep(100);
			}while (sc[i].isLogined() == false);
			sc[i].setVisible(true);
		}
		server.printStatus();
	}
}
class ThreadServer extends Thread {
	Server server;
	public  void run() {
		server.acceptClient();
	}
	ThreadServer(int port) {
		this.server = new Server(port);
	}
}