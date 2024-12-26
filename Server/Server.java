import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * ������
 */
public class Server {
    public static void main(String[] args) {
        System.out.println("This is Server");

        // ������ⲿ���Ա���ʹ��finally���йر�
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        BufferedReader br = null;

        // ����ServerSocket���������ܻ�����쳣��������Ҫ���쳣����
        try {
            // ��������˶˿ڣ��󶨶˿�10000
            serverSocket = new ServerSocket(10000);
            System.out.println("Waiting for request");

            // accept�������������õĶ˿ڣ����û�м��������ݻ�һֱ��������̣߳�ֱ���������ӳɹ�������clientSocket����
            clientSocket = serverSocket.accept();
            System.out.println("Connect Successfully");

            // ͨ����������ȡclientSocket������
            // 1. ��ȡ������
            InputStream is = clientSocket.getInputStream();
            // 2. ʹ��InputStreamReader���ֽ���ת�����ַ���������InputStreamReader��һ������
            /*
             * 3. ���ַ���ת���ɻ����ַ������ṩ�˻��幦�ܣ�������Ч�ض�ȡ�ַ����������
             * �Ӷ���InputStream ��ת��Ϊһ�����Ը�Ч��ȡ�ַ����е� BufferedReader
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
            // ServerSocket���ȿ���أ�clientSocket���ȹغ�
            // �رջ����ַ���
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // �ر�clientSocket
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // �ر�serverSocket
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