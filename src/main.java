import server.Server;

public class main {
    public static void main(String[] args) {

        /**
         * ����������һ���µ��߳������д����������
         * ���߳̽�ʵ����һ���˿ں�Ϊ10000��ProxyServer���󣬲���������Ϊ�ɼ���
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
