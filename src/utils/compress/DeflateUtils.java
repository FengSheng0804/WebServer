package utils.compress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class DeflateUtils {
    /**
     * 
     * 压缩.
     * 
     * 
     * @param inputByte 待压缩的字节数组
     * @return 压缩后的数据
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
                // 压缩并将压缩后的内容输出到字节输出流bos中
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
     * 解压缩.
     * 
     * @param inputByte 待解压缩的字节数组
     * @return 解压缩后的字节数组
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
                // 解压缩并将解压缩后的内容输出到字节输出流bos中
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

    // 读取文件进行压缩，得到压缩后的数据流
    public static byte[] compress(String filePath) {
        // 读取文件
        try {
            byte[] fileBytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath));
            byte[] compressedBytes = compress(fileBytes);
            return compressedBytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 读取文件进行解压缩，得到解压缩后的数据流
    public static byte[] uncompress(String filePath) {
        // 读取文件
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