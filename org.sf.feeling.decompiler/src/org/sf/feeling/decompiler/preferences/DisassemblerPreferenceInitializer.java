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

package org.sf.feeling.decompiler.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;

public class DisassemblerPreferenceInitializer extends AbstractPreferenceInitializer implements
		DisassemblerPreferenceConstats
{

	@Override
	public void initializeDefaultPreferences( )
	{
		IPreferenceStore store = JavaDecompilerPlugin.getDefault( ).getPreferenceStore( );

		PreferenceConverter.setDefault( store, BYTECODE_MNEMONIC, new RGB( 0, 0, 0 ) );
		store.setDefault( BYTECODE_MNEMONIC_BOLD, true );
		store.setDefault( BYTECODE_MNEMONIC_ITALIC, false );
		store.setDefault( BYTECODE_MNEMONIC_STRIKETHROUGH, false );
		store.setDefault( BYTECODE_MNEMONIC_UNDERLINE, false );
	}

}
