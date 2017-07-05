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
import org.eclipse.swt.widgets.Display;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;
import org.sf.feeling.decompiler.editor.JavaDecompilerClassFileEditor;
import org.sf.feeling.decompiler.i18n.Messages;
import org.sf.feeling.decompiler.util.UIUtil;

public class ByteCodeAction extends Action
{

	public ByteCodeAction( )
	{
		super( Messages.getString( "JavaDecompilerActionBarContributor.Action.ByteCode" ), AS_CHECK_BOX ); //$NON-NLS-1$
	}

	@Override
	public void run( )
	{
		JavaDecompilerPlugin.getDefault( ).setSourceMode(
				!isChecked( ) ? JavaDecompilerPlugin.BYTE_CODE_MODE : JavaDecompilerPlugin.SOURCE_MODE );
		final JavaDecompilerClassFileEditor editor = UIUtil.getActiveEditor( );
		if ( editor != null )
		{
			editor.showSource( );
			editor.notifyPropertiesChange( );
			Display.getDefault( ).asyncExec( new Runnable( ) {

				public void run( )
				{
					editor.setFocus( );
				}
			} );
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
		return JavaDecompilerPlugin.getDefault( ).getSourceMode( ) == JavaDecompilerPlugin.BYTE_CODE_MODE;
	}
}