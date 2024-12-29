package utils.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * GZIP����
 */
public abstract class GZipUtils {
    public static final int BUFFER = 1024;
    public static final String EXT = ".gz";

    /**
     * ����������ѹ��
     * 
     * @param is ������
     * @param os �����
     * @throws Exception
     */
    public static void compress(InputStream is, OutputStream os)
            throws Exception {
        GZIPOutputStream gos = new GZIPOutputStream(os);

        int count;
        byte data[] = new byte[BUFFER];
        while ((count = is.read(data, 0, BUFFER)) != -1) {
            gos.write(data, 0, count);
        }

        gos.finish();
        gos.flush();
        gos.close();
    }

    /**
     * byte[]����ѹ��
     * 
     * @param data ��Ҫѹ����byte[]����
     * @return
     * @throws Exception
     */
    public static byte[] compress(byte[] data) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // ѹ��
        compress(bais, baos);

        byte[] output = baos.toByteArray();

        baos.flush();
        baos.close();
        bais.close();

        return output;
    }

    /**
     * �ļ�ѹ��
     * 
     * @param file   ��Ҫѹ�����ļ�
     * @param delete �Ƿ�ɾ��ԭʼ�ļ�
     * @throws Exception
     */
    public static void compress(File file, boolean delete) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        FileOutputStream fos = new FileOutputStream(file.getPath() + EXT);

        compress(fis, fos);

        fis.close();
        fos.flush();
        fos.close();

        if (delete) {
            file.delete();
        }
    }

    /**
     * �ļ�ѹ����Ĭ�ϲ�ɾ��ԭʼ�ļ�
     * 
     * @param file ��Ҫѹ�����ļ�
     * @throws Exception
     */
    public static void compress(File file) throws Exception {
        // Ĭ�ϲ�ɾ��ԭʼ�ļ�
        compress(file, false);
    }

    /**
     * ָ��·���ļ�ѹ��
     * 
     * @param path   �ļ�·��
     * @param delete �Ƿ�ɾ��ԭʼ�ļ�
     * @throws Exception
     */
    public static void compress(String path, boolean delete) throws Exception {
        File file = new File(path);
        compress(file, delete);
    }

    /**
     * ָ��·���ļ�ѹ����Ĭ�ϲ�ɾ��ԭʼ�ļ�
     * 
     * @param path �ļ�·��
     * @throws Exception
     */
    public static void compress(String path) throws Exception {
        // Ĭ�ϲ�ɾ��ԭʼ�ļ�
        compress(path, false);
    }

    // ===============================================��ѹ��=========================================================

    /**
     * ���������ݽ�ѹ��
     * 
     * @param is ������
     * @param os �����
     * @throws Exception
     */
    public static void decompress(InputStream is, OutputStream os)
            throws Exception {
        GZIPInputStream gis = new GZIPInputStream(is);

        int count;
        byte data[] = new byte[BUFFER];
        while ((count = gis.read(data, 0, BUFFER)) != -1) {
            os.write(data, 0, count);
        }

        gis.close();
    }

    /**
     * byte[]���ݽ�ѹ��
     * 
     * @param data ��Ҫ��ѹ��������
     * @return byte[] ��ѹ���������
     * @throws Exception
     */
    public static byte[] decompress(byte[] data) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // ��ѹ��
        decompress(bais, baos);

        data = baos.toByteArray();

        baos.flush();
        baos.close();
        bais.close();

        return data;
    }

    /**
     * �ļ���ѹ��
     * 
     * @param file   ��Ҫ��ѹ�����ļ�
     * @param delete �Ƿ�ɾ��ԭʼ�ļ�
     * @throws Exception
     */
    public static void decompress(File file, boolean delete) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        FileOutputStream fos = new FileOutputStream(file.getPath().replace(EXT,
                ""));
        decompress(fis, fos);
        fis.close();
        fos.flush();
        fos.close();

        if (delete) {
            file.delete();
        }
    }

    /**
     * �ļ���ѹ����Ĭ�ϲ�ɾ��ԭʼ�ļ�
     * 
     * @param file ��Ҫ��ѹ�����ļ�
     * @throws Exception
     */
    public static void decompress(File file) throws Exception {
        // Ĭ�ϲ�ɾ��ԭʼ�ļ�
        decompress(file, true);
    }

    /**
     * ָ��·���ļ���ѹ��
     * 
     * @param path   �ļ�·��
     * @param delete �Ƿ�ɾ��ԭʼ�ļ�
     * @throws Exception
     */
    public static void decompress(String path, boolean delete) throws Exception {
        File file = new File(path);
        decompress(file, delete);
    }

    /**
     * ָ��·���ļ���ѹ����Ĭ�ϲ�ɾ��ԭʼ�ļ�
     * 
     * @param path �ļ�·��
     * @throws Exception
     */
    public static void decompress(String path) throws Exception {
        decompress(path, true);
    }

    /**
     * ɾ���ļ�
     * 
     * @param filePath �ļ�·��
     */
    public static boolean delete(String filePath) {
        File file = new File(filePath);
        // �����ж��ļ��Ƿ����
        if (file.exists()) {
            // ������ļ�������Ŀ¼��������ɾ��
            if (file.isFile()) {
                return file.delete();
            } else {
                System.out.println(filePath + " ����һ���ļ����޷�ɾ����");
                return false;
            }
        } else {
            System.out.println(filePath + " �ļ������ڣ��޷�ɾ����");
            return false;
        }
    }
}