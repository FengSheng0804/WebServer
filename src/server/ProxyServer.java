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

    private static Set<String> whiteList = new HashSet<>(); // ���������ϣ��洢������ʵ�IP��ַ
    private static Set<String> blackList = new HashSet<>(); // ���������ϣ��洢��ֹ���ʵ�IP��ַ
    private int port; // ��������������Ķ˿�
    private String targetHost; // Ŀ���������������
    private int targetPort; // Ŀ��������Ķ˿�

    static {
        // ��ʼ�������������һЩʾ��IP��ַ
        whiteList.add("127.0.0.1"); // IPv4�ı��ػػ���ַ
        whiteList.add("0:0:0:0:0:0:0:1"); // IPv6�ı��ػػ���ַ
        whiteList.add("localhost"); // ����������

        // ��ʼ�������������һЩʾ��IP��ַ
        blackList.add("192.168.1.200");
    }

    public ProxyServer(int port, String targetHost, int targetPort) {
        this.port = port;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        initializeGUI();
    }

    /**
     * ��ʼ��ͼ���û����棨GUI����
     */
    private void initializeGUI() {
        // ���ô��ڱ��⡢��С���رշ�ʽ������
        setTitle("Proxy Server");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // �����־����
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Serif", Font.PLAIN, 20));
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        // ��Ӱ�ť����
        JPanel panel = new JPanel();
        startButton = new JButton("Start Proxy Server");
        stopButton = new JButton("Stop Proxy Server");
        stopButton.setEnabled(false);

        // ��Ӱ�ť�����
        panel.add(startButton);
        panel.add(stopButton);
        add(panel, BorderLayout.SOUTH);

        // ��Ӱ�ť�¼�����startButton
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });

        // ��Ӱ�ť�¼�����stopButton
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopServer();
            }
        });
    }

    /**
     * ���������������
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
                    // �������������
                    serverSocket = new ServerSocket(port);
                    logArea.append("Proxy server started on port " + port + "\n");

                    // ѭ������
                    while (isRunning) {
                        Socket clientSocket = serverSocket.accept();
                        if (isWhiteListed(clientSocket.getInetAddress().getHostAddress())) {
                            // ����ͻ����ڰ������У���ӡ��־������һ���µ��߳���������������������
                            logArea.append("Client " + clientSocket.getInetAddress().getHostAddress()
                                    + " is in the white list\n");
                            // ����һ���µ��߳���������������������
                            new ProxyServerTaskHandler(clientSocket, targetHost, targetPort).start();
                        } else if (isBlackListed(clientSocket.getInetAddress().getHostAddress())) {
                            // ����ͻ����ں������У���ӡ��־���رտͻ����׽���
                            logArea.append("Client " + clientSocket.getInetAddress().getHostAddress()
                                    + " is in the black list\n");
                            clientSocket.close();
                            continue;
                        }
                    }
                } catch (IOException e) {
                    logArea.append("Error: " + e.getMessage() + "\n");
                } finally {
                    // �ر�serverSoket��
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
     * ֹͣ����������ķ�����
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

    // �Ƿ��ڰ�������
    private boolean isWhiteListed(String clientIP) {
        return whiteList.contains(clientIP);
    }

    // �Ƿ��ں�������
    private boolean isBlackListed(String clientIP) {
        return blackList.contains(clientIP);
    }

    // �Դ����������������д���
    private static class ProxyServerTaskHandler extends Thread {
        private Socket clientSocket; // �ͻ����׽���
        private String targetHost; // Ŀ���������������
        private int targetPort; // Ŀ��������Ķ˿�

        public ProxyServerTaskHandler(Socket clientSocket, String targetHost, int targetPort) {
            this.clientSocket = clientSocket;
            this.targetHost = targetHost;
            this.targetPort = targetPort;
        }

        @Override
        public void run() {
            try (Socket targetSocket = new Socket(targetHost, targetPort)) {
                // ��ȡ�ͻ����׽��ֵ������������ڶ�ȡ�ͻ��˷��͵����ݡ�
                InputStream clientInput = clientSocket.getInputStream();
                // ��ȡ�ͻ����׽��ֵ��������������ͻ��˷������ݡ�
                OutputStream clientOutput = clientSocket.getOutputStream();
                // ��ȡĿ��������׽��ֵ������������ڶ�ȡĿ����������͵����ݡ�
                InputStream targetInput = targetSocket.getInputStream();
                // ��ȡĿ��������׽��ֵ��������������Ŀ��������������ݡ�
                OutputStream targetOutput = targetSocket.getOutputStream();

                // ת���ͻ�������Ŀ�������
                new Thread(() -> forwardData(clientInput, targetOutput)).start();
                // ת��Ŀ���������Ӧ���ͻ���
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
         * ��������������ת������������������������ݡ�
         *
         * @param input  �����������ж�ȡ����
         * @param output �������������д������
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