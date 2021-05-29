/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.source.attach.finder;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTMLDocument;

public class EclipsePluginSourceByFTPSearchv3Finder extends EclipsePluginSourceByUrlPatternFinder {

	public EclipsePluginSourceByFTPSearchv3Finder() {
		super("http://www.search-ftp.com/lsftp.ashx?s={0}"); //$NON-NLS-1$
	}

	@Override
	protected String getHtmlFromUrl(URL baseUrl, String fileName) throws Exception {
		String html = getString(baseUrl);
		Pattern pattern = Pattern.compile("(?i)" //$NON-NLS-1$
				+ Pattern.compile("<a") //$NON-NLS-1$
				+ ".+" //$NON-NLS-1$
				+ Pattern.compile(fileName) + ".+" //$NON-NLS-1$
				+ Pattern.compile("</a>")); //$NON-NLS-1$
		Matcher matcher = pattern.matcher(html);
		if (matcher.find()) {
			String content = matcher.group();
			content = "http://www.search-ftp.com" //$NON-NLS-1$
					+ content.substring(content.indexOf("\"") + 1, content.lastIndexOf("\"")); //$NON-NLS-1$ //$NON-NLS-2$
			html = getString(new URL(content));
		}
		return html;
	}

	@Override
	protected void addLink(URL baseUrl, String fileName, javax.swing.text.html.HTMLDocument doc,
			HTMLDocument.Iterator aElement, List<String> links) throws Exception {
		SimpleAttributeSet s = (SimpleAttributeSet) aElement.getAttributes();

		String href = (String) s.getAttribute(javax.swing.text.html.HTML.Attribute.HREF);
		if ((href != null) && (href.contains("/lsftp.ashx?is="))) //$NON-NLS-1$
		{
			String text = getText(doc, aElement);
			String absHref = new URL(new URL("ftp://" + text + "/"), fileName).toString(); //$NON-NLS-1$ //$NON-NLS-2$
			links.add(absHref);
		}
	}

	public static void main(String[] args) {
		EclipsePluginSourceByFTPSearchv3Finder finder = new EclipsePluginSourceByFTPSearchv3Finder();
		List<SourceFileResult> results = new ArrayList<SourceFileResult>();
		finder.find("C:\\develop\\eclipse\\plugins\\org.eclipse.jdt.core_3.10.0.v20140902-0626.jar", results); //$NON-NLS-1$
		System.out.println(results.get(0).getSource());
	}
}
