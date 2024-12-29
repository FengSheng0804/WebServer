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
 * GZIP工具
 */
public abstract class GZipUtils {
    public static final int BUFFER = 1024;
    public static final String EXT = ".gz";

    /**
     * 输入流数据压缩
     * 
     * @param is 输入流
     * @param os 输出流
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
     * byte[]数据压缩
     * 
     * @param data 需要压缩的byte[]数据
     * @return
     * @throws Exception
     */
    public static byte[] compress(byte[] data) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 压缩
        compress(bais, baos);

        byte[] output = baos.toByteArray();

        baos.flush();
        baos.close();
        bais.close();

        return output;
    }

    /**
     * 文件压缩
     * 
     * @param file   需要压缩的文件
     * @param delete 是否删除原始文件
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
     * 文件压缩，默认不删除原始文件
     * 
     * @param file 需要压缩的文件
     * @throws Exception
     */
    public static void compress(File file) throws Exception {
        // 默认不删除原始文件
        compress(file, false);
    }

    /**
     * 指定路径文件压缩
     * 
     * @param path   文件路径
     * @param delete 是否删除原始文件
     * @throws Exception
     */
    public static void compress(String path, boolean delete) throws Exception {
        File file = new File(path);
        compress(file, delete);
    }

    /**
     * 指定路径文件压缩，默认不删除原始文件
     * 
     * @param path 文件路径
     * @throws Exception
     */
    public static void compress(String path) throws Exception {
        // 默认不删除原始文件
        compress(path, false);
    }

    // ===============================================解压缩=========================================================

    /**
     * 输入流数据解压缩
     * 
     * @param is 输入流
     * @param os 输出流
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
     * byte[]数据解压缩
     * 
     * @param data 需要解压缩的数据
     * @return byte[] 解压缩后的数据
     * @throws Exception
     */
    public static byte[] decompress(byte[] data) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 解压缩
        decompress(bais, baos);

        data = baos.toByteArray();

        baos.flush();
        baos.close();
        bais.close();

        return data;
    }

    /**
     * 文件解压缩
     * 
     * @param file   需要解压缩的文件
     * @param delete 是否删除原始文件
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
     * 文件解压缩，默认不删除原始文件
     * 
     * @param file 需要解压缩的文件
     * @throws Exception
     */
    public static void decompress(File file) throws Exception {
        // 默认不删除原始文件
        decompress(file, true);
    }

    /**
     * 指定路径文件解压缩
     * 
     * @param path   文件路径
     * @param delete 是否删除原始文件
     * @throws Exception
     */
    public static void decompress(String path, boolean delete) throws Exception {
        File file = new File(path);
        decompress(file, delete);
    }

    /**
     * 指定路径文件解压缩，默认不删除原始文件
     * 
     * @param path 文件路径
     * @throws Exception
     */
    public static void decompress(String path) throws Exception {
        decompress(path, true);
    }

    /**
     * 删除文件
     * 
     * @param filePath 文件路径
     */
    public static boolean delete(String filePath) {
        File file = new File(filePath);
        // 首先判断文件是否存在
        if (file.exists()) {
            // 如果是文件（不是目录），尝试删除
            if (file.isFile()) {
                return file.delete();
            } else {
                System.out.println(filePath + " 不是一个文件，无法删除。");
                return false;
            }
        } else {
            System.out.println(filePath + " 文件不存在，无法删除。");
            return false;
        }
    }
}