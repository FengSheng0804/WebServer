package utils.compress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class DeflateUtils {
    /**
     * 
     * ѹ��.
     * 
     * 
     * @param inputByte ��ѹ�����ֽ�����
     * @return ѹ���������
     * @throws IOException
     */
    public static byte[] compress(byte[] inputByte) throws IOException {
        int len = 0;
        Deflater defl = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        defl.setInput(inputByte);
        defl.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] outputByte = new byte[1024];
        try {
            while (!defl.finished()) {
                // ѹ������ѹ���������������ֽ������bos��
                len = defl.deflate(outputByte);
                bos.write(outputByte, 0, len);
            }
            defl.end();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bos.close();
        }
        return bos.toByteArray();
    }

    /**
     * 
     * ��ѹ��.
     * 
     * @param inputByte ����ѹ�����ֽ�����
     * @return ��ѹ������ֽ�����
     * @throws IOException
     */
    public static byte[] uncompress(byte[] inputByte) throws IOException {
        int len = 0;
        Inflater infl = new Inflater(true);
        infl.setInput(inputByte);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] outByte = new byte[1024];
        try {
            while (!infl.finished()) {
                // ��ѹ��������ѹ���������������ֽ������bos��
                len = infl.inflate(outByte);
                if (len == 0) {
                    break;
                }
                bos.write(outByte, 0, len);
            }
            infl.end();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bos.close();
        }
        return bos.toByteArray();
    }

    // ��ȡ�ļ�����ѹ�����õ�ѹ�����������
    public static byte[] compress(String filePath) {
        // ��ȡ�ļ�
        try {
            byte[] fileBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath));
            byte[] compressedBytes = compress(fileBytes);
            return compressedBytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ��ȡ�ļ����н�ѹ�����õ���ѹ�����������
    public static byte[] uncompress(String filePath) {
        // ��ȡ�ļ�
        try {
            byte[] fileBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath));
            byte[] uncompressedBytes = uncompress(fileBytes);
            return uncompressedBytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}