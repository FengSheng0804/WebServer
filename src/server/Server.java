package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 服务器
 */
public class Server {
    public static void main(String[] args) {
        System.out.println("This is Server");

        // 提高至外部，以便能使用finally进行关闭
        ServerSocket serverSocket = null;

        // 由于ServerSocket构造器可能会产生异常，所以需要对异常处理
        try {
            // 创建服务端端口，绑定端口10000
            serverSocket = new ServerSocket(10000);
            System.out.println("Waiting for request");

            // 服务器端一直监听客户端请求，每次有请求就创建一个新的socket
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                // 创建一个新的线程来处理客户端请求
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // ServerSocket是先开后关，clientSocket是先关后开
            // 关闭serverSocket
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