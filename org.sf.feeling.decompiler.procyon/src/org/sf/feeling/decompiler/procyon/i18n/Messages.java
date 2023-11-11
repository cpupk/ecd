
package org.sf.feeling.decompiler.procyon.i18n;

import org.sf.feeling.decompiler.i18n.EcdResouceBundle;

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
