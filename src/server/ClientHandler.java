package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.io.InputStream;
import java.io.InputStreamReader;

// �������Կͻ��˵����ݷ��͵���(�̳����߳��࣬ʵ��һ���ͻ����ܶ��߳̽��ն���ͻ���)
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