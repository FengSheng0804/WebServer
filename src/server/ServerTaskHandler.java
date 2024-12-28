package server;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTextArea;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.FileReader;

/**
 * 
 * TaskHandler��̳���Thread�࣬��������������ĵ����ͻ������ӡ�
 * ����ȡ�ͻ������󣬴������󣬲����ʵ�����Ӧ���ͻؿͻ��ˡ�
 * ������¼�������Ӧ��Ϣ��
 */
class ServerTaskHandler extends Thread {
    private Socket clientSocket;
    private BufferedReader br;
    private JTextArea logArea;

    public ServerTaskHandler(Socket clientSocket, JTextArea logArea) {
        this.logArea = logArea;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            InputStream is = clientSocket.getInputStream();
            // ͨ����������ȡ�ͻ��˷��͵�����
            br = new BufferedReader(new InputStreamReader(is));
            // ����û���������Ϣ��һ�������ַ�����Ϊ�˷�ֹ���ֶ��߳�����Ĳ��������ԣ�����¼��һ��line������
            StringBuilder requestBuilder = new StringBuilder();
            String line;
            String requestLine = null;
            while ((line = br.readLine()) != null && !line.isEmpty()) {
                requestBuilder.append(line).append("\n");
                if (requestLine == null) {
                    requestLine = line;
                }
            }
            String requestString = requestBuilder.toString();

            // ���¾�ΪGET����Ĵ���
            // ��������ͷ���������ʹ��
            Map<String, String> headers = processHeaders(requestString);
            // ��ȡ��������Դ��·��
            // System.out.println(requestLine);
            String URL = requestLine.split(" ")[1];

            // ʹ��Server�е�appendLog������ӵ��ı�������
            logArea.append("*****Request URL: " + URL + "*****\n");

            // ��ȡ��̬��Դ�ļ�
            responseController(URL);

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

    /**
     * ����ͻ������󲢷�����Ӧ�ľ�̬��Դ�ļ����൱����һ���ܿء�
     *
     * @param URL �ͻ��������URL·��
     * 
     *            �÷���ͨ������URL·����ȷ��������ļ����������ļ���չ���ж�Content-Type���͡�
     *            ����ļ�����������֧�֣������responseStaticResource���������ļ����ݡ�
     *            ����ļ������ڻ��ȡ�ļ�ʱ�������������response404��������404����
     *
     * @throws IOException �����ȡ�ļ�ʱ����IO����
     */
    private void responseController(String URL) {
        // ��ȡ��̬��Դ�ļ�������·����/web/index.html
        // String filePath = URL.substring(1); // ������
        String filePath = "src/" + URL.substring(1); // ������

        // ͨ����������ȡ�ļ�
        try {
            String fileExtension = filePath.substring(filePath.lastIndexOf(".") + 1);
            // �ж�content-type����
            switch (fileExtension) {
                case "7z":
                case "avi":
                case "bin":
                case "bmp":
                case "css":
                case "csv":
                case "doc":
                case "docx":
                case "exe":
                case "gif":
                case "gz":
                case "htm":
                case "html":
                case "ico":
                case "jar":
                case "jpeg":
                case "jpg":
                case "js":
                case "json":
                case "mp3":
                case "mp4":
                case "mpeg":
                case "odt":
                case "pdf":
                case "php":
                case "png":
                case "ppt":
                case "pptx":
                case "rar":
                case "sh":
                case "svg":
                case "tar":
                case "ttf":
                case "txt":
                case "webp":
                case "woff":
                case "woff2":
                case "xls":
                case "xlsx":
                case "xml":
                case "zip":
                    responseStaticResource(filePath, fileExtension);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            response404();
        }
    }

    /**
     * ��Ӧ��̬��Դ���󣬽�ָ���ļ�·���ľ�̬��Դ���͸��ͻ��ˡ�
     *
     * @param filePath      ��̬��Դ�ļ���·��
     * @param fileExtension ��̬��Դ�ļ�����չ��������ȷ����������
     * @throws IOException ����ڶ�ȡ�ļ���д��ͻ���ʱ���� I/O ����
     */
    private void responseStaticResource(String filePath, String fileExtension) throws IOException {
        // ��ʼ��
        InputStream is = new FileInputStream(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }

        byte[] fileBytes = baos.toByteArray();

        // ��ȡcontent-type����
        String content_type = getContentType(fileExtension);

        if (content_type.startsWith("text")) {
            // �ڽ����ݷ��͸������֮ǰ����Ҫ�����ݴ�����������ʶ�𵽵ı�����ʽ
            // �����У�Э��汾�� ״̬��(200��ʾ����ɹ�) ״ֵ̬(ok��ʾ����ɹ�)
            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1 200 OK\n")
                    .append("Content-Type:text/" + fileExtension + ";charset=utf-8\n\n");

            // ͨ����������ļ�д�ص��ͻ���
            OutputStream os = clientSocket.getOutputStream();
            os.write(sb.toString().getBytes());
            os.write(fileBytes);
            os.flush();
        } else {
            // �ڽ����ݷ��͸������֮ǰ����Ҫ�����ݴ�����������ʶ�𵽵ı�����ʽ
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            pw.println("HTTP/1.1 200 OK");
            pw.println("Content-Type: " + content_type);
            pw.println("Content-Length: " + fileBytes.length);
            pw.println(); // ���б�ʾͷ������
            pw.flush();

            // ͨ����������ļ�д�ص��ͻ���
            OutputStream os = clientSocket.getOutputStream();
            os.write(fileBytes);
            os.flush();
        }
        logArea.append("=====Response: " + content_type + ": " + filePath + " Successfully=====\n");

        // �ر���Դ
        is.close();
        baos.close();
    }

    /**
     * �����ļ���չ����ȡ��Ӧ��Content-Type���͡�
     * �÷�����content-type.txt�ļ��ж�ȡӳ���ϵ��������ƥ���Content-Type��
     * ���δ�ҵ�ƥ�����չ�����򷵻�Ĭ�ϵ�Content-Type "application/octet-stream"��
     *
     * @param fileExtension �ļ���չ��
     * @return ��Ӧ��Content-Type����
     */
    private String getContentType(String fileExtension) {
        // String fileName = "./data/content-type.txt"; // ������
        String fileName = "./src/data/content-type.txt"; // ������

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains(fileExtension)) {
                    return line.split(":")[1].trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "application/octet-stream"; // Ĭ�ϵ�content_type���������
    }

    /**
     * ����404 Not Found��Ӧ���ͻ��ˡ�
     * �˷�������ͻ��˷���һ��HTTP 404״̬���һ���򵥵�HTMLҳ�棬
     * ҳ������Ϊ��404 Not Found����
     * 
     * ʹ��PrintWriter��ͻ��������Ӧͷ����Ӧ���ݡ�
     * �������������з���IOException���Ჶ�񲢴�ӡ��ջ���١�
     */
    private void response404() {
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            pw.println("HTTP/1.1 404 Not Found");
            pw.println("Content-Type:text/html;charset=utf-8\n\n");
            pw.println("<html><body><h1 style='text-align:center'>404 Not Found</h1></body></html>");
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ��������ͷ
    /**
     * ����HTTP�����ַ����е�ͷ����Ϣ��
     *
     * @param requestString ����ͷ����Ϣ��HTTP�����ַ���
     * @return һ������ͷ��������Ϊ�������Ӧֵ��ӳ��
     */
    public Map<String, String> processHeaders(String requestString) {
        Map<String, String> headers = new HashMap<>();

        String[] lines = requestString.split("\r\n");

        for (String line : lines) {
            String[] parts = line.split(": ", 2);
            if (parts.length == 2) {
                headers.put(parts[0], parts[1]);
            }
        }
        return headers;
    }
}