package utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer {
    private int port; // 代理服务器监听的端口
    private String targetHost; // 目标服务器的主机名
    private int targetPort; // 目标服务器的端口

    public ProxyServer(int port, String targetHost, int targetPort) {
        this.port = port;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
    }

    /**
     * 启动代理服务器。
     * 
     * @throws IOException 如果在创建服务器套接字或接受客户端连接时发生 I/O 错误。
     */
    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Proxy server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ProxyTask(clientSocket, targetHost, targetPort)).start();
            }
        }
    }

    private static class ProxyTask implements Runnable {
        private Socket clientSocket; // 客户端套接字
        private String targetHost; // 目标服务器的主机名
        private int targetPort; // 目标服务器的端口

        public ProxyTask(Socket clientSocket, String targetHost, int targetPort) {
            this.clientSocket = clientSocket;
            this.targetHost = targetHost;
            this.targetPort = targetPort;
        }

        @Override
        public void run() {
            try (Socket targetSocket = new Socket(targetHost, targetPort)) {
                // 获取客户端套接字的输入流，用于读取客户端发送的数据。
                InputStream clientInput = clientSocket.getInputStream();
                // 获取客户端套接字的输出流，用于向客户端发送数据。
                OutputStream clientOutput = clientSocket.getOutputStream();
                // 获取目标服务器套接字的输入流，用于读取目标服务器发送的数据。
                InputStream targetInput = targetSocket.getInputStream();
                // 获取目标服务器套接字的输出流，用于向目标服务器发送数据。
                OutputStream targetOutput = targetSocket.getOutputStream();

                // 转发客户端请求到目标服务器
                new Thread(() -> forwardData(clientInput, targetOutput)).start();
                // 转发目标服务器响应到客户端
                forwardData(targetInput, clientOutput);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 将输入流的数据转发到输出流，仅用来传递数据。
         *
         * @param input  输入流，从中读取数据
         * @param output 输出流，将数据写入其中
         */
        private void forwardData(InputStream input, OutputStream output) {
            byte[] buffer = new byte[1024];
            int length;
            try {
                while ((length = input.read(buffer)) != -1) {
                    output.write(buffer, 0, length);
                    output.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
