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

public class DebugModeAction extends Action
{

	public DebugModeAction( )
	{
		super( Messages.getString( "DebugModeAction.Action.Text" ), //$NON-NLS-1$
				AS_CHECK_BOX );
	}

	@Override
	public void run( )
	{
		JavaDecompilerPlugin.getDefault( ).setDebugMode( !isChecked( ) );
		new DecompileAction( ).run( );
		JavaDecompilerClassFileEditor editor = UIUtil.getActiveEditor( );
		if ( editor != null )
		{
			editor.setFocus( );
			editor.notifyPropertiesChange( );
		}
	}

	@Override
	public boolean isChecked( )
	{
		return JavaDecompilerPlugin.getDefault( ).isDebugMode( );
	}

}