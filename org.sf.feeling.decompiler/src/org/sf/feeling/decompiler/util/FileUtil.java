/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;

public class FileUtil {

	public static void writeToFile(File file, String string) {
		try {
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			String encoding = null;
			try {
				encoding = JavaDecompilerPlugin.getDefault().getExportEncoding();
			} catch (Exception e) {
			}
			if (encoding == null || encoding.trim().length() == 0) {
				encoding = StandardCharsets.UTF_8.name();
			}
			try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), encoding))) {
				out.print(string);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeToBinarayFile(File file, InputStream source, boolean close) {
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(source);
			if (!file.exists()) {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			}
			try (BufferedOutputStream fouts = new BufferedOutputStream(new FileOutputStream(file))) {

				byte b[] = new byte[IOUtils.DEFAULT_BUFFER_SIZE];
				int i = 0;
				while ((i = bis.read(b)) != -1) {
					fouts.write(b, 0, i);
				}
				fouts.flush();
			}
			if (close) {
				bis.close();
			}
		} catch (IOException e) {
			Logger.getLogger(FileUtil.class.getName()).log(Level.WARNING, "Write binaray file failed.", //$NON-NLS-1$
					e);
			if (close) {
				try {
					if (bis != null) {
						bis.close();
					}
				} catch (IOException f) {
					Logger.getLogger(FileUtil.class.getName()).log(Level.WARNING, "Close input stream failed.", //$NON-NLS-1$
							f);
				}
			}
		}
	}

	public static boolean copyFile(String src, String des) {
		try (FileInputStream fis = new FileInputStream(src)) {
			writeToBinarayFile(new File(des), fis, false);
		} catch (Exception e) {
			Logger.getLogger(FileUtil.class.getName()).log(Level.WARNING, "Copy file failed.", //$NON-NLS-1$
					e);
		}
		return false;
	}

	public static boolean copyDirectory(File srcDirectory, File desDirectory) {
		if (srcDirectory == null || desDirectory == null) {
			return false;
		}

		return copyDirectory(srcDirectory.getAbsolutePath(), desDirectory.getAbsolutePath(), null);
	}

	public static boolean copyDirectory(String srcDirectory, String desDirectory) {
		return copyDirectory(srcDirectory, desDirectory, null);
	}

	public static boolean copyDirectory(String srcDirectory, String desDirectory, FileFilter filter) {
		try {
			File des = new File(desDirectory);
			if (!des.exists()) {
				des.mkdirs();
			}
			File src = new File(srcDirectory);
			File[] allFile = src.listFiles();
			int totalNum = allFile.length;
			String srcName = ""; //$NON-NLS-1$
			String desName = ""; //$NON-NLS-1$
			int currentFile = 0;
			for (currentFile = 0; currentFile < totalNum; currentFile++) {
				if (!allFile[currentFile].isDirectory()) {
					srcName = allFile[currentFile].toString();
					desName = desDirectory + File.separator + allFile[currentFile].getName();
					if (filter == null || filter.accept(new File(srcName)))
						copyFile(srcName, desName);
				} else {
					if (!copyDirectory(allFile[currentFile].getPath().toString(),
							desDirectory + File.separator + allFile[currentFile].getName().toString(), filter)) {
						Logger.getLogger(FileUtil.class.getName()).log(Level.WARNING, "Copy sub directory " //$NON-NLS-1$
								+ srcDirectory + "failed."); //$NON-NLS-1$
					}
				}
			}
			return true;
		} catch (Exception e) {
			Logger.getLogger(FileUtil.class.getName()).log(Level.WARNING, "Copy directory " + srcDirectory + "failed.", //$NON-NLS-1$ //$NON-NLS-2$
					e);
			return false;
		}
	}

	public static void copyDirectoryToDirectory(File srcDir, File destDir) throws IOException {
		copyDirectoryToDirectory(srcDir, destDir, null);
	}

	public static void copyDirectoryToDirectory(File srcDir, File destDir, FileFilter filter) throws IOException {
		if (srcDir == null) {
			throw new NullPointerException("Source must not be null"); //$NON-NLS-1$
		}
		if (srcDir.exists() && srcDir.isDirectory() == false) {
			throw new IllegalArgumentException("Source '" //$NON-NLS-1$
					+ destDir + "' is not a directory"); //$NON-NLS-1$
		}
		if (destDir == null) {
			throw new NullPointerException("Destination must not be null"); //$NON-NLS-1$
		}
		if (destDir.exists() && destDir.isDirectory() == false) {
			throw new IllegalArgumentException("Destination '" //$NON-NLS-1$
					+ destDir + "' is not a directory"); //$NON-NLS-1$
		}
		copyDirectory(srcDir.getAbsolutePath(), new File(destDir, srcDir.getName()).getAbsolutePath(), filter);
	}

	public static long sizeOfDirectory(File directory) {
		if (!directory.exists()) {
			String message = directory + " does not exist"; //$NON-NLS-1$
			throw new IllegalArgumentException(message);
		}
		if (!directory.isDirectory()) {
			String message = directory + " is not a directory"; //$NON-NLS-1$
			throw new IllegalArgumentException(message);
		}
		long size = 0;
		File[] files = directory.listFiles();
		if (files == null) { // null if security restricted
			return 0L;
		}
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				size += sizeOfDirectory(file);
			} else {
				size += file.length();
			}
		}

		return size;

	}

	/**
	 * Recursively delete a directory.
	 * 
	 * @param directory directory to delete
	 * @throws IOException in case deletion is unsuccessful
	 */
	public static void deleteDirectory(IProgressMonitor monitor, File directory, File base, int step)
			throws IOException {
		if (!directory.exists()) {
			return;
		}

		cleanDirectory(monitor, directory, base, step);
		if (!directory.delete()) {
			String message = "Unable to delete directory " + directory + "."; //$NON-NLS-1$ //$NON-NLS-2$
			throw new IOException(message);
		}
	}

	public static void deleteDirectory(IProgressMonitor monitor, File directory, int step) throws IOException {
		deleteDirectory(monitor, directory, directory, step);
	}

	public static void cleanDirectory(IProgressMonitor monitor, File directory, File base, int step)
			throws IOException {
		if (!directory.exists()) {
			String message = directory + " does not exist"; //$NON-NLS-1$
			throw new IllegalArgumentException(message);
		}

		if (!directory.isDirectory()) {
			String message = directory + " is not a directory"; //$NON-NLS-1$
			throw new IllegalArgumentException(message);
		}

		IOException exception = null;

		boolean isPackage = false;

		File[] files = directory.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (!isPackage && file.isFile()) {
				isPackage = true;
			}
			try {
				forceDelete(monitor, file, base, step);
			} catch (IOException ioe) {
				exception = ioe;
			}
		}

		if (isPackage) {
			if (monitor != null) {
				monitor.worked(step);
			}
		}

		if (null != exception) {
			throw exception;
		}
	}

	public static void forceDelete(IProgressMonitor monitor, File file, File base, int step) throws IOException {
		if (file.isDirectory()) {
			deleteDirectory(monitor, file, base, step);
		} else {
			if (monitor != null) {
				String taskName = file.getAbsolutePath().substring(
						base.getAbsolutePath().length() + new Long(System.currentTimeMillis()).toString().length() + 2);
				monitor.subTask(taskName);
			}
			if (!file.exists()) {
				throw new FileNotFoundException("File does not exist: " + file); //$NON-NLS-1$
			}
			if (!file.delete()) {
				String message = "Unable to delete file: " + file; //$NON-NLS-1$
				throw new IOException(message);
			}
		}
	}

	public static void recursiveZip(IProgressMonitor monitor, ZipOutputStream zos, File file, final String path,
			FileFilter filter, int step) throws FileNotFoundException, IOException {
		if (file.isDirectory()) {
			File[] files = file.listFiles(filter);
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					recursiveZip(monitor, zos, files[i], (path.length() > 0 ? (path + "/") : path) //$NON-NLS-1$
							+ files[i].getName(), filter, step);
				}
			}
			if (monitor != null) {
				monitor.worked(step);
			}
		}
		if (file.isFile()) {
			if (monitor != null) {
				monitor.subTask(path);
			}
			byte[] bt = new byte[4096];
			ZipEntry ze = new ZipEntry(path);
			ze.setSize(file.length());
			zos.putNextEntry(ze);
			try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file))) {
				int i = 0;
				while ((i = fis.read(bt)) != -1) {
					zos.write(bt, 0, i);
				}
			}
		}
	}

	public static void zipFile(File file, String zipFile) throws Exception {
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
			ZipEntry ze = null;
			byte[] buf = new byte[4096];
			int readLen = 0;
			ze = new ZipEntry(file.getName());
			ze.setSize(file.length());
			ze.setTime(file.lastModified());
			zos.putNextEntry(ze);
			try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
				while ((readLen = is.read(buf)) != -1) {
					zos.write(buf, 0, readLen);
				}
			}
		}
	}

	public static interface Filter {

		public boolean accept(String fileName);
	}

	public static void filterZipFile(String filePath, Filter filter) throws Exception {
		if (isZipFile(filePath) && filter != null) {
			File file = new File(filePath);
			File tmpFile;
			try (ZipFile zipFile = new ZipFile(file)) {
				try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
					ZipEntry entry = null;
					InputStream input = null;

					tmpFile = new File(file + ".tmp"); //$NON-NLS-1$
					try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tmpFile))) {
						zos.setLevel(1);
						while ((entry = zis.getNextEntry()) != null) {
							if (filter.accept(entry.getName())) {
								input = zipFile.getInputStream(entry);
								ZipEntry ze = new ZipEntry(entry.getName());
								ze.setSize(entry.getSize());
								ze.setTime(entry.getTime());
								zos.putNextEntry(ze);
								IOUtils.copy(zis, zos);
								input.close();
							}
						}
					}
				}
			}
			file.delete();
			tmpFile.renameTo(file);
		}
	}

	public static void zipDir(File dir, String classPackage, String zipFile) throws Exception {
		File[] files = null;
		if (new File(dir, classPackage).exists()) {
			files = new File(dir, classPackage).listFiles();
		} else if (dir.exists()) {
			files = dir.listFiles();
		}
		if (files != null) {
			try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
				ZipEntry ze = null;
				byte[] buf = new byte[4096];
				int readLen = 0;
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					if (file.isDirectory()) {
						continue;
					}
					ze = new ZipEntry((classPackage.length() > 0 ? (classPackage + "/") //$NON-NLS-1$
							: "") + file.getName()); //$NON-NLS-1$
					ze.setSize(file.length());
					ze.setTime(file.lastModified());
					zos.putNextEntry(ze);
					try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
						while ((readLen = is.read(buf)) != -1) {
							zos.write(buf, 0, readLen);
						}
					}
				}
			}
		}
	}

	public static boolean isZipFile(String path) {
		if (path == null)
			return false;
		try {
			try (ZipFile zipFile = new ZipFile(path)) {
				return true;
			}
		} catch (IOException e) {
			return false;
		}
	}

	public static String getContent(File file) {
		return getContent(file, Charset.defaultCharset().name());
	}

	public static String getContent(File file, String enconding) {
		if (file == null || !file.exists()) {
			return null;
		}
		long fileLenLong = file.length();
		if (fileLenLong > Integer.MAX_VALUE) {
			throw new RuntimeException("File too large: " + file);
		}
		int fileLen = (int) fileLenLong;
		try (InputStream is = new BufferedInputStream(new FileInputStream(file));
				ByteArrayOutputStream out = new ByteArrayOutputStream(fileLen)) {
			byte[] tmp = new byte[4096];
			while (true) {
				int r = is.read(tmp);
				if (r == -1)
					break;
				out.write(tmp, 0, r);
			}
			byte[] bytes = out.toByteArray();
			String content = new String(bytes, enconding);
			return content.trim();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void writeToFile(File file, String string, String encoding) {
		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), encoding))) {
				out.print(string);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void deltree(File root) {
		if (root == null || !root.exists()) {
			return;
		}

		if (root.isFile()) {
			root.delete();
			return;
		}

		File[] children = root.listFiles();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				deltree(children[i]);
			}
		}

		root.delete();
	}
}
