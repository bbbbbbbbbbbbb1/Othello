import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Normal_Othello_Driver {
	private static final int ROW = 8;	//行数
	private static Normal_Othello nothello = new Normal_Othello();
	private static String [][] grid;	//盤面(表示用)

	public static void main(String[] args) throws Exception {
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in), 1);	//入力受付用のバッファ
		String DELIMITER = ":";	//区切り文字(コロン)

		grid = nothello.getGrid();	//盤面読み込み
		System.out.println("countStone:石の数をカウント");
		nothello.countStone();	//石の数のカウント
		System.out.println("searchPlace:置ける場所の探索");
		nothello.searchPlace(nothello.getTurn());	//石をおける場所の探索
		System.out.println("Normal_Othelloクラスのオブジェクトを初期化した結果：");
		printStatus(nothello);	//各変数、メソッドの出力結果の表示

		System.out.println("judgeEnd:対局終了か判断\n");
		judgeLoop: while(nothello.judgeEnd() == false){
			nothello.countStone();
			nothello.searchPlace(nothello.getTurn());
			printStatus(nothello);	//各変数、メソッドの出力結果の表示
			printGrids(nothello);	//盤面の表示
			System.out.println("sendPass:パスかどうかチェック");
			if(nothello.sendPass(nothello.getTurn()) == false) {	//石が置ける場合の処理
				int[] p = new int[2];
				do {
					System.out.println("石を置く場所(行：列)をキーボードで入力してください。");
					System.out.println("6：4のように、コロンで区切って入力してください。");
					System.out.println("passと入力すると相手に手番が移ります。");
					System.out.print("==> ");
					String s = r.readLine();//文字列の入力
					if(s.equals("pass")) {	//passが入力された場合
						System.out.println("turnOverGridは初期状態です。\n");
						System.out.println("changeTurn:手番を変更します。\n");
						nothello.changeTurn();	//手番の変更
						continue judgeLoop;
					}
					System.out.println(s + " が入力されました。手番は " + nothello.getTurn() + " です。");
					String split[] = s.split(DELIMITER, -1);
					if(split.length == 2) {
						try {
							p[0] = Integer.parseInt(split[0]); p[1] = Integer.parseInt(split[1]);
							if(p[0] > 0 && p[0] <= ROW && p[1] > 0 && p[1] <= ROW) {
								//盤面上の置けない場所が選択された場合
								if(!(grid[p[0]][p[1]].equals("possible"))) {
									System.out.println("置けない場所が入力されました。\n");
									continue;
								}
							}
							else {	//盤面外など、想定外の番号が入力された場合
								System.out.println("不正な番号です。\n");
								p[0] = 0; p[1] = 0;
								continue;
							}
						}
						catch(Exception e) {	//ひらがな、カタカナなど不正な文字列が入力された場合
							System.out.println("正しい書式で入力されませんでした。\n");
							continue;
						}
					}
					else {	//誤った書式で入力された場合
						System.out.println("正しい書式で入力されませんでした。\n");
						continue;
					}
				} while(!(grid[p[0]][p[1]].equals("possible")));
				//石を置いたときの処理
				System.out.println("setOperation:操作を盤面に反映");
				nothello.setOperation(p, nothello.getTurn());
				System.out.println("countStone:石の数をカウント");
				nothello.countStone();
				int [][] turnOverGrid = nothello.getTurnOverGrid();
				int i = 0;
				//turnOverGridの表示
				while(turnOverGrid[i][0] > 0) {
					System.out.printf("turnOverGrid出力([%d][0], [%d][1]) ==> (%d, %d)\n",
							i, i, turnOverGrid[i][0], turnOverGrid[i][1]);
					i++;
				}
				//石を返す処理がなかった場合
				if(i == 0) {
					System.out.println("turnOverGridは初期状態です。\n");
				}
			}
			//石が置けずにパスになった場合
			else {
				System.out.println("置ける場所がありません。パスします。\n");
			}
			System.out.println("changeTurn:手番を変更します。\n");
			nothello.changeTurn();	//手番の変更
		}
		//以下、対局終了時の処理
		System.out.println("対局終了です。\n");
		System.out.println("judgeResult(\"black\")出力:" + nothello.judgeResult("black"));
		System.out.println("judgeResult(\"white\")出力:" + nothello.judgeResult("white"));
		System.out.println("");
		printStatus(nothello);	//各変数、メソッドの出力結果の表示
		printGrids(nothello);	//盤面の表示
	}

	//状態を表示する
	public static void printStatus(Normal_Othello nothello){
		System.out.println("====各変数の出力====");
		System.out.println("printStatus");
		System.out.println("getTurn出力:" + nothello.getTurn());
		System.out.println("getStoneNum(\"black\")出力:" + nothello.getStoneNum("black"));
		System.out.println("getStoneNum(\"white\")出力:" + nothello.getStoneNum("white"));
		System.out.println("judgeEnd出力:" + nothello.judgeEnd());
		System.out.println("sendPass出力:" + nothello.sendPass(nothello.getTurn()));
		System.out.println("====================\n");

	}

	//テスト用に盤面を表示する
	public static void printGrids(Normal_Othello nothello){
		String [][] grid = nothello.getGrid();
		System.out.println("====盤面のテスト出力====");
		System.out.println("printGrids");
		System.out.println("gridテスト出力：(10要素ごとに改行)");
		System.out.println("(黒：●、白：○、置けるマス：□、置けないマス：×、番兵：■、エラー：？)");
		for(int i = 0; i <= ROW + 1; i++){
			for(int j = 0; j <= ROW + 1; j++) {
				if(grid[i][j].equals("black")) {
					System.out.printf("●");
				}
				else if(grid[i][j].equals("white")) {
					System.out.printf("○");
				}
				else if(grid[i][j].equals("possible")) {
					System.out.printf("□");
				}
				else if(grid[i][j].equals("board")) {
					System.out.printf("×");
				}
				else if(grid[i][j].equals("banpei")){
					System.out.printf("■");
				}
				else {
					System.out.printf("？");
				}
				if(j == ROW + 1) {
					System.out.println("");
				}
			}
		}
		System.out.println("========================\n");
	}
}
