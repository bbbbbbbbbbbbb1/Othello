public class TIME_Othello extends Normal_Othello {
	private static final int ROW = 8;	//行、列の数
	private static final int DIRECTION = 8;	//石を返せる方向の数
	private String turn; //手番
	private String[][] grid = new String[ROW + 2][ROW + 2];	//盤面
		//番兵法を使用するため、行列を２マスずつ多く取っている。オセロ盤は[1][1]～[1][8]、･･･[8][1]～[8][8]まで格納
	//private int[][] possibleGrid = new int[ROW * ROW][2];	//置ける場所(行：置ける位置の最大数(マスの総数)、列：位置)
	private int[][] turnOverGrid = new int[DIRECTION * ROW][2];	//置いたときにひっくり返せる石(クライアント用)
		//行：DIRECTION * ROWは、同時にひっくり返せる石の最大数を仮定したもの、列：位置
	private int blackNum;	//黒の石の数
	private int whiteNum;	//白の石の数

	private String[] money = new String[4];	//各石１枚ごとの値段
	private int[] blackNum_time = new int[4];	//黒の石の数(石の種類別)
	private int[] whiteNum_time = new int[4];	//白の石の数(石の種類別)
	private int blackAmount;	//黒の総額
	private int whiteAmount;	//白の総額
	//private int blackScore;	//黒のスコア
	//private int whiteScore;	//白のスコア
	//private int starttime = 10;	//初期制限時間(仮決定で１０分)
	//private String timer;

	public TIME_Othello() {	//ROW = 8の場合の盤面の初期設定
		for(int i = 0; i <= ROW + 1; i++) {	//番兵法の使用のため
			if(i == 0 || i == ROW + 1) {
				for(int j = 0; j <= ROW + 1; j++) {
					grid[i][j] = "banpei";
				}
			}
			else {
				grid[i][0] = "banpei";
				grid[i][ROW + 1] = "banpei";
			}
		}

		//何も置いていないマスはboard(空白)を入れる
		for(int i = 1; i <= ROW; i++) {
			for(int j = 1; j <= ROW; j++) {
				grid[i][j] = "board";
			}
		}

		//初期配置(setMoney内でセット)
		//this.grid[4][4] = "black" + money[2];
		//this.grid[4][5] = "white" + money[2];
		//this.grid[5][4] = "white" + money[2];
		//this.grid[5][5] = "black" + money[2];

		//possibleGridの初期化
		/*for(int i = 0; i < possibleGrid.length; i++) {
			possibleGrid[i][0] = -1;
			possibleGrid[i][1] = -1;
		}*/
		//turnOverGridの初期化
		for(int i = 0; i < turnOverGrid.length; i++) {
			turnOverGrid[i][0] = -1;
			turnOverGrid[i][1] = -1;
		}
		this.turn = "black";	//最初は黒の手番
		//石の数(石の種類別)をセット
		this.blackNum_time[0] = 0;
		this.blackNum_time[1] = 0;
		this.blackNum_time[2] = 2;
		this.blackNum_time[3] = 0;
		this.whiteNum_time[0] = 0;
		this.whiteNum_time[1] = 0;
		this.whiteNum_time[2] = 2;
		this.whiteNum_time[3] = 0;

		//総額を初期化
		this.blackAmount = 0;
		this.whiteAmount = 0;
	}

	public String getTurn() {	//手番を取得
		return this.turn;
	}

	public String[][] getGrid() {	//盤面を取得
		return this.grid;
	}

	public int[][] getTurnOverGrid() {	//置ける位置の取得
		return this.turnOverGrid;
	}

	public int getAmount(String color) {	//総額を取得
		if(color.equals("black")) {
			return this.blackAmount;
		}
		else {
			return this.whiteAmount;
		}
	}

	public void setMoney(String[] money) {	//それぞれの石の値段をセット
		for(int i = 0; i < 4; i++) {
			this.money[i] = money[i];
		}
		//初期配置
		this.grid[4][4] = "black" + this.money[2];
		this.grid[4][5] = "white" + this.money[2];
		this.grid[5][4] = "white" + this.money[2];
		this.grid[5][5] = "black" + this.money[2];
	}

	public void searchPlace(String color) {	//置ける場所の探索
		String opponent_color;	//相手の石の色
		//int possibleNum = 0;	//石を置けるマスの数
		//opponent_colorの設定がここから
		if(color.equals("black")) {
			opponent_color = "white";
		}
		else {
			opponent_color = "black";
		}
		//ここまで
		//置ける場所の探索
		for(int m = 1; m <= ROW; m++) {	//行の繰り返し
			columnLoop : for(int n = 1; n <= ROW; n++) {	//列の繰り返し
				if(grid[m][n] != "board" && grid[m][n] != "possible") {	//空白じゃなかったら飛ばす
					continue;
				}
				for(int u = -1; u <= 1; u++) {	//上下方向(-1：上、1：下)
					for(int v = -1; v <= 1; v++) {	//左右方向(-1：左、1：右)
						if(u == 0 && v == 0) {	//uもvも0だったらそのマス自身を指すので飛ばす
							continue;
						}
						int k;
						for(k = 1; grid[m + k * u][n + k * v].startsWith(opponent_color); k++) {}	//kを増やす
						//k > 1：隣のマスが自分の石だと置けない
						if(k > 1 && grid[m + k * u][n + k * v].startsWith(color)) {
							//置けるマスに登録
							grid[m][n] = "possible";
							//possibleGrid[possibleNum][0] = m;
							//possibleGrid[possibleNum][1] = n;
							//possibleNum++;
							continue columnLoop;	//別のマスの探索に移る
						}
						//空白か番兵なら他の方向の探索に移る
						else {
							continue;
						}
					}
				}
			}
		}
	}

	public void setOperation(int[] p, String color, int mchoice) {	//自分の操作を設定
		int m = p[0];	//置くマスの行
		int n = p[1];	//置くマスの列
		String opponent_color;	//相手の石の色
		int possibleNum = 0;	//ひっくり返せる石の数

		//opponent_colorの設定がここから
		if(color.equals("black")) {
			opponent_color = "white";
		}
		else {
			opponent_color = "black";
		}
		this.grid[m][n] = color + money[mchoice];
		System.out.printf("%s", grid[m][n]);
		//ここまで
		//石をひっくり返すのと、turnOverGridの設定
		for(int u = -1; u <= 1; u++) {	//上下方向(-1：上、1：下)
			for(int v = -1; v <= 1; v++) {	//左右方向(-1：左、1：右)
				if(u == 0 && v == 0) {	//uもvも0だったらそのマス自身を指すので飛ばす
					continue;
				}
				int k;
				for(k = 1; grid[m + k * u][n + k * v].startsWith(opponent_color); k++) {}	//kを増やす
				//k > 1：隣のマスが自分の石だと置けない
				if(k > 1 && grid[m + k * u][n + k * v].startsWith(color)) {
					for(int i = 1; i < k; i++) {
						grid[m + i * u][n + i * v] = grid[m + i * u][n + i * v].replace(opponent_color, color);	//石をひっくり返す
						//ひっくり返せる石に登録
						turnOverGrid[possibleNum][0] = m + i * u;
						turnOverGrid[possibleNum][1] = n + i * v;
						possibleNum++;
					}
				}
			}
		}
	}

	public void countStone() {	//石の個数を数える
		//石の個数の初期化
		this.blackNum = 0;
		this.whiteNum = 0;
		for(int i = 0; i < 4; i++) {
			this.blackNum_time[i] = 0;
			this.whiteNum_time[i] = 0;
		}
		for(int i = 1; i <= ROW; i++) {
			for(int j = 1; j <= ROW; j++) {
				if(grid[i][j].startsWith("black")) {	//黒の個数を増やす
					if(grid[i][j].endsWith(money[3])) {
						this.blackNum_time[3]++;
					}
					else if(grid[i][j].endsWith(money[2])) {
						this.blackNum_time[2]++;
					}
					else if(grid[i][j].endsWith(money[1])) {
						this.blackNum_time[1]++;
					}
					else if(grid[i][j].endsWith(money[0])) {
						this.blackNum_time[0]++;
					}
					this.blackNum++;
				}
				else if(grid[i][j].startsWith("white")) {	//白の個数を増やす
					if(grid[i][j].endsWith(money[3])) {
						this.whiteNum_time[3]++;
					}
					else if(grid[i][j].endsWith(money[2])) {
						this.whiteNum_time[2]++;
					}
					else if(grid[i][j].endsWith(money[1])) {
						this.whiteNum_time[1]++;
					}
					else if(grid[i][j].endsWith(money[0])) {
						this.whiteNum_time[0]++;
					}
					this.whiteNum++;
				}
			}
		}
	}

	public int getStoneNum(String color) {	//石の個数を取得
		if(color.equals("black")) {
			return this.blackNum;	//黒の個数を取得
		}
		else {
			return this.whiteNum;	//白の個数を取得
		}
	}

	public void calcAmountNum() {	//総額を計算
		this.blackAmount = 0;
		this.whiteAmount = 0;
		for(int i = 0; i < 4; i++) {
			this.blackAmount += (Integer.parseInt(money[i]) * blackNum_time[i]);
			this.whiteAmount += (Integer.parseInt(money[i]) * whiteNum_time[i]);
		}
	}

	public void changeTurn() {	//手番の変更と配列のリセット
		if(this.turn.equals("black")) {	//黒の手番なら
			this.turn = "white";	//白に変更
		}
		else if(this.turn.equals("white")) {	//白の手番なら
			this.turn = "black";	//黒に変更
		}
		/*for(int i = 0; i < possibleGrid.length; i++) {	//possibleGridのリセット(全要素に-1を代入)
			possibleGrid[i][0] = -1;
			possibleGrid[i][1] = -1;
		}*/
		for(int i = 0; i < turnOverGrid.length; i++) {	////turnOverGridのリセット(全要素に-1を代入)
			turnOverGrid[i][0] = -1;
			turnOverGrid[i][1] = -1;
		}

		for(int i = 1; i <= ROW; i++) {
			for(int j = 1; j <= ROW; j++) {
				if(grid[i][j].equals("possible")) {
					grid[i][j] = "board";
				}
			}
		}
	}

	public boolean judgeEnd() {	//対局終了を判断(true：対局終了)
		if(sendPass("black") == true && sendPass("white") == true) {	//黒も白もパスか、全マスが埋まっている
			return true;	//対局終了
		}
		else {
			return false;
		}
	}

	public String judgeResult(String color, int bscore, int wscore) {	//対局結果を返す
		//クライアントの色が黒か白かで処理を変える
		if(color.equals("black")) {
			if(bscore > wscore) {
				return "win";
			}
			else if(bscore < wscore) {
				return "lose";
			}
			else {
				return "draw";
			}
		}
		else {
			if(bscore > wscore) {
				return "lose";
			}
			else if(bscore < wscore) {
				return "win";
			}
			else {
				return "draw";
			}
		}
	}

	//
	/*public void calcScore(int time) {	//現時点では金額をそのままスコアに設定
		this.blackScore = 0;
		this.whiteScore = 0;
		for(int i = 0; i < 4; i++) {
			this.blackScore += (Integer.parseInt(money[i]) * blackNum_time[i]);
			this.whiteScore += (Integer.parseInt(money[i]) * whiteNum_time[i]);
		}
		this.blackScore += time;
		this.whiteScore += time;
	}*/

	public boolean sendPass(String color) {	//true：パス
		String opponent_color;	//相手の石の色
		//opponent_colorの設定がここから
		if(color.equals("black")) {
			opponent_color = "white";
		}
		else {
			opponent_color = "black";
		}
		//ここまで
		for(int m = 1; m <= ROW; m++) {	//行の繰り返し
			for(int n = 1; n <= ROW; n++) {	//列の繰り返し
				if(grid[m][n] != "board" && grid[m][n] != "possible") {	//空白じゃなかったら飛ばす
					continue;
				}
				for(int u = -1; u <= 1; u++) {	//上下方向(-1：上、1：下)
					for(int v = -1; v <= 1; v++) {	//左右方向(-1：左、1：右)
						if(u == 0 && v == 0) {	//uもvも0だったらそのマス自身を指すので飛ばす
							continue;
						}
						int k;
						for(k = 1; grid[m + k * u][n + k * v].startsWith(opponent_color); k++) {}
						//k > 1：隣のマスが自分の石だと置けない
						if(k > 1 && grid[m + k * u][n + k * v].startsWith(color)) {
							return false;	//置けたらfalse
						}
						else {
							continue;
						}
					}
				}
			}
		}
		return true;	//どこにも置けなかったらtrue(パス)
	}

	//以下デバッグ用メソッド(このクラス単独で使う)

	/*public boolean possibleStone(int x, int y) {	//入力されたマスに石を置けるかチェック(true：置ける)
		int m = 0, n = 0;
		while(possibleGrid[m][n] >= 1 && possibleGrid[m][n] <= ROW) {
			if(possibleGrid[m][0] == x && possibleGrid[m][1] == y) {	//置けるマスに登録されていたら
				return true;
			}
			m++;
		}
		return false;
	}
	public static void main(String[] args) {
		TIME_Othello no = new TIME_Othello();
		while(no.judgeEnd() == false) {	//対局終了か判断
			if(no.sendPass(no.turn) == false) {	//パスか判断
				no.searchPlace(no.turn);	//置ける場所の探索
				//オセロ盤の表示
				System.out.printf("  |");
				for(int j = 1; j <= ROW; j++) {
					System.out.printf("  %d", j);
					if(j == ROW) {
						System.out.println("");
					}
				}
				for(int i = 1; i <= ROW; i++) {
					System.out.printf("%d | ", i);
					for(int j = 1; j<= ROW; j++) {
						if(no.grid[i][j] == "black") {
							System.out.printf("● ");
						}
						else if(no.grid[i][j] == "white") {
							System.out.printf("○ ");
						}
						else {
							System.out.printf("   ");
						}
						if(j == ROW) {
							System.out.println("");
						}
					}
				}
				//石の個数を数えて表示
				System.out.printf("黒：%d    白：%d\n", no.blackNum, no.whiteNum);
				System.out.printf("%sの手番です\n", no.turn);
				int n = 0;
				//置ける場所の表示
				while(no.possibleGrid[n][0] >= 1) {
					System.out.printf("(%d, %d)に置けます\n", no.possibleGrid[n][0], no.possibleGrid[n][1]);
					n++;
				}
				n = 0;
				System.out.println("");
				int x, y;
				//置くマスの入力
				do {
					System.out.printf("> 行：");
					x = new java.util.Scanner(System.in).nextInt();
					System.out.printf("> 列：");
					y = new java.util.Scanner(System.in).nextInt();
				} while(no.possibleStone(x, y) == false);	//置けなかったら再入力
				int[] p = new int[2];
				p[0] = x; p[1] = y;
				//操作を反映
				no.setOperation(p, no.turn);
				no.countStone();
				//ひっくり返したマスの表示
				while(no.turnOverGrid[n][0] >= 1) {
					System.out.printf("(%d, %d)をひっくり返しました\n", no.turnOverGrid[n][0], no.turnOverGrid[n][1]);
					n++;
				}
			}
			no.changeTurn();	//手番の変更と配列のリセット
		}
		//最終的な盤面の状態を表示
		for(int i = 1; i <= ROW; i++) {
			System.out.printf("%d | ", i);
			for(int j = 1; j<= ROW; j++) {
				if(no.grid[i][j] == "black") {
					System.out.printf("● ");
				}
				else if(no.grid[i][j] == "white") {
					System.out.printf("○ ");
				}
				else {
					System.out.printf("   ");
				}
				if(j == ROW) {
					System.out.println("");
				}
			}
		}
		//最後の石の数を表示
		System.out.printf("対局終了\n黒：%d    白：%d\n", no.blackNum, no.whiteNum);
		//勝敗判断
		if(no.judgeResult("black").equals("win")) {
			System.out.printf("黒の勝ちです\n");
		}
		else if(no.judgeResult("black").equals("lose")) {
			System.out.printf("白の勝ちです\n");
		}
		else {
			System.out.printf("引き分けです\n");
		}
	}*/
}