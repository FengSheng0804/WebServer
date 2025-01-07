package server;

import utils.compress.DeflateUtils;
import utils.compress.GZipUtils;

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
    public synchronized void run() {
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
                    // 若URL中包含参数，则去掉参数，因为有参数是POST请求
                    if (line.contains("?")) {
                        line = line.substring(0, line.indexOf("?"));
                    }
                    requestLine = line;
                }
            }

            String requestString = requestBuilder.toString();
            String URL = null;
            if (requestLine != null) {
                URL = requestLine.split(" ")[1];
            }

            // 检测是否为代理服务器
            String ProxyServerIP = isProxyServer(requestString);
            if (ProxyServerIP != null) {
                logArea.append("Proxy Server Detected, IP is " + ProxyServerIP);
            } else {
                logArea.append("The request did'nt come from ProxyServer");

                // 为了测试代理服务器，我们将拒绝所有非代理服务器的请求
                response403();
                return;
            }

            // 以下均为GET请求的处理
            // 检查是否为未实现的请求方法，若是则返回501错误
            if (requestLine != null && !requestLine.startsWith("GET")) {
                logArea.append("Not Implemented request method: " + requestLine + "\n");
                response501();
                return;
            }

            if (URL == null) {
                return;
            } else {
                // 使用Server中的appendLog方法添加到文本区域中
                logArea.append("*****Request URL: " + URL + "*****\n");

                // 先对文件进行压缩处理
                // 处理请求头，方便后续使用
                Map<String, String> headers = processHeaders(requestString);
                // 获取Accept-Encoding字段
                String acceptEncoding = headers.get("Accept-Encoding");
                String compressMethod = null;
                if (acceptEncoding != null) {
                    String[] acceptMethod = acceptEncoding.split(",");
                    if (acceptMethod.length != 0) {
                        compressMethod = acceptMethod[0];
                    }
                }

                // 进入静态资源文件总控方法，并接收返回值判断文件是否存在
                if (responseController(URL, compressMethod)) {
                    logArea.append("File: " + URL + " Exist\n");
                } else {
                    logArea.append("File: " + URL + " Didn't Exist\n");
                    response404();
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            response500();
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
    @SuppressWarnings("finally")
    private boolean responseController(String URL, String compressMethod) throws IOException {
        boolean isExist = false;

        // 获取当前工作目录，如果是运行目录的话会在src中，如果是调试目录的话会在src的上一级
        String currentDir = System.getProperty("user.dir");
        String filePath = null;
        if (currentDir.endsWith("src")) {
            filePath = URL.substring(1);
        } else {
            filePath = "src" + URL;
        }

        // 通过输入流读取文件
        try {
            String fileExtension = filePath.substring(filePath.lastIndexOf(".") + 1);
            responseStaticResource(filePath, fileExtension, compressMethod);
            isExist = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return isExist;
        }
    }

    /**
     * 响应静态资源请求，将指定文件路径的静态资源发送给客户端。
     *
     * @param filePath      静态资源文件的路径
     * @param fileExtension 静态资源文件的扩展名，用于确定内容类型
     * @throws IOException 如果在读取文件或写入客户端时发生 I/O 错误
     */
    private void responseStaticResource(String filePath, String fileExtension, String compressMethod)
            throws IOException {
        // 是否压缩
        boolean isCompressByGzip = false;
        boolean isCompressByDeflate = false;
        byte[] compressedBytes = null;
        switch (compressMethod) {
            case "gzip":
                try {
                    // 对文件进行压缩，压缩不删除原文件
                    GZipUtils.compress(filePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 更新文件路径
                filePath = filePath + ".gz";
                // 告诉浏览器使用压缩
                isCompressByGzip = true;
                logArea.append("~~~~~Compress by Gzip: " + filePath + " Successfully~~~~~\n");
                break;
            case "deflate":
                try {
                    // 对文件进行压缩，压缩不删除原文件
                    compressedBytes = DeflateUtils.compress(filePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isCompressByDeflate = true;
                logArea.append("~~~~~Compress by Deflate: " + filePath + " Successfully~~~~~\n");
                break;
            default:
                // 不压缩
                break;
        }
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
        // 如果不压缩的话
        if (!isCompressByGzip && !isCompressByDeflate) {
            if (content_type != null && content_type.startsWith("text")) {
                // 在将数据发送给浏览器之前，需要将数据处理成浏览器能识别到的报文形式
                // 响应头：协议版本号 状态码(200表示请求成功) 状态值(ok表示请求成功)
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
            // 如果采用gzip压缩的话
        } else if (isCompressByGzip) {
            if (content_type != null && content_type.startsWith("text")) {
                // 在将数据发送给浏览器之前，需要将数据处理成浏览器能识别到的报文形式
                // 响应头：协议版本号 状态码(200表示请求成功) 状态值(ok表示请求成功)
                StringBuilder sb = new StringBuilder();
                sb.append("HTTP/1.1 200 OK\n")
                        .append("Content-Encoding: gzip\n")
                        .append("Content-Type:text/" + fileExtension + ";charset=utf-8\n\n");

                // 通过输出流将文件写回到客户端
                OutputStream os = clientSocket.getOutputStream();
                os.write(sb.toString().getBytes());
                os.write(fileBytes);
                os.flush();

                // 在传输完数据之后，删除压缩文件
                if (GZipUtils.delete(filePath)) {
                    logArea.append("#####Delete: " + filePath + " Successfully#####\n");
                } else {
                    logArea.append("#####Delete: " + filePath + " Failed#####\n");
                }
            } else {
                // 在将数据发送给浏览器之前，需要将数据处理成浏览器能识别到的报文形式
                PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
                pw.println("HTTP/1.1 200 OK");
                pw.println("Content-Encoding: gzip");
                pw.println("Content-Type: " + content_type);
                pw.println("Content-Length: " + fileBytes.length);
                pw.println(); // 空行表示头部结束
                pw.flush();

                // 通过输出流将文件写回到客户端
                OutputStream os = clientSocket.getOutputStream();
                os.write(fileBytes);
                os.flush();

                // 在传输完数据之后，删除压缩文件
                if (GZipUtils.delete(filePath)) {
                    logArea.append("%%%%%Delete: " + filePath + " Successfully%%%%%\n");
                } else {
                    logArea.append("%%%%%Delete: " + filePath + " Failed%%%%%\n");
                }
            }
            // 如果采用deflate压缩的话
        } else {
            if (compressedBytes != null) {
                // 在将数据发送给浏览器之前，需要将数据处理成浏览器能识别到的报文形式
                // 响应头：协议版本号 状态码(200表示请求成功) 状态值(ok表示请求成功)
                if (content_type != null && content_type.startsWith("text")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("HTTP/1.1 200 OK\n")
                            .append("Content-Encoding: deflate\n")
                            .append("Content-Type:text/" + fileExtension + ";charset=utf-8\n\n");

                    OutputStream os = clientSocket.getOutputStream();
                    os.write(sb.toString().getBytes());
                    os.write(compressedBytes);
                    os.flush();
                } else {
                    PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
                    pw.println("HTTP/1.1 200 OK");
                    pw.println("Content-Encoding: deflate");
                    pw.println("Content-Type: " + content_type);
                    pw.println("Content-Length: " + compressedBytes.length);
                    pw.println(); // 空行表示头部结束
                    pw.flush();

                    OutputStream os = clientSocket.getOutputStream();
                    os.write(compressedBytes);
                    os.flush();
                }
            }
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
        // 获取当前工作目录，如果是运行目录的话会在src中，如果是调试目录的话会在src的上一级
        String currentDir = System.getProperty("user.dir");
        String filePath = null;
        if (currentDir.endsWith("src")) {
            filePath = "./data/content-type.txt";
        } else {
            filePath = "./src/data/content-type.txt";
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains(fileExtension) && line != null) {
                    return line.split(":")[1].trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 默认的content_type类型是这个
        return "application/octet-stream";
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

        if (requestString != null) {
            String[] lines = requestString.split("\n");

            for (String line : lines) {
                if (line != null) {
                    String[] parts = line.split(": ", 2);
                    if (parts.length == 2) {
                        headers.put(parts[0], parts[1]);
                    }
                }
            }
        }
        return headers;
    }

    // 检测是否为代理服务器
    public static String isProxyServer(String message) {
        if (message.contains("via")) {
            return message.split("via: ")[1].split(" ")[1];
        }
        return null;
    }

    /**
     * 发送400 Bad Request响应给客户端。
     * 此方法会向客户端发送一个HTTP 400状态码和一个简单的HTML页面，
     * 页面内容为“400 Bad Request”。
     * 
     * 使用PrintWriter向客户端输出响应头和响应内容。
     * 如果在输出过程中发生IOException，会捕获并打印堆栈跟踪。
     */
    private void response400() {
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            pw.println("HTTP/1.1 400 Bad Request");
            pw.println("Content-Type:text/html;charset=utf-8\n\n");
            pw.println("<html><body><h1 style='text-align:center'>400 Bad Request</h1></body></html>");
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送403 Forbidden响应给客户端。
     * 此方法会向客户端发送一个HTTP 403状态码和一个简单的HTML页面，
     * 页面内容为“403 Forbidden”。
     * 
     * 使用PrintWriter向客户端输出响应头和响应内容。
     * 如果在输出过程中发生IOException，会捕获并打印堆栈跟踪。
     */
    private void response403() {
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            pw.println("HTTP/1.1 403 Forbidden");
            pw.println("Content-Type:text/html;charset=utf-8\n\n");
            pw.println("<html><body><h1 style='text-align:center'>403 Forbidden</h1></body></html>");
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    /**
     * 发送500 Internal Server Error响应给客户端。
     * 此方法会向客户端发送一个HTTP 500状态码和一个简单的HTML页面，
     * 页面内容为“500 Internal Server Error”。
     * 
     * 使用PrintWriter向客户端输出响应头和响应内容。
     * 如果在输出过程中发生IOException，会捕获并打印堆栈跟踪。
     */
    private void response500() {
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            pw.println("HTTP/1.1 500 Internal Server Error");
            pw.println("Content-Type:text/html;charset=utf-8\n\n");
            pw.println("<html><body><h1 style='text-align:center'>500 Internal Server Error</h1></body></html>");
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送501 Not Implemented响应给客户端。
     * 此方法会向客户端发送一个HTTP 501状态码和一个简单的HTML页面，
     * 页面内容为“501 Not Implemented”。
     * 
     * 使用PrintWriter向客户端输出响应头和响应内容。
     * 如果在输出过程中发生IOException，会捕获并打印堆栈跟踪。
     */
    private void response501() {
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            pw.println("HTTP/1.1 501 Not Implemented");
            pw.println("Content-Type:text/html;charset=utf-8\n\n");
            pw.println("<html><body><h1 style='text-align:center'>501 Not Implemented</h1></body></html>");
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送503 Service Unavailable响应给客户端。
     * 此方法会向客户端发送一个HTTP 503状态码和一个简单的HTML页面，
     * 页面内容为“503 Service Unavailable”。
     * 
     * 使用PrintWriter向客户端输出响应头和响应内容。
     * 如果在输出过程中发生IOException，会捕获并打印堆栈跟踪。
     */

    private void response503() {
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            pw.println("HTTP/1.1 503 Service Unavailable");
            pw.println("Content-Type:text/html;charset=utf-8\n\n");
            pw.println("<html><body><h1 style='text-align:center'>503 Service Unavailable</h1></body></html>");
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}