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

// 处理来自客户端的数据发送的类(继承自线程类，实现一个客户端能多线程接收多个客户端)
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
            // 通过输入流获取客户端发送的数据
            InputStream is = clientSocket.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            // 处理用户请求
            String requestLine = br.readLine(); // 请求行：GET /hello HTTP/1.1
            String URL = requestLine.split(" ")[1]; // 获取到请求资源的路径

            // 使用Server中的appendLog方法添加到文本区域中
            logArea.append("*****Request URL: " + URL + "*****\n");

            // 读取静态资源文件
            System.out.println(URL);
            responseStaticResource(URL);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 关闭socket
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 读取静态资源文件：相当于是一个总控
    private void responseStaticResource(String URL) {
        // 读取静态资源文件：请求路径：web/index.html
        String filePath = URL; // 用于调试程序

        // 通过输入流读取文件
        try {
            String fileExtension = filePath.substring(filePath.lastIndexOf(".") + 1);
            // 判断content-type类型
            switch (fileExtension) {
                // 文本类型：
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
                // 非文本类型：
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

    // 读取html/htm文件，需要外部处理异常
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

        // 在将数据发送给浏览器之前，需要将数据处理成浏览器能识别到的报文形式
        // 请求行：协议版本号 状态码(200表示请求成功) 状态值(ok表示请求成功)
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 200 OK\n")
                .append("Content-Type:text/" + fileClass + ";charset=utf-8\n\n");

        // 通过输出流将文件写回到客户端
        OutputStream os = clientSocket.getOutputStream();
        os.write(sb.toString().getBytes());
        os.write(fileBytes);
        os.flush();
        logArea.append("=====Response HTML: " + filePath + " Successfully=====\n");

        // 关闭资源
        is.close();
        baos.close();
    }

    // 读取png/jpeg/jpg文件，需要外部处理异常
    private void responseNotText(String filePath, String fileClass) throws IOException {
        // 读取图片文件
        InputStream is = new FileInputStream(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }

        byte[] imageBytes = baos.toByteArray();

        // 在将数据发送给浏览器之前，需要将数据处理成浏览器能识别到的报文形式
        // 请求行：协议版本号 状态码(200表示请求成功) 状态值(ok表示请求成功)

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
                // 未知类型
                fileString = "application/octet-stream";
                break;
        }

        // 在将数据发送给浏览器之前，需要将数据处理成浏览器能识别到的报文形式
        PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
        pw.println("HTTP/1.1 200 OK");
        pw.println("Content-Type: " + fileString);
        pw.println("Content-Length: " + imageBytes.length);
        pw.println(); // 空行表示头部结束
        pw.flush();

        // 通过输出流将文件写回到客户端
        OutputStream os = clientSocket.getOutputStream();
        os.write(imageBytes);
        os.flush();

        logArea.append("=====Response Image: " + filePath + " Successfully=====\n");

        // 关闭资源
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