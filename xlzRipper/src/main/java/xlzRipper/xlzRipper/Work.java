package xlzRipper.xlzRipper;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FileUtils;

import java.awt.Toolkit;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.*;
import javax.swing.*;
public class Work extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final byte[] BUFFER = new byte[1024];
	
	public static void xlzPacker(File source) throws IOException, FileNotFoundException {
		String[] sourceFiles = source.list();

		for (String sourceFileName : sourceFiles) {

			if (sourceFileName.endsWith(".xlz")) {

				ZipFile war = new ZipFile(source + "//" + sourceFileName);
				ZipOutputStream xlzTargetFile = new ZipOutputStream(new FileOutputStream(source + "//" + "tmp.tmp"));

				addSkeleton(war, xlzTargetFile);

				// now append some extra content

				addTranslatedContent(source, sourceFileName, xlzTargetFile);

				war.close();
				xlzTargetFile.close();
			}

			fileReplace(source, sourceFileName);
		}
	}

	private static void addSkeleton(ZipFile war, ZipOutputStream xlzTargetFile) throws IOException {
		Enumeration<? extends ZipEntry> entries = war.entries();
		while (entries.hasMoreElements()) {

			// ZipEntry e = entries.nextElement(); chuj wi czemu tak nie dziala
			ZipEntry e = new ZipEntry(entries.nextElement().getName());

			if (!e.getName().equals("content.xlf")) {

				System.out.println("copy: " + e.getName());
				xlzTargetFile.putNextEntry(e);

				if (!e.isDirectory()) {
					copy(war.getInputStream(e), xlzTargetFile);
				}

				xlzTargetFile.closeEntry();
			}

		}
	}

	private static void addTranslatedContent(File source, String sourceFileName, ZipOutputStream xlzTargetFile)
			throws FileNotFoundException, IOException {
		FileInputStream fin = new FileInputStream(source + "//" + sourceFileName + ".xlf");

		ZipEntry e = new ZipEntry("content.xlf");
		xlzTargetFile.putNextEntry(e);
		copy(fin, xlzTargetFile);
		xlzTargetFile.closeEntry();
	}

	private static void fileReplace(File source, String sourceFileName) {
		File xlzOryginalFile = new File(source + "//" + sourceFileName);
		xlzOryginalFile.renameTo(new File(source + "//" + sourceFileName + ".old"));
		File tmp = new File(source + "//" + "tmp.tmp");
		tmp.renameTo(new File(source + "//" + sourceFileName));
	}

	public static void copy(InputStream input, OutputStream output) throws IOException {
		int bytesRead;
		while ((bytesRead = input.read(BUFFER)) > 0) {
			output.write(BUFFER, 0, bytesRead);
		}
	}

	public static void xlzReaper(File source) throws FileNotFoundException, IOException {
		String[] sourceFiles = source.list();

		for (String sourceFileName : sourceFiles) {

			if (sourceFileName.endsWith(".xlz")) {

				byte[] buffer = new byte[1024];
				ZipInputStream zis = new ZipInputStream(new FileInputStream(source + "//" + sourceFileName));
				ZipEntry zipEntry = zis.getNextEntry();

				while (zipEntry != null) {
					System.out.println(zipEntry.getName());
					saveXlz(source, sourceFileName, buffer, zis, zipEntry);
					zipEntry = zis.getNextEntry();
				}

				zis.closeEntry();
				zis.close();
				
				addTarget(source, sourceFileName);
			}
		
		}
	}

	private static void addTarget(File source, String sourceFileName) throws IOException {
		String tmpFile = FileUtils.readFileToString(new File(source + "//" + sourceFileName + ".xlf"), StandardCharsets.UTF_8);
		tmpFile = tmpFile.replaceAll("<source>(.*?)</source>","<source>$1</source><target>$1</target>");
		FileUtils.writeStringToFile(new File(source + "//" + sourceFileName + ".xlf"), tmpFile, StandardCharsets.UTF_8);
	}

	private static void saveXlz(File destDir, String sourceFileName, byte[] buffer, ZipInputStream zis,
			ZipEntry zipEntry) throws IOException, FileNotFoundException {

		if (zipEntry.getName().equals("content.xlf")) {

			String targetFileName = sourceFileName + ".xlf";
			System.out.println(targetFileName);
			File newFile = newFile(destDir, targetFileName);
			
			FileOutputStream fos = new FileOutputStream(newFile);

			copy(zis, fos);

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
