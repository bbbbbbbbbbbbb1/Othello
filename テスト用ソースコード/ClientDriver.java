import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientDriver{

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in), 1);
		Socket socket = null;
		PrintWriter out = null;
		Client oclient = new Client();
		oclient.titleDisp();
		System.out.println("ClientDriver:テスト用サーバに接続します");
		try {
			//oclient.connectServer("localhost", 10000);
			socket = new Socket("localhost", 10000); //EchoServerに接続
			out = new PrintWriter(socket.getOutputStream(), true); //データ送信用オブジェクトの用意
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			System.out.println("ClientDriver:サーバとの接続が切れました");
		}

		System.out.println("タイトル画面でモードの選択と名前を入力してください");
		System.out.println("受信用テストメッセージを入力してください");
		System.out.println("送信する受信用テストメッセージとタイミングは以下の通り");
		System.out.println("1.名前入力後　　　　  :「login success」または「Name is not available」または「room is max」");
		System.out.println("2.「login success」後：「black(または white):[相手のプレイヤ名]」");
		System.out.println("3.チャット画面表示後 ：「complete chatDisp ack」");
		System.out.println("4.チャット           ：「chat msg:[チャットで送るメッセージ内容]」または「time ask:[5～15の整数]」");
		System.out.println("5.time ask後         ：「time check:[相手から希望された時間 / NG]」");
		System.out.println("6.対局画面表示後     ：「complete gameDisp ack」");
		System.out.println("7.対局中             ：ノーマルモードは「othello operation:[x]:[y]:[black/white]:[タイマー残り秒数の整数]\r\n" +
						   "                      : 一刻千金モードは「othello operation:[x]:[y]:[black/white]:[価格の種類0/1/2/3]:[タイマー残り秒数の整数]」");
		System.out.println("8.対局中②           ：投了は「resign」、時間切れは「timeover」、チート検出用「timeerror」、\r\n"
						   + "					   相手のタイマーを止める「stopmytimer:[タイマー残り秒数の整数]」");
		System.out.println("9.その他             :相手の接続切れ「quit」\n");

		while(true){
			String s = null;
			try {
				s = r.readLine();
				if (s != null){
					out.println(s); // EchoServerへ一行送信
					out.flush();
					if (s.equals("quit")){
						socket.close();
					}
				}
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
			//oclient.receiveMessage(s);
			System.out.println("\nClientDriver:テストメッセージ「" + s + "」を受信しました");
			System.out.println("ClientDriver:テスト操作を行った後、受信用テストメッセージを入力してください\n");
		}

	}

}