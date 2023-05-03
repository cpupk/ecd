/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.jd.decompiler;

import java.io.File;
import java.util.Collections;

import org.eclipse.core.runtime.Path;
import org.sf.feeling.decompiler.jd.JDCoreDecompilerPlugin;

import jd.ide.eclipse.editors.JDSourceMapper;

public class JDCoreSourceMapper extends JDSourceMapper {

	public JDCoreSourceMapper() {
		super(new File("."), new Path("."), "", Collections.<String, String>emptyMap()); //$NON-NLS-1$ //$NON-NLS-2$
		origionalDecompiler = new JDCoreDecompiler(this);
	}

	@Override
	protected String getDecompilerName() {
		return JDCoreDecompilerPlugin.decompilerType;
	}

	@Override
	protected String getDecompilerVersion() {
		return JDCoreDecompilerPlugin.decompilerVersion;
	}

}
