package utils;

import java.io.File;
import java.io.FilenameFilter;

//清空所有javac编译生成的.class文件
public class Delete {
	public static void main(String[] args) {
		String path = "src";
		// 删除指定路径下的所有.class文件
		deleteFiles(path);
		// 递归删除子目录中的.class文件
		deleteSubFiles(path);
	}

	// 递归删除子目录中的.class文件
	public static void deleteSubFiles(String path) {
		File file = new File(path);
		// 获取所有子目录
		File[] subdirs = file.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return new File(dir, name).isDirectory();
			}
		});
		if (subdirs != null) {
			for (File subdir : subdirs) {
				// 递归删除子目录中的.class文件
				deleteSubFiles(subdir.getAbsolutePath());
				// 删除子目录中的.class文件
				deleteFiles(subdir.getAbsolutePath());
			}
		}
	}

	// 删除指定路径下的所有.class文件
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
