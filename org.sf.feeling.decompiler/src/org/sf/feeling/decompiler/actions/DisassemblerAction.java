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

package org.sf.feeling.decompiler.actions;

import org.eclipse.jface.action.Action;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;
import org.sf.feeling.decompiler.editor.JavaDecompilerClassFileEditor;
import org.sf.feeling.decompiler.i18n.Messages;
import org.sf.feeling.decompiler.util.UIUtil;

public class DisassemblerAction extends Action
{

	public DisassemblerAction( )
	{
		super( Messages.getString( "JavaDecompilerActionBarContributor.Action.Disassembler" ), AS_CHECK_BOX ); //$NON-NLS-1$
	}

	@Override
	public void run( )
	{
		JavaDecompilerPlugin.getDefault( ).setSourceMode(
				!isChecked( ) ? JavaDecompilerPlugin.DISASSEMBLER_MODE : JavaDecompilerPlugin.SOURCE_MODE );
		JavaDecompilerClassFileEditor editor = UIUtil.getActiveEditor( );
		if ( editor != null )
		{
			editor.showSource( );
			editor.setFocus( );
			editor.notifyPropertiesChange( );
		}
	}

	@Override
	public boolean isEnabled( )
	{
		JavaDecompilerClassFileEditor editor = UIUtil.getActiveEditor( );
		return editor != null;
	}

	public boolean isChecked( )
	{
		return JavaDecompilerPlugin.getDefault( ).getSourceMode( ) == JavaDecompilerPlugin.DISASSEMBLER_MODE;
	}
}