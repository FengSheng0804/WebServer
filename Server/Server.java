import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 服务器
 */
public class Server {
    public static void main(String[] args) {
        System.out.println("This is Server");

        // 提高至外部，以便能使用finally进行关闭
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        BufferedReader br = null;

        // 由于ServerSocket构造器可能会产生异常，所以需要对异常处理
        try {
            // 创建服务端端口，绑定端口10000
            serverSocket = new ServerSocket(10000);
            System.out.println("Waiting for request");

            // accept：监听上面配置的端口，如果没有监听到内容会一直阻塞这个线程，直到建立连接成功，返回clientSocket对象
            clientSocket = serverSocket.accept();
            System.out.println("Connect Successfully");

            // 通过输入流读取clientSocket的数据
            // 1. 获取输入流
            InputStream is = clientSocket.getInputStream();
            // 2. 使用InputStreamReader将字节流转化成字符流，其中InputStreamReader是一个桥梁
            /*
             * 3. 将字符流转化成缓冲字符流，提供了缓冲功能，可以有效地读取字符、数组和行
             * 从而，InputStream 被转换为一个可以高效读取字符和行的 BufferedReader
             */
            br = new BufferedReader(new InputStreamReader(is));

            System.out.println("Waiting for inputting");
            String result = null;
            while ((result = br.readLine()) != null) {
                System.out.println(result);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // ServerSocket是先开后关，clientSocket是先关后开
            // 关闭缓冲字符流
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 关闭clientSocket
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 关闭serverSocket
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}