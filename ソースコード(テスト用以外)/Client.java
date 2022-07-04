import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.TextArea;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class Client extends JFrame{

	private String myName = ""; //プレイヤ名
	private String myMode = ""; //対戦モード
	private String myColor = ""; //先手後手情報(白黒)
	private String opponentName = ""; //相手のプレイヤ名
	private PrintWriter out;//データ送信用オブジェクト
	private Receiver receiver; //データ受信用オブジェクト
	private static JLayeredPane activePane = null; //アクティブなレイヤーペインへの参照を保持する変数
	private String title_message = ""; //受信したメッセージを保持する文字列(タイトル～ログイン用)
	private static Clip bgmclip = null; //BGM再生用のクリップ
	private static final int SOUNDEFFECT_NUM = 9; //使用する効果音の種類
	private static Clip seclip[] = new Clip[SOUNDEFFECT_NUM]; //効果音再生用のクリップ
	private static boolean seopened = false; //効果音ファイルを開いたらtrueにする変数
	private static boolean playsound = true; //音を流すかの情報を保持する変数
	private static int soundVolume = 50;	//BGMと効果音の音量
	private boolean LoginSuccess = false; //ログインに成功した場合にtrue
	private boolean roommax = false; //サーバ同時接続可能人数が超過した場合にtrue
	private boolean matchingnow = false; //マッチング中にtrueとしておくフラグ
	private boolean matchingok = false; //マッチングが完了した際にtrueとするフラグ
	private boolean serverdown = false; //マッチング中にサーバ接続が切断された際にtrueとするフラグ
	private ChatJFrame chf = null;	//チャット画面表示用オブジェクト
	//chatDispFlg:
	//自分のチャット画面の表示をしたらtrueにする、双方のタイマーが表示されたらfalseにする(タイマー同期用変数)
	private boolean chatDispFlg = false;
	//private Toolkit toolkit = getToolkit();
	//private Dimension dimension = toolkit.getScreenSize(); //画面の大きさを取得
	private Socket socket = null;
	private int starttime = 10;//タイマーの仮の初期値。分単位
	private int[] mnum= {15,5,8,2};
	String [][] grids = new String[8][8]; //getGridメソッドにより局面情報を取得
	private gameJFrame gmf = null;//対戦画面表示用オブジェクト
	//gameDispFlg:
	//自分の対局画面の表示をしたらtrueにする、双方のタイマーが表示されたらfalseにする(タイマー同期用変数)
	private boolean gameDispFlg = false;
	private static String turn = "";//手番
	private Normal_Othello nothello = new Normal_Othello();//ノーマルオセロ用オブジェクト
	private TIME_Othello tothello = new TIME_Othello();//一刻千金モード用オブジェクト
	private static GameTimer gt1;
	private static GameTimer gt2;
	//private boolean resign = false;
	private boolean gameEnd = false;
	private int lastTime1,lastTime2;
	private int score1,score2;
	//private boolean myDisp = false;
	//private boolean opponentDisp = false;


	public Client() {
	}

	public void titleDisp(){	//タイトル・ログイン画面の表示
		class titleJFrame extends JFrame implements ChangeListener, MouseListener {
			private JButton normalButton, timeButton; //対戦モード選択用ボタン
			private JButton soundButton; //音を流すかの切替用ボタン
			private JSlider soundSlider;	//音量を調節するスライダー
			private JLabel soundSliderLabel;	//音量を表示するラベル
			private JLabel background_title; // タイトル背景用ラベル
			private JLayeredPane titlep; //タイトル画面のレイヤーペイン
			private ImageIcon titleIcon; //アイコン
			//private ImageIcon normalIcon, timeIcon; //アイコン
			private JFrame loginf; //ログイン画面のフレーム
			private JPanel loginp; //ログイン画面のパネル
			private JLabel loginl1; //ログイン画面のラベル
			private JLabel loginl2; //ログイン画面のラベル
			private JTextField logintf; //ログイン画面の名前入力欄
			private JButton loginb_ok; //ログイン画面のOKボタン
			private JButton loginb_return; //ログイン画面の、スタート画面に戻るボタン
			private JFrame matchf = new JFrame("マッチング中…"); //マッチング中の画面のフレーム
			private boolean receiver_titleActive = true; //タイトル画面でデータ受信を検知するためのフラグ
			private boolean matchingFailed = false; //対戦相手がいなかった場合にtrueとするフラグ

			// コンストラクタ
			public titleJFrame() {
				//ウィンドウ設定
				try {
					UIManager.setLookAndFeel(new MetalLookAndFeel());	//Mac用のLook&Feel更新処理
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
				setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じる場合の処理
				setTitle("一石千金");//ウィンドウのタイトル
				setSize(524, 712);//ウィンドウのサイズを設定
				//setLocation(dimension.width / 2 - 262, dimension.height / 2 - 356); //ウィンドウを画面中央に配置する
				setLocationRelativeTo(activePane); //ウィンドウを画面中央もしくは最後に表示していたウィンドウの中央に配置する
				titlep = new JLayeredPane();
				getContentPane().add(titlep); //フレームのペインにpを追加
				activePane = titlep; //アクティブなレイヤーペインを更新

				//アイコン設定(画像ファイルをアイコンとして使う)
				titleIcon = new ImageIcon("title.jpg");
				//normalIcon = new ImageIcon("title_NormalButton.jpg");
				//timeIcon = new ImageIcon("title_TIMEButton.jpg");
				background_title = new JLabel(titleIcon); //タイトル背景用のラベルのアイコンを設定
				normalButton = new JButton("ノーマルオセロで遊ぶ"); //ノーマルモード選択用のボタンを生成
				timeButton = new JButton("一石千金オセロで遊ぶ"); //一刻千金モード選択用のボタンを生成

				//ラベル・ボタン設定
				titlep.setLayout(null);
				titlep.add(background_title); //タイトル背景用のラベルをペインに貼り付け
				background_title.setBounds(0, 0, 510, 675);
				titlep.add(normalButton); //ノーマルモード選択用のボタンをペインに貼り付け
				normalButton.setBounds(87, 450, 335, 54);//ボタンの大きさと位置を設定する．
				normalButton.addMouseListener(this);//マウス操作を認識できるようにする
				normalButton.setActionCommand("normal");//ボタンを識別するための名前を付加する
				normalButton.setBackground(new Color(0, 153, 0)); //ボタンの背景の色を設定
				normalButton.setOpaque(true);
				normalButton.setFont(new Font("ＭＳ 明朝", Font.BOLD, 24)); //ボタンのフォントを設定
				titlep.add(timeButton); //一刻千金モード選択用のボタンをペインに貼り付け
				timeButton.setBounds(87, 550, 335, 54);//ボタンの大きさと位置を設定する．
				timeButton.addMouseListener(this);//マウス操作を認識できるようにする
				timeButton.setActionCommand("time");//ボタンを識別するための名前を付加する
				timeButton.setBackground(Color.yellow); //ボタンの背景の色を設定
				timeButton.setOpaque(true);
				timeButton.setFont(new Font("ＭＳ 明朝", Font.BOLD, 24)); //ボタンのフォントを設定
				soundButton = new JButton("SOUND : ON ");
				titlep.add(soundButton); //音を流すかの切替用ボタンをペインに貼り付け
				soundButton.setFocusPainted(false);
				soundButton.setBounds(10, 10, 200, 40);//ボタンの大きさと位置を設定する．
				soundButton.addMouseListener(this);//マウス操作を認識できるようにする
				soundButton.setActionCommand("sound");//ボタンを識別するための名前を付加する
				soundButton.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
				if (playsound == true) {
					soundButton.setBackground(new Color(243,204,185));
				}
				else {
					soundButton.setText("SOUND : OFF"); //ボタンの表示を切り替える
					soundButton.setBackground(new Color(176,190,219));
				}
				soundButton.setOpaque(true);
				soundSlider = new JSlider(0, 100, soundVolume);	//音量を調節するスライダー
				titlep.add(soundSlider);
				soundSlider.setBounds(230, 30, 200, 30);
				soundSlider.addChangeListener(this);
				soundSlider.setOpaque(true);	//不透明にする
				soundSlider.setBackground(new Color(178, 120, 107));
				soundSliderLabel = new JLabel(" SOUND VOLUME : " + this.soundSlider.getValue());
				soundSliderLabel.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 16));
				soundSliderLabel.setBounds(230, 10, 200, 20);
				soundSliderLabel.setOpaque(true);	//不透明にする
				if(playsound == true) {
					soundSlider.setEnabled(true);
					if(soundVolume > 0) {
						soundSliderLabel.setEnabled(true);
					}
					else {
						soundSliderLabel.setEnabled(false);
					}
				}
				else {
					soundSlider.setEnabled(false);
					soundSliderLabel.setEnabled(false);
				}
				soundSliderLabel.setBackground(new Color(178, 120, 107));
				titlep.add(soundSliderLabel);

				titlep.moveToBack(background_title); //背景を一番後ろに変更

				//受信メッセージ確認用スレッドの作成と起動
				//Receiver_title rt = new Receiver_title();
				//rt.start();
			}

			public void stateChanged(ChangeEvent e) {
				//音量を変更したときの処理
				if(e.getSource() == this.soundSlider && this.soundSlider.isEnabled() == true) {
					soundVolume = this.soundSlider.getValue();
					Client.this.setVolume(soundVolume);
					this.soundSliderLabel.setText(" SOUND VOLUME : " + soundVolume);
					if(soundVolume > 0) {
						this.soundSliderLabel.setEnabled(true);
					}
					else {
						this.soundSliderLabel.setEnabled(false);
					}
				}
			}

			//ログイン画面表示
			public void loginDisp(JLabel loginl1, int loginl1x, JLabel loginl2, int loginl2x) { //引数にラベルとラベルの表示位置のx座標を受け取る
				loginf = new JFrame("名前の入力");
				loginf.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				loginf.setSize(205, 200); //ウィンドウのサイズを設定
				//loginf.setLocation(dimension.width / 2 - 102, dimension.height / 2 - 90); //ウィンドウを画面中央に配置する
				loginf.setLocationRelativeTo(activePane); //タイトル画面の中央に表示
				loginp = new JPanel();
				loginp.setLayout(null);
				loginf.add(loginp); //フレームにパネルを追加
				loginp.add(loginl1); //パネルにラベルを追加
				loginl1.setBounds(loginl1x, 0, 300, 25); //ラベルの位置を設定
				loginp.add(loginl2);
				loginl2.setBounds(loginl2x, 20, 300, 25); //ラベルの位置を設定
				logintf = new JTextField(10);
				loginp.add(logintf); //パネルに名前入力欄を追加
				logintf.setBounds(10, 50, 170, 25); //名前入力欄の位置を設定
				loginb_ok = new JButton("OK");
				loginp.add(loginb_ok); //パネルにOKボタンを追加
				loginb_ok.setBounds(20, 80, 150, 25); //OKボタンの位置を設定
				loginb_ok.addMouseListener(this); //マウス操作を認識できるようにする
				loginb_ok.setActionCommand("login_OK"); //ボタンを識別するための名前を付加する
				loginb_return = new JButton("スタート画面に戻る");
				loginp.add(loginb_return); //パネルにスタート画面に戻るボタンを追加
				loginb_return.setBounds(20, 110, 150, 25); //スタート画面に戻るボタンの位置を設定
				loginb_return.addMouseListener(this); //マウス操作を認識できるようにする
				loginb_return.setActionCommand("login_return"); //ボタンを識別するための名前を付加する
				loginf.setVisible(true);
			}

			//マッチング中の画面表示
			public void matchingDisp(JFrame matchf) {
				matchf.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				matchf.setSize(195, 110); //ウィンドウのサイズを設定
				//matchf.setLocation(dimension.width / 2 - 97, dimension.height / 2 - 55); //ウィンドウを画面中央に配置する
				matchf.setLocationRelativeTo(activePane); //タイトル画面の中央に表示
				JPanel matchp = new JPanel();
				matchp.setLayout(null);
				matchf.add(matchp); //フレームにパネルを追加
				JLabel matchl1 = new JLabel("マッチング中です");
				matchp.add(matchl1); //パネルにラベルを追加
				matchl1.setBounds(20, 0, 300, 25); //ラベルの位置を設定
				JLabel matchl2 = new JLabel("しばらくお待ちください");
				matchp.add(matchl2); //パネルにラベルを追加
				matchl2.setBounds(20, 20, 300, 25); //ラベルの位置を設定
				matchf.setVisible(true);
			}
			//マッチング失敗時の画面表示
			/*public void matchingFailedDisp() {
				JFrame f = new JFrame("マッチング失敗");
				f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				f.setSize(195, 150); //ウィンドウのサイズを設定
				//f.setLocation(dimension.width / 2 - 97, dimension.height / 2 - 75); //ウィンドウを画面中央に配置する
				f.setLocationRelativeTo(p); //タイトル画面の中央に表示
				JPanel p = new JPanel();
				p.setLayout(null);
				f.add(p); //フレームにパネルを追加
				JLabel l1 = new JLabel("対戦相手がみつかりま");
				p.add(l1); //パネルにラベルを追加
				l1.setBounds(20, 0, 300, 25); //ラベルの位置を設定
				JLabel l2 = new JLabel("せんでした");
				p.add(l2); //パネルにラベルを追加
				l2.setBounds(20, 20, 300, 25); //ラベルの位置を設定
				JLabel l3 = new JLabel("スタート画面に戻ります");
				p.add(l3); //パネルにラベルを追加
				l3.setBounds(20, 40, 300, 25); //ラベルの位置を設定
				JButton b_ok = new JButton("OK");
				p.add(b_ok); //パネルにOKボタンを追加
				b_ok.setBounds(45, 75, 90, 25); //OKボタンの位置を設定
				b_ok.addActionListener(new ActionListener() { //マウス操作を認識できるようにする
					public void actionPerformed(ActionEvent e) {
						soundEffect("push");
						message = new String("restart");
						f.dispose();
					}
				});
				f.setVisible(true);
			}*/

			//ログイン時の処理
			public void loginok(boolean ok) { //okがtrueならログイン成功、falseならログイン失敗
				if(ok == true) {
					System.out.println("ログイン成功。マッチングを開始します。");
					matchingDisp(matchf);
				}
				else {
					System.out.println("ログイン失敗");
					loginl1 = new JLabel("同じ名前が使われています");
					loginl2 = new JLabel("名前を入力し直してください");
					loginDisp(loginl1, 25, loginl2, 20);
				}
			}

			//サーバ同時接続可能人数が超過したことを通知
			public void showroommax() {
				soundEffect("alert");
				JOptionPane.showMessageDialog(this, "部屋が満員で入れませんでした", "満室です",
						JOptionPane.WARNING_MESSAGE);
				soundEffect("push");
				roommax = false;
				receiver_titleActive = false;
				Client oclient = new Client(); //起動し直す
				oclient.titleDisp();
				dispose();
			}

			//マッチング中にサーバ接続が切れたことを通知
			public void showserverdown() {
				soundEffect("alert");
				JOptionPane.showMessageDialog(this, "サーバ接続が切断されました", "サーバ接続エラー",
						JOptionPane.WARNING_MESSAGE);
				soundEffect("push");
				serverdown = false;
				receiver_titleActive = false;
				Client oclient = new Client(); //起動し直す
				oclient.titleDisp();
				dispose();
			}

			class Receiver_title extends Thread {
				public void run(){
					try {
						while(receiver_titleActive == true) {//データを受信し続ける
							if (LoginSuccess == true) {
								LoginSuccess = false;
								loginok(true);
								matchingnow = true;
								for (int t = 60; t > 0 ;t--) { //マッチングを待機する
									if (title_message.startsWith("black") || title_message.startsWith("white")) {	//自分の色を受信したらマッチング完了とする
										myColor = new String(title_message.substring(0, 5));
										title_message = new String("OK");
										break;
									}
									if (serverdown == true) break;
									sleep(1000);
								}
								matchingnow = false;
								matchf.dispose();
								if (serverdown == false) {
									if (!(title_message.equals("OK"))) {
										matchingFailed = true;
										//message = new String("restart");
										try {
											if (receiver != null) {
												sendMessage("quit");
												socket.close(); //サーバへの接続を切断する
												receiver.accept = false;
											}
										} catch(IOException e){
											System.err.println("サーバ接続切断時にエラーが発生しました: " + e);
											e.printStackTrace();
										}
										//マッチング相手がいなかった場合はそれを表示
										//matchingFailedDisp();
										soundEffect("alert");
										JOptionPane.showMessageDialog(titleJFrame.this,
												"対戦相手が見つかりませんでした\nスタート画面に戻ります", "マッチング失敗",
												JOptionPane.WARNING_MESSAGE);
										soundEffect("push");
										title_message = new String("restart");
									}
									else {
										receiver_titleActive = false;
										matchingok = true;
										chf = new ChatJFrame();
										sleep(1000);
										chf.chatDisp("CHAT_WINDOW");	//チャット画面の表示
										/*if(myColor.startsWith("white")) {	//白だと表示が遅いので
											sendMessage("complete chatDisp");	//表示が終わったことを相手(黒)に伝えて
											chf.chat_timer.start();	//チャットタイマースタート
										}*/
										dispose();
									}
								}
								else {
									showserverdown();
								}
							}
							else if (title_message.equals("Name is not available")) {
								loginok(false);
								title_message = new String("");
							}
							else if (title_message.equals("restart")) {
								normalButton.setEnabled(true);
								timeButton.setEnabled(true);
								soundButton.setEnabled(true);
								/*try {
									if (receiver != null) {
										sendMessage("quit");
										socket.close(); //サーバへの接続を切断する
										receiver.accept = false;
									}
								} catch(IOException e){
									System.err.println("サーバ接続切断時にエラーが発生しました: " + e);
								}*/
								title_message = new String("");
							}
							if (roommax == true) {
								if (matchingFailed == false) {
									showroommax();
								}
								else {
									roommax = false;
								}
							}
							sleep(10);
						}
					}
					catch(InterruptedException e){
					}
				}
			}

			//マウスクリック時の処理
			public void mouseClicked(MouseEvent e) {
				JButton theButton = (JButton)e.getComponent();//クリックしたオブジェクトを得る．キャストを忘れずに
				String command = theButton.getActionCommand();//ボタンの名前を取り出す
				System.out.println("マウスがクリックされました。押されたボタンは " + command + "です。");//テスト用に標準出力
				if (command.equals("normal") || command.equals("time") || command.equals("sound")) { //タイトル画面の処理
					if (theButton.isEnabled() == true) { //ボタンが有効の場合のみ動作
						if (command.equals("normal") || command.equals("time")) { //モード選択がされた時
							soundEffect("title");
							normalButton.setEnabled(false); //モード選択後はボタンを押せないようにする
							timeButton.setEnabled(false); //モード選択後はボタンを押せないようにする
							soundButton.setEnabled(false); //モード選択後はボタンを押せないようにする
							myMode = command;
							loginl1 = new JLabel("名前を入力してください");
							loginl2 = new JLabel("(10文字以内)");
							loginDisp(loginl1, 30, loginl2, 60);
						}
						else { //音を流すかの切替用ボタンが押された時
							playsound = !playsound;
							if (playsound == false) { //音が流れる状態だった場合
								soundButton.setText("SOUND : OFF"); //ボタンの表示を切り替える
								soundButton.setBackground(new Color(176,190,219));
								soundSlider.setEnabled(false);	//soundSliderを使用できなくする
								soundSliderLabel.setEnabled(false);	//soundSliderLabelを灰色にする
								//soundVolume = 0;	//音量を0に設定(スライダーはリセットしない)
								//setVolume(soundVolume);
								bgm(""); //流れているBGMを止める
							}
							else { //音が流れない状態だった場合
								soundButton.setText("SOUND : ON "); //ボタンの表示を切り替える
								soundButton.setBackground(new Color(243,204,185));
								soundSlider.setEnabled(true);	//soundSliderを使用できるようにする
								soundSliderLabel.setEnabled(true);	//soundSliderLabelの色を戻す
								soundVolume = soundSlider.getValue();
								//if(soundVolume <= 0) {
								//	soundVolume = 100;
								//	soundSlider.setValue(100);
								//}
								soundEffect("open"); //効果音ファイルを開く
								bgm("title"); //BGMを流す
							}
						}
					}
				}
				else if (command.equals("login_OK") || command.equals("login_return")) { //ログイン画面の処理
					if (command.equals("login_OK")) { //ログイン画面でOKボタンが押された時
						if (!(myName = logintf.getText()).equals("")) {
							if (myName.length() <= 10) {
								soundEffect("push");
								loginf.dispose();
								if (receiver_titleActive == true) {	//既にReceiver_titleが起動している場合
									receiver_titleActive = false;	//起動済のReceiver_titleが終了するまで待つ
									try {
										Thread.sleep(100);
									} catch(InterruptedException ie) {
									}
								}
								receiver_titleActive = true;
								matchingFailed = false;
								Receiver_title rt = new Receiver_title();
								rt.start();
								try {
									connectServer("localhost", 10000); //サーバに接続
								}
								catch (IOException ioe){
									System.err.println("サーバ接続時にエラーが発生しました: " + e);
									ioe.printStackTrace();
									//connectServerFailedDisp();
									soundEffect("alert");
									JOptionPane.showMessageDialog(this, "サーバへの接続に失敗しました\nタイトル画面に戻ります", "サーバ接続失敗",
											JOptionPane.WARNING_MESSAGE);
									soundEffect("push");
									title_message = new String("restart");
								}
								if (!title_message.equals("restart")) {
									sendMessage(myName + ":" + myMode);
								}
							}
							else {
								soundEffect("alert");
								loginl1.setText("名前は10文字以内で");
								loginl1.setBounds(40, 0, 300, 25);
								loginl2.setText("入力してください");
								loginl2.setBounds(40, 20, 300, 25);
							}
						}
					}
					else { //ログイン画面でスタート画面に戻るボタンが押された時
						soundEffect("cancel");
						normalButton.setEnabled(true); //対戦モード選択用ボタンをまた押せるようにする
						timeButton.setEnabled(true);
						soundButton.setEnabled(true);
						loginf.dispose();
					}
				}
			}
			public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
			public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
			public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
			public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
		}

		if (seopened == false && playsound == true) { //まだ効果音ファイルを開いておらず、かつ音を流す設定である場合は
			soundEffect("open"); //効果音ファイルを開いておく
		}
		titleJFrame title = new titleJFrame();
		title.setVisible(true);
		bgm("title");
	}

	//サーバへの接続失敗画面表示
	/*public void connectServerFailedDisp() {
		JFrame f = new JFrame("サーバ接続失敗");
		f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		f.setSize(195, 150); //ウィンドウのサイズを設定
		f.setLocation(dimension.width / 2 - 97, dimension.height / 2 - 75); //ウィンドウを画面中央に配置する
		JPanel p = new JPanel();
		p.setLayout(null);
		f.add(p); //フレームにパネルを追加
		JLabel l1 = new JLabel("サーバへの接続に失敗");
		p.add(l1); //パネルにラベルを追加
		l1.setBounds(20, 0, 300, 25); //ラベルの位置を設定
		JLabel l2 = new JLabel("しました");
		p.add(l2); //パネルにラベルを追加
		l2.setBounds(20, 20, 300, 25); //ラベルの位置を設定
		JLabel l3 = new JLabel("スタート画面に戻ります");
		p.add(l3); //パネルにラベルを追加
		l3.setBounds(20, 40, 300, 25); //ラベルの位置を設定
		JButton b_ok = new JButton("OK");
		p.add(b_ok); //パネルにOKボタンを追加
		b_ok.setBounds(45, 75, 90, 25); //OKボタンの位置を設定
		b_ok.addActionListener(new ActionListener() { //マウス操作を認識できるようにする
			public void actionPerformed(ActionEvent e) {
				f.dispose();
				message = new String("restart");
			}
		});
		f.setVisible(true);
	}*/

	class ChatJFrame extends JFrame implements ActionListener, ChangeListener, MouseListener {
		private JLayeredPane chat_layer; //チャット画面のレイヤ
		private JLabel background;	//背景画像

		private JPanel chat_p;	//チャットメッセージ用のパネル
		private TextArea chat_area;	//チャットメッセージ表示用のテキストエリア

		private JPanel time_p;	//制限時間決定の枠のパネル
		private JLabel time_label1;	//制限時間の条件を表示するラベル
		private JLabel time_label2;	//"分"と表示するラベル
		private JTextField time_text;	//制限時間を入力するTextField
		private JButton time_ok;	//制限時間を決定するボタン

		private JTextField msg_text;	//メッセージを入力するTextField
		private JButton send_button;	//メッセージの送信ボタン

		private JButton time_yes;	//制限時間確認で"はい"
		private JButton time_no;	//制限時間確認で"いいえ"

		private JSlider soundSlider;	//音量を調節するスライダー
		private JLabel soundSliderLabel;	//音量を表示するラベル

		private Timer chat_timer;	//チャットタイマー
		private int sec;	//チャットの経過時間
		private static final int DEFAULT_SEC = 180;	//タイマーの制限時間
		private JPanel timer_p;	//タイマー用のパネル
		private JLabel timer_label1;	//"チャットの残り時間"と表示するラベル
		private JLabel timer_label2;	//チャットタイマーを表示

		private JFrame time_ask;	//時間決定確認画面のフレーム
		private JLabel time_ask_label;	//時間決定確認画面のラベル

		private JFrame time_waiting;	//相手の時間決定承認待ち画面のフレーム
		private JLabel time_waiting_label;	//相手の時間決定承認待ち画面のラベル

		private JFrame time_check;	//時間決定確認結果のフレーム
		private JLayeredPane time_check_layer;	//時間決定確認結果画面のレイヤ
		private JLabel time_check_label;
		private JButton time_check_ok;	//制限時間決定の確認ボタン

		public void chatDisp(String mode) {
			/*****************************接続の時間切れを確認***********************************/
			if(mode.equals("CONNECTION_TIME_OVER")) {
				JOptionPane.showMessageDialog(this, "サーバの接続に失敗しました\nタイトル画面に戻ります");
			}
			/***********************対戦相手の有無の結果メッセージ表示***************************/
			else if(mode.equals("MATCH_YES_NO")) {
				JOptionPane.showMessageDialog(this, "対戦相手が見つかりました");
				JOptionPane.showMessageDialog(this, "対戦相手が見つかりませんでした\nタイトル画面に戻ります");
			}
			/******************************チャット画面等の表示*********************************/
			else if(mode.equals("CHAT_WINDOW")){
				this.windowDisp(mode);
				this.setVisible(true);
			}
			else if(mode.equals("TIME_ASK_WINDOW")){
				this.windowDisp(mode);
			}
			else if(mode.equals("TIME_CHECK_WINDOW")) {
				//chf2 = new ChatJFrame();
				this.windowDisp(mode);
				//this.setVisible(true);
			}
		}

		public void windowDisp(String mode) {
			/*****************************接続の時間切れを確認***********************************/
			//if(mode == "CONNECTION_TIME_OVER") {
				//JOptionPane.showMessageDialog(this, "サーバの接続に失敗しました\nタイトル画面に戻ります");
				/*JFrame not_connect = new JFrame("サーバ接続失敗");
				not_connect.setVisible(true);
				Container c = getContentPane();
				c.setLayout(null);
				JLabel
				String myName = JOptionPane.showInputDialog(null,"名前を入力してください","名前の入力",JOptionPane.QUESTION_MESSAGE);*/
			//}

			/***********************対戦相手の有無の結果メッセージ表示***************************/
			//else if(mode == "MATCH_YES_NO") {
				//JOptionPane.showMessageDialog(this, "対戦相手が見つかりました");
				//JOptionPane.showMessageDialog(this, "対戦相手が見つかりませんでした\nタイトル画面に戻ります");
			//}

			/*******************************チャット画面表示*************************************/
			if(mode.equals("CHAT_WINDOW")) {
				//ウィンドウ設定
				//setTitle("チャット" + myColor);
				setTitle("チャット" + myColor + " 自分: " + myName + " 相手: " + opponentName);
				setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じる場合の処理
				setSize(800, 600);	//チャットウィンドウのサイズ設定
				//setLocation(dimension.width / 2 - 400, dimension.height / 2 - 300); //ウィンドウを画面中央に配置する
				setLocationRelativeTo(activePane); //タイトル画面の中央に表示
				this.chat_layer = new JLayeredPane();
				getContentPane().add(this.chat_layer); //フレームのペインにチャット画面のレイヤを追加
				activePane = this.chat_layer; //アクティブなレイヤーペインを更新
				this.chat_layer.setLayout(null);	//コンポーネントを座標指定で置く

				//背景設定
				this.background = new JLabel(new ImageIcon("background.jpg"));
				this.background.setBounds(0, 0, 800, 900);
				this.background.setLayout(null);	//コンポーネントを座標指定で置く

				//チャットのメッセージラベルの表示
				this.chat_p = new JPanel();
				this.chat_p.setBounds(10, 10, 460, 330);

				//チャットのテキストエリアの表示
				//this.chat_area = new JTextArea("チャット\n");
				//this.chat_area = new TextArea("チャット\n", 20, 60);
				this.chat_area = new TextArea("チャット\n");
				this.chat_area.setPreferredSize(new Dimension(440, 310));
				this.chat_area.setBackground(new Color(255, 255, 200));
				this.chat_area.setFocusable(false);	//チャットのテキストエリアを編集不可に設定
				this.chat_p.add(chat_area);

				//チャットの利用方法の説明の表示
				this.chat_area.append("ここでは対局の持ち時間を何分にするかについて\n");
				this.chat_area.append("話し合ってください.\n");
				this.chat_area.append("対局では自分の手番である間持ち時間が減っていき,\n");
				this.chat_area.append("自分の持ち時間を全て使い切ってしまった場合,\n");
				this.chat_area.append("その時点で敗北となります.\n");
				if (myMode.equals("time")) {	//一石千金モードである場合は、一石千金モードのルールについての説明も行う
					this.chat_area.append("～一石千金モードについて～\n");
					this.chat_area.append("一石千金モードでは, 4種類の価格の石を\n");
					this.chat_area.append("自由に使い分けてオセロを行います.\n");
					this.chat_area.append("ただし, 石の種類ごとに\n");
					this.chat_area.append("自分が持っている個数が設定されており,\n");
					this.chat_area.append("所持数が0個である石を置くことはできません.\n");
					this.chat_area.append("また, 石の最初の所持数は種類ごとに異なります.\n");
					this.chat_area.append("勝敗は, 対局終了時点における\n");
					this.chat_area.append("「自分の石の合計金額+最終残り時間(秒数)」をスコアとし,\n");
					this.chat_area.append("そのスコアが高い方が勝利となります.\n");
					this.chat_area.append("石の価格は初期持ち時間に応じて\n");
					this.chat_area.append("適切なゲームバランスとなるように算出されます.\n");
				}

				//持ち時間決定の枠の表示
				this.time_p = new JPanel();
				this.time_p.setBounds(480, 10, 270, 300);
				this.time_p.setLayout(new FlowLayout());

				this.time_label1 = new JLabel("<html>持ち時間(５～１５分、<br>分刻み)を入力して<br>ください");
				this.time_label1.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 20));

				this.time_text = new JTextField();
				this.time_text.setPreferredSize(new Dimension(150, 50));
				this.time_text.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));

				this.time_label2 = new JLabel("分");
				this.time_label2.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));

				this.time_ok = new JButton("決定");
				this.time_ok.setPreferredSize(new Dimension(150, 100));
				this.time_ok.addMouseListener(this);

				this.time_p.add(this.time_label1);
				this.time_p.add(this.time_text);
				this.time_p.add(this.time_label2);
				this.time_p.add(this.time_ok);

				//送信メッセージのTextField
				this.msg_text = new JTextField();
				this.msg_text.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
				this.msg_text.setBounds(10, 370, 460, 50);

				//送信ボタン
				this.send_button = new JButton("メッセージ送信");
				this.send_button.setBounds(165, 450, 150, 50);
				this.send_button.addMouseListener(this);

				//タイマー
				this.sec = 0;
				this.chat_timer = new Timer(1000, this);
				this.timer_p = new JPanel();
				this.timer_p.setBounds(480, 330, 270, 120);
				this.timer_p.setLayout(null);
				this.timer_label1 = new JLabel("チャットの残り時間");
				this.timer_label1.setBounds(0, 0, 270, 30);
				this.timer_label1.setHorizontalAlignment(JLabel.CENTER);
				this.timer_label1.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 16));
				this.timer_label2 = new JLabel();
				this.timer_label2.setBounds(0, 40, 270, 60);
				this.timer_label2.setHorizontalAlignment(JLabel.CENTER);
				this.timer_label2.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 30));
				this.timer_p.add(this.timer_label1);
				this.timer_p.add(this.timer_label2);

				if (playsound == true) {
					//音量を調節するスライダー
					this.soundSlider = new JSlider(0, 100, soundVolume);
					this.soundSlider.setBounds(520, 490, 205, 30);
					this.soundSlider.addChangeListener(this);
					this.soundSlider.setOpaque(true);	//不透明にする(見辛かったので)
					this.soundSlider.setEnabled(true);
					this.chat_layer.add(this.soundSlider);

					//音量のラベル
					this.soundSliderLabel = new JLabel(" SOUND VOLUME : " + this.soundSlider.getValue());
					this.soundSliderLabel.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 16));
					this.soundSliderLabel.setBounds(520, 470, 205, 20);
					this.soundSliderLabel.setOpaque(true);	//不透明にする
					if(this.soundSlider.getValue() > 0) {
						this.soundSliderLabel.setEnabled(true);
					}
					else {
						this.soundSliderLabel.setEnabled(false);
					}
					this.chat_layer.add(this.soundSliderLabel);
				}

				//レイヤに貼り付け
				this.chat_layer.add(this.background);
				this.chat_layer.add(this.chat_p);
				this.chat_layer.add(this.time_p);
				this.chat_layer.add(this.msg_text);
				this.chat_layer.add(this.send_button);
				this.chat_layer.add(this.timer_p);
				this.chat_layer.moveToBack(this.background); //背景を一番後ろに変更

				//タイマー同期用処理
				if(chatDispFlg == false) {	//自分の方が先に対局画面を表示したら
					Client.this.sendMessage("complete chatDisp");
					Client.this.chatDispFlg = true;	//フラグ変数をtrueにする
				}
				else {	//相手の方が先に対局画面を表示したら
					Client.this.sendMessage("complete chatDisp ack");
					chf.chat_timer.start();	//チャットタイマースタート
					Client.this.chatDispFlg = false;	//フラグ変数をfalseにする(一応)
					bgm("chat");
				}
			}

			/**************************相手の時間決定承認待ち画面の表示*****************************/
			else if(mode.equals("TIME_WAITNG_WINDOW")) {
				this.time_waiting = new JFrame("制限時間承認待ち");
				this.time_waiting.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //閉じるボタンでは閉じないようにする
				this.time_waiting.setSize(400, 220);	//チャットウィンドウのサイズ設定
				this.time_waiting.setLocationRelativeTo(activePane); //チャット画面の中央に表示
				this.time_waiting.setLayout(null);

				//ラベルの表示
				this.time_waiting_label = new JLabel("<html>相手に希望持ち時間が<br>「" + this.time_text.getText()
						+ "分」<br>であることを伝えました.<br>相手の承認待ちです.");
				this.time_waiting_label.setHorizontalAlignment(JLabel.CENTER);
				this.time_waiting_label.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
				this.time_waiting_label.setBounds(10, 10, 380, 150);

				//コンポーネントの貼り付け
				this.time_waiting.add(this.time_waiting_label);

				this.time_waiting.setVisible(true);	//相手の時間決定承認待ち画面の可視化
			}

			//時間決定画面の表示
			/****************************時間決定確認画面の表示*******************************/
			else if(mode.equals("TIME_ASK_WINDOW")) {
				this.time_ok.setEnabled(false);	//持ち時間決定ボタンを無効化する
				this.time_text.setEditable(false);	//持ち時間入力欄を無効化する
				this.time_ask = new JFrame("制限時間の確認");
				//this.time_ask.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);//ウィンドウを閉じる場合の処理
				this.time_ask.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //閉じるボタンでは閉じないようにする
				this.time_ask.setSize(400, 300);	//チャットウィンドウのサイズ設定
				this.time_ask.setLayout(null);	//コンポーネントを座標指定で置く
				//this.time_ask.setLocationRelativeTo(null);
				this.time_ask.setLocationRelativeTo(activePane); //チャット画面の中央に表示

				//ラベルの表示
				this.time_ask_label = new JLabel("<html>制限時間は<br>「" + this.time_text.getText()
						+ "分」<br>でよろしいですか？");
				this.time_ask_label.setHorizontalAlignment(JLabel.CENTER);
				this.time_ask_label.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
				this.time_ask_label.setBounds(10, 10, 380, 150);

				//"はい"ボタンの表示
				this.time_yes = new JButton("はい");
				this.time_yes.setBounds(50, 150, 100, 50);
				this.time_yes.addMouseListener(this);

				//"いいえ"ボタンの表示
				this.time_no = new JButton("いいえ");
				this.time_no.setBounds(225, 150, 100, 50);
				this.time_no.addMouseListener(this);

				//コンポーネントの貼り付け
				this.time_ask.add(this.time_ask_label);
				this.time_ask.add(this.time_yes);
				this.time_ask.add(this.time_no);

				this.time_ask.setVisible(true);	//時間決定確認画面の可視化
			}


			/**************************時間決定確認結果の表示*****************************/
			else if(mode.equals("TIME_CHECK_WINDOW")) {
				this.chat_timer.stop();	//タイマーを止める
				//if (this.time_ok.isEnabled() == true) this.time_ok.setEnabled(false);	//もし持ち時間決定ボタンが有効になっている場合は無効化する
				this.msg_text.setEditable(false);	//チャットメッセージ入力欄を無効化する
				this.send_button.setEnabled(false);	//チャットメッセージ送信ボタンを無効化する
				if (this.time_ask != null) this.time_ask.dispose(); //時間決定確認画面が開いている場合は閉じる
				if (this.time_waiting != null) this.time_waiting.dispose();	//相手の時間決定承認待ち画面が開いている場合は閉じる
				this.time_check = new JFrame("制限時間の確認");
				//this.time_check.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);//ウィンドウを閉じる場合の処理
				this.time_check.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //閉じるボタンでは閉じないようにする
				this.time_check.setSize(400, 300);	//チャットウィンドウのサイズ設定
				//this.time_check.setLocationRelativeTo(null);	//ウィンドウを中央に開く
				this.time_check.setLocationRelativeTo(activePane); //チャット画面の中央に表示

				this.time_check_layer = new JLayeredPane();
				//this.time_check_layer.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				this.time_check_layer.setPreferredSize(new Dimension(400, 300));
				//this.time_check_layer.pack();
				getContentPane().add(this.time_check_layer); //フレームのペインに時間決定確認画面のレイヤを追加
				this.time_check_layer.setLayout(null);	//コンポーネントを座標指定で置く

				//ラベルの表示
				starttime = Integer.parseInt(chf.time_text.getText());
				this.time_check_label = new JLabel("<html>制限時間が<br>「" + starttime
						+ "分」<br>に決定しました！<br>対局を開始します！");
				this.time_check_label.setHorizontalAlignment(JLabel.CENTER);
				this.time_check_label.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
				this.time_check_label.setBounds(10, 10, 380, 150);

				//ボタンの表示
				this.time_check_ok = new JButton("対局開始");
				this.time_check_ok.setBounds(140, 180, 100, 50);
				this.time_check_ok.addMouseListener(this);

				//コンポーネントの貼り付け
				this.time_check_layer.add(this.time_check_label);
				this.time_check_layer.add(this.time_check_ok);

				this.time_check.add(this.time_check_layer);

				this.time_check_layer.setVisible(true);	//時間決定確認結果画面のレイヤの可視化
				this.time_check.setVisible(true);	//時間決定確認結果画面の可視化
			}
		}

		//タイマー設定
		public void actionPerformed(ActionEvent e) {
			int sec_now = DEFAULT_SEC - this.sec;	//残り時間を求める
			int rest_min = (int)(sec_now / 60);	//表示用の分
			int rest_sec = sec_now -(60 * rest_min);	//表示用の秒

			//以下残り時間の表示

			//残り1分未満になったら秒のみ表示
			if(sec_now < 60) {
				this.timer_label2.setText(rest_sec + "秒");
			}
			//残り1分以上なら分も表示
			else {
				this.timer_label2.setText(rest_min + "分" + rest_sec + "秒");
			}

			//制限時間が過ぎたとき
			if(sec_now <= 0) {
				this.chat_timer.stop();	//タイマーを止める
				this.time_text.setText("10");	//対局制限時間を１０分に設定
				windowDisp("TIME_CHECK_WINDOW");
			}
			//経過時間を１秒増やす
			else {
				this.sec++;
			}
		}

		public void stateChanged(ChangeEvent e) {
			//音量を変更したときの処理
			if(e.getSource() == this.soundSlider && this.soundSlider.isEnabled() == true) {
				//if(playsound == false) {	//SOUND:OFFだった場合
				//	playsound = true;	//SOUND:ONにする
				//	soundEffect("open"); //効果音ファイルを開く
				//}
				soundVolume = this.soundSlider.getValue();
				Client.this.setVolume(soundVolume);
				this.soundSliderLabel.setText(" SOUND VOLUME : " + soundVolume);
				if(soundVolume > 0) {
					this.soundSliderLabel.setEnabled(true);
				}
				else {
					this.soundSliderLabel.setEnabled(false);
				}
			}
		}

		public void mouseClicked(MouseEvent e) {
			/*************************制限時間の指定************************************/
			if(e.getSource() == this.time_ok && this.time_ok.isEnabled() == true) {
				String s = this.time_text.getText();	//制限時間の取り出し
				//int型への変換のtry
				try {
					int x = Integer.parseInt(s);	//int型へ変換
					if(x >= 5 && x <= 15) {	//正しい制限時間
						soundEffect("push");
						this.time_ok.setEnabled(false);
						this.time_text.setEditable(false);
						String time_str = this.time_text.getText();
						Client.this.sendMessage("time ask:" + time_str);
						windowDisp("TIME_WAITNG_WINDOW");
					}
					else {	//誤った制限時間
						soundEffect("alert");
						JOptionPane.showMessageDialog(this, "制限時間は５分～１５分(分刻み)で\n入力してください");
						soundEffect("push");
						this.time_text.setText("");	//TextFieldのリセット
					}
				}
				catch(Exception time_e) {	//変換の失敗
					time_e.printStackTrace();
					soundEffect("alert");
					JOptionPane.showMessageDialog(this, "制限時間は整数で入力してください");
					soundEffect("push");
					this.time_text.setText("");	//TextFieldのリセット
				}
			}
			/*************************メッセージの送信*********************************/
			else if(e.getSource() == this.send_button && this.send_button.isEnabled() == true) {
				//自分のメッセージの表示
				//自分のメッセージの表示
				//System.out.println("mousec:"+chf + "\n");
				String message = this.msg_text.getText();
				if (message.length() > 0) {
					Client.this.sendMessage("chat msg:" + message);
					chat_area.append(myName + "： "+ message + "\n");
					this.msg_text.setText("");
				}
				soundEffect("chat");
			}
			/*************************制限時間確認画面*********************************/
			else if(e.getSource() == this.time_yes) {
				//Component c = (Component)e.getSource();
				String time_str = this.time_text.getText();
				Client.this.sendMessage("time check:" + time_str);
				Window w = SwingUtilities.getWindowAncestor(this.time_yes);
				w.dispose();
				windowDisp("TIME_CHECK_WINDOW");
				soundEffect("push");
			}
			else if(e.getSource() == this.time_no) {
				this.time_text.setText("");
				this.time_ok.setEnabled(true);
				this.time_text.setEditable(true);
				Client.this.sendMessage("time check:NG");
				//Component c = (Component)e.getSource();
				Window w = SwingUtilities.getWindowAncestor(this.time_no);
				w.dispose();
				soundEffect("cancel");
			}
			/*************************制限時間決定確認画面*****************************/
			else if(e.getSource() == this.time_check_ok) {
				soundEffect("gamestart");
				gmf = new gameJFrame();

				gmf.windowDisp(myMode);
				Window w = SwingUtilities.getWindowAncestor(this.time_check_ok);
				w.dispose();
				w = SwingUtilities.getWindowAncestor(this.time_ok);
				w.dispose();
				chf = null;
				//
				//bgm("game");

				/*if(myColor.equals("black")) {
					System.out.println("自分のタイマー始動");
					gt1.start();
					gt2.start();
					gt2.setStop();
				}else{
					System.out.println("相手のタイマー始動");
					gt2.start();
					gt1.start();
					gt1.setStop();
				}*/

			}
		}
		public void mousePressed(MouseEvent e) {

		}
		public void mouseReleased(MouseEvent e) {

		}
		public void mouseEntered(MouseEvent e) {

		}
		public void mouseExited(MouseEvent e) {

		}
	}
	class gameJFrame extends JFrame implements ChangeListener, MouseListener {

		private Container c; // コンテナ
		private JButton buttonArray[][];//オセロ盤用のボタン配列
		private JButton stopButton; //投了用ボタン
		private JLabel colorLabel; // 色表示用ラベル
		private JLabel turnLabel; // 手番表示用ラベル
		private JLabel timeLabel1;//「残り時間」の表示
		private JLabel timerLabel1;//自分の残り時間表示
		private JLabel timeLabel2;//「残り時間」の表示
		private JLabel timerLabel2;//相手の残り時間表示
		private JLabel bnumLabel;//黒石の個数表示
		private JLabel wnumLabel;//石の個数表示
		private int bnum = 0;//黒石の個数
		private int wnum = 0;//白石の個数
		private int bmoney;//現在の黒石の総額
		private int wmoney;//現在の黒石の総額
		private int timer1;//自分の残り時間
		//private int timer2;//相手の残り時間
		private JLabel tableLabel1,tableLabel2;	//価格の一覧
		private String[] money = new String[4];//価格
		private String[] mmark = {"△"," ×","□","○"};//各価格のマーク
		private JButton mrButton[];//価格の選択ボタン

		private ImageIcon blackIcon, whiteIcon, boardIcon
						  ,blackIcon1,blackIcon2,blackIcon3,blackIcon4
						  ,whiteIcon1,whiteIcon2,whiteIcon3,whiteIcon4
						  ,toblackIcon1,toblackIcon2,toblackIcon3
						  ,towhiteIcon1,towhiteIcon2,towhiteIcon3
						  ,backIcon,possibleIcon; //アイコン

		private JSlider soundSlider;	//音量を調節するスライダー
		private JLabel soundSliderLabel;	//音量を表示するラベル

		private JLayeredPane p; //レイヤーペイン
		private JLabel background_game; // タイトル背景用ラベル

		//private int mchoice = -1;//選択した石の種類(価格)-1は選択なしの状態
		private int mchoice = 0;//選択した石の種類(価格)初めは一番価格の低い石を選択した状態にする
		private String turn = "";//手番
//		int lastTime1,lastTime2;
//		int score1,score2;
		private boolean hurry = false;

	//	String [][] grids = new String[8][8]; //getGridメソッドにより局面情報を取得

		public gameJFrame() {

			//アイコン設定(画像ファイルをアイコンとして使う)
			backIcon = new ImageIcon("game_background_3.png");
			background_game = new JLabel(backIcon);
			whiteIcon = new ImageIcon("White.jpg");
			blackIcon = new ImageIcon("Black.jpg");
			boardIcon = new ImageIcon("Green.jpg");
			whiteIcon1 = new ImageIcon("White1.jpg");
			whiteIcon2 = new ImageIcon("White2.jpg");
			whiteIcon3 = new ImageIcon("White3.jpg");
			whiteIcon4 = new ImageIcon("White4.jpg");
			blackIcon1 = new ImageIcon("Black1.jpg");
			blackIcon2 = new ImageIcon("Black2.jpg");
			blackIcon3 = new ImageIcon("Black3.jpg");
			blackIcon4 = new ImageIcon("Black4.jpg");
			towhiteIcon1 = new ImageIcon("toWhite1.jpg");
			towhiteIcon2 = new ImageIcon("toWhite2.jpg");
			towhiteIcon3 = new ImageIcon("toWhite3.jpg");
			toblackIcon1 = new ImageIcon("toBlack1.jpg");
			toblackIcon2 = new ImageIcon("toBlack2.jpg");
			toblackIcon3 = new ImageIcon("toBlack3.jpg");
			possibleIcon = new ImageIcon("Possible.png");

		}

		public void windowDisp(String mode) {
			if(mode.equals("normal")) {
				//
				turn = nothello.getTurn();
				//盤面取得
				if (myColor.equals("black")) {
					nothello.searchPlace(turn);
				}
				grids = nothello.getGrid();
				//個数
				nothello.countStone();
				bnum = nothello.getStoneNum("black");
				wnum = nothello.getStoneNum("white");


				//ウィンドウ設定
				setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じる場合の処理
				setTitle("NORMAL_OTHELLO 自分: " + myName + " 相手: " + opponentName);//ウィンドウのタイトル
				setSize(800,525);	//ウィンドウのサイズを設定
				//setLocation(dimension.width / 2 - 400, dimension.height / 2 - 250); //ウィンドウを画面中央に配置する
				setLocationRelativeTo(activePane); //チャット画面の中央に表示
				this.p = new JLayeredPane();
				this.c = getContentPane();
				this.c.add(p); //フレームのペインにpを追加
				activePane = p; //アクティブなレイヤーペインを更新

				//背景
				p.setLayout(null);
				c.setBackground(new Color(211,167,123));

				//盤面
				buttonArray = new JButton[8][8];//ボタンの配列を作成
				for(int i = 1;i < 9;i++) {
					for(int j = 1;j < 9;j++) {
						if(grids[i][j].equals("black")){ buttonArray[i-1][j-1] = new JButton(blackIcon);}
						if(grids[i][j].equals("white")){ buttonArray[i-1][j-1] = new JButton(whiteIcon);}
						if(grids[i][j].equals("board") || grids[i][j].equals("possible")) {
							buttonArray[i-1][j-1] = new JButton(boardIcon);
						}
						p.add(buttonArray[i-1][j-1]);//ボタンの配列をペインに貼り付け
						int x = (i-1) * 40+10;
						int y = (j-1) * 40+10;
						buttonArray[i-1][j-1].setBounds(x, y, 40, 40);//ボタンの大きさと位置を設定する．
						buttonArray[i-1][j-1].addMouseListener(this);//マウス操作を認識できるようにする
						buttonArray[i-1][j-1].setActionCommand((Integer.toString(i)+","+Integer.toString(j)));
						//ボタンを識別するための名前は「i,j」
					}
				}

				//投了ボタン
				stopButton = new JButton("投了");
				p.add(stopButton); //投了のボタンをペインに貼り付け
				stopButton.setBounds(550, 370, 200, 50);//ボタンの大きさと位置を設定する．
				stopButton.addMouseListener(this);//マウス操作を認識できるようにする
				stopButton.setActionCommand("stop");//ボタンを識別するための名前を付加する
				stopButton.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
				stopButton.setBackground(new Color(255,176,129));
				stopButton.setOpaque(true);
				stopButton.setEnabled(false);

				//自分の色
				colorLabel = new JLabel();
				if(myColor.equals("black")) {
					colorLabel.setText("あなたの色：黒");
				}else {
					colorLabel.setText("あなたの色：白");
				}
				p.add(colorLabel);
				colorLabel.setBounds(90,8 * 40 + 20, 250, 40);
				colorLabel.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));

				//黒の個数
				bnumLabel = new JLabel("黒："+bnum+"個");
				bnumLabel.setBounds(10, 8 * 40 + 70, 200, 30);
				p.add(bnumLabel);
				bnumLabel.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));

				//白の個数
				wnumLabel = new JLabel("白："+wnum+"個");
				wnumLabel.setBounds(10, 8 * 40 + 110,200, 30);
				p.add(wnumLabel);
				wnumLabel.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));

				//手番
				turnLabel = new JLabel("相手の準備待ち");
			/*	if(turn.equals(myColor)) {
					turnLabel.setText("あなたの番です");
				}else {
					turnLabel.setText("相手の番です");
				}*/
				turnLabel.setBounds(300, 370, 200, 50);
				turnLabel.setBackground(new Color(255,255,200));
				turnLabel.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
				turnLabel.setOpaque(true);
				turnLabel.setHorizontalAlignment(JLabel.CENTER);
				p.add(turnLabel);

				//自分の時間
				timeLabel1 = new JLabel("残り時間:");
				timeLabel1.setBounds(400, 45, 150, 50);
				timeLabel1.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
				p.add(timeLabel1);

				//自分の残り時間
				timerLabel1 = new JLabel("00:00");
				timerLabel1.setBounds(450,100,300, 100);
				timerLabel1.setFont(new Font("Times New Roman", Font.BOLD, 80));
				p.add(timerLabel1);

				//相手の時間
				timeLabel2 = new JLabel("相手の残り時間:");
				timeLabel2.setBounds(400, 210,200, 40);
				timeLabel2.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
				p.add(timeLabel2);

				//相手の残り時間
				timerLabel2 = new JLabel("00:00");
				timerLabel2.setBounds(600, 210,100, 40);
				timerLabel2.setFont(new Font("Times New Roman", Font.BOLD, 30));
				p.add(timerLabel2);

				if (playsound == true) {
					//音量を調節するスライダー
					soundSlider = new JSlider(0, 100, soundVolume);
					p.add(soundSlider);
					soundSlider.setBounds(550, 435, 200, 30);
					soundSlider.addChangeListener(this);
					soundSlider.setOpaque(false);	//透明にする
					soundSlider.setEnabled(true);

					//音量のラベル
					soundSliderLabel = new JLabel(" SOUND VOLUME : " + this.soundSlider.getValue());
					soundSliderLabel.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 16));
					soundSliderLabel.setBounds(550, 420, 200, 20);
					soundSliderLabel.setOpaque(false);	//透明にする
					if(soundSlider.getValue() > 0) {
						soundSliderLabel.setEnabled(true);
					}
					else {
						soundSliderLabel.setEnabled(false);
					}
					p.add(soundSliderLabel);
				}

				gt1 = new GameTimer(1,starttime);
				gt1.settime(gt1.gettime());
				gt2 = new GameTimer(2,starttime);
				gt2.settime(gt2.gettime());

				this.setVisible(true);

				//タイマー同期用処理
				if(gameDispFlg == false) {	//自分の方が先に対局画面を表示したら
					Client.this.sendMessage("complete gameDisp");
					Client.this.gameDispFlg = true;	//フラグ変数をtrueにする
				}
				else {	//相手の方が先に対局画面を表示したら
					Client.this.sendMessage("complete gameDisp ack");
					if(myColor.equals("black")) {
						repaintallBoard();	//置ける場所を表示して再描画
						System.out.println("自分のタイマー始動");
						gt1.start();
						gt2.start();
						gt2.setStop();
					}else{
						System.out.println("相手のタイマー始動");
						gt2.start();
						gt1.start();
						gt1.setStop();
					}
					if(turn.equals(myColor)) {
						gmf.turnLabel.setText("あなたの番です");
					}else {
						gmf.turnLabel.setText("相手の番です");
					}
					gmf.stopButton.setEnabled(true);
					Client.this.gameDispFlg = false;	//フラグ変数をfalseにする(一応)
					bgm("game");
				}

			}else if(mode.equals("time")) {		//一刻千金モード
				turn = tothello.getTurn();
				//価格設定
				int m1=1*starttime;
				int m2=3*starttime;
				int m3=5*starttime;
				int m4=10*starttime;

				money[0] = Integer.toString(m1);
				money[1] = Integer.toString(m2);
				money[2] = Integer.toString(m3);
				money[3] = Integer.toString(m4);
				tothello.setMoney(money);
				//盤面取得
				if (myColor.equals("black")) {
					tothello.searchPlace(turn);
				}
				grids = tothello.getGrid();
				//個数、価格
				tothello.countStone();
				tothello.calcAmountNum();
				bnum = tothello.getStoneNum("black");
				wnum = tothello.getStoneNum("white");
				bmoney = tothello.getAmount("black");
				wmoney = tothello.getAmount("white");
				//ウィンドウ設定
				setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じる場合の処理
				setTitle("TIME_OTHELLO 自分: " + myName + " 相手: " + opponentName); //ウィンドウのタイトル
				setSize(800,525);	//ウィンドウのサイズを設定
				//setLocation(dimension.width / 2 - 400, dimension.height / 2 - 250); //ウィンドウを画面中央に配置する
				setLocationRelativeTo(activePane); //チャット画面の中央に表示
				p = new JLayeredPane();
				c = getContentPane();
				c.add(p); //フレームのペインにpを追加
				activePane = p; //アクティブなレイヤーペインを更新

				p.setLayout(null);
				p.add(background_game); //タイトル背景用のラベルをペインに貼り付け
				background_game.setBounds(0, 0, 800, 500);

				//盤面
				buttonArray = new JButton[8][8];//ボタンの配列を作成
				for(int i = 1;i < 9;i++) {
					for(int j = 1;j < 9;j++) {
						if(grids[i][j].startsWith("black")){
							if(grids[i][j].endsWith(money[0])) {buttonArray[i-1][j-1] = new JButton(blackIcon1);}
							if(grids[i][j].endsWith(money[1])) {buttonArray[i-1][j-1] = new JButton(blackIcon2);}
							if(grids[i][j].endsWith(money[2])) {buttonArray[i-1][j-1] = new JButton(blackIcon3);}
							if(grids[i][j].endsWith(money[3])) {buttonArray[i-1][j-1] = new JButton(blackIcon4);}
						}
						if(grids[i][j].startsWith("white")){
							if(grids[i][j].endsWith(money[0])) {buttonArray[i-1][j-1] = new JButton(whiteIcon1);}
							if(grids[i][j].endsWith(money[1])) {buttonArray[i-1][j-1] = new JButton(whiteIcon2);}
							if(grids[i][j].endsWith(money[2])) {buttonArray[i-1][j-1] = new JButton(whiteIcon3);}
							if(grids[i][j].endsWith(money[3])) {buttonArray[i-1][j-1] = new JButton(whiteIcon4);}
						}
						if(grids[i][j].equals("board") || grids[i][j].equals("possible")) {
							buttonArray[i-1][j-1] = new JButton(boardIcon);
						}

						p.add(buttonArray[i-1][j-1]);//ボタンの配列をペインに貼り付け
						int x = (i-1) * 40+20;
						int y = (j-1) * 40+20;
						buttonArray[i-1][j-1].setBounds(x, y, 40, 40);//ボタンの大きさと位置を設定する．
						buttonArray[i-1][j-1].addMouseListener(this);//マウス操作を認識できるようにする
						buttonArray[i-1][j-1].setActionCommand((Integer.toString(i)+","+Integer.toString(j)));
						//ボタンを識別するための名前は(i,j)
					}
				}

				//投了ボタン
				stopButton = new JButton("投了");
				p.add(stopButton); //投了のボタンをペインに貼り付け
				stopButton.setBounds(550, 370, 200, 50);//ボタンの大きさと位置を設定する．
				stopButton.addMouseListener(this);//マウス操作を認識できるようにする
				stopButton.setActionCommand("stop");//ボタンを識別するための名前を付加する
				stopButton.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
				stopButton.setBackground(new Color(255,176,129));
				stopButton.setOpaque(true);
				stopButton.setEnabled(false);

				//自分の色
				colorLabel = new JLabel();
				if(myColor.equals("black")) {
					colorLabel.setText("あなたの色：黒");
				}else {
					colorLabel.setText("あなたの色：白");
				}
				p.add(colorLabel);
				colorLabel.setBounds(90,8 * 40 + 30, 200, 25);
				colorLabel.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));

				//黒の総額
				bnumLabel = new JLabel("黒："+bnum+"個,"+bmoney+"円");
				bnumLabel.setBounds(20, 8 * 40 + 70, 250, 30);
