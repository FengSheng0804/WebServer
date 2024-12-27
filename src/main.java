import server.Server;

public class main {
    public static void main(String[] args) {

        /**
         * 创建并启动一个新的线程来运行代理服务器。
         * 该线程将实例化一个端口号为10000的ProxyServer对象，并将其设置为可见。
         */
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Server server = new Server(10000);
                server.setVisible(true);
            }
        });
        serverThread.start();

        try {
            serverThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
