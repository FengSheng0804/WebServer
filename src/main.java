import server.Server;
import server.proxyServer.ProxyServer;

public class main {
    /**
     * 程序入口点。
     * 
     * 该方法创建并启动一个新的线程来运行代理服务器。
     * 代理服务器将在端口号为10000的端口上运行，并将其设置为可见。
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        /**
         * 创建并启动一个新的线程来运行服务器。
         * 该线程将实例化一个端口号为8080的ProxyServer对象，并将其设置为可见。
         */
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Server server = new Server(8080);
                // 启动服务器
                server.setVisible(true);
            }
        });
        serverThread.start();

        /**
         * 创建并启动一个新的线程来运行代理服务器。
         * 该线程将实例化一个端口号为10000的ProxyServer对象，并将其设置为可见。
         */
        Thread proxyServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ProxyServer proxyServer = new ProxyServer(10000, "127.0.0.1", 8080);
                // 启动代理服务器
                proxyServer.setVisible(true);
            }
        });
        proxyServerThread.start();

        try {
            // 等待线程执行完毕
            proxyServerThread.join();
            serverThread.join();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