//				bnumLabel.setBackground(new Color(0,0,83));
//				bnumLabel.setOpaque(true);
				p.add(bnumLabel);
				bnumLabel.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));


				//白の総額
				wnumLabel = new JLabel("白："+wnum+"個,"+wmoney+"円");
				wnumLabel.setBounds(20, 8 * 40 + 110,250, 30);
				p.add(wnumLabel);
				wnumLabel.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));

				//手番
				turnLabel = new JLabel("相手の準備待ち");
			/*	if(turn.equals(myColor)) {
					turnLabel.setText("あなたの番です");
				}else {
					turnLabel.setText("相手の番です");
				}*/
				turnLabel.setBounds(300, 370, 200, 50);
				turnLabel.setBackground(new Color(255,255,200));
				turnLabel.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
				turnLabel.setOpaque(true);
				turnLabel.setHorizontalAlignment(JLabel.CENTER);
				p.add(turnLabel);

				//自分の時間
				timeLabel1 = new JLabel("残り時間:");
				timeLabel1.setBounds(400, 120, 150, 50);
				timeLabel1.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
				p.add(timeLabel1);

				//自分の残り時間
				timerLabel1 = new JLabel("00:00");
				timerLabel1.setBounds(450,160,300, 100);
				timerLabel1.setFont(new Font("Times New Roman", Font.BOLD, 80));
				p.add(timerLabel1);

				//相手の時間
				timeLabel2 = new JLabel("相手の残り時間:");
				timeLabel2.setBounds(400, 250,200, 40);
				timeLabel2.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
				p.add(timeLabel2);

				//相手の残り時間
				timerLabel2 = new JLabel("00:00");
				timerLabel2.setBounds(600, 250,100, 40);
				timerLabel2.setFont(new Font("Times New Roman", Font.BOLD, 30));
				p.add(timerLabel2);

				//価格一覧表
				tableLabel1 = new JLabel("△："+money[0]+"円　 ×："+money[1]+"円");
				tableLabel1.setBounds(450, 300, 300, 20);
				tableLabel1.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 20));
				p.add(tableLabel1);
				tableLabel2 = new JLabel("□："+money[2]+"円　○："+money[3]+"円");
				tableLabel2.setBounds(450, 330, 300, 20);
				tableLabel2.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 20));
				p.add(tableLabel2);

				//価格決定ボタン
				mrButton = new JButton[4];
				mrButton[0] = new JButton(mmark[0]+"：残り"+mnum[0]+"個");
				p.add(mrButton[0]);
				mrButton[0].setBounds(360,20, 200, 40);
				mrButton[0].setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 14));
				mrButton[0].addMouseListener(this);
				mrButton[0].setActionCommand("0");
				//mrButton[0].setBackground(new Color(245,185,83));
				mrButton[0].setBackground((new Color(201,102,10)));
				mrButton[0].setOpaque(true);

				mrButton[1] = new JButton(mmark[1]+"：残り"+mnum[1]+"個");
				p.add(mrButton[1]);
				mrButton[1].setBounds(560,20, 200, 40);
				mrButton[1].setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 14));
				mrButton[1].addMouseListener(this);
				mrButton[1].setActionCommand("1");
				mrButton[1].setBackground(new Color(245,185,83));
				mrButton[1].setOpaque(true);

				mrButton[2] = new JButton(mmark[2]+"：残り"+mnum[2]+"個");
				p.add(mrButton[2]);
				mrButton[2].setBounds(360,60, 200, 40);
				mrButton[2].setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 14));
				mrButton[2].addMouseListener(this);
				mrButton[2].setActionCommand("2");
				mrButton[2].setBackground(new Color(245,185,83));
				mrButton[2].setOpaque(true);

				mrButton[3] = new JButton(mmark[3]+"：残り"+mnum[3]+"個");
				p.add(mrButton[3]);
				mrButton[3].setBounds(560,60, 200, 40);
				mrButton[3].setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 14));
				mrButton[3].addMouseListener(this);
				mrButton[3].setActionCommand("3");
				mrButton[3].setBackground(new Color(245,185,83));
				mrButton[3].setOpaque(true);

				if (playsound == true) {
					//音量を調節するスライダー
					soundSlider = new JSlider(0, 100, soundVolume);
					p.add(soundSlider);
					soundSlider.setBounds(550, 435, 200, 30);
					soundSlider.addChangeListener(this);
					soundSlider.setOpaque(false);	//透明にする
					soundSlider.setEnabled(true);

					//音量のラベル
					this.soundSliderLabel = new JLabel(" SOUND VOLUME : " + this.soundSlider.getValue());
					this.soundSliderLabel.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 16));
					this.soundSliderLabel.setBounds(550, 420, 200, 20);
					this.soundSliderLabel.setOpaque(false);	//透明にする
					if(this.soundSlider.getValue() > 0) {
						this.soundSliderLabel.setEnabled(true);
					}
					else {
						this.soundSliderLabel.setEnabled(false);
					}
					p.add(soundSliderLabel);
				}

				p.moveToBack(background_game); //背景を一番後ろに変更
				gt1 = new GameTimer(1,starttime);
				gt1.settime(gt1.gettime());
				gt2 = new GameTimer(2,starttime);
				gt2.settime(gt2.gettime());

				this.setVisible(true);

				//タイマー同期用処理
				if(gameDispFlg == false) {	//自分の方が先に対局画面を表示したら
					Client.this.sendMessage("complete gameDisp");
					Client.this.gameDispFlg = true;	//フラグ変数をtrueにする
				}
				else {	//相手の方が先に対局画面を表示したら
					Client.this.sendMessage("complete gameDisp ack");
					if(myColor.equals("black")) {
						repaintallBoard();	//置ける場所を表示して再描画
						System.out.println("自分のタイマー始動");
						gt1.start();
						gt2.start();
						gt2.setStop();
					}else{
						System.out.println("相手のタイマー始動");
						gt2.start();
						gt1.start();
						gt1.setStop();
					}
					if(turn.equals(myColor)) {
						gmf.turnLabel.setText("あなたの番です");
					}else {
						gmf.turnLabel.setText("相手の番です");
					}
					gmf.stopButton.setEnabled(true);
					Client.this.gameDispFlg = false;	//フラグ変数をfalseにする(一応)
					bgm("game");
				}
			}


		}

		public void stateChanged(ChangeEvent e) {
			//音量を変更したときの処理
			if(e.getSource() == this.soundSlider && this.soundSlider.isEnabled() == true) {
				//if(playsound == false) {	//SOUND:OFFだった場合
				//	playsound = true;	//SOUND:ONにする
				//	soundEffect("open"); //効果音ファイルを開く
				//}
				soundVolume = this.soundSlider.getValue();
				Client.this.setVolume(soundVolume);
				this.soundSliderLabel.setText(" SOUND VOLUME : " + soundVolume);
				if(soundVolume > 0) {
					this.soundSliderLabel.setEnabled(true);
				}
				else {
					this.soundSliderLabel.setEnabled(false);
				}
			}
		}


		public void repaintallBoard() {
			/*盤面の再描画
			 * 盤面受け取る
			 * 盤面を描画する
			 * 効果音を鳴らす
			 */
			if(myMode.equals("normal")) {
				grids = nothello.getGrid();
				for(int i = 1;i < 9;i++) {
					for(int j = 1;j < 9;j++) {
						//if(grids[i][j].equals("black")){ buttonArray[i-1][j-1].setIcon(blackIcon);}
						//if(grids[i][j].equals("white")){ buttonArray[i-1][j-1].setIcon(whiteIcon);}
						if(grids[i][j].equals("board")){ buttonArray[i-1][j-1].setIcon(boardIcon);}
						if(grids[i][j].equals("possible")){ buttonArray[i-1][j-1].setIcon(possibleIcon);}
						//効果音ならす
					}
				}

			}else {
				grids = tothello.getGrid();
				for(int i = 1;i < 9;i++) {
					for(int j = 1;j < 9;j++) {
						//if(grids[i][j].startsWith("black")){
							//if(grids[i][j].endsWith(money[0])) {buttonArray[i-1][j-1].setIcon(blackIcon1);}
							//if(grids[i][j].endsWith(money[1])) {buttonArray[i-1][j-1].setIcon(blackIcon2);}
							//if(grids[i][j].endsWith(money[2])) {buttonArray[i-1][j-1].setIcon(blackIcon3);}
							//if(grids[i][j].endsWith(money[3])) {buttonArray[i-1][j-1].setIcon(blackIcon4);}
						//}
						//if(grids[i][j].startsWith("white")){
							//if(grids[i][j].endsWith(money[0])) {buttonArray[i-1][j-1].setIcon(whiteIcon1);}
							//if(grids[i][j].endsWith(money[1])) {buttonArray[i-1][j-1].setIcon(whiteIcon2);}
							//if(grids[i][j].endsWith(money[2])) {buttonArray[i-1][j-1].setIcon(whiteIcon3);}
							//if(grids[i][j].endsWith(money[3])) {buttonArray[i-1][j-1].setIcon(whiteIcon4);}
						//}
						if(grids[i][j].equals("board")){buttonArray[i-1][j-1].setIcon(boardIcon);}
						if(grids[i][j].equals("possible")){buttonArray[i-1][j-1].setIcon(possibleIcon);}
						//効果音ならす
					}
				}
			}
		}

		public void turnsquare(int x,int y,boolean animation) {
			/*ひっくり返す
			 * 元々あった石とは逆の色に変える
			 */
			if(myMode.equals("normal")) {
				if(grids[x][y].equals("black")) {
					if (animation == true) {
						turnanimationThread anime = new turnanimationThread(buttonArray[x-1][y-1], "toblack");
						anime.start();
						try {
							anime.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					buttonArray[x-1][y-1].setIcon(blackIcon);
				}else if(grids[x][y].equals("white")){
					if (animation == true) {
						turnanimationThread anime = new turnanimationThread(buttonArray[x-1][y-1], "towhite");
						anime.start();
						try {
							anime.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					buttonArray[x-1][y-1].setIcon(whiteIcon);
				}
			}else if(myMode.equals("time")){
				if(grids[x][y].startsWith("black")){
					if (animation == true) {
						turnanimationThread anime = new turnanimationThread(buttonArray[x-1][y-1], "toblack");
						anime.start();
						try {
							anime.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if(grids[x][y].endsWith(money[0])) {buttonArray[x-1][y-1].setIcon(blackIcon1);}
					if(grids[x][y].endsWith(money[1])) {buttonArray[x-1][y-1].setIcon(blackIcon2);}
					if(grids[x][y].endsWith(money[2])) {buttonArray[x-1][y-1].setIcon(blackIcon3);}
					if(grids[x][y].endsWith(money[3])) {buttonArray[x-1][y-1].setIcon(blackIcon4);}
				}
				if(grids[x][y].startsWith("white")){
					if (animation == true) {
						turnanimationThread anime = new turnanimationThread(buttonArray[x-1][y-1], "towhite");
						anime.start();
						try {
							anime.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if(grids[x][y].endsWith(money[0])) {buttonArray[x-1][y-1].setIcon(whiteIcon1);}
					if(grids[x][y].endsWith(money[1])) {buttonArray[x-1][y-1].setIcon(whiteIcon2);}
					if(grids[x][y].endsWith(money[2])) {buttonArray[x-1][y-1].setIcon(whiteIcon3);}
					if(grids[x][y].endsWith(money[3])) {buttonArray[x-1][y-1].setIcon(whiteIcon4);}
				}
			}
			//効果音ならす
			soundEffect("put");
		}

		class turnanimationThread extends Thread {
			JButton button;
			String toColor;

			public turnanimationThread(JButton button, String toColor) {
				this.button = button;
				this.toColor = new String(toColor);
			}

			public void run() {
				try {
					if (toColor.equals("toblack")) {
						button.setIcon(toblackIcon1);
						sleep(100);
						button.setIcon(toblackIcon2);
						sleep(100);
						button.setIcon(toblackIcon3);
						sleep(100);
					}
					else if (toColor.equals("towhite")) {
						button.setIcon(towhiteIcon1);
						sleep(100);
						button.setIcon(towhiteIcon2);
						sleep(100);
						button.setIcon(towhiteIcon3);
						sleep(100);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void repaintOther() {
			/*盤面以外の再描画
			 * 個数書き換え
			 * TIMEなら総計も書き換え
			 * turn書き換え
			 */
			if(myMode.equals("normal")) {
				nothello.countStone();
				bnum = nothello.getStoneNum("black");
				wnum = nothello.getStoneNum("white");
			//	nothello.changeTurn();
				turn = nothello.getTurn();
				bnumLabel.setText("黒："+bnum+"個");
				wnumLabel.setText("白："+wnum+"個");

				if(turn.equals(myColor)) {
					turnLabel.setText("あなたの番です");
				}else {
					turnLabel.setText("相手の番です");
				}
			}else {
				tothello.countStone();
				tothello.calcAmountNum();
				bnum = tothello.getStoneNum("black");
				wnum = tothello.getStoneNum("white");
				bmoney = tothello.getAmount("black");
				wmoney = tothello.getAmount("white");
			//	tothello.changeTurn();
				turn = tothello.getTurn();
				bnumLabel.setText("黒："+bnum+"個,"+bmoney+"円");
				wnumLabel.setText("白："+wnum+"個,"+wmoney+"円");

				if(turn.equals(myColor)) {
					turnLabel.setText("あなたの番です");
				}else {
					turnLabel.setText("相手の番です");
				}
			}
		}

		public void timerDisp(int playerno,String smin,String ssec) {
			//	turn = tothello.getTurn();
				if(playerno == 1) {
					timerLabel1.setText(smin + ":" + ssec);
				}else if(playerno == 2){
					timerLabel2.setText(smin + ":" + ssec);
				}
		}

		public void resultDisp(String win) {
			stopButton.setBackground(new Color(184,184,184)); //投了ボタンを灰色にする
			stopButton.setEnabled(false);
			turnLabel.setVisible(false);
			timeLabel1.setVisible(false);
			timeLabel2.setVisible(false);
			timerLabel2.setVisible(false);
			gt1.end = true;
			gt2.end = true;
			gameEnd = true;
			JButton exitButton = new JButton("退室する");
			p.add(exitButton); //退室のボタンをペインに貼り付け
			exitButton.setBounds(300, 370, 200, 50);//ボタンの大きさと位置を設定する．
			exitButton.addMouseListener(this);//マウス操作を認識できるようにする
			exitButton.setActionCommand("exit");//ボタンを識別するための名前を付加する
			exitButton.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
			exitButton.setBackground(new Color(255,150,150));
			exitButton.setOpaque(true);
			exitButton.setHorizontalAlignment(JLabel.CENTER);

//			int lastTime1,lastTime2;
//			int score1,score2;

//			lastTime1 = gt1.gettime();
//			lastTime2 = gt2.gettime();

			String lastTime1min,lastTime1sec,lastTime2min,lastTime2sec;
			//int lt1min,lt1sec,lt2min,lt2sec;

			if(lastTime1 >= 0 && lastTime2 >= 0) {
				if((lastTime1/60) < 10) {
					lastTime1min = "0"+ (int)(lastTime1/60);
				}else {
					lastTime1min = ""+ (int)(lastTime1/60);
				}
				if((lastTime1%60) < 10) {
					lastTime1sec = "0"+ (int)(lastTime1%60);
				}else {
					lastTime1sec = ""+ (int)(lastTime1%60);
				}
				if((lastTime2/60) < 10) {
					lastTime2min = "0"+ (int)(lastTime2/60);
				}else {
					lastTime2min = ""+ (int)(lastTime2/60);
				}
				if((lastTime2%60) < 10) {
					lastTime2sec = "0"+ (int)(lastTime2%60);
				}else {
					lastTime2sec = ""+ (int)(lastTime2%60);
				}
			}else {
				lastTime1min = "--";
				lastTime1sec = "--";
				lastTime2min = "--";
				lastTime2sec = "--";
			}

			JLabel timeLabel3 = new JLabel("最終残り時間:");
			JLabel timerLabel3 = new JLabel("00:00");
			timerLabel3.setText(lastTime1min + ":" + lastTime1sec);
			JLabel timeLabel4 = new JLabel("相手の最終残り時間:");
			JLabel timerLabel4 = new JLabel("00:00");
			timerLabel4.setText(lastTime2min + ":" + lastTime2sec);
			if (hurry == true) { //赤い字になっている場合は黒に戻す
				timerLabel1.setForeground(Color.black);
			}
			timerLabel1.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 140));
			if (myMode.equals("normal")) { //通常モードの時
				if(win.equals("win")) { //勝利の時
					timerLabel1.setBounds(370,20,500, 200);
					timerLabel1.setText("勝利!");
				}
				else if(win.equals("lose")){ //敗北の時
					timerLabel1.setBounds(390,20,500, 200);
					timerLabel1.setText("敗北");
				}else if(win.equals("draw")){
					timerLabel1.setBounds(390,20,500, 200);
					timerLabel1.setText("引分");
				}
				timeLabel3.setBounds(370, 200,200, 40);
				timeLabel3.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
				p.add(timeLabel3);
				timerLabel3.setBounds(620, 200,100, 40);
				timerLabel3.setFont(new Font("Times New Roman", Font.BOLD, 30));
				p.add(timerLabel3);
				timeLabel4.setBounds(370, 230,250, 40);
				timeLabel4.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
				p.add(timeLabel4);
				timerLabel4.setBounds(620, 230,100, 40);
				timerLabel4.setFont(new Font("Times New Roman", Font.BOLD, 30));
				p.add(timerLabel4);
			}
			else { //一刻千金モードの時
//				score1 = tothello.getScore(myColor) + lastTime1;
//				if(myColor.equals("black")) {
//					score2 = tothello.getScore("white") + lastTime2;
//				}else if(myColor.equals("white")){
//					score2 = tothello.getScore("black") + lastTime2;
//				}

				if(win.equals("win")) { //勝利の時
					timerLabel1.setBounds(370,10,500, 200);
					timerLabel1.setText("勝利!");
				}
				else if(win.equals("lose")){ //敗北の時
					timerLabel1.setBounds(390,10,500, 200);
					timerLabel1.setText("敗北");
				}else if(win.equals("draw")) {
					timerLabel1.setBounds(390,10,500, 200);
					timerLabel1.setText("引分");
				}
				for (int i = 0; i<4; i++) {
					mrButton[i].setVisible(false);
				}
				timeLabel3.setBounds(370, 180,200, 40);
				timeLabel3.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
				p.add(timeLabel3);
				timerLabel3.setBounds(620, 180,100, 40);
				timerLabel3.setFont(new Font("Times New Roman", Font.BOLD, 30));
				p.add(timerLabel3);
				JLabel scoreLabel1 = new JLabel("あなたのスコア:");
				scoreLabel1.setBounds(370, 210,200, 40);
				scoreLabel1.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
				p.add(scoreLabel1);
				JLabel scorerLabel1 = new JLabel("000");
				if(score1 < 10) {
					scorerLabel1.setText("00"+score1);
				}else if(score1 < 100) {
					scorerLabel1.setText("0"+score1);
				}else {
					scorerLabel1.setText(""+score1);
				}
				scorerLabel1.setBounds(620, 210,200, 40);
				scorerLabel1.setFont(new Font("Times New Roman", Font.BOLD, 24));
				p.add(scorerLabel1);
				timeLabel4.setBounds(370, 240,250, 40);
				timeLabel4.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
				p.add(timeLabel4);
				timerLabel4.setBounds(620, 240,100, 40);
				timerLabel4.setFont(new Font("Times New Roman", Font.BOLD, 30));
				p.add(timerLabel4);
				JLabel scoreLabel2 = new JLabel("相手のスコア:");
				scoreLabel2.setBounds(370, 270,200, 40);
				scoreLabel2.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 24));
				p.add(scoreLabel2);
				JLabel scorerLabel2 = new JLabel("000");
				if(score2 < 10) {
					scorerLabel2.setText("00"+score2);
				}else if(score2 < 100) {
					scorerLabel2.setText("0"+score2);
				}else {
					scorerLabel2.setText(""+score2);
				}
				scorerLabel2.setBounds(620, 270,200, 40);
				scorerLabel2.setFont(new Font("Times New Roman", Font.BOLD, 24));
				p.add(scorerLabel2);
				p.moveToBack(background_game); //背景を一番後ろに変更
			}
			bgm(win);
		}

		public void timeResignDisp() {
			if(myMode.equals("normal")) {
				lastTime1 = gt1.gettime();
				lastTime2 = gt2.gettime();
				if(lastTime1 == 0) {
					gt1.setStop();
					Client.this.sendMessage("timeover");
					gmf.resultDisp("lose");//敗北画面の表示
				}else if(lastTime2 == 0) {
					//	gt2.setStop();
					}
			}else {
				lastTime1 = gt1.gettime();
				lastTime2 = gt2.gettime();
				if(lastTime1 == 0) {
					gt1.setStop();
					Client.this.sendMessage("timeover");
					score1 = tothello.getAmount(myColor) + lastTime1;
					if(myColor.equals("black")) {
						score2 = tothello.getAmount("white") + lastTime2;
					}else if(myColor.equals("white")){
						score2 = tothello.getAmount("black") + lastTime2;
					}
					gmf.resultDisp("lose");//敗北画面の表示
				}else if(lastTime2 == 0) {
				//	gt2.setStop();
				}
			}



		}


		//投了時の確認画面表示
		public int stopCheck() {
			 int select = JOptionPane.showOptionDialog(this,"投了しますか？","投了の確認",
				      JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,null,null);
			 System.out.println("select="+select);
			 return select;
		}



		//マウスクリック時の処理
		public void mouseClicked(MouseEvent e) {
			JButton theButton = (JButton)e.getComponent();//クリックしたオブジェクトを得る．キャストを忘れずに
			String command = theButton.getActionCommand();//ボタンの名前を取り出す
			System.out.println("マウスがクリックされました。押されたボタンは " + command + "です。");//テスト用に標準出力
			if(stopButton.isEnabled() == true) { //対局が終了したらボタン操作を受け付けない
				if(turn.equals(myColor)) {//自分の手番ならば操作を受付
					if(myMode.equals("normal")) {
						if(command.equals("stop")) {
							soundEffect("push");
							//本当に投了するのか尋ねる
							if(stopCheck() == 0 && gameEnd == false) {
								soundEffect("push");
								//投了の際にはサーバに送る
								Client.this.sendMessage("resign");
								gt1.setStop();//自分のタイマーを止める
								lastTime1 = gt1.gettime();
								lastTime2 = gt2.gettime();
								//敗北画面の表示
								nothello.changeTurn();
								turn = nothello.getTurn();
								gmf.repaintallBoard();
								gmf.resultDisp("lose");
							}
							else {
								soundEffect("cancel");
							}

						}else{//盤面上を押したとき
//							if(turn.equals(myColor) ) {
								String g[] = command.split("[,]");
								System.out.println(Integer.parseInt(g[0])+Integer.parseInt(g[1]));
								//おける場所かどうか調べるだめなら警告表示
								if(!(grids[Integer.parseInt(g[0])][Integer.parseInt(g[1])].equals("possible"))) {
									soundEffect("alert");
									JOptionPane.showMessageDialog(this, "そこには石を置けません", "置けない場所の選択",
											JOptionPane.WARNING_MESSAGE);
									soundEffect("push");
								}else {
									gt1.setStop();//タイマー止める
									gt1.settime(gt1.gettime()); //1秒未満切り上げ
									timer1 = gt1.gettime();
									//サーバに送る
									Client.this.sendMessage("othello operation:"+g[0]+":"+g[1]+":"+myColor+":"+timer1);
									//othelloクラスに渡して処理してもらう
									int po[] = {Integer.parseInt(g[0]),Integer.parseInt(g[1])};
									nothello.setOperation(po,myColor);
									gmf.turnsquare(po[0],po[1],false);
									//ひとつずつひっくり返す
									int[][] turnGrids = nothello.getTurnOverGrid();
									int n = -1;
									for(int i = 0;1 <= turnGrids[i][0];i++){
										n++;
									}
									turnThread[] tr = new turnThread[n + 1];
									for (int i = 0; i <= n; i++) {
										tr[i] = new turnThread(turnGrids[i][0], turnGrids[i][1], i, tr);
										tr[i].start();
									}

									nothello.changeTurn();
									turn = nothello.getTurn();
									//	nothello.searchPlace(turn);
									gmf.repaintallBoard();
									gmf.repaintOther();
									/*if(nothello.judgeEnd() == true) {//自分のこの1手で対局終了ならば
										lastTime1 = gt1.gettime();
										lastTime2 = gt2.gettime();
										resultDisp(nothello.judgeResult(myColor));
									}else {
										//gt2.setStop();//相手のタイマー開始
									}*/


								}

//							}else {
//								JOptionPane.showMessageDialog(this, "あなたの手番ではありません", "Warn",
//							        	JOptionPane.WARNING_MESSAGE);
//							}

						}
					}else if(myMode.equals("time")) {//一刻千金モードの時
						if(command.equals("stop")) {
							soundEffect("push");
							if(stopCheck()  == 0 && gameEnd == false) {//本当に投了するのか尋ねる
								soundEffect("push");
								Client.this.sendMessage("resign");//投了の際にはサーバに送る
								gt1.setStop();
								lastTime1 = gt1.gettime();
								lastTime2 = gt2.gettime();
								score1 = tothello.getAmount(myColor) + lastTime1;
								if(myColor.equals("black")) {
									score2 = tothello.getAmount("white") + lastTime2;
								}else if(myColor.equals("white")){
									score2 = tothello.getAmount("black") + lastTime2;
								}
								tothello.changeTurn();
								turn = tothello.getTurn();
								gmf.repaintallBoard();
								gmf.resultDisp("lose");//敗北画面の表示
							}
							else {
								soundEffect("cancel");
							}
						}else if(command.matches("[0123]")) {
							soundEffect("select");
							int com = Integer.parseInt(command);
							if(mnum[com] > 0) {
								mrButton[com].setBackground(new Color(201,102,10));
								mchoice = com;
								for(int i=0;i < 4;i++) {
									if(i != com) {
										mrButton[i].setBackground(new Color(245,185,83));
									}
									//石の数が0個のボタンの設定
									if(mnum[i] <= 0) {
										mrButton[i].setBackground(Color.GRAY);	//グレーにする
										mrButton[i].setEnabled(false);//カーソルを合わせても枠を表示しない
									}
								}
							}else {
								soundEffect("alert");
								JOptionPane.showMessageDialog(this, "その価格の石はありません", "置けない石の選択",
										JOptionPane.WARNING_MESSAGE);
								soundEffect("push");
							}
						}else{//盤面上を押したとき
//							if(turn.equals(myColor) ) {
							//	System.out.println("盤面");
								String g[] = command.split("[,]");
								//	System.out.println(Integer.parseInt(g[0])+","+Integer.parseInt(g[1]));
								//おける場所かどうか調べるだめなら警告表示
								if(mchoice == -1) {
									soundEffect("alert");
									JOptionPane.showMessageDialog(this, "置く石の種類を選択してください", "石の種類の未決定",
											JOptionPane.WARNING_MESSAGE);
									soundEffect("push");
								}else if(!(grids[Integer.parseInt(g[0])][Integer.parseInt(g[1])].equals("possible"))) {
									soundEffect("alert");
									JOptionPane.showMessageDialog(this, "そこには石を置けません", "置けない場所の選択",
											JOptionPane.WARNING_MESSAGE);
									soundEffect("push");
								}else {
									gt1.setStop();//タイマー止める
									gt1.settime(gt1.gettime()); //1秒未満切り上げ
									timer1 = gt1.gettime();
									//サーバに送る
									//mchoiceをmycolorに付け足す
									//	String mm = myColor+money[mchoice];
									//	System.out.println(mm);
									Client.this.sendMessage("othello operation:"+g[0]+":"+g[1]+":"+myColor+":"+mchoice+":"+timer1);
									//othelloクラスに渡して処理してもらう
									int po[] = {Integer.parseInt(g[0]),Integer.parseInt(g[1])};
									tothello.setOperation(po,myColor,mchoice);
									gmf.turnsquare(po[0],po[1],false);

									//ひとつずつひっくり返す
									int[][] turnGrids = tothello.getTurnOverGrid();
									int n = -1;
									for(int i = 0;1 <= turnGrids[i][0];i++){
										n++;
									}
									turnThread[] tr = new turnThread[n + 1];
									for (int i = 0; i <= n; i++) {
										tr[i] = new turnThread(turnGrids[i][0], turnGrids[i][1], i, tr);
										tr[i].start();
									}

									tothello.changeTurn();
									turn = tothello.getTurn();
									//	tothello.searchPlace(turn);
									gmf.repaintallBoard();
									gmf.repaintOther();
									/*for(int i = 0;i<4;i++) {//石の選択を元に戻す
										mrButton[i].setBackground(new Color(245,185,83));
									}
									mnum[mchoice]--;//残り石の個数減らす
									mrButton[mchoice].setText(mmark[mchoice]+"：残り"+mnum[mchoice]+"個");
									mchoice = -1;
									//石の数が0個のボタンの設定
									for(int i = 0; i < 4; i++) {
										if(mnum[i] <= 0) {
											mrButton[i].setBackground(Color.GRAY);	//グレーにする
											mrButton[i].setEnabled(false);//カーソルを合わせても枠を表示しない
										}
									}*/
									mnum[mchoice]--;//残り石の個数減らす
									mrButton[mchoice].setText(mmark[mchoice]+"：残り"+mnum[mchoice]+"個");
									if(mnum[mchoice] <= 0) {//今選んでいる石がなくなった場合
										if(mnum[mchoice] <= 0) {
											mrButton[mchoice].setBackground(Color.GRAY);	//グレーにする
											mrButton[mchoice].setEnabled(false);//カーソルを合わせても枠を表示しない
										}
										mchoice = -1;//もう他に持っている石が無い場合のため-1にしておく
										for(int i = 0; i < 4;i++) {//持っている石の中で一番価格の低い石を選んでおく
											if(mnum[i] > 0) {
												mrButton[i].setBackground(new Color(201,102,10));
												mchoice = i;
												break;
											}
										}
									}
									/*if(tothello.judgeEnd() == true) {//自分のこの1手で対局終了ならば
										lastTime1 = gt1.gettime();
										lastTime2 = gt2.gettime();
										score1 = tothello.getAmount(myColor) + lastTime1;
										if(myColor.equals("black")) {
											score2 = tothello.getAmount("white") + lastTime2;
											resultDisp(tothello.judgeResult(myColor,score1,score2));
										}else if(myColor.equals("white")){
											score2 = tothello.getAmount("black") + lastTime2;
											resultDisp(tothello.judgeResult(myColor,score2,score1));
										}
									}else {
										//gt2.setStop();//相手のタイマー開始
									}*/
								}

//							}else {
//								JOptionPane.showMessageDialog(this, "あなたの手番ではありません", "Warn",
//							        	JOptionPane.WARNING_MESSAGE);
//							}
						}
					}
				}else {
					soundEffect("alert");
					JOptionPane.showMessageDialog(this, "あなたの手番ではありません", "相手の手番です",
							JOptionPane.WARNING_MESSAGE);
					soundEffect("push");
				}
			}
			else if(command.equals("exit")) { //退室のボタンが押されたとき
				soundEffect("push");
				if (socket.isClosed() == false) { //まだ通信が切断されていない場合
					Client.this.sendMessage("quit"); //サーバから切断する
					receiver.accept = false; //receiverを止める
					out.close();
				}
				Client oclient = new Client(); //起動し直す
				oclient.titleDisp();
				gmf.dispose(); //対局画面を閉じる
			}
		}
		public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
		public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
		public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
		public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
	}

	class turnThread extends Thread {
		int x;
		int y;
		int num;
		turnThread[] tr;
		boolean end = false;

		public turnThread(int x, int y, int num, turnThread[] tr) {
			this.x = x;
			this.y = y;
			this.num = num;
			this.tr = tr;
		}
		public void run() {
			if (num != 0) {
				try {
					for (int i = 0; i < num; i++) {
						while(tr[i].end == false) {
							sleep(50);
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			gmf.turnsquare(x, y, true);
			//効果音ならす
			end = true;
			if (num == tr.length - 1) { //このスレッドでアニメーションが最後なら
				if (myMode.equals("normal")) {
					if (nothello.judgeEnd() == true) {//自分のこの1手で対局終了ならば
						lastTime1 = gt1.gettime();
						lastTime2 = gt2.gettime();

						gmf.resultDisp(nothello.judgeResult(myColor));
					}else {
						if(myColor.equals("black")) {
							if(nothello.sendPass("white") == false) {
								gt2.setStop();//相手のタイマー開始
							}
						}
						else {
							if(nothello.sendPass("black") == false) {
								gt2.setStop();//相手のタイマー開始
							}
						}
						//gt2.setStop();//相手のタイマー開始
					}
				}
				else if (myMode.equals("time")) {
					if(tothello.judgeEnd() == true) {//自分のこの1手で対局終了ならば
						lastTime1 = gt1.gettime();
						lastTime2 = gt2.gettime();
						score1 = tothello.getAmount(myColor) + lastTime1;
						if(myColor.equals("black")) {
							score2 = tothello.getAmount("white") + lastTime2;
							gmf.resultDisp(tothello.judgeResult(myColor,score1,score2));
						}else if(myColor.equals("white")){
							score2 = tothello.getAmount("black") + lastTime2;
							gmf.resultDisp(tothello.judgeResult(myColor,score2,score1));
						}

					}else {
						if(myColor.equals("black")) {
							if(tothello.sendPass("white") == false) {
								gt2.setStop();//相手のタイマー開始
							}
						}
						else {
							if(tothello.sendPass("black") == false) {
								gt2.setStop();//相手のタイマー開始
							}
						}
						//gt2.setStop();//相手のタイマー開始
					}
				}
			}
			//gt2.setStop();//相手のタイマー開始
		}
	}

	public class GameTimer extends Thread{//対局用タイマー内部クラス

		private boolean end = false;
		private boolean stop = false;
		private int starttime;
		private int min;
		private int sec;
		private String smin,ssec;
		private int now;
		private int playerno;
		private int i;

		public GameTimer(int playerno ,int starttime) {//どっちのタイマーかも引数にする
			this.playerno = playerno;
			this.starttime = starttime;
			now = starttime*60;
		}

		public void run() {
			i = starttime*60;
			now = i;

			while(!end){
				if(i <= 0) {
					//	Client.this.sendMessage("resign");
						gmf.timeResignDisp();
						break;
				}
				min = (int)(i/60);
				sec = i%60;
				smin = String.valueOf(min);
				ssec = String.valueOf(sec);

				if(min < 10) {smin = "0"+ min;}
				if(min == 0) {
					smin = "00";
					if(gmf.hurry == false && playerno == 1) {
						gmf.hurry = true;
						gmf.timerLabel1.setForeground(Color.red);
						bgm("hurry");
					}
				}
				if(sec < 10) {ssec = "0"+ sec;}
				if(sec == 0) {ssec = "00";}

			//	turn = tothello.getTurn();
			//	System.out.println("timerturn:"+turn);
				gmf.timerDisp(playerno,smin,ssec);

				try {
					Thread.sleep(1000);
					synchronized(this){
						if (stop) wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				i--;
				now = i;
			}
		}
		public synchronized void setStop() {
			stop = !stop;
			if (!stop) {
				notify();
			}
		}
		public int gettime() {
			return now;
		}

		public void settime(int newtime) {
			i = newtime;
			min = (int)(i/60);
			sec = i%60;
			smin = String.valueOf(min);
			ssec = String.valueOf(sec);

			if(min < 10) {smin = "0"+ min;}
			if(min == 0) {smin = "00";}
			if(sec < 10) {ssec = "0"+ sec;}
			if(sec == 0) {ssec = "00";}
			gmf.timerDisp(playerno,smin,ssec);
			now = i;
		}

	}


	public void connectServer(String ipAddress, int port) throws IOException {	// サーバに接続
		try {
			socket = new Socket(ipAddress, port); //サーバ(ipAddress, port)に接続
			out = new PrintWriter(socket.getOutputStream(), true); //データ送信用オブジェクトの用意
			receiver = new Receiver(socket); //受信用オブジェクトの準備
			receiver.start();//受信用オブジェクト(スレッド)起動
		} catch (UnknownHostException e) {
			System.err.println("ホストのIPアドレスが判定できません: " + e);
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			/*System.err.println("サーバ接続時にエラーが発生しました: " + e);
			e.printStackTrace();
			//connectServerFailedDisp();
			soundEffect("alert");
			JOptionPane.showMessageDialog(this, "サーバへの接続に失敗しました\nタイトル画面に戻ります", "サーバ接続失敗",
					JOptionPane.WARNING_MESSAGE);
			soundEffect("push");
			title_message = new String("restart");*/
			throw e;
			//System.exit(-1);
		}
	}
	// データ受信用スレッド(内部クラス)
	class Receiver extends Thread {
			private InputStreamReader sisr; //受信データ用文字ストリーム
			private BufferedReader br; //文字ストリーム用のバッファ
			private boolean accept = true;

			// 内部クラスReceiverのコンストラクタ
			Receiver (Socket socket){
				try{
					sisr = new InputStreamReader(socket.getInputStream()); //受信したバイトデータを文字ストリームに
					br = new BufferedReader(sisr);//文字ストリームをバッファリングする
				} catch (IOException e) {
					System.err.println("データ受信時にエラーが発生しました: " + e);
					e.printStackTrace();
				}
			}
			// 内部クラス Receiverのメソッド
			public void run(){
				try{
					/*int sec = 0;	//対戦相手探索のタイマー
					waitLoop: while(true) {
						if(sec >= 60) {	//対戦相手が１分間見つからなかった場合
							throw new Exception("時間内に対戦相手が見つかりませんでした");
						}
						String inputLine = br.readLine();
						if(inputLine != null) {	//受信し続ける
							if(waitOpponent(inputLine) == true) {	//受信したメッセージが"black"か"white"なら
								break waitLoop;	//ループwaitLoopを抜けて次の無限ループへ
							}
						}
						Thread.sleep(1000);	//１秒をカウント
						sec++;
					}*/
					while(accept == true) {//データを受信し続ける
							String inputLine = br.readLine();//受信データを一行分読み込む
						if (inputLine != null){//データを受信したら
							receiveMessage(inputLine);//データ受信用メソッドを呼び出す
						}
					}
				} catch (IOException e){
					//System.err.println("データ受信時にエラーが発生しました: " + e);
					e.printStackTrace();
					System.out.println("通信を切断しました");
					out.close();
					//out.flush();
					receiver.accept = false; //receiverを止める
					if (gameEnd == false) {
						if (matchingnow== false) { //マッチング前にサーバとの接続が切れた時
							roommax = true;
							//receiver.accept = false; //receiverを止める
						}
						else { //マッチング中にサーバとの接続が切れた時
							serverdown = true;
						}
						if (matchingok == true) { //マッチング後にサーバとの接続が切れた時
							//receiver.accept = false;
							if (chf != null) { //チャット画面が開いている場合
								chf.chat_timer.stop(); //チャットタイマーを止める
								if (chf.time_waiting != null) chf.time_waiting.dispose();	//相手の時間決定承認待ち画面が開いている場合は閉じる
								if (chf.time_ask != null) chf.time_ask.dispose(); //時間決定確認画面が開いている場合は閉じる
								if (chf.time_check != null) chf.time_check.dispose(); //時間決定確認結果画面が開いている場合は閉じる
								soundEffect("alert");
								JOptionPane.showMessageDialog(chf, "サーバ接続が切断されました\nタイトルに戻ります", "サーバ接続エラー",
										JOptionPane.WARNING_MESSAGE);
								soundEffect("push");
							}
							if (gmf != null) { //対局画面が開いている場合
								gt1.end = true; //タイマーを止める
								gt2.end = true;
								soundEffect("alert");
								JOptionPane.showMessageDialog(gmf, "サーバ接続が切断されました\nタイトルに戻ります", "サーバ接続エラー",
										JOptionPane.WARNING_MESSAGE);
								soundEffect("push");
							}
							Client oclient = new Client(); //起動し直す
							oclient.titleDisp();
							if (chf != null) { //チャット画面が開いている場合
								chf.dispose(); //チャット画面を閉じる
							}
							if (gmf != null) { //対局画面が開いている場合
								gmf.dispose(); //対局画面を閉じる
							}
						}
					}
				} catch (Exception e){
					e.printStackTrace();
					//System.err.println("データ受信時にエラーが発生しました: " + e);
				}
			}
			@SuppressWarnings("finally")
			public boolean waitOpponent(String s) {	//対戦相手
				try {

					if(s.equals("black") || s.equals("white")) {	//自分の色を受信したら
						//System.out.println("input:1:" + s);
						receiveMessage(s);	//メッセージ受信用のメソッドを呼び出して
						return true;	//trueを返す
					}
					else {	//それ以外のメッセージなら
						//System.out.println("input:2:" + s);
						receiveMessage(s);	//メッセージ受信用のメソッドを呼び出して
						return false;	//falseを返す
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				finally {
					return false;
				}
			}
		}

	public void receiveMessage(String msg){	// メッセージの受信
		String DELIMITER = ":";
		String[] act_and_message = msg.split(DELIMITER, -1);
		System.out.println("サーバからメッセージ " + msg + " を受信しました"); //テスト用標準出力
		if (matchingok == false) {
			title_message = new String(msg); //タイトル～ログインではmessageにより受信文字列を認識する
		}
		try {
			System.out.println("length：" + act_and_message.length);
			for(int i = 0; i < act_and_message.length; i++) {	//受信したメッセージの表示(デバッグ用)
				System.out.printf("[%d]：%s\n", i, act_and_message[i]);
			}
			switch(act_and_message[0]) {
			case "login success":	//ログインの成功
				LoginSuccess = true;
				//System.out.println(act_and_message[0]);
				break;
			case "Name is not available":	//ログイン名の使用不可
				//throw new Exception("その名前は使用不可能です");
				break;
			case "black": case "white":	//自分の色を受信したら
				//System.out.println(act_and_message[0]);
				//this.chf.chatDisp("CHAT_WINDOW");	//チャット画面の表示
				//if(act_and_message[0].equals("white")) {	//白だと表示が遅いので
				//	sendMessage("complete chatDisp");	//表示が終わったことを相手(黒)に伝えて
				//	this.chf.chat_timer.start();	//チャットタイマースタート
				//}
				for(int i = 1; i < act_and_message.length; i++) {
					this.opponentName = new String(this.opponentName + act_and_message[i]);	//文字列の追加
					if(i == (act_and_message.length - 1)) {
						//if(msg.endsWith(":")) {
						//	this.opponentName = new String(this.opponentName + ":");	//プレイヤ名の末尾がコロンの場合
						//}
						break;
					}
					this.opponentName = new String(this.opponentName + ":");	//コロンが含まれていたらプレイヤ名の一部とする
				}
				break;
			case "complete chatDisp":	//相手がチャット画面の表示が終わったことを受信
				this.chatDispFlg = true;	//フラグ変数をtrueにする
				break;
			case "complete chatDisp ack":	//自分が先にチャット画面を表示した場合、相手が表示でき次第受信
				chf.chat_timer.start();	//チャットタイマースタート
				this.chatDispFlg = false;	//フラグ変数をfalseにする(一応)
				bgm("chat");
				break;
			case "chat msg":	//チャットメッセージの受信
				this.chf.chat_area.append(opponentName + "： ");	//文字列の追加
				for(int i = 1; i < act_and_message.length; i++) {
					this.chf.chat_area.append(act_and_message[i]);	//文字列の追加
					if(i == (act_and_message.length - 1)) {
						//if(msg.endsWith(":")) {
						//	this.chf.chat_area.append(":");	//チャットメッセージの末尾がコロンの場合
						//}
						this.chf.chat_area.append("\n");	//メッセージの最後に改行を加えて終了
						break;
					}
					this.chf.chat_area.append(":");	//コロンが含まれていたらメッセージの一部とする
				}
				soundEffect("chat");
				break;
			case "time ask":	//相手が希望対局時間を送信してきた場合
				this.chf.time_text.setText(act_and_message[1]);
				soundEffect("push");
				this.chf.chatDisp("TIME_ASK_WINDOW");
				break;
			case "time check":	//相手がこちらの希望対局時間を承認もしくは拒否した場合
				//chf2 = new ChatJFrame();
				//this.chf2.time_text = new JTextField(act_and_message[1]);
				//this.chf2.time_text.setText(act_and_message[1]);	//制限時間決定確認画面のテキスト設定
				if (act_and_message[1].equals("NG")) {
					this.chf.time_ok.setEnabled(true);	//持ち時間決定ボタンを有効化する
					this.chf.time_text.setEditable(true);	//持ち時間入力欄を有効化する
					this.chf.time_waiting.dispose();	//相手の時間決定承認待ち画面を閉じる
					soundEffect("cancel");
					JOptionPane.showMessageDialog(chf, "相手が持ち時間承認を拒否しました", "持ち時間承認拒否",
							JOptionPane.WARNING_MESSAGE);
					soundEffect("push");
				}
				else {
					soundEffect("push");
					this.chf.chatDisp("TIME_CHECK_WINDOW");
				}
				//this.chf2.chatDisp("TIME_CHECK_WINDOW");	//制限時間決定確認画面の表示
				break;
			case "complete gameDisp":	//相手が先に対局画面を表示した場合、その相手から受信
				System.out.printf("receive_complete gameDisp\n");
				this.gameDispFlg = true;	//フラグ変数をtrueにする
				break;
			case "complete gameDisp ack":	//自分が先に対局画面を表示した場合、相手が表示でき次第受信
				System.out.printf("receive_complete gameDisp ack\n");
				if(myColor.equals("black")) {
					gmf.repaintallBoard();	//置ける場所を表示して再描画
					gmf.turnLabel.setText("あなたの番です");
					System.out.println("自分のタイマー始動");
					gt1.start();
					gt2.start();
					gt2.setStop();
				}else{
					gmf.turnLabel.setText("相手の番です");
					System.out.println("相手のタイマー始動");
					gt2.start();
					gt1.start();
					gt1.setStop();
				}
				gmf.stopButton.setEnabled(true);
				this.gameDispFlg = false;	//フラグ変数をfalseにする(一応)
				bgm("game");
				break;
			case "othello operation":
				/*if(act_and_message[1].equals("pass")) {
				}*/
				if(myMode.equals("normal")) {
					/*//相手のタイマー止める
					gt2.setStop();*/
					int gt2t = gt2.gettime();
					if((Integer.parseInt(act_and_message[4])-(gt2t)) > 5 || (Integer.parseInt(act_and_message[4])-(gt2t)) < -5) {
						//チート対策。5秒より大きい差があった場合
						gt2.setStop();//相手のタイマーを止める
						lastTime1 = -1;
						lastTime2 = -1;
						soundEffect("announce");
						JOptionPane.showMessageDialog(gmf, "タイマーへの不正な干渉の疑いがあります", "タイマーエラー",
								JOptionPane.WARNING_MESSAGE);
						soundEffect("push");
						gmf.resultDisp("draw");//引き分け画面の表示
						//resign = true;
						Client.this.sendMessage("timeerror");
					}else {
						gt2.settime(Integer.parseInt(act_and_message[4])); //相手のタイマーに合わせる
						if(!(act_and_message[1].equals("pass"))){
							//相手のタイマー止める
							gt2.setStop();
							//gt2.settime(Integer.parseInt(act_and_message[4])); //相手のタイマーに合わせる
							int pos[] = {Integer.parseInt(act_and_message[1]),Integer.parseInt(act_and_message[2])};
							nothello.setOperation(pos, act_and_message[3]);
							gmf.turnsquare(pos[0], pos[1], false);
							int[][] turnGrids = null;
							turnGrids = nothello.getTurnOverGrid();
							System.out.println();

							for(int i = 0;1 <= turnGrids[i][0];i++){
								gmf.turnsquare(turnGrids[i][0],turnGrids[i][1], true);
							 	//効果音ならす
							}
						}


						nothello.changeTurn();
						turn = nothello.getTurn();
						gmf.repaintOther();
						if(nothello.judgeEnd() == false) {//この相手の手で対局終了か判断
							if(nothello.sendPass(turn) == false) {	//自分がパスするか判断
								nothello.searchPlace(turn);
								gmf.repaintallBoard();
								gt1.setStop();//自分のタイマー開始

							}else {//パスの場合
								gmf.timer1 = gt1.gettime();
								gt1.settime(gmf.timer1); //1秒未満切り上げ
								//Client.this.sendMessage("stopmytimer:"+gmf.timer1);//相手に自分のタイマーを止めてもらう
								soundEffect("cancel");
								JOptionPane.showMessageDialog(gmf, "パスします");
								soundEffect("push");
								//相手にパスであることを送る
								Client.this.sendMessage("othello operation:pass:pass:"+myColor+":"+gmf.timer1);
								nothello.changeTurn();
								turn = nothello.getTurn();
								gmf.repaintOther();
								if(nothello.judgeEnd() == false) {//パスの後もう一度対局終了か判断
									//自分のタイマーは止まったまま。相手のを再度動かす
									gt2.setStop();
								}else {//自分のパスで対局終了の場合
									//対局終了画面
									lastTime1 = gt1.gettime();
									lastTime2 = gt2.gettime();
									gmf.resultDisp(nothello.judgeResult(myColor));
									//gameEnd = true;
									//Client.this.sendMessage("quit");
								}

							}
						}else {//相手のこの1手で対局終了の場合
							//対局終了画面
							lastTime1 = gt1.gettime();
							lastTime2 = gt2.gettime();
							gmf.resultDisp(nothello.judgeResult(myColor));
							//gameEnd = true;
							//Client.this.sendMessage("quit");
						}
					}

				}else if(myMode.equals("time")){
					/*//相手のタイマー止める
					gt2.setStop();*/
					int gt2t = gt2.gettime();
					if((Integer.parseInt(act_and_message[5])-(gt2t)) > 5 || (Integer.parseInt(act_and_message[5])-(gt2t)) < -5) {
						//チート対策。5秒より大きい差があった場合
						gt2.setStop();//相手のタイマーを止める
						lastTime1 = -1;
						lastTime2 = -1;
						soundEffect("announce");
						JOptionPane.showMessageDialog(gmf, "タイマーへの不正な干渉の疑いがあります", "タイマーエラー",
								JOptionPane.WARNING_MESSAGE);
						soundEffect("push");
						gmf.resultDisp("draw");//引き分け画面の表示
						//resign = true;
						Client.this.sendMessage("timeerror");
					}else {
						gt2.settime(Integer.parseInt(act_and_message[5])); //相手のタイマーに合わせる
						if(!(act_and_message[1].equals("pass"))){
							//相手のタイマー止める
							gt2.setStop();
							//gt2.settime(Integer.parseInt(act_and_message[5])); //相手のタイマーに合わせる
							int pos[] = {Integer.parseInt(act_and_message[1]),Integer.parseInt(act_and_message[2])};
							tothello.setOperation(pos, act_and_message[3],Integer.parseInt(act_and_message[4]));
							int[][] turnGrids = null;
							turnGrids = tothello.getTurnOverGrid();
							gmf.turnsquare(pos[0], pos[1],false);

							for(int i = 0;1 <= turnGrids[i][0];i++){
								gmf.turnsquare(turnGrids[i][0],turnGrids[i][1],true);
							 	//効果音ならす
							}
						}
						else { //相手がパスの場合
							mnum[0]++; //自分の石のうち最も価格の低い石を1つ増やす
							gmf.mrButton[0].setText(gmf.mmark[0]+"：残り"+mnum[0]+"個");
							if(mnum[0] <= 1) {//最も価格の低い石を持っていなかった場合は、また選択できるようにする
								gmf.mrButton[0].setBackground(new Color(245,185,83));
								gmf.mrButton[0].setEnabled(true);
							}
							if(gmf.mchoice == -1) {//持っている石が全部なくなった状態だった場合は今増やした石を選択する
								gmf.mrButton[0].setBackground(new Color(201,102,10));
								gmf.mchoice = 0;
							}
						}

						tothello.changeTurn();
						turn = tothello.getTurn();
						gmf.repaintOther();
						if(tothello.judgeEnd() == false) {//対局終了か判断
							if(tothello.sendPass(turn) == false) {	//パスするか判断
								tothello.searchPlace(turn);
								gmf.repaintallBoard();
								gt1.setStop();

							}else {//自分がパスの場合
								gmf.timer1 = gt1.gettime();
								gt1.settime(gmf.timer1); //1秒未満切り上げ
								//Client.this.sendMessage("stopmytimer:"+gmf.timer1);//相手に自分のタイマーを止めてもらう
								soundEffect("cancel");
								JOptionPane.showMessageDialog(gmf, "パスします");
								soundEffect("push");
								//サーバーにパスを送る
								Client.this.sendMessage("othello operation:pass:pass:"+myColor+":0:"+gmf.timer1);
								tothello.changeTurn();
								turn = tothello.getTurn();
								gmf.repaintOther();
								if(tothello.judgeEnd() == false) {//パスの後もう一度対局終了か判断
									//自分のタイマーは止まったまま。相手のを再度動かす
									gt2.setStop();
								}else {//自分のパスで対局終了の場合
									//対局終了画面
									lastTime1 = gt1.gettime();
									lastTime2 = gt2.gettime();
									//gmf.resultDisp(nothello.judgeResult(myColor));
									score1 = tothello.getAmount(myColor) + lastTime1;
									if(myColor.equals("black")) {
										score2 = tothello.getAmount("white") + lastTime2;
										gmf.resultDisp(tothello.judgeResult(myColor,score1,score2));
									}else if(myColor.equals("white")){
										score2 = tothello.getAmount("black") + lastTime2;
										gmf.resultDisp(tothello.judgeResult(myColor,score2,score1));
									}
									//gameEnd = true;
									//Client.this.sendMessage("quit");
								}
							}
						}else {//相手のこの1手で対局終了の場合
							//対局終了画面
							lastTime1 = gt1.gettime();
							lastTime2 = gt2.gettime();
							score1 = tothello.getAmount(myColor) + lastTime1;
							if(myColor.equals("black")) {
								score2 = tothello.getAmount("white") + lastTime2;
								gmf.resultDisp(tothello.judgeResult(myColor,score1,score2));
							}else if(myColor.equals("white")){
								score2 = tothello.getAmount("black") + lastTime2;
								gmf.resultDisp(tothello.judgeResult(myColor,score2,score1));
							}
	//						gmf.resultDisp(tothello.judgeResult(myColor));
							//gameEnd = true;
							//Client.this.sendMessage("quit");
						}
					}
				}
				break;
			case "resign":	//相手の投了
				gt2.setStop();//相手のタイマーを止める
				lastTime1 = gt1.gettime();
				lastTime2 = gt2.gettime();
				soundEffect("announce");
				JOptionPane.showMessageDialog(gmf, "対戦相手が投了しました", "投了",
						JOptionPane.WARNING_MESSAGE);
				soundEffect("push");
				if(myMode.equals("time")) {
					score1 = tothello.getAmount(myColor) + lastTime1;
					if(myColor.equals("black")) {
						score2 = tothello.getAmount("white") + lastTime2;
					}else if(myColor.equals("white")){
						score2 = tothello.getAmount("black") + lastTime2;
					}
				}
				gmf.resultDisp("win");//勝利画面の表示
				//resign = true;
				//Client.this.sendMessage("quit");
				break;
			case "timeover":	//相手の時間切れ
				gt2.setStop();//相手のタイマーを止める
				lastTime1 = gt1.gettime();
				lastTime2 = 0;
				JOptionPane.showMessageDialog(gmf, "対戦相手の制限時間が0になりました", "時間切れ",
						JOptionPane.WARNING_MESSAGE);
				if(myMode.equals("time")) {
					score1 = tothello.getAmount(myColor) + lastTime1;
					if(myColor.equals("black")) {
						score2 = tothello.getAmount("white") + lastTime2;
					}else if(myColor.equals("white")){
						score2 = tothello.getAmount("black") + lastTime2;
					}
				}
				gmf.resultDisp("win");//勝利画面の表示
				//resign = true;
				break;
			case "timeerror":	//タイマーの明らかな差(チート)
				gt2.setStop();//相手のタイマーを止める
				lastTime1 = -1;
				lastTime2 = -1;
				soundEffect("announce");
				JOptionPane.showMessageDialog(gmf, "タイマーへの不正な干渉の疑いがあります", "タイマーエラー",
						JOptionPane.WARNING_MESSAGE);
				soundEffect("push");
				gmf.resultDisp("draw");//引き分け画面の表示
				//resign = true;
				break;
			case "quit":
//				//投了の後、または通常終了
//				if(resign == false && gameEnd == false) {
//					gmf.resultDisp(nothello.judgeResult(myColor));
//				}
//
				Client.this.sendMessage("quit");
				receiver.accept = false; //receiverを止める
				out.close();
				if (gameEnd == false) {
					if (chf != null) { //チャット画面が開いている場合
						chf.chat_timer.stop(); //チャットタイマーを止める
						if (chf.time_waiting != null) chf.time_waiting.dispose();	//相手の時間決定承認待ち画面が開いている場合は閉じる
						if (chf.time_ask != null) chf.time_ask.dispose(); //時間決定確認画面が開いている場合は閉じる
						if (chf.time_check != null) chf.time_check.dispose(); //時間決定確認結果画面が開いている場合は閉じる
					}
					if (gmf != null) { //対局画面が開いている場合
						gt1.end = true; //タイマーを止める
						gt2.end = true;
					}
					soundEffect("alert");
					JOptionPane.showMessageDialog(activePane, "対戦相手の接続が切れました\nタイトルに戻ります", "接続切れ",
							JOptionPane.WARNING_MESSAGE);
					soundEffect("push");
					Client oclient = new Client(); //起動し直す
					oclient.titleDisp();
					if (chf != null) { //チャット画面が開いている場合
						chf.dispose(); //チャット画面を閉じる
					}
					if (gmf != null) { //対局画面が開いている場合
						gmf.dispose(); //対局画面を閉じる
					}
				}
				break;
			case "stopmytimer":
				//相手のタイマー止める
				gt2.setStop();
				gt2.settime(Integer.parseInt(act_and_message[1])); //相手のタイマーに合わせる
				break;
			case "room is max":
				roommax = true;
				receiver.accept = false; //receiverを止める
				out.close();
				break;
			default:
				throw new Exception("正しくメッセージが受信されませんでした");
			}
		}
		catch(Exception e) {
			System.err.println("メッセージ受信時にエラーが発生しました");
			e.printStackTrace();
		}
	}

	public void sendMessage(String msg){	// サーバに操作情報を送信
		out.println(msg);//送信データをバッファに書き出す
		out.flush();//送信データを送る
		System.out.println("サーバにメッセージ " + msg + " を送信しました"); //テスト標準出力
	}
	//public void acceptOperation(String command){	// プレイヤの操作を受付
	//}
	//public void acceptChat(){	// チャットの操作を受付
	//}
	//public void judgeContinue(){		//終了継続判定
	//}
	public void	bgm(String bgm){	//BGMを流す
		if (bgmclip != null) {
			//bgmclip.stop();
			//bgmclip.flush();
			//bgmclip.setFramePosition(0);
			bgmclip.close();
		}
		if (playsound == true) {
			if (!(bgm.equals(""))) {
				try {
					String bgmtitle = "";
					switch (bgm) {
					case "title":
						bgmtitle = new String("PerituneMaterial_Labyrinth_loop.wav");
						break;
					case "chat":
						bgmtitle = new String("bgm_maoudamashii_piano30.wav");
						break;
					case "game":
						bgmtitle = new String("PerituneMaterial_Wish5_loop.wav");
						break;
					case "hurry":
						bgmtitle = new String("bgm_maoudamashii_orchestra24.wav");
						break;
					case "win":
						bgmtitle = new String("bgm_maoudamashii_healing13.wav");
						break;
					case "lose":
						bgmtitle = new String("bgm_maoudamashii_piano40.wav");
						break;
					case "draw":
						bgmtitle = new String("bgm_maoudamashii_acoustic49.wav");
						break;
					default:
						break;
					}
					AudioInputStream ais = AudioSystem.getAudioInputStream(new File(bgmtitle)); //ファイルを読み込む
					AudioFormat af = ais.getFormat(); //ファイル形式を取得
					DataLine.Info data = new DataLine.Info(Clip.class, af);
					bgmclip = (Clip)AudioSystem.getLine(data);
					bgmclip.open(ais);
					this.setVolume(soundVolume);
					bgmclip.loop(Clip.LOOP_CONTINUOUSLY);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	public void soundEffect(String sound){	//効果音を流す
		if (playsound == true) {
			int soundindex = -1;
			try {
				switch (sound) {
				case "title":
					soundindex = 0;
					break;
				case "push":
					soundindex = 1;
					break;
				case "cancel":
					soundindex = 2;
					break;
				case "alert":
					soundindex = 3;
					break;
				case "chat":
					soundindex = 4;
					break;
				case "gamestart":
					soundindex = 5;
					break;
				case "put":
					soundindex = 6;
					break;
				case "select":
					soundindex = 7;
					break;
				case "announce":
					soundindex = 8;
					break;
				case "open": //各音楽ファイルを開く
					String soundtitle[] = { //各効果音のファイル名
							new String("decision28.wav"), //インデックス：0, "title"
							new String("cursor1.wav"), //インデックス：1, "push"
							new String("cancel2.wav"), //インデックス：2, "cancel"
							new String("cancel5.wav"), //インデックス：3, "alert"
							new String("decision22.wav"), //インデックス：4, "chat"
							new String("se_maoudamashii_onepoint12.wav"), //インデックス：5, "gamestart"
							new String("wallet-close1.wav"), //インデックス：6, "put"
							new String("cursor4.wav"), //インデックス：7, "select"
							new String("cursor3.wav") //インデックス：8, "announce"
					};
					for (int i = 0; i < SOUNDEFFECT_NUM; i++) { //もし既に開いているクリップがあれば閉じておく
						if (seclip[i] != null) {
							seclip[i].close();
						}
					}
					for (int i = 0; i < SOUNDEFFECT_NUM; i++) {
						AudioInputStream ais = AudioSystem.getAudioInputStream(new File(soundtitle[i])); //ファイルを読み込む
						AudioFormat af = ais.getFormat(); //ファイル形式を取得
						DataLine.Info data = new DataLine.Info(Clip.class, af);
						seclip[i] = (Clip)AudioSystem.getLine(data);
						seclip[i].open(ais);
						this.setVolume(soundVolume);
					}
					seopened = true; //効果音ファイルが開かれているという情報を保持
					break;
				default:
					break;
				}
				if (soundindex != -1) {
					seclip[soundindex].stop();
					seclip[soundindex].flush();
					seclip[soundindex].setFramePosition(0);
					seclip[soundindex].start();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			for (int i = 0; i < SOUNDEFFECT_NUM; i++) { //もし既に開いているクリップがあれば閉じておく
				if (seclip[i] != null) {
					seclip[i].close();
				}
			}
			seopened = false; //効果音ファイルが閉じられているという情報を保持
		}
	}
	public void setVolume(int volume) {
		if(bgmclip != null) {
			FloatControl bgm_ctrl = (FloatControl)bgmclip.getControl(FloatControl.Type.MASTER_GAIN);
			bgm_ctrl.setValue((float)Math.log10((float)volume / 100) * 20);
		}
		for(int i = 0; i < SOUNDEFFECT_NUM; i++) {
			if (seclip[i] != null) {
				FloatControl se_ctrl = (FloatControl)seclip[i].getControl(FloatControl.Type.MASTER_GAIN);
				se_ctrl.setValue((float)Math.log10((float)volume / 100) * 20);
			}
		}
	}

	/*
		//マウスクリック時の処理
	public void mouseClicked(MouseEvent e) {
		JButton theButton = (JButton)e.getComponent();//クリックしたオブジェクトを得る．キャストを忘れずに
		String command = theButton.getActionCommand();//ボタンの名前を取り出す
		System.out.println("マウスがクリックされました。押されたボタンは " + command + "です。");//テスト用に標準出力
		}
	}
	public void mouseEntered(MouseEvent e) {}//マウスがオブジェクトに入ったときの処理
	public void mouseExited(MouseEvent e) {}//マウスがオブジェクトから出たときの処理
	public void mousePressed(MouseEvent e) {}//マウスでオブジェクトを押したときの処理
	public void mouseReleased(MouseEvent e) {}//マウスで押していたオブジェクトを離したときの処理
*/

	//テスト用のmain
		public static void main(String args[]){
			Client oclient = new Client();
			//oclient.connectServer("localhost", 10000);
			oclient.titleDisp();
		}
}