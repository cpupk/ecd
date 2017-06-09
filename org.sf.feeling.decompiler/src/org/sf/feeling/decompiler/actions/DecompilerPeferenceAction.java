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
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.sf.feeling.decompiler.editor.JavaDecompilerClassFileEditor;
import org.sf.feeling.decompiler.i18n.Messages;
import org.sf.feeling.decompiler.util.UIUtil;

public class DecompilerPeferenceAction extends Action
{

	public DecompilerPeferenceAction( )
	{
		super( Messages.getString( "JavaDecompilerActionBarContributor.Action.Preferences" ) ); //$NON-NLS-1$
	}

	@Override
	public void run( )
	{
		JavaDecompilerClassFileEditor editor = UIUtil.getActiveDecompilerEditor( );
		if ( editor != null )
		{
			PreferencesUtil
					.createPreferenceDialogOn( Display.getDefault( ).getActiveShell( ),
							"org.sf.feeling.decompiler.Main", //$NON-NLS-1$
							editor.collectContextMenuPreferencePages( ),
							null )
					.open( );
		}
		else
		{
			PreferencesUtil.createPreferenceDialogOn( Display.getDefault( ).getActiveShell( ),
					"org.sf.feeling.decompiler.Main", //$NON-NLS-1$
					new String[]{
							"org.sf.feeling.decompiler.Main" //$NON-NLS-1$
					},
					null ).open( );
		}
	}

	@Override
	public boolean isEnabled( )
	{
		return true;
	}
}