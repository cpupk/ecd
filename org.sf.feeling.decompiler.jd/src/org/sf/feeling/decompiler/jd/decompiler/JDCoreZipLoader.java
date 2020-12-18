/*******************************************************************************
 * Copyright (c) 2020 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.jd.decompiler;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.api.loader.LoaderException;

/**
 * 
 * JD-Core Loader implementation for decompiling classes directly from within a
 * ZIP/JAR archive
 * 
 * @author Jan Peter Stotz
 *
 */
public class JDCoreZipLoader implements Loader, Closeable {

	private final ZipFile zipFile;

	/**
	 * Lookup table for all extracted class files: maps class name without .class
	 * extension to it's ZipEntry
	 */
	private final Map<String, ZipEntry> entriesMap = new TreeMap<>();

	public JDCoreZipLoader(Path zipFilePath) throws ZipException, IOException {
		super();
		zipFile = new ZipFile(zipFilePath.toFile());
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			String name = entry.getName();
			if (name.startsWith("/")) {
				name = name.substring(1);
			}
			if (name.endsWith(".class")) {
				name = name.substring(0, name.length() - 6);
			}
			entriesMap.put(name, entry);
		}
	}

	@Override
	public boolean canLoad(String internalName) {
		boolean result = entriesMap.containsKey(internalName);
		return result;
	}

	@Override
	public byte[] load(String internalName) throws LoaderException {
		ZipEntry entry = entriesMap.get(internalName);
		if (entry == null) {
			return null;
		}
		byte[] buffer = new byte[8 * 1024];
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			try (InputStream in = zipFile.getInputStream(entry)) {
				int read;
				while ((read = in.read(buffer)) >= 0) {
					out.write(buffer, 0, read);
				}
				return out.toByteArray();
			}
		} catch (IOException e) {
			throw new LoaderException(e);
		}
	}

	@Override
	public void close() throws IOException {
		zipFile.close();
	}

}
