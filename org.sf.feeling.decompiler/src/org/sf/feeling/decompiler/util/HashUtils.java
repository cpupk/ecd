/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

public class HashUtils {

	public static byte[] md5Digest(File file) {
		String md5 = md5Hash(file);
		if (md5 != null) {
			return md5.getBytes();
		}
		return null;
	}

	public static String md5Hash(File file) {
		if (file != null) {
			try (InputStream fis = new FileInputStream(file)) {
				String result = new String(Hex.encodeHex(DigestUtils.md5(fis)));
				return result;
			} catch (IOException e) {
				Logger.debug(e);
			}
		}
		return null;
	}

	public static String sha1Hash(File file) {
		if (file != null) {
			try (InputStream fis = new FileInputStream(file)) {
				return new String(Hex.encodeHex(DigestUtils.sha1(fis)));
			} catch (IOException e) {
				Logger.debug(e);
			}
		}
		return null;
	}

	public static String md5Hash(String string) {
		if (string == null)
			return null;
		byte[] content = string.getBytes();
		if (content != null) {
			String result = new String(Hex.encodeHex(DigestUtils.md5(content)));
			return result;
		}
		return null;
	}

	public static byte[] sha1Digest(File file) {
		String md5 = sha1Hash(file);
		if (md5 != null) {
			return md5.getBytes();
		}
		return null;
	}

}
