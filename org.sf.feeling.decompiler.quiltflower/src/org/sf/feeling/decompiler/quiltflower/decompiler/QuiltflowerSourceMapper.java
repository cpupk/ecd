/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.quiltflower.decompiler;

import org.eclipse.core.runtime.Path;
import org.sf.feeling.decompiler.editor.BaseDecompilerSourceMapper;
import org.sf.feeling.decompiler.quiltflower.QuiltflowerDecompilerPlugin;

public class QuiltflowerSourceMapper extends BaseDecompilerSourceMapper {

	public QuiltflowerSourceMapper() {
		super(new Path("."), ""); //$NON-NLS-1$ //$NON-NLS-2$
		origionalDecompiler = new QuiltflowerDecompiler();
	}

	@Override
	protected String getDecompilerName() {
		return QuiltflowerDecompilerPlugin.decompilerType;
	}

	@Override
	protected String getDecompilerVersion() {
		return QuiltflowerDecompilerPlugin.decompilerVersion;
	}

}