package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 服务器
 */
public class Server extends JFrame {
    public JTextArea logArea;
    private int port;
    private JButton startButton;
    private JButton stopButton;
    private ServerSocket serverSocket;
    private boolean isRunning;

    // 带有初始化操作的构造函数
    public Server(int port) {
        this.port = port;
        initializeGUI();
    }

    // 初始化GUI界面
    private void initializeGUI() {
        // 设置窗口标题、大小、关闭方式、布局
        setTitle("Server");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 添加日志区域
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        // 添加按钮区域
        JPanel panel = new JPanel();
        startButton = new JButton("Start Server");
        stopButton = new JButton("Stop Server");
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

    // 添加内容到日志区域
    public void appendLog(String log) {
        logArea.append(log + "\n");
    }

    // 启动服务器
    private void startServer() {
        isRunning = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        logArea.append("Server preparing\n");

        new Thread(new Runnable() {
            // 重写run方法
            @Override
            public void run() {
                try {
                    long timeBegin = System.currentTimeMillis();
                    serverSocket = new ServerSocket(port);
                    long timeEnd = System.currentTimeMillis();
                    logArea.append(String.format("Server is ready and waiting for request, cost: %d ms\n",
                            timeEnd - timeBegin));

                    // 循环监听客户端连接
                    while (isRunning) {
                        Socket clientSocket = serverSocket.accept();
                        logArea.append("New client connected\n");
                        new ClientHandler(clientSocket, logArea).start();
                    }
                } catch (IOException e) {
                    logArea.append("Error: " + e.getMessage() + "\n");
                } finally {
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

    // 停止服务器
    private void stopServer() {
        isRunning = false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        logArea.append("Server stopped\n");

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logArea.append("Error closing server socket: " + e.getMessage() + "\n");
            }
        }
    }
}