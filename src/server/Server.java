package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ������
 */
public class Server extends JFrame {
    public JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private int port;

    /**
     * ���г�ʼ�������Ĺ��캯����
     */
    public Server(int port) {
        this.port = port;
        initializeGUI();
    }

    /**
     * ��ʼ��ͼ���û����棨GUI����
     */
    private void initializeGUI() {
        // ���ô��ڱ��⡢��С���رշ�ʽ������
        setTitle("Server");
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
        startButton = new JButton("Start Server");
        stopButton = new JButton("Stop Server");
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
     * ����־��Ϣ׷�ӵ���־����
     *
     * @param log Ҫ׷�ӵ���־��Ϣ
     */
    public void appendLog(String log) {
        logArea.append(log + "\n");
    }

    /**
     * �����������ķ�����
     */
    private void startServer() {
        isRunning = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        logArea.append("Server preparing\n");

        new Thread(new Runnable() {
            // ��дrun����
            @Override
            public void run() {
                try {
                    // ����serverSocket
                    long timeBegin = System.currentTimeMillis();
                    serverSocket = new ServerSocket(port);
                    long timeEnd = System.currentTimeMillis();
                    logArea.append(String.format("Server is ready and waiting for request, cost: %d ms\n",
                            timeEnd - timeBegin));

                    // ѭ�������ͻ�������
                    while (isRunning) {
                        Socket clientSocket = serverSocket.accept();
                        new TaskHandler(clientSocket, logArea).start();
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

    /**
     * ֹͣ�������ķ�����
     * �� isRunning ��־����Ϊ false������ startButton ��ť������ stopButton ��ť������ logArea ��׷��
     * "Server stopped" ��Ϣ��
     * ��� serverSocket ��Ϊ�գ����Թر� serverSocket��
     * ����ڹر� serverSocket ʱ���� IOException������ logArea ��׷�Ӵ�����Ϣ��
     */
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