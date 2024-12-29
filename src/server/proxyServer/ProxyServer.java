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
    // ��־����������ʾ�������������־��Ϣ
    public static JTextArea logArea;
    // ������ť�������������������
    private JButton startButton;
    // ֹͣ��ť������ֹͣ���������
    private JButton stopButton;
    // �������׽��֣����ڼ����ͻ�������
    private ServerSocket serverSocket;
    // ���������������״̬
    private boolean isRunning;

    // ���������ϣ��洢������ʵ�IP��ַ
    private static Set<String> whiteList = new HashSet<>();
    // ���������ϣ��洢��ֹ���ʵ�IP��ַ
    private static Set<String> blackList = new HashSet<>();
    // ��������������Ķ˿�
    private int port;
    // Ŀ���������������
    private String targetHost;
    // Ŀ��������Ķ˿�
    private int targetPort;
    // ��ӻ������
    private ProxyServerCache cache;

    static {
        // ��ʼ�������������һЩʾ��IP��ַ

        // IPv4�ı��ػػ���ַ
        whiteList.add("127.0.0.1");
        // IPv6�ı��ػػ���ַ
        whiteList.add("0:0:0:0:0:0:0:1");
        // ����������
        whiteList.add("localhost");
        // ������������ַ
        whiteList.add("192.168.96.238");
        // �ֻ���������ַ
        whiteList.add("192.168.96.148");
        // ipad��������ַ
        whiteList.add("192.168.96.211");

        // ��ʼ��������
    }

    public ProxyServer(int port, String targetHost, int targetPort) {
        this.port = port;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        initializeGUI();
        // ��ʼ������
        cache = new ProxyServerCache();
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
        // ���ô��������������״̬Ϊ true
        isRunning = true;
        // ����������ť
        startButton.setEnabled(false);
        // ����ֹͣ��ť
        stopButton.setEnabled(true);
        // ����־�������������׼����Ϣ
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
                            logArea.append("White: " + clientSocket.getInetAddress().getHostAddress() + "\n");
                            // ����һ���µ��߳���������������������
                            new ProxyServerTaskHandler(clientSocket, targetHost, targetPort, cache).start();
                        } else {
                            // ����ͻ��˲��ڰ������У���ӡ��־���رտͻ����׽���
                            logArea.append(
                                    "Not in white list: " + clientSocket.getInetAddress().getHostAddress() + "\n");
                            clientSocket.close();
                            continue;
                        }
                    }
                } catch (IOException e) {
                    logArea.append("Error: " + e.getMessage() + "\n");
                } finally {
                    // �رշ������׽���
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
        // ���ô��������������״̬Ϊ false
        isRunning = false;
        // ����������ť
        startButton.setEnabled(true);
        // ����ֹͣ��ť
        stopButton.setEnabled(false);
        // ����־���������ֹͣ��Ϣ
        logArea.append("Proxy server stopped\n");
        // �رշ������׽���
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

    // �ڲ���ProxyServerTaskHandler���Դ����������������д���
    private static class ProxyServerTaskHandler extends Thread {
        // �ͻ����׽���
        private Socket clientSocket;
        // Ŀ���������������
        private String targetHost;
        // Ŀ��������Ķ˿�
        private int targetPort;
        // ��ӻ������
        private ProxyServerCache cache;

        // ���캯��
        public ProxyServerTaskHandler(Socket clientSocket, String targetHost, int targetPort, ProxyServerCache cache) {
            this.clientSocket = clientSocket;
            this.targetHost = targetHost;
            this.targetPort = targetPort;
            this.cache = cache;
        }

        @Override
        public void run() {
            try (Socket targetSocket = new Socket(targetHost, targetPort)) {
                // ���ڶ�ȡ�ͻ��˷��͵����ݡ�
                InputStream clientInput = clientSocket.getInputStream();
                // ������ͻ��˷������ݡ�
                OutputStream clientOutput = clientSocket.getOutputStream();
                // ���ڶ�ȡĿ����������͵����ݡ�
                InputStream targetInput = targetSocket.getInputStream();
                // ������Ŀ��������������ݡ�
                OutputStream targetOutput = targetSocket.getOutputStream();

                // ��ȡ�ͻ��˷��͵����ݲ�������д�ؿͻ���
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientInput));
                StringBuilder requestBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    requestBuilder.append(line).append("\n");
                }
                String request = requestBuilder.toString();

                // ��ȡ����ͷ
                String header = null;
                String requestURL = null;
                if (request != null) {
                    header = request.split("\n")[0].split(" ")[1];
                    requestURL = "http://" + targetHost + ":" + targetPort + header;

                    // ���������ݱ��浽һ������ʹ��InputStream��ȡ���ݵı�����
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // ת��request֮��'\n'��ʧ�ˣ������Ҫ����'\n'�������ڽ������ĵĻ�������⡣
                    ByteArrayInputStream copiedInputStream = new ByteArrayInputStream((request + '\n').getBytes());
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

                    // ���Դӻ����ȡ��Ӧ
                    byte[] cachedResponse = cache.get(requestURL);
                    if (cachedResponse != null) {
                        // ��������д�����Ӧ����ֱ�ӷ��ػ������Ӧ����
                        InputStream cachedInputStream = new ByteArrayInputStream(cachedResponse);
                        // ���������Ӧ����ת�����ͻ���
                        forwardData(cachedInputStream, clientOutput);
                        logArea.append("Cache hit for URL: " + requestURL + "\n");
                    } else {
                        if (cache.isFull()) {
                            // ����������������Ƴ�����Ļ�����
                            cache.removeLeastRecentlyUsed();
                            logArea.append("Cache is full, removing least recently used item\n");
                        }
                        // ת����Ŀ�������
                        forwardData(copiedInputStream, targetOutput);
                        // ��ȡĿ�����������Ӧ����
                        ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = targetInput.read(buffer)) != -1) {
                            responseBuffer.write(buffer, 0, length);
                            clientOutput.write(buffer, 0, length);
                        }
                        byte[] responseContent = responseBuffer.toByteArray();

                        // �������������Ӧ����
                        cache.put(requestURL, responseContent);
                        logArea.append("Cache miss for URL: " + requestURL + "\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // �رտͻ����׽���
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * �����ݴ�������ת��������� �С�
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
            logArea.append("Forwarding data successfully\n");
        }
    }
}