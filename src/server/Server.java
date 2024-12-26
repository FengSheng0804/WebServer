package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * ������
 */
public class Server {
    public static void main(String[] args) {
        System.out.println("This is Server");

        // ������ⲿ���Ա���ʹ��finally���йر�
        ServerSocket serverSocket = null;

        // ����ServerSocket���������ܻ�����쳣��������Ҫ���쳣����
        try {
            // ��������˶˿ڣ��󶨶˿�10000
            serverSocket = new ServerSocket(10000);
            System.out.println("Waiting for request");

            // ��������һֱ�����ͻ�������ÿ��������ʹ���һ���µ�socket
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

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

class ClientHandler extends Thread {
    private Socket clientSocket;
    private BufferedReader br;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            // ͨ����������ȡ�ͻ��˷��͵�����
            InputStream is = clientSocket.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            String str = null;
            while ((str = br.readLine()) != null) {
                System.out.println("Client: " + str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // �ر���Դ
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // �ر�socket
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}