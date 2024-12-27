package server;

import java.net.Socket;
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

// �������Կͻ��˵����ݷ��͵���(�̳����߳��࣬ʵ��һ���ͻ����ܶ��߳̽��ն���ͻ���)
class ClientHandler extends Thread {
    private Socket clientSocket;
    private BufferedReader br;
    private JTextArea logArea;

    public ClientHandler(Socket clientSocket, JTextArea logArea) {
        this.logArea = logArea;
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

            // ʹ��Server�е�appendLog������ӵ��ı�������
            logArea.append("*****Request URL: " + URL + "*****\n");

            // ��ȡ��̬��Դ�ļ�
            System.out.println(URL);
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

    // ��ȡ��̬��Դ�ļ����൱����һ���ܿ�
    private void responseController(String URL) {
        // ��ȡ��̬��Դ�ļ�������·����/web/index.html
        String filePath = URL.startsWith("/") ? URL.substring(1) : URL;

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

    private void responseStaticResource(String filePath, String fileExtension) throws IOException {
        InputStream is = new FileInputStream(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }

        byte[] fileBytes = baos.toByteArray();

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

    // ��ȡcontent-type����
    private String getContentType(String fileExtension) {
        try (BufferedReader br = new BufferedReader(new FileReader("./server/data/content-type.txt"))) {
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

    // 404 Not Found
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
}