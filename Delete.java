import java.io.*;

//�������javac�������ɵ�.class�ļ�
public class Delete {
	public static void main(String[] args) throws Exception {
		// String path = JOptionPane.showInputDialog(null, "Input the path of the
		// directory to delete .class files from:");

		// ���а���.class�ļ���Ŀ¼
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
