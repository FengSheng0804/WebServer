import server.Server;
import server.proxyServer.ProxyServer;

public class main {
    /**
     * ������ڵ㡣
     * 
     * �÷�������������һ���µ��߳������д����������
     * ������������ڶ˿ں�Ϊ10000�Ķ˿������У�����������Ϊ�ɼ���
     * 
     * @param args �����в���
     */
    public static void main(String[] args) {
        /**
         * ����������һ���µ��߳������з�������
         * ���߳̽�ʵ����һ���˿ں�Ϊ8080��ProxyServer���󣬲���������Ϊ�ɼ���
         */
        Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Server server = new Server(8080);
                // ����������
                server.setVisible(true);
            }
        });
        serverThread.start();

        /**
         * ����������һ���µ��߳������д����������
         * ���߳̽�ʵ����һ���˿ں�Ϊ10000��ProxyServer���󣬲���������Ϊ�ɼ���
         */
        Thread proxyServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ProxyServer proxyServer = new ProxyServer(10000, "127.0.0.1", 8080);
                // �������������
                proxyServer.setVisible(true);
            }
        });
        proxyServerThread.start();

        try {
            // �ȴ��߳�ִ�����
            proxyServerThread.join();
            serverThread.join();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
