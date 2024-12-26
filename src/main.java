import utils.Delete;
import server.Server;
import client.Client;

public class main {
    public static void main(String[] args) {
        // ������������
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Server.build(10000);
            }
        });
        serverThread.start();
        // �����ͻ���
        for (int i = 0; i < 10; i++) {
            final int clientNumber = i;
            Thread clientThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Client.build("127.0.0.1", 10000, String.format("I'm client %d", clientNumber));
                }
            });
            clientThread.start();
            try {
                clientThread.join();
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            serverThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
