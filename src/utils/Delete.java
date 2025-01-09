package utils;

import java.io.File;
import java.io.FilenameFilter;

//�������javac�������ɵ�.class�ļ�
public class Delete {
	public static void main(String[] args) {
		String src_path = "src";
		// ɾ��ָ��·���µ�����.class��.gz�ļ�
		deleteFiles(src_path);
		// �ݹ�ɾ����Ŀ¼�е�.class��.gz�ļ�
		deleteSubFiles(src_path);

		String web_path = "web";
		deleteFiles(web_path);
		deleteSubFiles(web_path);
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
				// �ݹ�ɾ����Ŀ¼�е�.class��.gz�ļ�
				deleteSubFiles(subdir.getAbsolutePath());
				// ɾ����Ŀ¼�е�.class��.gz�ļ�
				deleteFiles(subdir.getAbsolutePath());
			}
		}
	}

	// ɾ��ָ��·���µ�����.class��.gz�ļ�
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
