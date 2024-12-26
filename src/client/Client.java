package client;

import java.net.Socket;
import java.io.PrintWriter;

/**
 * �ͻ��ˣ���Ҫ���ڷ���������������ˣ����շ������˵���Ӧ
 */
public class Client {
    private String IP = ""; // IP��ַ
    private int port; // �˿ں�

    public Client(String IP, int port) {
        this.port = port;
        this.IP = IP;
    }

    // �����ͻ��˵ĺ���
    public void build(String message) {

        // ������ⲿ���Ա���ʹ��finally���йر�
        Socket clientSocket = null;
        PrintWriter pw = null;

        // ����Socket���������ܻ�����쳣��������Ҫ���쳣����
        try {
            // �����ͻ���Socket��ָ����������ַ�Ͷ˿�
            clientSocket = new Socket(this.IP, this.port);
            {
                // ͨ���������������˷�������
                // 1. ��ȡ�����
                pw = new PrintWriter(clientSocket.getOutputStream());
                // 2. ��������˷�������
                pw.write(message);
                // ����д�������Ӧ�ñ����ڻ������ڣ�ֻ�е�ִ��flush��ʱ��ŻὫ�������ڵ����������
                // ���������رյ�ʱ��Ҳ��ˢ�»������������ֽ����������������ᱣ���ڻ������ڣ�
                pw.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // �ر������
            if (pw != null) {
                pw.close();
            }
            // �ر�clientSocket
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
