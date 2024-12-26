package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 服务器
 */
public class Server {
    private int port; // 端口号

    // 服务器socket的构造函数
    public Server(int port) {
        this.port = port;
    }

    // 服务器socket启动的函数
    public void build() {

        // 提高至外部，以便能使用finally进行关闭
        ServerSocket serverSocket = null;

        // 由于ServerSocket构造器可能会产生异常，所以需要对异常处理
        try {
            System.out.println("Server preparing");
            long time_begin = System.currentTimeMillis();

            // 创建服务端端口，绑定端口
            serverSocket = new ServerSocket(this.port);
            long time_end = System.currentTimeMillis();
            System.out.println(
                    String.format("Server is ready and waiting for request, cost: %d ms", time_end - time_begin));

            // 服务器端一直监听客户端请求，每次有请求就创建一个新的socket
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // System.out.println("New client connected\n");

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