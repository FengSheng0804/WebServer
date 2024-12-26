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
        System.out.println("This is Server");

        // ������ⲿ���Ա���ʹ��finally���йر�
        ServerSocket serverSocket = null;

        // ����ServerSocket���������ܻ�����쳣��������Ҫ���쳣����
        try {
            // ��������˶˿ڣ��󶨶˿�
            serverSocket = new ServerSocket(this.port);
            System.out.println("Waiting for request");

            // ��������һֱ�����ͻ�������ÿ��������ʹ���һ���µ�socket
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected\n");

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