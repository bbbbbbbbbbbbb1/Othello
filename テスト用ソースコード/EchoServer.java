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
    		System.out.println("EchoServer:�T�[�o���N�����܂���");
    		try {
    			sock[0] = server.accept(); // �N���C�A���g�h���C�o����̐ڑ���҂�

    			System.out.println("EchoServer:�N���C�A���g�h���C�o�Ɛڑ����܂���");
    			BufferedReader in = new BufferedReader(
    					new InputStreamReader(sock[0].getInputStream()));
    			sock[1] = server.accept(); // �N���C�A���g����̐ڑ���҂�
    			System.out.println("EchoServer:�N���C�A���g�Ɛڑ����܂���");
    			PrintWriter out[] = new PrintWriter[2];
    			out[0] = new PrintWriter(sock[0].getOutputStream());
    			out[1] = new PrintWriter(sock[1].getOutputStream());
    			String s;
    			while(true) { // �N���C�A���g�h���C�o�����s��M
    				s = in.readLine();
    				out[1].print(s + "\r\n"); // �N���C�A���g�ֈ�s���M
    				out[1].flush();
    				if(s.equals("quit")) {
    					break;
    				}
    			}
    			sock[0].close(); // �N���C�A���g�h���C�o����̐ڑ���ؒf
    			sock[1].close(); // �N���C�A���g����̐ڑ���ؒf
    			server.close();
    			System.out.println("EchoServer�ؒf���܂���");
    		} catch (IOException e) {
    			System.err.println(e);
    		}
    	} catch (IOException e) {
    		System.err.println(e);
    	}
    }
}