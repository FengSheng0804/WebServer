package server.proxyServer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProxyServer extends JFrame {
    // 日志区域，用于显示代理服务器的日志信息
    public static JTextArea logArea;
    // 启动按钮，用于启动代理服务器
    private JButton startButton;
    // 停止按钮，用于停止代理服务器
    private JButton stopButton;
    // 服务器套接字，用于监听客户端连接
    private ServerSocket serverSocket;
    // 代理服务器的运行状态
    private boolean isRunning;

    // 白名单集合，存储允许访问的IP地址
    private static Set<String> whiteList = new HashSet<>();
    // 黑名单集合，存储禁止访问的IP地址
    private static Set<String> blackList = new HashSet<>();
    // 代理服务器监听的端口
    private int port;
    // 目标服务器的主机名
    private String targetHost;
    // 目标服务器的端口
    private int targetPort;
    // 添加缓存机制
    private ProxyServerCache cache;

    static {
        // 初始化白名单，添加一些示例IP地址

        // IPv4的本地回环地址
        whiteList.add("127.0.0.1");
        // IPv6的本地回环地址
        whiteList.add("0:0:0:0:0:0:0:1");
        // 本地主机名
        whiteList.add("localhost");
        // 本机局域网地址
        whiteList.add("192.168.96.238");
        // 手机局域网地址
        whiteList.add("192.168.96.148");
        // ipad局域网地址
        whiteList.add("192.168.96.211");

        // 初始化黑名单
    }

    public ProxyServer(int port, String targetHost, int targetPort) {
        this.port = port;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        initializeGUI();
        // 初始化缓存
        cache = new ProxyServerCache();
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
        // 设置代理服务器的运行状态为 true
        isRunning = true;
        // 禁用启动按钮
        startButton.setEnabled(false);
        // 启用停止按钮
        stopButton.setEnabled(true);
        // 在日志区域中添加启动准备信息
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
                            logArea.append("White: " + clientSocket.getInetAddress().getHostAddress() + "\n");
                            // 创建一个新的线程来处理代理服务器的任务
                            new ProxyServerTaskHandler(clientSocket, targetHost, targetPort, cache).start();
                        } else {
                            // 如果客户端不在白名单中，打印日志并关闭客户端套接字
                            logArea.append(
                                    "Not in white list: " + clientSocket.getInetAddress().getHostAddress() + "\n");
                            clientSocket.close();
                            continue;
                        }
                    }
                } catch (IOException e) {
                    logArea.append("Error: " + e.getMessage() + "\n");
                } finally {
                    // 关闭服务器套接字
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
        // 设置代理服务器的运行状态为 false
        isRunning = false;
        // 启用启动按钮
        startButton.setEnabled(true);
        // 禁用停止按钮
        stopButton.setEnabled(false);
        // 在日志区域中添加停止信息
        logArea.append("Proxy server stopped\n");
        // 关闭服务器套接字
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

    // 内部类ProxyServerTaskHandler：对代理服务器的任务进行处理
    private static class ProxyServerTaskHandler extends Thread {
        // 客户端套接字
        private Socket clientSocket;
        // 目标服务器的主机名
        private String targetHost;
        // 目标服务器的端口
        private int targetPort;
        // 添加缓存机制
        private ProxyServerCache cache;

        // 构造函数
        public ProxyServerTaskHandler(Socket clientSocket, String targetHost, int targetPort, ProxyServerCache cache) {
            this.clientSocket = clientSocket;
            this.targetHost = targetHost;
            this.targetPort = targetPort;
            this.cache = cache;
        }

        @Override
        public void run() {
            try (Socket targetSocket = new Socket(targetHost, targetPort)) {
                // 用于读取客户端发送的数据。
                InputStream clientInput = clientSocket.getInputStream();
                // 用于向客户端发送数据。
                OutputStream clientOutput = clientSocket.getOutputStream();
                // 用于读取目标服务器发送的数据。
                InputStream targetInput = targetSocket.getInputStream();
                // 用于向目标服务器发送数据。
                OutputStream targetOutput = targetSocket.getOutputStream();

                // 读取客户端发送的数据并将其再写回客户端
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientInput));
                StringBuilder requestBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    requestBuilder.append(line).append("\n");
                }
                String request = requestBuilder.toString();

                // 获取请求头
                String header = null;
                String requestURL = null;
                if (request != null) {
                    header = request.split("\n")[0].split(" ")[1];
                    requestURL = "http://" + targetHost + ":" + targetPort + header;

                    // 将请求数据保存到一个可以使用InputStream读取数据的变量中
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // 转成request之后'\n'消失了，因此需要加上'\n'，否则在解析报文的会出现问题。
                    ByteArrayInputStream copiedInputStream = new ByteArrayInputStream((request + '\n').getBytes());
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

                    // 尝试从缓存获取响应
                    byte[] cachedResponse = cache.get(requestURL);
                    if (cachedResponse != null) {
                        // 如果缓存中存在响应，则直接返回缓存的响应内容
                        InputStream cachedInputStream = new ByteArrayInputStream(cachedResponse);
                        // 将缓存的响应内容转发到客户端
                        forwardData(cachedInputStream, clientOutput);
                        logArea.append("Cache hit for URL: " + requestURL + "\n");
                    } else {
                        if (cache.isFull()) {
                            // 如果缓存已满，则移除最早的缓存项
                            cache.removeLeastRecentlyUsed();
                            logArea.append("Cache is full, removing least recently used item\n");
                        }
                        // 转发到目标服务器
                        forwardData(copiedInputStream, targetOutput);
                        // 读取目标服务器的响应内容
                        ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = targetInput.read(buffer)) != -1) {
                            responseBuffer.write(buffer, 0, length);
                            clientOutput.write(buffer, 0, length);
                        }
                        byte[] responseContent = responseBuffer.toByteArray();

                        // 缓存服务器的响应内容
                        cache.put(requestURL, responseContent);
                        logArea.append("Cache miss for URL: " + requestURL + "\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 关闭客户端套接字
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 将数据从输入流转发到输出流 中。
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
            logArea.append("Forwarding data successfully\n");
        }
    }
}