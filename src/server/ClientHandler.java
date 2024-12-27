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
            responseController(URL);

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
    private void responseController(String URL) {
        // 读取静态资源文件：请求路径：/web/index.html
        String filePath = URL.startsWith("/") ? URL.substring(1) : URL;

        // 通过输入流读取文件
        try {
            String fileExtension = filePath.substring(filePath.lastIndexOf(".") + 1);
            // 判断content-type类型
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
            // 在将数据发送给浏览器之前，需要将数据处理成浏览器能识别到的报文形式
            // 请求行：协议版本号 状态码(200表示请求成功) 状态值(ok表示请求成功)
            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1 200 OK\n")
                    .append("Content-Type:text/" + fileExtension + ";charset=utf-8\n\n");

            // 通过输出流将文件写回到客户端
            OutputStream os = clientSocket.getOutputStream();
            os.write(sb.toString().getBytes());
            os.write(fileBytes);
            os.flush();
        } else {
            // 在将数据发送给浏览器之前，需要将数据处理成浏览器能识别到的报文形式
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            pw.println("HTTP/1.1 200 OK");
            pw.println("Content-Type: " + content_type);
            pw.println("Content-Length: " + fileBytes.length);
            pw.println(); // 空行表示头部结束
            pw.flush();

            // 通过输出流将文件写回到客户端
            OutputStream os = clientSocket.getOutputStream();
            os.write(fileBytes);
            os.flush();
        }
        logArea.append("=====Response: " + content_type + ": " + filePath + " Successfully=====\n");

        // 关闭资源
        is.close();
        baos.close();
    }

    // 获取content-type类型
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
        return "application/octet-stream"; // 默认的content_type类型是这个
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