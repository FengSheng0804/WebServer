package utils;

import java.io.File;
import java.io.FilenameFilter;

//清空所有javac编译生成的.class文件
public class Delete {
	public static void main(String[] args) {
		String src_path = "src";
		// 删除指定路径下的所有.class和.gz文件
		deleteFiles(src_path);
		// 递归删除子目录中的.class和.gz文件
		deleteSubFiles(src_path);

		String web_path = "web";
		deleteFiles(web_path);
		deleteSubFiles(web_path);
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
				// 递归删除子目录中的.class和.gz文件
				deleteSubFiles(subdir.getAbsolutePath());
				// 删除子目录中的.class和.gz文件
				deleteFiles(subdir.getAbsolutePath());
			}
		}
	}

	// 删除指定路径下的所有.class和.gz文件
	public static void deleteFiles(String path) {
		File[] classFs = new File(path).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".class");
			}
		});

		File[] classGz = new File(path).listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".gz");
			}
		});

		if (classFs != null) {
			for (File f : classFs) {
				f.delete();
			}
		}

		if (classGz != null) {
			for (File f : classGz) {
				f.delete();
			}
		}
	}
}
