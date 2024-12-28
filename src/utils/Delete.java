package utils;

import java.io.File;
import java.io.FilenameFilter;

//�������javac�������ɵ�.class�ļ�
public class Delete {
	public static void main(String[] args) {
		String path = "src";
		// ɾ��ָ��·���µ�����.class�ļ�
		deleteFiles(path);
		// �ݹ�ɾ����Ŀ¼�е�.class�ļ�
		deleteSubFiles(path);
	}

	// �ݹ�ɾ����Ŀ¼�е�.class�ļ�
	public static void deleteSubFiles(String path) {
		File file = new File(path);
		// ��ȡ������Ŀ¼
		File[] subdirs = file.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return new File(dir, name).isDirectory();
			}
		});
		if (subdirs != null) {
			for (File subdir : subdirs) {
				// �ݹ�ɾ����Ŀ¼�е�.class�ļ�
				deleteSubFiles(subdir.getAbsolutePath());
				// ɾ����Ŀ¼�е�.class�ļ�
				deleteFiles(subdir.getAbsolutePath());
			}
		}
	}

	// ɾ��ָ��·���µ�����.class�ļ�
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
