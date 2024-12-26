package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.io.InputStream;
import java.io.InputStreamReader;

// 处理来自客户端的数据发送的类(继承自线程类，实现一个客户端能多线程接收多个客户端)
class ClientHandler extends Thread {
    private Socket clientSocket;
    private BufferedReader br;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            // 通过输入流获取客户端发送的数据
            InputStream is = clientSocket.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            String str = null;
            while ((str = br.readLine()) != null) {
                System.out.println("Client: " + str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 关闭socket
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