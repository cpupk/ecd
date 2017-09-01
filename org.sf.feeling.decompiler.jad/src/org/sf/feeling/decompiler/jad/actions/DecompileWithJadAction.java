/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.jad.actions;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.Action;
import org.sf.feeling.decompiler.jad.JadDecompilerPlugin;
import org.sf.feeling.decompiler.jad.i18n.Messages;
import org.sf.feeling.decompiler.util.UIUtil;

public class DecompileWithJadAction extends Action
{

	public DecompileWithJadAction( )
	{
		super( Messages.getString( "JavaDecompilerActionBarContributor.Action.DecompileWithJad" ) ); //$NON-NLS-1$
		this.setImageDescriptor( JadDecompilerPlugin.getImageDescriptor( "icons/jad_16.gif" ) ); //$NON-NLS-1$
	}

	@Override
	public void run( )
	{
		try
		{
			new DecompileWithJadHandler( ).execute( null );
		}
		catch ( ExecutionException e )
		{
		}
	}

	@Override
	public boolean isEnabled( )
	{
		return UIUtil.getActiveEditor( ) != null || UIUtil.getActiveSelection( ) != null;
	}
}