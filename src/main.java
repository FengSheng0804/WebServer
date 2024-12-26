import server.Server;
import client.Client;

public class main {
    public static void main(String[] args) {
        // 启动服务器端
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Server server = new Server(10000);
                server.build();
            }
        });
        serverThread.start();

        // // 启动客户端
        // for (int i = 1; i <= 10; i++) {
        // final int clientNumber = i;
        // Thread clientThread = new Thread(new Runnable() {
        // @Override
        // public void run() {
        // Client client = new Client("127.0.0.1", 10000);
        // client.build(String.format("I'm client %d", clientNumber));
        // }
        // });
        // clientThread.start();
        // try {
        // clientThread.join();
        // Thread.sleep(1000);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // }

        try {
            serverThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
