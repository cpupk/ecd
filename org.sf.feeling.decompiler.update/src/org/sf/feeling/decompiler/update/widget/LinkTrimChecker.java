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

package org.sf.feeling.decompiler.update.widget;

import org.sf.feeling.decompiler.util.Logger;

public class LinkTrimChecker
{

	public void displayTrayLink( boolean show )
	{
		if ( supportV1( ) )
		{
			TrayLinkV1.displayTrayLink( show );
		}
		else if ( supportV2( ) )
		{
			TrayLinkV2.displayTrayLink( show );
		}
	}

	private boolean supportV1( )
	{
		try
		{
			Class.forName( "org.eclipse.e4.ui.model.application.ui.menu.MToolControl" ); //$NON-NLS-1$
			return true;
		}
		catch ( Exception e )
		{
			Logger.debug( e );
		}
		return false;
	}

	private boolean supportV2( )
	{
		try
		{
			Class.forName( "org.eclipse.ui.internal.WindowTrimProxy" ); //$NON-NLS-1$
			return true;
		}
		catch ( Exception e )
		{
			Logger.debug( e );
		}
		return false;
	}
}
