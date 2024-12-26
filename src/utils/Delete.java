package utils;

import java.io.*;

//�������javac�������ɵ�.class�ļ�
public class Delete {
	public static void delete() {
		String path = "../src";
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
