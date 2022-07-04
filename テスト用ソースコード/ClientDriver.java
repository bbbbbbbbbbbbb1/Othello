import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientDriver{

	public static void main(String[] args) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in), 1);
		Socket socket = null;
		PrintWriter out = null;
		Client oclient = new Client();
		oclient.titleDisp();
		System.out.println("ClientDriver:�e�X�g�p�T�[�o�ɐڑ����܂�");
		try {
			//oclient.connectServer("localhost", 10000);
			socket = new Socket("localhost", 10000); //EchoServer�ɐڑ�
			out = new PrintWriter(socket.getOutputStream(), true); //�f�[�^���M�p�I�u�W�F�N�g�̗p��
		} catch (IOException e) {
			// TODO �����������ꂽ catch �u���b�N
			e.printStackTrace();
			System.out.println("ClientDriver:�T�[�o�Ƃ̐ڑ����؂�܂���");
		}

		System.out.println("�^�C�g����ʂŃ��[�h�̑I���Ɩ��O����͂��Ă�������");
		System.out.println("��M�p�e�X�g���b�Z�[�W����͂��Ă�������");
		System.out.println("���M�����M�p�e�X�g���b�Z�[�W�ƃ^�C�~���O�͈ȉ��̒ʂ�");
		System.out.println("1.���O���͌�@�@�@�@  :�ulogin success�v�܂��́uName is not available�v�܂��́uroom is max�v");
		System.out.println("2.�ulogin success�v��F�ublack(�܂��� white):[����̃v���C����]�v");
		System.out.println("3.�`���b�g��ʕ\���� �F�ucomplete chatDisp ack�v");
		System.out.println("4.�`���b�g           �F�uchat msg:[�`���b�g�ő��郁�b�Z�[�W���e]�v�܂��́utime ask:[5�`15�̐���]�v");
		System.out.println("5.time ask��         �F�utime check:[���肩���]���ꂽ���� / NG]�v");
		System.out.println("6.�΋ǉ�ʕ\����     �F�ucomplete gameDisp ack�v");
		System.out.println("7.�΋ǒ�             �F�m�[�}�����[�h�́uothello operation:[x]:[y]:[black/white]:[�^�C�}�[�c��b���̐���]\r\n" +
						   "                      : �ꍏ������[�h�́uothello operation:[x]:[y]:[black/white]:[���i�̎��0/1/2/3]:[�^�C�}�[�c��b���̐���]�v");
		System.out.println("8.�΋ǒ��A           �F�����́uresign�v�A���Ԑ؂�́utimeover�v�A�`�[�g���o�p�utimeerror�v�A\r\n"
						   + "					   ����̃^�C�}�[���~�߂�ustopmytimer:[�^�C�}�[�c��b���̐���]�v");
		System.out.println("9.���̑�             :����̐ڑ��؂�uquit�v\n");

		while(true){
			String s = null;
			try {
				s = r.readLine();
				if (s != null){
					out.println(s); // EchoServer�ֈ�s���M
					out.flush();
					if (s.equals("quit")){
						socket.close();
					}
				}
			} catch (IOException e) {
				// TODO �����������ꂽ catch �u���b�N
				e.printStackTrace();
			}
			//oclient.receiveMessage(s);
			System.out.println("\nClientDriver:�e�X�g���b�Z�[�W�u" + s + "�v����M���܂���");
			System.out.println("ClientDriver:�e�X�g������s������A��M�p�e�X�g���b�Z�[�W����͂��Ă�������\n");
		}

	}

}