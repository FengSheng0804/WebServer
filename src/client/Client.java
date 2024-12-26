package client;

import java.net.Socket;
import java.io.PrintWriter;

/**
 * �ͻ��ˣ���Ҫ���ڷ���������������ˣ����շ������˵���Ӧ
 */
public class Client {
    public static void build(String IP, int port, String message) {
        System.out.println("This is Client");

        // ������ⲿ���Ա���ʹ��finally���йر�
        Socket clientSocket = null;
        PrintWriter pw = null;

        // ����Socket���������ܻ�����쳣��������Ҫ���쳣����
        try {
            // �����ͻ���Socket��ָ����������ַ�Ͷ˿�
            clientSocket = new Socket("127.0.0.1", 10000);
            {
                System.out.println("Connect Successfully");

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
