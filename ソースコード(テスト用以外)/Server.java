import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server{
	private int port; // サーバの待ち受けポート
	private boolean [] online; //オンライン状態管理用配列		// クライアントの接続状況
	private PrintWriter [] out; //データ送信用オブジェクト
	private Receiver [] receiver; //データ受信用オブジェクト

	private final static int CLIENT_NUM = 30;
	private String[] playerNames;	//プレイヤ名
	private String[] modes;		//対戦モード
	private int [][] ClientPare = new int[CLIENT_NUM/2][2];		//対戦ペア

	//対戦相手のマッチング待ちであるクライアントの識別番号
	private int waitingNormalClient = -1;
	private int waitingMoneyClient = -1;

	class Prompt extends Thread {
		Scanner stdin = new Scanner(System.in);
		String command;
		Prompt() {
		}
		public void run() {
			while (true) {
				command = stdin.next();
				if (command.equals("printstatus")) {
					printStatus();
				}
			}
		}
	}
	private Prompt pt;

	public boolean getLoginInfo(String message, int playerNo) {
		String DELIMITER = ":";	// 区切り文字
		String[] name_and_mode = message.split(DELIMITER);
		String playerName="",  mode="";
		if(name_and_mode.length == 2) {
			playerName = name_and_mode[0];
			mode = name_and_mode[1];
		} else if(name_and_mode.length > 2) {
			int p = message.lastIndexOf(DELIMITER);
			playerName = message.substring(0, p);
			mode = message.substring(p+1, message.length());
		} else {
			System.out.println("不正なログイン情報");
			quit(playerNo);
			return false;
		}

		System.out.println("name:" + playerName + "\tmode:" + mode);
		if(isUsedName(playerName) == false) {	//名前が使用可能な場合
			playerNames[playerNo] = playerName;
			modes[playerNo] = mode;
			System.out.println("login permitted\r\n");
			return true;
		} else {	// 名前すでに使われている場合
			// クライアントに名前が使えないこと伝達.
			out[playerNo].print("Name is not available\r\n");
			out[playerNo].flush();
			System.out.println("login denied\r\n");
			quit(playerNo);
			return false;
		}
	}

	//プレイヤ名の比較
	public boolean isUsedName(String playerName){
		for(int i = 0; i < playerNames.length; i++)
			if (playerName.equals(playerNames[i]))
				return true;
		return false;
	}

	//対戦相手の検索
	public void searchOpponent(int playerNo, String mode){
		if(mode.equals("normal")) {				//通常のオセロモード
			if (waitingNormalClient == -1)		//マッチング待ちのクライアントがいない場合
				waitingNormalClient = playerNo;
			else {
				int pare = getAvailablePareNo();
				if (pare != -1) {
					ClientPare[pare][0] = waitingNormalClient;
					ClientPare[pare][1] = playerNo;
					sendColor(waitingNormalClient, "black:"+playerNames[playerNo]);
					sendColor(playerNo, "white:" + playerNames[waitingNormalClient]);
				} else {	//対戦ペアが作れない場合
					quit(waitingNormalClient);
					quit(playerNo);
				}
				waitingNormalClient = -1;
				printStatus();
			}
		}
		else if(mode.equals("time")) {	//Time is money モード
			if (waitingMoneyClient == -1)		//マッチング待ちのクライアントがいない場合
				waitingMoneyClient = playerNo;
			else {
				int pare = getAvailablePareNo();
				if (pare != -1) {
					ClientPare[pare][0] = waitingMoneyClient;
					ClientPare[pare][1] = playerNo;
					sendColor(waitingMoneyClient, "black:" + playerNames[playerNo]);
					sendColor(playerNo, "white:" + playerNames[waitingMoneyClient]);
				} else {
					quit(waitingNormalClient);
					quit(playerNo);
				}
				waitingMoneyClient = -1;
				printStatus();
			}
		}
	}



	// 識別番号playerNoの対戦相手の識別番号を返す. なければ-1を返す.
	public int getOpponent(int playerNo) {
		for (int i=0; i < ClientPare.length; i++) {
			if ( ClientPare[i][0] == playerNo)
				return ClientPare[i][1];
			else if ( ClientPare[i][1] == playerNo)
				return ClientPare[i][0];
		}
		return -1;
	}

	// 利用可能な（対戦中でない）識別番号を返す.
	public int getAvailablePlayerNo(){
		for(int i=0; i < online.length; i++) {
			if (online[i] == false) {
				online[i] = true;
				return i;
			}
		}
		return -1;
	}
	//プレイヤの識別番号に対するペア番号を返す.
//	識別番号playerNoが何番目のペアかを返す.
	public int getPare(int playerNo){

		for(int i=0; i < ClientPare.length; i++)
			if (ClientPare[i][0]==playerNo || ClientPare[i][1]==playerNo)
				return i;
		return -1;
	}

	//利用可能なペア番号を返す.
	public int getAvailablePareNo(){
		for(int i=0; i < ClientPare.length; i++)
			if(ClientPare[i][0] == -1 && ClientPare[i][1]==-1)
				return i;
		return -1;
	}


	//コンストラクタ
	public Server(int port) { //待ち受けポートを引数とする
		this.port = port; //待ち受けポートを渡す
		out = new PrintWriter [CLIENT_NUM]; //データ送信用オブジェクトをCLIENT_NUM個クライアント分用意
		receiver = new Receiver [CLIENT_NUM]; //データ受信用オブジェクトをCLIENT_NUM個クライアント分用意
		online = new boolean[CLIENT_NUM]; //オンライン状態管理用配列を用意
		playerNames = new String[CLIENT_NUM];
		modes = new String[CLIENT_NUM];
		ClientPare = new int[15][2];
		for(int i=0; i<ClientPare.length; i++)
			for(int j=0; j<2; j++)
				ClientPare[i][j] = -1;
		pt = new Prompt();
	}

	// データ受信用スレッド(内部クラス)
	class Receiver extends Thread {
		private InputStreamReader sisr; //受信データ用文字ストリーム
		private BufferedReader br; //文字ストリーム用のバッファ
		private int playerNo; //プレイヤを識別するための番号

		// 内部クラスReceiverのコンストラクタ
		Receiver (Socket socket, int playerNo){
			try{
				this.playerNo = playerNo; //プレイヤ番号を渡す
				System.out.println("playerNo = " + playerNo);
				sisr = new InputStreamReader(socket.getInputStream());
				br = new BufferedReader(sisr);
			} catch (IOException e) {
				System.err.println("データ受信時にエラーが発生しました: " + e);
			}
		}
		// 内部クラス Receiverのメソッド
		public void run(){
			try{
				boolean matchingok = true;
				boolean othermessage = false;
				boolean logindenied = false; //ログインに失敗したときtrueとする変数
				while(true) {	//ログイン処理
					String logininfo = br.readLine();
					if (logininfo ==null)
						continue;
					if (getLoginInfo(logininfo, playerNo) == true) {
						out[playerNo].print("login success\r\n");
						out[playerNo].flush();
						break;
					}
					else {
						logindenied = true;
					}
				}
				searchOpponent(playerNo, modes[playerNo]);
				String matchinginfo = null;
				while(logindenied == false) {	//ログインに成功した場合、対戦の接続を待機
					if (othermessage == false)
						matchinginfo = br.readLine();//データを一行分読み込む
					if (matchinginfo != null){ //データを受信したら
						if (matchinginfo.equals("quit")) {
							disconnect(playerNo);
							playerNo += CLIENT_NUM; //quit()で対戦相手のマッチング待ちであるクライアントの識別番号の初期化処理を行うようにする
							quit(playerNo);
							matchingok = false; //このスレッドを終了する
							break;
						}
						else {
							othermessage = true;
						}
					}
					if (getOpponent(playerNo) != -1)
						break;
				}
				//printStatus(); //接続状態を出力する

				while(logindenied == false && matchingok == true) {// データを受信し続ける(ログインに成功した場合のみ)
					String inputLine = null;
					if (othermessage == false) {
						inputLine = br.readLine();//データを一行分読み込む
					}
					else {
						inputLine = new String(matchinginfo);
						othermessage = false;
					}
					if (inputLine != null){ //データを受信したら
						if (getOpponent(playerNo) != -1)
							forwardMessage(inputLine, playerNo); //もう一方に転送する
						//if (inputLine.equals("quit") || inputLine.equals("resign")) {
						if (inputLine.equals("quit")) {
							disconnect(playerNo);
							quit(playerNo);
							break;
						}
					}
				}
			} catch (IOException e){ // 接続が切れたとき
				System.err.println("プレイヤ " + playerNo + "との接続が切れました．");
				if (getOpponent(playerNo) != -1) //対戦相手がいる場合は切断を知らせる
					forwardMessage("quit", playerNo);
				else //対戦相手がいない場合
					playerNo += CLIENT_NUM; //quit()で対戦相手のマッチング待ちであるクライアントの識別番号の初期化処理を行うようにする
				quit(playerNo);
				//printStatus(); //接続状態を出力する
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// メソッド
	public void acceptClient(){ //クライアントの接続(サーバの起動)
		try {
			System.out.println("サーバが起動しました．");
			ServerSocket ss = new ServerSocket(port); //サーバソケットを用意


			while (true) {

				Socket socket = ss.accept(); //新規接続を受け付ける
				System.out.println("クライアントとの接続開始");
				int playerNo = getAvailablePlayerNo();

				if (playerNo != -1) {	//最大接続数を超えてない場合
					out[playerNo] = new PrintWriter(socket.getOutputStream(), true);
					receiver[playerNo] = new Receiver(socket, playerNo);
					receiver[playerNo].start();
				} else {
					System.out.println("最大接続数を超過しているため接続を切断");
					PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
					pw.print("room is max\r\n");
					pw.flush();
					pw.close();
					socket.close();
				}
			}
		} catch (Exception e) {
			System.err.println("ソケット作成時にエラーが発生しました: " + e);
		}
	}

	public void printStatus(){ //クライアント接続状態の確認
		System.out.println("クライアントの接続状況");
		for (int i = 0; i < online.length; i++ )
			if (online[i])
				System.out.println("payerNo"+ i + " is connected");
		for(int i = 0; i < ClientPare.length; i++) {
			int p1 = ClientPare[i][0], p2 = ClientPare[i][1];
			if (p1!=-1 && p2!=-1)
				System.out.printf("PlayerNo%d[%s] vs PlayerNo%d[%s] : Mode:%s\n", p1, playerNames[p1], p2, playerNames[p2], modes[p1]);
//			if(ClientPare[i][0]!=-1 && ClientPare[i][1] != -1)
//				System.out.printf("Player%d vs Player%d : Mode:%s\n", ClientPare[i][0], ClientPare[i][1], modes[ClientPare[i][0]]);

		}
		System.out.println("");
	}

	public void sendColor(int playerNo, String color){ //先手後手情報(白黒)の送信
		out[playerNo].print(color + "\r\n");
		out[playerNo].flush();
	}

	public void forwardMessage(String msg, int playerNo){ //操作情報の転送	//盤面情報の送受信
		int dest_playerNo = getOpponent(playerNo);	//送信先の識別番号
		if (dest_playerNo != -1) {
			out[dest_playerNo].print(msg + "\r\n");
			out[dest_playerNo].flush();
			System.out.println("playerNo"+playerNo + " send \"" + msg + "\" to playerNo"+dest_playerNo);
		} else {
			System.out.println("playerNo" + playerNo +":"+ msg );
		}

	}
	//対局終了処理
	public void quit(int playerNo){
		boolean matchingok = true;
		if(playerNo >= CLIENT_NUM) {
			matchingok = false;
			playerNo -= CLIENT_NUM;
		}
		online[playerNo] = false;
		playerNames[playerNo] = "";
		int pare = getPare(playerNo);
		if(pare!=-1) {
			ClientPare[pare][0]=-1;
			ClientPare[pare][1]=-1;
		}
		else if (matchingok == false) { //ログイン済かつ対戦相手がいない場合は対戦相手のマッチング待ちであるクライアントの識別番号を初期化する
			if (modes[playerNo] != null) {
				if (modes[playerNo].equals("normal")) {
					waitingNormalClient = -1;
				}
				else {
					waitingMoneyClient = -1;
				}
			}
		}
		modes[playerNo] = "";

		System.out.println("playerNo"+playerNo + "を終了");

		//printStatus();
	}
	//クライアントの切断
	public void disconnect(int playerNo) throws IOException {
		receiver[playerNo].sisr.close();
		out[playerNo].close();
		System.out.println("playerNo" + playerNo + "を切断");
	}



	public static void main(String[] args){ //main
		Server server = new Server(10000); //待ち受けポート10000番でサーバオブジェクトを準備
		server.pt.start();
		server.acceptClient(); //クライアント受け入れを開始
	}
}