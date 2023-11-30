/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.i18n;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class EcdResouceBundle {

	protected final ResourceBundle resourceBundle;

	public EcdResouceBundle(Class<?> messagesClass) {
		ClassLoader classLoader = messagesClass.getClassLoader();
		Locale targetLocale = Locale.getDefault();
		String resourceBundleName = messagesClass.getPackageName() + ".messages";
		this.resourceBundle = ResourceBundle.getBundle(resourceBundleName, targetLocale, classLoader);
	}

	public String getString(String key) {
		try {
			String result = resourceBundle.getString(key);
			try {
				result = new String(result.getBytes("ISO-8859-1"), "utf-8"); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (UnsupportedEncodingException e) {
				return '!' + key + '!';
			}
			return result;
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * Gets formatted translation for current local
	 * 
	 * @param key the key
	 * @return translated value string
	 */
	public String getFormattedString(String key, Object[] arguments) {
		return MessageFormat.format(getString(key), arguments);
	}
}
