import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TIME_OthelloDriver {
	private static String turn;
	private static String[][] grid;

	public static void main(String[] args) throws Exception{
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in), 1);
		TIME_Othello tothello = new TIME_Othello(); //初期化
		String state;
		String money[] = {"10", "30", "50", "100"};

		System.out.println("石の金額は{\"" + money[0] + "\", \"" + money[1] + "\", \"" + money[2] + "\", \"" + money[3] + "\"}に設定します。");
		tothello.setMoney(money);

		System.out.println("テスト1：TIME_Othelloクラスのオブジェクトを初期化した結果：");
		state = printStatus(tothello);
		printGrid(tothello);

		while(!state.equals("end")) { //対局が終了するまで実行
			System.out.println("石を置く場所(行と列、2つの数字(1～8)及び石の種類(0～3))をキーボードで1つずつ入力してください。");
			System.out.println("1つ目の入力をpassにすると相手に手番が移ります。");
			int po[] = {1, 1};
			int mchoice = 0;
			boolean pass = false;
			while(true) {
				while (true) {
					String s = r.readLine(); //文字列の入力
					if (s.equals("pass")) {
						pass = true;
						System.out.println("passが入力されました。");
						break;
					}
					else {
						try {
							po[0] = Integer.parseInt(s);
							if (po[0] >= 1 && po[0] <= 8) {
								break;
							}
							else {
								System.out.println("1～8の数字を入力してください。");
							}
						} catch (Exception e){
							System.out.println("int型への変換に失敗しました。もう一度入力してください。");
						}
					}
				}
				if (pass == false) {
					while (true) {
						String s = r.readLine(); //文字列の入力
						try {
							po[1] = Integer.parseInt(s);
							if (po[1] >= 1 && po[1] <= 8) {
								break;
							}
							else {
								System.out.println("1～8の数字を入力してください。");
							}
						} catch (Exception e){
							System.out.println("int型への変換に失敗しました。もう一度入力してください。");
						}
					}
					if (grid[po[0]][po[1]].equals("possible")) { //石が置ける場所か確認する
						break;
					}
					else {
						System.out.println("そこには石を置けません。石を置く場所を入力し直してください。");
					}
				}
				else {
					break;
				}
			}
			if (pass == false) {
				while (true) {
					String s = r.readLine(); //文字列の入力
					try {
						mchoice = Integer.parseInt(s);
						if (mchoice >= 0 && mchoice <= 3) {
							break;
						}
						else {
							System.out.println("0～3以外の値が入力されました。もう一度入力してください。");
						}
					} catch (Exception e){
						System.out.println("int型への変換に失敗しました。もう一度入力してください。");
					}
				}
				System.out.println("(" + po[0] + ", " + po[1] + ", " + mchoice + ")が入力されました。手番は" + turn + "です。");
				tothello.setOperation(po, turn, mchoice);
				System.out.println("ひっくり返せる石(turnOverGrid)を取得(getTurnOverGrid()を実行)。");
				int[][] turnOverGrid = tothello.getTurnOverGrid();

				System.out.println("turnOverGridテスト出力：");
				for (int i = 0; i < 64; i++) {
					System.out.println(turnOverGrid[i][0] + ", " + turnOverGrid[i][1]);
				}

				state = printStatus(tothello);
				printGrid(tothello);
			}
			System.out.println("手番を変更します。");
			tothello.changeTurn();
			state = printStatus(tothello);
			printGrid(tothello);
			if (state.equals("pass")) {
				System.out.println("置ける場所がありません。パスします。");
				System.out.println("手番を変更します。");
				tothello.changeTurn();
				state = printStatus(tothello);
				printGrid(tothello);
			}
		}
	}

	//状態を表示する
	public static String printStatus(TIME_Othello tothello) {
		turn = tothello.getTurn();
		System.out.println("getTurn出力：" + turn);

		System.out.println("石の個数を数えます(countStone()を実行)。");
		tothello.countStone();

		int blackNum = tothello.getStoneNum("black");
		int whiteNum = tothello.getStoneNum("white");
		System.out.println("getStoneNum(\"black\")出力：" + blackNum);
		System.out.println("getStoneNum(\"white\")出力：" + whiteNum);

		System.out.println("総額を計算します(calcAmountNum()を実行)。");
		tothello.calcAmountNum();

		int blackAmount = tothello.getAmount("black");
		int whiteAmount = tothello.getAmount("white");
		System.out.println("getAmount(\"black\")出力：" + blackAmount);
		System.out.println("getAmount(\"white\")出力：" + whiteAmount);

		boolean end = tothello.judgeEnd();
		System.out.println("judgeEnd出力：" + end);

		if (end ==true) {
			System.out.println("最終残り時間は5分(300秒)とします。");
			int bscore = blackAmount + 300;
			System.out.println("黒のスコア：" + bscore);
			int wscore = whiteAmount + 300;
			System.out.println("白のスコア：" + wscore);
			System.out.println("judgeResult(black, " + bscore + ", " + wscore + ")出力：" + tothello.judgeResult("black", bscore, wscore));
			System.out.println("judgeResult(white, " + bscore + ", " + wscore + ")出力：" + tothello.judgeResult("white", bscore, wscore));
			return "end";
		}
		else {
		boolean pass = tothello.sendPass(turn);
		System.out.println("sendPass(\"" + turn + "\")出力：" + pass);
			if (pass == true) {
				return "pass";
			}
		}
		return "";
	}

	//テスト用に盤面を表示する
	public static void printGrid(TIME_Othello tothello) {
		System.out.println("Grid(盤面)を取得(getGrid()を実行)。");
		grid = tothello.getGrid();

		System.out.println("Gridテスト出力：");
		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 8; j++) {
				System.out.printf("%-9s", grid[i][j]);
			}
			System.out.printf("\n");
		}
		System.out.printf("\n");

		System.out.println("置ける場所を探索(searchPlace(\"" + turn + "\"))");
		tothello.searchPlace(turn);

		System.out.println("Grid(盤面)を取得(getGrid()を実行)。");
		grid = tothello.getGrid();

		System.out.println("Gridテスト出力：");
		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 8; j++) {
				System.out.printf("%-9s", grid[i][j]);
			}
			System.out.printf("\n");
		}
		System.out.printf("\n");
	}
}