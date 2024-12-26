package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

// �������Կͻ��˵����ݷ��͵���(�̳����߳��࣬ʵ��һ���ͻ����ܶ��߳̽��ն���ͻ���)
class ClientHandler extends Thread {
    private Socket clientSocket;
    private BufferedReader br;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            // ͨ����������ȡ�ͻ��˷��͵�����
            InputStream is = clientSocket.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            // �����û�����
            String requestLine = br.readLine(); // �����У�GET /hello HTTP/1.1
            String URL = requestLine.split(" ")[1]; // ��ȡ��������Դ��·��

            // ������Ӧ
            System.out.println("Request URL: " + URL);
            if (URL.endsWith(".html") || URL.endsWith(".htm")) {
                // ��ȡ��̬��Դ�ļ�
                responseStaticResource(URL);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // �ر���Դ
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // �ر�socket
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ��ȡ��̬��Դ�ļ����ظ���HTML���ļ�Ϊ��׺���ļ�
    private void responseStaticResource(String URL) {
        // ��ȡ��̬��Դ�ļ�������·����web/index.html
        String filePath = URL.substring(1); // ȥ��URL�еĵ�һ���ַ�'/'

        // ͨ����������ȡ�ļ�
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            StringBuilder sb = new StringBuilder();
            // �ڽ����ݷ��͸������֮ǰ����Ҫ�����ݴ�����������ʶ�𵽵ı�����ʽ
            // �����У�Э��汾�� ״̬��(200��ʾ����ɹ�) ״ֵ̬(ok��ʾ����ɹ�)
            sb.append("HTTP/1.1 200 OK\n")
                    // ����ͷ���ļ��ĸ�ʽ(��Ϊ���ʵ���html�ļ�������ʹ��text/html) �ַ���(ʹ��utf-8)
                    // ����Ҫ�������'\n'����һ�����������У��ڶ�����֤���ĺ�����֮�������ڵĿ���
                    .append("Content-Type:text/html;charset=utf-8\n\n");
            String temp = "";
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
            }

            // ͨ����������ļ�д�ص��ͻ���
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            // �����ݷ��͸��ͻ���
            pw.println(sb);
            pw.flush();
        } catch (IOException e) {
            // ����ļ�δ�ҵ����򷵻�404 Not Found
            try {
                PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
                pw.println("HTTP/1.1 404 Not Found");
                pw.println("Content-Type:text/html;charset=utf-8\n\n");
                pw.println("<html><body><h1>404 Not Found</h1></body></html>");
                pw.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}