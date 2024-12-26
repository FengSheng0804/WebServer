package client;

import java.net.Socket;
import java.io.PrintWriter;

/**
 * 客户端：主要用于发送请求给服务器端，接收服务器端的响应
 */
public class Client {
    private String IP = ""; // IP地址
    private int port; // 端口号

    public Client(String IP, int port) {
        this.port = port;
        this.IP = IP;
    }

    // 启动客户端的函数
    public void build(String message) {

        // 提高至外部，以便能使用finally进行关闭
        Socket clientSocket = null;
        PrintWriter pw = null;

        // 由于Socket构造器可能会产生异常，所以需要对异常处理
        try {
            // 创建客户端Socket，指定服务器地址和端口
            clientSocket = new Socket(this.IP, this.port);
            {
                // 通过输出流向服务器端发送数据
                // 1. 获取输出流
                pw = new PrintWriter(clientSocket.getOutputStream());
                // 2. 向服务器端发送数据
                pw.write(message);
                // 本来写入的数据应该保存在缓冲区内，只有当执行flush的时候才会将缓冲区内的数据输出，
                // 或者在流关闭的时候也会刷新缓冲区（除了字节流其他所有流都会保存在缓冲区内）
                pw.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭输出流
            if (pw != null) {
                pw.close();
            }
            // 关闭clientSocket
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
