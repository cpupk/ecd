/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.i18n;

public class Messages {

	private static final EcdResouceBundle RESOURCE_BUNDLE = new EcdResouceBundle(Messages.class);

	private Messages() {
	}

	public static String getString(String key) {
		return RESOURCE_BUNDLE.getString(key);
	}

	/**
	 * Gets formatted translation for current local
	 * 
	 * @param key the key
	 * @return translated value string
	 */
	public static String getFormattedString(String key, Object[] arguments) {
		return RESOURCE_BUNDLE.getFormattedString(key, arguments);
	}

}
