/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.source.attach.finder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.sf.feeling.decompiler.source.attach.utils.SourceAttachUtil;
import org.sf.feeling.decompiler.source.attach.utils.SourceBindingUtil;
import org.sf.feeling.decompiler.source.attach.utils.UrlDownloader;
import org.sf.feeling.decompiler.util.HashUtils;
import org.sf.feeling.decompiler.util.Logger;

public class GrepCodeSourceCodeFinder extends AbstractSourceCodeFinder {

	protected boolean canceled = false;

	@Override
	public void cancel() {
		this.canceled = true;
	}

	@Override
	public void find(String binFile, List<SourceFileResult> results) {
		try {
			String md5 = HashUtils.md5Hash(new File(binFile));
			String srcUrl = "http://grepcode.com/snapshot/" + md5 + "?rel=file&kind=source&n=0"; //$NON-NLS-1$ //$NON-NLS-2$

			String[] sourceFiles = SourceBindingUtil.getSourceFileByDownloadUrl(srcUrl);
			if (sourceFiles != null && sourceFiles[0] != null && new File(sourceFiles[0]).exists()) {
				File sourceFile = new File(sourceFiles[0]);
				File tempFile = new File(sourceFiles[1]);
				SourceFileResult result = new SourceFileResult(this, binFile, sourceFile, tempFile, 100);
				results.add(result);
				return;
			}

			String tmpFile = new UrlDownloader().download(srcUrl);
			if (tmpFile != null && new File(tmpFile).exists() && SourceAttachUtil.isSourceCodeFor(tmpFile, binFile)) {
				setDownloadUrl(srcUrl);
				String name = FilenameUtils.getBaseName(binFile) + "-sources.jar"; //$NON-NLS-1$
				SourceFileResult object = new SourceFileResult(this, binFile, tmpFile, name, 50);
				Logger.debug(toString() + " FOUND: " + object, null); //$NON-NLS-1$
				results.add(object);
			}
		} catch (Throwable e) {
			Logger.debug(e);
		}
	}

	public static void main(String[] args) {
		GrepCodeSourceCodeFinder finder = new GrepCodeSourceCodeFinder();
		List<SourceFileResult> results = new ArrayList<SourceFileResult>();
		finder.find("C:\\Users\\Feeling\\.m2\\repository\\ant\\ant\\1.6.5\\ant-1.6.5.jar", results); //$NON-NLS-1$
		System.out.println(results.get(0).getSource());
	}
}