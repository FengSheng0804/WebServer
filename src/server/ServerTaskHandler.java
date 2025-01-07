package server;

import utils.compress.DeflateUtils;
import utils.compress.GZipUtils;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTextArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.FileReader;

/**
 * 
 * TaskHandler��̳���Thread�࣬��������������ĵ����ͻ������ӡ�
 * ����ȡ�ͻ������󣬴������󣬲����ʵ�����Ӧ���ͻؿͻ��ˡ�
 * ������¼�������Ӧ��Ϣ��
 */
class ServerTaskHandler extends Thread {
    private Socket clientSocket;
    private BufferedReader br;
    private JTextArea logArea;

    public ServerTaskHandler(Socket clientSocket, JTextArea logArea) {
        this.logArea = logArea;
        this.clientSocket = clientSocket;
    }

    @Override
    public synchronized void run() {
        try {
            InputStream is = clientSocket.getInputStream();
            // ͨ����������ȡ�ͻ��˷��͵�����
            br = new BufferedReader(new InputStreamReader(is));
            // ����û���������Ϣ��һ�������ַ�����Ϊ�˷�ֹ���ֶ��߳�����Ĳ��������ԣ�����¼��һ��line������
            StringBuilder requestBuilder = new StringBuilder();
            String line;
            String requestLine = null;
            while ((line = br.readLine()) != null && !line.isEmpty()) {
                requestBuilder.append(line).append("\n");
                if (requestLine == null) {
                    // ��URL�а�����������ȥ����������Ϊ�в�����POST����
                    if (line.contains("?")) {
                        line = line.substring(0, line.indexOf("?"));
                    }
                    requestLine = line;
                }
            }

            String requestString = requestBuilder.toString();
            String URL = null;
            if (requestLine != null) {
                URL = requestLine.split(" ")[1];
            }

            // ����Ƿ�Ϊ���������
            String ProxyServerIP = isProxyServer(requestString);
            if (ProxyServerIP != null) {
                logArea.append("Proxy Server Detected, IP is " + ProxyServerIP);
            } else {
                logArea.append("The request did'nt come from ProxyServer");

                // Ϊ�˲��Դ�������������ǽ��ܾ����зǴ��������������
                response403();
                return;
            }

            // ���¾�ΪGET����Ĵ���
            // ����Ƿ�Ϊδʵ�ֵ����󷽷��������򷵻�501����
            if (requestLine != null && !requestLine.startsWith("GET")) {
                logArea.append("Not Implemented request method: " + requestLine + "\n");
                response501();
                return;
            }

            if (URL == null) {
                return;
            } else {
                // ʹ��Server�е�appendLog������ӵ��ı�������
                logArea.append("*****Request URL: " + URL + "*****\n");

                // �ȶ��ļ�����ѹ������
                // ��������ͷ���������ʹ��
                Map<String, String> headers = processHeaders(requestString);
                // ��ȡAccept-Encoding�ֶ�
                String acceptEncoding = headers.get("Accept-Encoding");
                String compressMethod = null;
                if (acceptEncoding != null) {
                    String[] acceptMethod = acceptEncoding.split(",");
                    if (acceptMethod.length != 0) {
                        compressMethod = acceptMethod[0];
                    }
                }

                // ���뾲̬��Դ�ļ��ܿط����������շ���ֵ�ж��ļ��Ƿ����
                if (responseController(URL, compressMethod)) {
                    logArea.append("File: " + URL + " Exist\n");
                } else {
                    logArea.append("File: " + URL + " Didn't Exist\n");
                    response404();
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            response500();
        } finally {
            // �ر���Դ
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // �ر�socket
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * ����ͻ������󲢷�����Ӧ�ľ�̬��Դ�ļ����൱����һ���ܿء�
     *
     * @param URL �ͻ��������URL·��
     * 
     *            �÷���ͨ������URL·����ȷ��������ļ����������ļ���չ���ж�Content-Type���͡�
     *            ����ļ�����������֧�֣������responseStaticResource���������ļ����ݡ�
     *            ����ļ������ڻ��ȡ�ļ�ʱ�������������response404��������404����
     *
     * @throws IOException �����ȡ�ļ�ʱ����IO����
     */
    @SuppressWarnings("finally")
    private boolean responseController(String URL, String compressMethod) throws IOException {
        boolean isExist = false;

        // ��ȡ��ǰ����Ŀ¼�����������Ŀ¼�Ļ�����src�У�����ǵ���Ŀ¼�Ļ�����src����һ��
        String currentDir = System.getProperty("user.dir");
        String filePath = null;
        if (currentDir.endsWith("src")) {
            filePath = URL.substring(1);
        } else {
            filePath = "src" + URL;
        }

        // ͨ����������ȡ�ļ�
        try {
            String fileExtension = filePath.substring(filePath.lastIndexOf(".") + 1);
            responseStaticResource(filePath, fileExtension, compressMethod);
            isExist = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return isExist;
        }
    }

    /**
     * ��Ӧ��̬��Դ���󣬽�ָ���ļ�·���ľ�̬��Դ���͸��ͻ��ˡ�
     *
     * @param filePath      ��̬��Դ�ļ���·��
     * @param fileExtension ��̬��Դ�ļ�����չ��������ȷ����������
     * @throws IOException ����ڶ�ȡ�ļ���д��ͻ���ʱ���� I/O ����
     */
    private void responseStaticResource(String filePath, String fileExtension, String compressMethod)
            throws IOException {
        // �Ƿ�ѹ��
        boolean isCompressByGzip = false;
        boolean isCompressByDeflate = false;
        byte[] compressedBytes = null;
        switch (compressMethod) {
            case "gzip":
                try {
                    // ���ļ�����ѹ����ѹ����ɾ��ԭ�ļ�
                    GZipUtils.compress(filePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // �����ļ�·��
                filePath = filePath + ".gz";
                // ���������ʹ��ѹ��
                isCompressByGzip = true;
                logArea.append("~~~~~Compress by Gzip: " + filePath + " Successfully~~~~~\n");
                break;
            case "deflate":
                try {
                    // ���ļ�����ѹ����ѹ����ɾ��ԭ�ļ�
                    compressedBytes = DeflateUtils.compress(filePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isCompressByDeflate = true;
                logArea.append("~~~~~Compress by Deflate: " + filePath + " Successfully~~~~~\n");
                break;
            default:
                // ��ѹ��
                break;
        }
        // ��ʼ��
        InputStream is = new FileInputStream(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }

        byte[] fileBytes = baos.toByteArray();

        // ��ȡcontent-type����
        String content_type = getContentType(fileExtension);
        // �����ѹ���Ļ�
        if (!isCompressByGzip && !isCompressByDeflate) {
            if (content_type != null && content_type.startsWith("text")) {
                // �ڽ����ݷ��͸������֮ǰ����Ҫ�����ݴ�����������ʶ�𵽵ı�����ʽ
                // ��Ӧͷ��Э��汾�� ״̬��(200��ʾ����ɹ�) ״ֵ̬(ok��ʾ����ɹ�)
                StringBuilder sb = new StringBuilder();
                sb.append("HTTP/1.1 200 OK\n")
                        .append("Content-Type:text/" + fileExtension + ";charset=utf-8\n\n");

                // ͨ����������ļ�д�ص��ͻ���
                OutputStream os = clientSocket.getOutputStream();
                os.write(sb.toString().getBytes());
                os.write(fileBytes);
                os.flush();
            } else {
                // �ڽ����ݷ��͸������֮ǰ����Ҫ�����ݴ�����������ʶ�𵽵ı�����ʽ
                PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
                pw.println("HTTP/1.1 200 OK");
                pw.println("Content-Type: " + content_type);
                pw.println("Content-Length: " + fileBytes.length);
                pw.println(); // ���б�ʾͷ������
                pw.flush();

                // ͨ����������ļ�д�ص��ͻ���
                OutputStream os = clientSocket.getOutputStream();
                os.write(fileBytes);
                os.flush();
            }
            // �������gzipѹ���Ļ�
        } else if (isCompressByGzip) {
            if (content_type != null && content_type.startsWith("text")) {
                // �ڽ����ݷ��͸������֮ǰ����Ҫ�����ݴ�����������ʶ�𵽵ı�����ʽ
                // ��Ӧͷ��Э��汾�� ״̬��(200��ʾ����ɹ�) ״ֵ̬(ok��ʾ����ɹ�)
                StringBuilder sb = new StringBuilder();
                sb.append("HTTP/1.1 200 OK\n")
                        .append("Content-Encoding: gzip\n")
                        .append("Content-Type:text/" + fileExtension + ";charset=utf-8\n\n");

                // ͨ����������ļ�д�ص��ͻ���
                OutputStream os = clientSocket.getOutputStream();
                os.write(sb.toString().getBytes());
                os.write(fileBytes);
                os.flush();

                // �ڴ���������֮��ɾ��ѹ���ļ�
                if (GZipUtils.delete(filePath)) {
                    logArea.append("#####Delete: " + filePath + " Successfully#####\n");
                } else {
                    logArea.append("#####Delete: " + filePath + " Failed#####\n");
                }
            } else {
                // �ڽ����ݷ��͸������֮ǰ����Ҫ�����ݴ�����������ʶ�𵽵ı�����ʽ
                PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
                pw.println("HTTP/1.1 200 OK");
                pw.println("Content-Encoding: gzip");
                pw.println("Content-Type: " + content_type);
                pw.println("Content-Length: " + fileBytes.length);
                pw.println(); // ���б�ʾͷ������
                pw.flush();

                // ͨ����������ļ�д�ص��ͻ���
                OutputStream os = clientSocket.getOutputStream();
                os.write(fileBytes);
                os.flush();

                // �ڴ���������֮��ɾ��ѹ���ļ�
                if (GZipUtils.delete(filePath)) {
                    logArea.append("%%%%%Delete: " + filePath + " Successfully%%%%%\n");
                } else {
                    logArea.append("%%%%%Delete: " + filePath + " Failed%%%%%\n");
                }
            }
            // �������deflateѹ���Ļ�
        } else {
            if (compressedBytes != null) {
                // �ڽ����ݷ��͸������֮ǰ����Ҫ�����ݴ�����������ʶ�𵽵ı�����ʽ
                // ��Ӧͷ��Э��汾�� ״̬��(200��ʾ����ɹ�) ״ֵ̬(ok��ʾ����ɹ�)
                if (content_type != null && content_type.startsWith("text")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("HTTP/1.1 200 OK\n")
                            .append("Content-Encoding: deflate\n")
                            .append("Content-Type:text/" + fileExtension + ";charset=utf-8\n\n");

                    OutputStream os = clientSocket.getOutputStream();
                    os.write(sb.toString().getBytes());
                    os.write(compressedBytes);
                    os.flush();
                } else {
                    PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
                    pw.println("HTTP/1.1 200 OK");
                    pw.println("Content-Encoding: deflate");
                    pw.println("Content-Type: " + content_type);
                    pw.println("Content-Length: " + compressedBytes.length);
                    pw.println(); // ���б�ʾͷ������
                    pw.flush();

                    OutputStream os = clientSocket.getOutputStream();
                    os.write(compressedBytes);
                    os.flush();
                }
            }
        }
        logArea.append("=====Response: " + content_type + ": " + filePath + " Successfully=====\n");

        // �ر���Դ
        is.close();
        baos.close();
    }

    /**
     * �����ļ���չ����ȡ��Ӧ��Content-Type���͡�
     * �÷�����content-type.txt�ļ��ж�ȡӳ���ϵ��������ƥ���Content-Type��
     * ���δ�ҵ�ƥ�����չ�����򷵻�Ĭ�ϵ�Content-Type "application/octet-stream"��
     *
     * @param fileExtension �ļ���չ��
     * @return ��Ӧ��Content-Type����
     */
    private String getContentType(String fileExtension) {
        // ��ȡ��ǰ����Ŀ¼�����������Ŀ¼�Ļ�����src�У�����ǵ���Ŀ¼�Ļ�����src����һ��
        String currentDir = System.getProperty("user.dir");
        String filePath = null;
        if (currentDir.endsWith("src")) {
            filePath = "./data/content-type.txt";
        } else {
            filePath = "./src/data/content-type.txt";
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains(fileExtension) && line != null) {
                    return line.split(":")[1].trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Ĭ�ϵ�content_type���������
        return "application/octet-stream";
    }

    // ��������ͷ
    /**
     * ����HTTP�����ַ����е�ͷ����Ϣ��
     *
     * @param requestString ����ͷ����Ϣ��HTTP�����ַ���
     * @return һ������ͷ��������Ϊ�������Ӧֵ��ӳ��
     */
    public Map<String, String> processHeaders(String requestString) {
        Map<String, String> headers = new HashMap<>();

        if (requestString != null) {
            String[] lines = requestString.split("\n");

            for (String line : lines) {
                if (line != null) {
                    String[] parts = line.split(": ", 2);
                    if (parts.length == 2) {
                        headers.put(parts[0], parts[1]);
                    }
                }
            }
        }
        return headers;
    }

    // ����Ƿ�Ϊ���������
    public static String isProxyServer(String message) {
        if (message.contains("via")) {
            return message.split("via: ")[1].split(" ")[1];
        }
        return null;
    }

    /**
     * ����400 Bad Request��Ӧ���ͻ��ˡ�
     * �˷�������ͻ��˷���һ��HTTP 400״̬���һ���򵥵�HTMLҳ�棬
     * ҳ������Ϊ��400 Bad Request����
     * 
     * ʹ��PrintWriter��ͻ��������Ӧͷ����Ӧ���ݡ�
     * �������������з���IOException���Ჶ�񲢴�ӡ��ջ���١�
     */
    private void response400() {
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            pw.println("HTTP/1.1 400 Bad Request");
            pw.println("Content-Type:text/html;charset=utf-8\n\n");
            pw.println("<html><body><h1 style='text-align:center'>400 Bad Request</h1></body></html>");
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ����403 Forbidden��Ӧ���ͻ��ˡ�
     * �˷�������ͻ��˷���һ��HTTP 403״̬���һ���򵥵�HTMLҳ�棬
     * ҳ������Ϊ��403 Forbidden����
     * 
     * ʹ��PrintWriter��ͻ��������Ӧͷ����Ӧ���ݡ�
     * �������������з���IOException���Ჶ�񲢴�ӡ��ջ���١�
     */
    private void response403() {
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            pw.println("HTTP/1.1 403 Forbidden");
            pw.println("Content-Type:text/html;charset=utf-8\n\n");
            pw.println("<html><body><h1 style='text-align:center'>403 Forbidden</h1></body></html>");
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ����404 Not Found��Ӧ���ͻ��ˡ�
     * �˷�������ͻ��˷���һ��HTTP 404״̬���һ���򵥵�HTMLҳ�棬
     * ҳ������Ϊ��404 Not Found����
     * 
     * ʹ��PrintWriter��ͻ��������Ӧͷ����Ӧ���ݡ�
     * �������������з���IOException���Ჶ�񲢴�ӡ��ջ���١�
     */
    private void response404() {
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            pw.println("HTTP/1.1 404 Not Found");
            pw.println("Content-Type:text/html;charset=utf-8\n\n");
            pw.println("<html><body><h1 style='text-align:center'>404 Not Found</h1></body></html>");
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ����500 Internal Server Error��Ӧ���ͻ��ˡ�
     * �˷�������ͻ��˷���һ��HTTP 500״̬���һ���򵥵�HTMLҳ�棬
     * ҳ������Ϊ��500 Internal Server Error����
     * 
     * ʹ��PrintWriter��ͻ��������Ӧͷ����Ӧ���ݡ�
     * �������������з���IOException���Ჶ�񲢴�ӡ��ջ���١�
     */
    private void response500() {
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            pw.println("HTTP/1.1 500 Internal Server Error");
            pw.println("Content-Type:text/html;charset=utf-8\n\n");
            pw.println("<html><body><h1 style='text-align:center'>500 Internal Server Error</h1></body></html>");
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ����501 Not Implemented��Ӧ���ͻ��ˡ�
     * �˷�������ͻ��˷���һ��HTTP 501״̬���һ���򵥵�HTMLҳ�棬
     * ҳ������Ϊ��501 Not Implemented����
     * 
     * ʹ��PrintWriter��ͻ��������Ӧͷ����Ӧ���ݡ�
     * �������������з���IOException���Ჶ�񲢴�ӡ��ջ���١�
     */
    private void response501() {
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            pw.println("HTTP/1.1 501 Not Implemented");
            pw.println("Content-Type:text/html;charset=utf-8\n\n");
            pw.println("<html><body><h1 style='text-align:center'>501 Not Implemented</h1></body></html>");
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ����503 Service Unavailable��Ӧ���ͻ��ˡ�
     * �˷�������ͻ��˷���һ��HTTP 503״̬���һ���򵥵�HTMLҳ�棬
     * ҳ������Ϊ��503 Service Unavailable����
     * 
     * ʹ��PrintWriter��ͻ��������Ӧͷ����Ӧ���ݡ�
     * �������������з���IOException���Ჶ�񲢴�ӡ��ջ���١�
     */

    private void response503() {
        try {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
            pw.println("HTTP/1.1 503 Service Unavailable");
            pw.println("Content-Type:text/html;charset=utf-8\n\n");
            pw.println("<html><body><h1 style='text-align:center'>503 Service Unavailable</h1></body></html>");
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}