package xlzRipper.xlzRipper;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.*;

public class Main {

	private static final byte[] BUFFER = new byte[1024];

	public static void main(String[] args) throws IOException {

		
		File source = new File("C://workspace_eclipse");
		File destDir = new File("C://workspace_eclipse");
			//File destDir = new File(System.getProperty("user.dir"));

		String[] sourceFiles = source.list();

		for (String sourceFileName : sourceFiles) {

			if (sourceFileName.endsWith(".xlf")) {

				// System.out.println(sourceFileName);

			}
		}

		// read war.zip and write to append.zip
		ZipFile war = new ZipFile("la.xlz");
		ZipOutputStream append = new ZipOutputStream(new FileOutputStream("append.zip"));

		// first, copy contents from existing war
		Enumeration<? extends ZipEntry> entries = war.entries();
		while (entries.hasMoreElements()) {
			
			//ZipEntry e = entries.nextElement(); chuj wi czemu tak nie dziala
			ZipEntry e = new ZipEntry (entries.nextElement().getName());

			System.out.println("copy: " + e.getName());
			
			append.putNextEntry(e);
			
			if (!e.isDirectory()) {
				copy(war.getInputStream(e), append);
			}
			append.closeEntry();
		}

		// now append some extra content
		ZipEntry e = new ZipEntry("answer.txt");
		System.out.println("append: " + e.getName());
		append.putNextEntry(e);
		append.write("42\n".getBytes());
		append.closeEntry();

		// close
		war.close();
		append.close();

	//	 xlzReaper(source, destDir);
	}

	public static void copy(InputStream input, OutputStream output) throws IOException {
		int bytesRead;
		while ((bytesRead = input.read(BUFFER)) > 0) {
			output.write(BUFFER, 0, bytesRead);
		}
	}

	private static void xlzReaper(File source, File destDir) throws FileNotFoundException, IOException {
		String[] sourceFiles = source.list();

		for (String sourceFileName : sourceFiles) {

			if (sourceFileName.endsWith(".xlz")) {

				byte[] buffer = new byte[1024];
				ZipInputStream zis = new ZipInputStream(new FileInputStream(source + "//" + sourceFileName));
				ZipEntry zipEntry = zis.getNextEntry();

				while (zipEntry != null) {
					System.out.println(zipEntry.getName());
					saveXlz(destDir, sourceFileName, buffer, zis, zipEntry);
					zipEntry = zis.getNextEntry();
				}

				zis.closeEntry();
				zis.close();
			}
		}
	}

	private static void saveXlz(File destDir, String sourceFileName, byte[] buffer, ZipInputStream zis,
			ZipEntry zipEntry) throws IOException, FileNotFoundException {

		if (zipEntry.getName().equals("content.xlf")) {

			String targetFileName = sourceFileName + ".xlf";
			System.out.println(targetFileName);
			File newFile = newFile(destDir, targetFileName);
			FileOutputStream fos = new FileOutputStream(newFile);
			int len;

			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}

			fos.close();
		}
	}

	private static File newFile(File destinationDir, String targetFileName) throws IOException {
		File destFile = new File(destinationDir, targetFileName);

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + targetFileName);
		}

		return destFile;
	}
}
