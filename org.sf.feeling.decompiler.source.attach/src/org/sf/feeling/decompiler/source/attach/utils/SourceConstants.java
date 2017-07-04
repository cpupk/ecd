/*******************************************************************************
 * Copyright (c) 2017 Chen Chao(cnfree2000@hotmail.com).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Chen Chao  - initial API and implementation
 *******************************************************************************/

package org.sf.feeling.decompiler.source.attach.utils;

import java.io.File;

import org.sf.feeling.decompiler.JavaDecompilerPlugin;

public class SourceConstants
{

	public static final File SourceAttacherDir = new File( String.valueOf(
			System.getProperty( "user.home" ) ) + File.separatorChar + ".decompiler" + File.separatorChar + "source" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public static final File SourceTempDir = new File(
			JavaDecompilerPlugin.getDefault( ).getPreferenceStore( ).getString( JavaDecompilerPlugin.TEMP_DIR )
					+ File.separatorChar
					+ "source" ); //$NON-NLS-1$

	public static final String SourceAttachPath = SourceAttacherDir.getAbsolutePath( );
	public static final String SourceTempPath = SourceTempDir.getAbsolutePath( );
	public static final String TEMP_SOURCE_PREFIX = "source"; //$NON-NLS-1$

}
