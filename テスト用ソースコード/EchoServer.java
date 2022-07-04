import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {
    static int port = 10000;

    public static void main(String[] args) {
    	try {
    		ServerSocket server = new ServerSocket(port);
    		Socket sock[] = new Socket[2];
    		System.out.println("EchoServer:サーバが起動しました");
    		try {
    			sock[0] = server.accept(); // クライアントドライバからの接続を待つ

    			System.out.println("EchoServer:クライアントドライバと接続しました");
    			BufferedReader in = new BufferedReader(
    					new InputStreamReader(sock[0].getInputStream()));
    			sock[1] = server.accept(); // クライアントからの接続を待つ
    			System.out.println("EchoServer:クライアントと接続しました");
    			PrintWriter out[] = new PrintWriter[2];
    			out[0] = new PrintWriter(sock[0].getOutputStream());
    			out[1] = new PrintWriter(sock[1].getOutputStream());
    			String s;
    			while(true) { // クライアントドライバから一行受信
    				s = in.readLine();
    				out[1].print(s + "\r\n"); // クライアントへ一行送信
    				out[1].flush();
    				if(s.equals("quit")) {
    					break;
    				}
    			}
    			sock[0].close(); // クライアントドライバからの接続を切断
    			sock[1].close(); // クライアントからの接続を切断
    			server.close();
    			System.out.println("EchoServer切断しました");
    		} catch (IOException e) {
    			System.err.println(e);
    		}
    	} catch (IOException e) {
    		System.err.println(e);
    	}
    }
}