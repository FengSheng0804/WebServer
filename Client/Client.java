import java.net.Socket;
import java.io.PrintWriter;

/**
 * 客户端：主要用于发送请求给服务器端，接收服务器端的响应
 */
public class Client {
    public static void main(String[] args) {
        System.out.println("This is Client");

        // 提高至外部，以便能使用finally进行关闭
        Socket clientSocket = null;
        PrintWriter pw = null;

        // 由于Socket构造器可能会产生异常，所以需要对异常处理
        try {
            // 创建客户端Socket，指定服务器地址和端口
            clientSocket = new Socket("127.0.0.1", 10000);
            {
                System.out.println("Connect Successfully");

                // 通过输出流向服务器端发送数据
                // 1. 获取输出流
                pw = new PrintWriter(clientSocket.getOutputStream());
                // 2. 向服务器端发送数据
                pw.write("Hello, Server");
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
