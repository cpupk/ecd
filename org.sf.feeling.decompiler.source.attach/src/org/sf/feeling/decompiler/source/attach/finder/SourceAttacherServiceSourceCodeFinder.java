/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.source.attach.finder;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.sf.feeling.decompiler.source.attach.utils.SourceAttachUtil;
import org.sf.feeling.decompiler.source.attach.utils.SourceBindingUtil;
import org.sf.feeling.decompiler.source.attach.utils.UrlDownloader;
import org.sf.feeling.decompiler.util.HashUtils;
import org.sf.feeling.decompiler.util.Logger;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

public class SourceAttacherServiceSourceCodeFinder extends AbstractSourceCodeFinder implements SourceCodeFinder {

	public static final String SERVICE = "http://javasourceattacher2.appspot.com"; //$NON-NLS-1$
	private boolean canceled = false;

	@Override
	public String toString() {
		return getClass().toString();
	}

	@Override
	public void cancel() {
		this.canceled = true;
	}

	@Override
	public void find(String binFile, List<SourceFileResult> results) {
		File bin = new File(binFile);
		String url = null;
		String fileDownloaded = null;
		try {
			if (this.canceled)
				return;
			InputStream is2 = null;
			URLConnection conn = null;
			try {
				String md5 = HashUtils.md5Hash(bin);
				String serviceUrl = "http://javasourceattacher2.appspot.com/rest/libraries?md5=" + md5; //$NON-NLS-1$
				conn = new URL(serviceUrl).openConnection();
				conn.setConnectTimeout(5000);
				conn.setReadTimeout(5000);
				conn.setReadTimeout(5000);
				is2 = conn.getInputStream();
				String str = IOUtils.toString(is2);
				JsonArray json = Json.parse(str).asArray();

				for (int i = 0; i < json.size(); i++) {
					if (this.canceled)
						return;
					JsonObject obj = json.get(i).asObject();
					JsonObject source = obj.get("source").asObject(); //$NON-NLS-1$
					if ((source != null) && (!source.isNull())) {
						JsonArray ar = source.get("urls").asArray(); //$NON-NLS-1$
						if ((ar != null) && (!ar.isEmpty())) {
							String url1 = ar.get(0).asString();
							String[] sourceFiles = SourceBindingUtil.getSourceFileByDownloadUrl(url1);
							if (sourceFiles != null && sourceFiles[0] != null && new File(sourceFiles[0]).exists()) {
								File sourceFile = new File(sourceFiles[0]);
								File tempFile = new File(sourceFiles[1]);
								SourceFileResult result = new SourceFileResult(this, bin.getAbsolutePath(), sourceFile,
										tempFile, 100);
								results.add(result);
								return;
							}
						}
					}
				}

				for (int i = 0; i < json.size(); i++) {
					if (this.canceled)
						return;
					JsonObject obj = json.get(i).asObject();
					JsonObject source = obj.get("source").asObject(); //$NON-NLS-1$
					if ((source != null) && (!source.isNull())) {
						JsonArray ar = source.get("urls").asArray(); //$NON-NLS-1$
						if ((ar != null) && (!ar.isEmpty())) {
							String url1 = ar.get(0).asString();
							String tmpFile = new UrlDownloader().download(url1);
							if (tmpFile != null && new File(tmpFile).exists()
									&& SourceAttachUtil.isSourceCodeFor(tmpFile, bin.getAbsolutePath())) {
								setDownloadUrl(url);
								fileDownloaded = tmpFile;
								url = url1;
								break;
							}
						}
					}
				}

				if ((url != null) && (fileDownloaded != null)) {
					String name = url.substring(url.lastIndexOf('/') + 1);

					SourceFileResult object = new SourceFileResult(this, binFile, fileDownloaded, name, 90);
					Logger.debug(toString() + " FOUND: " + object, null); //$NON-NLS-1$
					results.add(object);
				}
			} finally {
				IOUtils.closeQuietly(is2);
			}
			IOUtils.closeQuietly(is2);
		} catch (Throwable e) {
			Logger.debug(e);
		}
	}

	public static void main(String[] args) {
		SourceAttacherServiceSourceCodeFinder finder = new SourceAttacherServiceSourceCodeFinder();
		List<SourceFileResult> results = new ArrayList<SourceFileResult>();
		finder.find("C:\\Users\\Feeling\\.m2\\repository\\ant\\ant\\1.6.5\\ant-1.6.5.jar", results); //$NON-NLS-1$
		System.out.println(results.get(0).getSource());
	}
}
