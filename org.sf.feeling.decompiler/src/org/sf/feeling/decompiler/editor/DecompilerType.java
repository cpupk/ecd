/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.editor;

import org.sf.feeling.decompiler.JavaDecompilerPlugin;
import org.sf.feeling.decompiler.fernflower.FernFlowerDecompiler;

public class DecompilerType {

	public static final String FernFlower = FernFlowerDecompiler.decompilerType;

	private static String[] decompilerTypes = null;

	public static String[] getDecompilerTypes() {
		if (decompilerTypes == null) {
			decompilerTypes = JavaDecompilerPlugin.getDefault().getDecompilerDescriptorTypes();
		}
		return decompilerTypes;
	}
}
