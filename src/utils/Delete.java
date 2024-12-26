package utils;

import java.io.File;
import java.io.FilenameFilter;

//清空所有javac编译生成的.class文件
public class Delete {
	public static void main(String[] args) {
		String path = "src";
		deleteFiles(path);
		deleteSubFiles(path);
	}

	public static void deleteSubFiles(String path) {
		File file = new File(path);
		File[] subdirs = file.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return new File(dir, name).isDirectory();
			}
		});
		if (subdirs != null) {
			for (File subdir : subdirs) {
				deleteSubFiles(subdir.getAbsolutePath());
				deleteFiles(subdir.getAbsolutePath());
			}
		}
	}

	public static void deleteFiles(String path) {
		File[] fs = new File(path).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".class");
			}
		});
		if (fs != null) {
			for (File f : fs) {
				f.delete();
			}
		}
	}
}
