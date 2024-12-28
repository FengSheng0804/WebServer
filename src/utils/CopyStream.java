package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyStream {
    // 复制输入流
    public static InputStream copyInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    // 复制输出流
    public static OutputStream copyOutputStream(OutputStream outputStream) {
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();
                outputStream.write(toByteArray());
            }
        };
    }
}
