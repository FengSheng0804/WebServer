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
 * TaskHandler类继承自Thread类，负责处理与服务器的单个客户端连接。
 * 它读取客户端请求，处理请求，并将适当的响应发送回客户端。
 * 它还记录请求和响应信息。
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
            // 通过输入流获取客户端发送的数据
            br = new BufferedReader(new InputStreamReader(is));
            // 输出用户的请求信息（一个超长字符串，为了防止出现多线程引起的不可再现性）并记录第一个line的数据
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

            // 以下均为GET请求的处理
            // 处理请求头，方便后续使用
            Map<String, String> headers = processHeaders(requestString);
            // 获取到请求资源的路径
            // System.out.println(requestLine);
            String URL = requestLine.split(" ")[1];

            // 使用Server中的appendLog方法添加到文本区域中
            logArea.append("*****Request URL: " + URL + "*****\n");

            // 读取静态资源文件
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

    /**
     * 处理客户端请求并返回相应的静态资源文件，相当于是一个总控。
     *
     * @param URL 客户端请求的URL路径
     * 
     *            该方法通过解析URL路径来确定请求的文件，并根据文件扩展名判断Content-Type类型。
     *            如果文件存在且类型支持，则调用responseStaticResource方法返回文件内容。
     *            如果文件不存在或读取文件时发生错误，则调用response404方法返回404错误。
     *
     * @throws IOException 如果读取文件时发生IO错误
     */
    private void responseController(String URL) {
        // 读取静态资源文件：请求路径：/web/index.html
        // String filePath = URL.substring(1); // 运行用
        String filePath = "src/" + URL.substring(1); // 调试用

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

    /**
     * 响应静态资源请求，将指定文件路径的静态资源发送给客户端。
     *
     * @param filePath      静态资源文件的路径
     * @param fileExtension 静态资源文件的扩展名，用于确定内容类型
     * @throws IOException 如果在读取文件或写入客户端时发生 I/O 错误
     */
    private void responseStaticResource(String filePath, String fileExtension) throws IOException {
        // 初始化
        InputStream is = new FileInputStream(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }

        byte[] fileBytes = baos.toByteArray();

        // 获取content-type类型
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

    /**
     * 根据文件扩展名获取对应的Content-Type类型。
     * 该方法从content-type.txt文件中读取映射关系，并返回匹配的Content-Type。
     * 如果未找到匹配的扩展名，则返回默认的Content-Type "application/octet-stream"。
     *
     * @param fileExtension 文件扩展名
     * @return 对应的Content-Type类型
     */
    private String getContentType(String fileExtension) {
        // String fileName = "./data/content-type.txt"; // 运行用
        String fileName = "./src/data/content-type.txt"; // 调试用

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
        return "application/octet-stream"; // 默认的content_type类型是这个
    }

    /**
     * 发送404 Not Found响应给客户端。
     * 此方法会向客户端发送一个HTTP 404状态码和一个简单的HTML页面，
     * 页面内容为“404 Not Found”。
     * 
     * 使用PrintWriter向客户端输出响应头和响应内容。
     * 如果在输出过程中发生IOException，会捕获并打印堆栈跟踪。
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

    // 处理请求头
    /**
     * 处理HTTP请求字符串中的头部信息。
     *
     * @param requestString 包含头部信息的HTTP请求字符串
     * @return 一个包含头部名称作为键及其对应值的映射
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