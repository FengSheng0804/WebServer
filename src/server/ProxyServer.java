package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProxyServer extends JFrame {
    public static JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private ServerSocket serverSocket;
    private boolean isRunning;

    private static Set<String> whiteList = new HashSet<>(); // 白名单集合，存储允许访问的IP地址
    private static Set<String> blackList = new HashSet<>(); // 黑名单集合，存储禁止访问的IP地址
    private int port; // 代理服务器监听的端口
    private String targetHost; // 目标服务器的主机名
    private int targetPort; // 目标服务器的端口

    static {
        // 初始化白名单，添加一些示例IP地址
        whiteList.add("127.0.0.1"); // IPv4的本地回环地址
        whiteList.add("0:0:0:0:0:0:0:1"); // IPv6的本地回环地址
        whiteList.add("localhost"); // 本地主机名

        // 初始化黑名单，添加一些示例IP地址
        blackList.add("192.168.1.200");
    }

    public ProxyServer(int port, String targetHost, int targetPort) {
        this.port = port;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        initializeGUI();
    }

    /**
     * 初始化图形用户界面（GUI）。
     */
    private void initializeGUI() {
        // 设置窗口标题、大小、关闭方式、布局
        setTitle("Proxy Server");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 添加日志区域
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Serif", Font.PLAIN, 20));
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        // 添加按钮区域
        JPanel panel = new JPanel();
        startButton = new JButton("Start Proxy Server");
        stopButton = new JButton("Stop Proxy Server");
        stopButton.setEnabled(false);

        // 添加按钮到面板
        panel.add(startButton);
        panel.add(stopButton);
        add(panel, BorderLayout.SOUTH);

        // 添加按钮事件监听startButton
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });

        // 添加按钮事件监听stopButton
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopServer();
            }
        });
    }

    /**
     * 启动代理服务器。
     */
    public void startServer() {
        isRunning = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        logArea.append("Proxy server preparing\n");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 启动代理服务器
                    serverSocket = new ServerSocket(port);
                    logArea.append("Proxy server started on port " + port + "\n");

                    // 循环监听
                    while (isRunning) {
                        Socket clientSocket = serverSocket.accept();
                        if (isWhiteListed(clientSocket.getInetAddress().getHostAddress())) {
                            // 如果客户端在白名单中，打印日志并创建一个新的线程来处理代理服务器的任务
                            logArea.append("Client " + clientSocket.getInetAddress().getHostAddress()
                                    + " is in the white list\n");
                            // 创建一个新的线程来处理代理服务器的任务
                            new ProxyServerTaskHandler(clientSocket, targetHost, targetPort).start();
                        } else if (isBlackListed(clientSocket.getInetAddress().getHostAddress())) {
                            // 如果客户端在黑名单中，打印日志并关闭客户端套接字
                            logArea.append("Client " + clientSocket.getInetAddress().getHostAddress()
                                    + " is in the black list\n");
                            clientSocket.close();
                            continue;
                        }
                    }
                } catch (IOException e) {
                    logArea.append("Error: " + e.getMessage() + "\n");
                } finally {
                    // 关闭serverSoket和
                    if (serverSocket != null) {
                        try {
                            serverSocket.close();
                        } catch (IOException e) {
                            logArea.append("Error closing server socket: " + e.getMessage() + "\n");
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * 停止代理服务器的方法。
     */
    public void stopServer() {
        isRunning = false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        logArea.append("Proxy server stopped\n");

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logArea.append("Error closing server socket: " + e.getMessage() + "\n");
            }
        }
    }

    // 是否在白名单中
    private boolean isWhiteListed(String clientIP) {
        return whiteList.contains(clientIP);
    }

    // 是否在黑名单中
    private boolean isBlackListed(String clientIP) {
        return blackList.contains(clientIP);
    }

    // 对代理服务器的任务进行处理
    private static class ProxyServerTaskHandler extends Thread {
        private Socket clientSocket; // 客户端套接字
        private String targetHost; // 目标服务器的主机名
        private int targetPort; // 目标服务器的端口

        public ProxyServerTaskHandler(Socket clientSocket, String targetHost, int targetPort) {
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
                logArea.append("Forwarding data successfully\n");
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