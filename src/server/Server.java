package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * ������
 */
public class Server {
    private int port; // �˿ں�

    // ������socket�Ĺ��캯��
    public Server(int port) {
        this.port = port;
    }

    // ������socket�����ĺ���
    public void build() {

        // ������ⲿ���Ա���ʹ��finally���йر�
        ServerSocket serverSocket = null;

        // ����ServerSocket���������ܻ�����쳣��������Ҫ���쳣����
        try {
            System.out.println("Server preparing");
            long time_begin = System.currentTimeMillis();

            // ��������˶˿ڣ��󶨶˿�
            serverSocket = new ServerSocket(this.port);
            long time_end = System.currentTimeMillis();
            System.out.println(
                    String.format("Server is ready and waiting for request, cost: %d ms", time_end - time_begin));

            // ��������һֱ�����ͻ�������ÿ��������ʹ���һ���µ�socket
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // System.out.println("New client connected\n");

                // ����һ���µ��߳�������ͻ�������
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // ServerSocket���ȿ���أ�clientSocket���ȹغ�
            // �ر�serverSocket
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}