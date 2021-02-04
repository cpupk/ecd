/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.source.attach.finder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.text.EditorKit;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.sf.feeling.decompiler.source.attach.utils.SourceAttachUtil;
import org.sf.feeling.decompiler.source.attach.utils.SourceBindingUtil;
import org.sf.feeling.decompiler.source.attach.utils.UrlDownloader;
import org.sf.feeling.decompiler.util.FileUtil;
import org.sf.feeling.decompiler.util.FileUtil.Filter;
import org.sf.feeling.decompiler.util.Logger;

public class JreSourceCodeFinder extends AbstractSourceCodeFinder {

	private boolean canceled = false;

	@Override
	public void cancel() {
		this.canceled = true;
	}

	@Override
	public void find(String binFile, List<SourceFileResult> results) {
		try {
			String[] metaInfo = findMetaInfoFromFile(binFile);
			if ((metaInfo == null) || (!"Java Runtime Environment".equals(metaInfo[0]))) //$NON-NLS-1$
				return;
			String version = metaInfo[1];
			String lookup = null;
			String linkText = null;
			if (version.startsWith("1.7.0")) //$NON-NLS-1$
			{
				lookup = "http://hg.openjdk.java.net/jdk7u/jdk7u/jdk/tags"; //$NON-NLS-1$
				String u = "u" + version.substring(6); //$NON-NLS-1$
				linkText = "jdk7" + u + "-"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (version.startsWith("1.8.0")) //$NON-NLS-1$
			{
				lookup = "http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/tags"; //$NON-NLS-1$
				String u = "u" + version.substring(6); //$NON-NLS-1$
				linkText = "jdk8" + u + "-"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (lookup != null) {
				URL baseUrl = new URL(lookup);
				String html = getString(baseUrl);

				EditorKit kit = new HTMLEditorKit();
				HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
				doc.putProperty("IgnoreCharsetDirective", new Boolean(true)); //$NON-NLS-1$
				Reader reader = new java.io.StringReader(html);
				kit.read(reader, doc, 0);

				HTMLDocument.Iterator it = doc.getIterator(HTML.Tag.A);
				String srcUrl = null;
				while (it.isValid()) {
					SimpleAttributeSet s = (SimpleAttributeSet) it.getAttributes();
					String href = (String) s.getAttribute(HTML.Attribute.HREF);
					if ((href != null) && (href.contains("/rev/"))) //$NON-NLS-1$
					{
						String text = getText(doc, it).trim();
						if (text.startsWith(linkText)) {

							srcUrl = new URL(baseUrl, href.replace("/rev/", "/archive/") + ".zip").toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							break;
						}
					}
					it.next();
				}
				if ((srcUrl != null) && (!this.canceled)) {
					String[] sourceFiles = SourceBindingUtil.getSourceFileByDownloadUrl(srcUrl);
					if (sourceFiles != null && sourceFiles[0] != null && new File(sourceFiles[0]).exists()) {
						File sourceFile = new File(sourceFiles[0]);
						File tempFile = new File(sourceFiles[1]);
						SourceFileResult result = new SourceFileResult(this, binFile, sourceFile, tempFile, 100);
						results.add(result);
						return;
					}

					String tmpFile = new UrlDownloader().download(srcUrl);

					FileUtil.filterZipFile(tmpFile, new Filter() {

						@Override
						public boolean accept(String fileName) {
							return fileName.endsWith(".java") && fileName.indexOf("src/share/classes") != -1; //$NON-NLS-1$ //$NON-NLS-2$
						}
					});

					if (tmpFile != null && new File(tmpFile).exists()
							&& SourceAttachUtil.isSourceCodeFor(tmpFile, binFile)) {
						setDownloadUrl(srcUrl);
						String name = srcUrl.substring(srcUrl.lastIndexOf('/') + 1);
						name = "jre_" + name; //$NON-NLS-1$
						SourceFileResult object = new SourceFileResult(this, binFile, tmpFile, name, 50);
						Logger.debug(toString() + " FOUND: " + object, null); //$NON-NLS-1$
						results.add(object);
					}
				}
			}
		} catch (Exception e) {
			Logger.debug(e);
		}
	}

	protected String[] findMetaInfoFromFile(String binFile) throws Exception {
		String[] result = null;

		try (ZipInputStream in = new ZipInputStream(new FileInputStream(binFile))) {
			byte[] data = new byte[2048];
			String zipEntryName;
			do {
				ZipEntry entry = in.getNextEntry();
				if (entry == null) {
					break;
				}

				zipEntryName = entry.getName();
			} while (!zipEntryName.equals("META-INF/MANIFEST.MF")); //$NON-NLS-1$
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			for (;;) {
				int read = in.read(data);
				if (read < 0)
					break;
				os.write(data, 0, read);
			}
			Properties props = new Properties();
			props.load(new ByteArrayInputStream(os.toByteArray()));
			String title = props.getProperty("Implementation-Title"); //$NON-NLS-1$
			String version = props.getProperty("Implementation-Version"); //$NON-NLS-1$
			result = new String[] { title, version };
		}
		return result;
	}

	public static void main(String[] args) {
		JreSourceCodeFinder finder = new JreSourceCodeFinder();
		List<SourceFileResult> results = new ArrayList<SourceFileResult>();
		finder.find("C:\\develop\\jdk1.6\\jre\\lib\\deploy.jar", results); //$NON-NLS-1$
		System.out.println(results.get(0).getSource());
	}
}
