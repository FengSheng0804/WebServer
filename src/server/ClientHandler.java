package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

// 处理来自客户端的数据发送的类(继承自线程类，实现一个客户端能多线程接收多个客户端)
class ClientHandler extends Thread {
    private Socket clientSocket;
    private BufferedReader br;

    public ClientHandler(Socket clientSocket) {
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

            // 返回响应
            System.out.println("Request URL: " + URL);
            if (URL.endsWith(".html") || URL.endsWith(".htm")) {
                // 读取静态资源文件
                responseStaticResource(URL);
            }
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

    // 读取静态资源文件：回复以HTML或文件为后缀的文件
    private void responseStaticResource(String URL) {
        // 读取静态资源文件：请求路径：web/index.html
        String filePath = URL.substring(1); // 去掉URL中的第一个字符'/'

        // 通过输入流读取文件
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            StringBuilder sb = new StringBuilder();
            // 在将数据发送给浏览器之前，需要将数据处理成浏览器能识别到的报文形式
            // 请求行：协议版本号 状态码(200表示请求成功) 状态值(ok表示请求成功)
            sb.append("HTTP/1.1 200 OK\n")
                    // 请求头：文件的格式(因为访问的是html文件，所以使用text/html) 字符集(使用utf-8)
                    // 后面要添加两个'\n'：第一个是用来换行，第二个保证报文和正文之间必须存在的空行
                    .append("Content-Type:text/html;charset=utf-8\n\n");
            String temp = "";
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
            }

            // 通过输出流将文件写回到客户端
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            // 将数据发送给客户端
            pw.println(sb);
            pw.flush();
        } catch (IOException e) {
            // 如果文件未找到，则返回404 Not Found
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