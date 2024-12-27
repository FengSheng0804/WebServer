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
            responseStaticResource(URL);

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
    private void responseStaticResource(String URL) {
        // ��ȡ��̬��Դ�ļ�������·����web/index.html
        String filePath = URL; // ���ڵ��Գ���

        // ͨ����������ȡ�ļ�
        try {
            String fileExtension = filePath.substring(filePath.lastIndexOf(".") + 1);
            // �ж�content-type����
            switch (fileExtension) {
                // �ı����ͣ�
                case "htm":
                case "html":
                case "css":
                case "csv":
                case "txt":
                case "js":
                case "xml":
                case "odt":
                    responseText(filePath, fileExtension);
                    break;
                // ���ı����ͣ�
                case "7z":
                case "avi":
                case "bin":
                case "bmp":
                case "doc":
                case "docx":
                case "exe":
                case "gif":
                case "gz":
                case "ico":
                case "jar":
                case "jpeg":
                case "jpg":
                case "json":
                case "mp3":
                case "mp4":
                case "mpeg":
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
                case "webp":
                case "woff":
                case "woff2":
                case "xls":
                case "xlsx":
                case "zip":
                    responseNotText(filePath, fileExtension);
                    break;
                default:
                    response404();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ��ȡhtml/htm�ļ�����Ҫ�ⲿ�����쳣
    private void responseText(String filePath, String fileClass) throws IOException {
        InputStream is = new FileInputStream(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }

        byte[] fileBytes = baos.toByteArray();

        String fileString;
        switch (fileClass) {
            case "htm":
            case "html":
                fileString = "text/html";
                break;
            case "css":
                fileString = "text/css";
                break;
            case "csv":
                fileString = "text/csv";
                break;
            case "txt":
                fileString = "text/plain";
                break;
            case "js":
                fileString = "text/javascript";
                break;
            default:
                fileString = "text/plain";
                break;
        }

        // �ڽ����ݷ��͸������֮ǰ����Ҫ�����ݴ�����������ʶ�𵽵ı�����ʽ
        // �����У�Э��汾�� ״̬��(200��ʾ����ɹ�) ״ֵ̬(ok��ʾ����ɹ�)
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 200 OK\n")
                .append("Content-Type:text/" + fileClass + ";charset=utf-8\n\n");

        // ͨ����������ļ�д�ص��ͻ���
        OutputStream os = clientSocket.getOutputStream();
        os.write(sb.toString().getBytes());
        os.write(fileBytes);
        os.flush();
        logArea.append("=====Response HTML: " + filePath + " Successfully=====\n");

        // �ر���Դ
        is.close();
        baos.close();
    }

    // ��ȡpng/jpeg/jpg�ļ�����Ҫ�ⲿ�����쳣
    private void responseNotText(String filePath, String fileClass) throws IOException {
        // ��ȡͼƬ�ļ�
        InputStream is = new FileInputStream(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }

        byte[] imageBytes = baos.toByteArray();

        // �ڽ����ݷ��͸������֮ǰ����Ҫ�����ݴ�����������ʶ�𵽵ı�����ʽ
        // �����У�Э��汾�� ״̬��(200��ʾ����ɹ�) ״ֵ̬(ok��ʾ����ɹ�)

        String fileString;
        switch (fileClass) {
            case "7z":
                fileString = "application/x-7z-compressed";
                break;
            case "avi":
                fileString = "video/x-msvideo";
                break;
            case "bin":
                fileString = "application/octet-stream";
                break;
            case "bmp":
                fileString = "image/bmp";
                break;
            case "doc":
                fileString = "application/msword";
                break;
            case "docx":
                fileString = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                break;
            case "exe":
                fileString = "application/x-msdownload";
                break;
            case "gif":
                fileString = "image/gif";
                break;
            case "gz":
                fileString = "application/gzip";
                break;
            case "ico":
                fileString = "image/vnd.microsoft.icon";
                break;
            case "jar":
                fileString = "application/java-archive";
                break;
            case "jpeg":
            case "jpg":
                fileString = "image/jpeg";
                break;
            case "json":
                fileString = "application/json";
                break;
            case "mp3":
                fileString = "audio/mpeg";
                break;
            case "mp4":
                fileString = "video/mp4";
                break;
            case "mpeg":
                fileString = "video/mpeg";
                break;
            case "odt":
                fileString = "application/vnd.oasis.opendocument.text";
                break;
            case "pdf":
                fileString = "application/pdf";
                break;
            case "php":
                fileString = "application/x-httpd-php";
                break;
            case "png":
                fileString = "image/png";
                break;
            case "ppt":
                fileString = "application/vnd.ms-powerpoint";
                break;
            case "pptx":
                fileString = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                break;
            case "rar":
                fileString = "application/vnd.rar";
                break;
            case "sh":
                fileString = "application/x-sh";
                break;
            case "svg":
                fileString = "image/svg+xml";
                break;
            case "tar":
                fileString = "application/x-tar";
                break;
            case "ttf":
                fileString = "font/ttf";
                break;
            case "webp":
                fileString = "image/webp";
                break;
            case "woff":
                fileString = "font/woff";
                break;
            case "woff2":
                fileString = "font/woff2";
                break;
            case "xls":
                fileString = "application/vnd.ms-excel";
                break;
            case "xlsx":
                fileString = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                break;
            case "xml":
                fileString = "application/xml";
                break;
            case "zip":
                fileString = "application/zip";
                break;
            default:
                // δ֪����
                fileString = "application/octet-stream";
                break;
        }

        // �ڽ����ݷ��͸������֮ǰ����Ҫ�����ݴ�����������ʶ�𵽵ı�����ʽ
        PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
        pw.println("HTTP/1.1 200 OK");
        pw.println("Content-Type: " + fileString);
        pw.println("Content-Length: " + imageBytes.length);
        pw.println(); // ���б�ʾͷ������
        pw.flush();

        // ͨ����������ļ�д�ص��ͻ���
        OutputStream os = clientSocket.getOutputStream();
        os.write(imageBytes);
        os.flush();

        logArea.append("=====Response Image: " + filePath + " Successfully=====\n");

        // �ر���Դ
        is.close();
        baos.close();
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