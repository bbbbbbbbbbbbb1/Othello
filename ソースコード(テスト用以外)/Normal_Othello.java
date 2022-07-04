public class Normal_Othello {
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
	//private String timer;

	public Normal_Othello() {	//ROW = 8の場合の盤面の初期設定
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
		//初期配置
		this.grid[4][4] = "black";
		this.grid[4][5] = "white";
		this.grid[5][4] = "white";
		this.grid[5][5] = "black";
		//何も置いていないマスはboard(空白)を入れる
		for(int i = 1; i <= ROW; i++) {
			for(int j = 1; j <= ROW; j++) {
				if(grid[i][j] != "black" && grid[i][j] != "white") {
					grid[i][j] = "board";
				}
			}
		}

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
		this.blackNum = 2;	//黒は２個
		this.whiteNum = 2;	//白は２個
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

	public void receiveGrid(String[][] grid) {	//相手から受信した盤面を設定
		this.grid = grid;
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
						for(k = 1; grid[m + k * u][n + k * v].equals(opponent_color); k++) {}	//kを増やす
						//k > 1：隣のマスが自分の石だと置けない
						if(k > 1 && grid[m + k * u][n + k * v].equals(color)) {
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

	public void setOperation(int[] p, String color) {	//自分の操作を設定
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
		this.grid[m][n] = color;
		//ここまで
		//石をひっくり返すのと、turnOverGridの設定
		for(int u = -1; u <= 1; u++) {	//上下方向(-1：上、1：下)
			for(int v = -1; v <= 1; v++) {	//左右方向(-1：左、1：右)
				if(u == 0 && v == 0) {	//uもvも0だったらそのマス自身を指すので飛ばす
					continue;
				}
				int k;
				for(k = 1; grid[m + k * u][n + k * v].equals(opponent_color); k++) {}	//kを増やす
				//k > 1：隣のマスが自分の石だと置けない
				if(k > 1 && grid[m + k * u][n + k * v].equals(color)) {
					for(int i = 1; i < k; i++) {
						grid[m + i * u][n + i * v] = color;	//石をひっくり返す
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
		for(int i = 1; i <= ROW; i++) {
			for(int j = 1; j <= ROW; j++) {
				if(grid[i][j].equals("black")) {	//黒の個数を増やす
					this.blackNum++;
				}
				else if(grid[i][j].equals("white")) {	//白の個数を増やす
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

	public String judgeResult(String color) {	//対局結果を返す
		//クライアントの色が黒か白かで処理を変える
		if(color.equals("black")) {
			if(this.blackNum > this.whiteNum) {
				return "win";
			}
			else if(this.blackNum < this.whiteNum) {
				return "lose";
			}
			else {
				return "draw";
			}
		}
		else {
			if(this.blackNum > this.whiteNum) {
				return "lose";
			}
			else if(this.blackNum < this.whiteNum) {
				return "win";
			}
			else {
				return "draw";
			}
		}
	}

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
						for(k = 1; grid[m + k * u][n + k * v].equals(opponent_color); k++) {}
						//k > 1：隣のマスが自分の石だと置けない
						if(k > 1 && grid[m + k * u][n + k * v].equals(color)) {
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
}