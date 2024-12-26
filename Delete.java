import java.io.*;
import javax.swing.*;

//清空所有javac编译生成的.class文件
public class Delete {
	public static void main(String[] args) throws Exception {
		// String path = JOptionPane.showInputDialog(null, "Input the path of the
		// directory to delete .class files from:");

		// 所有包含.class文件的目录
		String[] paths = { "./", "./Server", "./Client" };

		for (String p : paths) {
			deleteFiles(p);
		}
	}

	public static void deleteFiles(String path) {
		File[] fs = new File(path).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".class");
			}
		});
		for (File f : fs)
			f.delete();
	}
}
