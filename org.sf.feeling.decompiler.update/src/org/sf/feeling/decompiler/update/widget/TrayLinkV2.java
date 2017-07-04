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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WindowTrimProxy;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.layout.TrimLayout;
import org.sf.feeling.decompiler.util.ReflectionUtils;

public class TrayLinkV2
{

	private static Map<IWorkbenchWindow, HtmlLinkTrimItem> trayLinks = new ConcurrentHashMap<IWorkbenchWindow, HtmlLinkTrimItem>( );
	private static Map<IWorkbenchWindow, WindowTrimProxy> trayLinkTrims = new ConcurrentHashMap<IWorkbenchWindow, WindowTrimProxy>( );

	public static void displayTrayLink( IWorkbenchWindow window, boolean show )
	{
		if ( window == null )
			return;

		if ( !trayLinks.containsKey( window ) )
		{
			HtmlLinkTrimItem trayLink = new HtmlLinkTrimItem( window.getShell( ) );
			WindowTrimProxy trayLinkTrim = new WindowTrimProxy( trayLink,
					"org.sf.feeling.decompiler.update.widget.HtmlLinkTrimItem", //$NON-NLS-1$
					"Tray Link", //$NON-NLS-1$
					SWT.BOTTOM | SWT.TOP ) {

				@Override
				public void handleClose( )
				{
					getControl( ).dispose( );
				}

				@Override
				public boolean isCloseable( )
				{
					return true;
				}
			};
			trayLinks.put( window, trayLink );
			trayLinkTrims.put( window, trayLinkTrim );
		}

		if ( trayLinks.containsKey( window ) )
		{
			HtmlLinkTrimItem trayLink = trayLinks.get( window );
			WindowTrimProxy trayLinkTrim = trayLinkTrims.get( window );
			if ( show )
			{
				if ( trayLink.getLayoutData( ) == null )
				{
					trayLinkTrim.setWidthHint( trayLink.computeSize( SWT.DEFAULT, SWT.DEFAULT ).x );
					trayLinkTrim.setHeightHint(
							( (WorkbenchWindow) window ).getStatusLineManager( ).getControl( ).computeSize( SWT.DEFAULT,
									SWT.DEFAULT ).y );
				}

				TrimLayout defaultLayout = (TrimLayout) ReflectionUtils.getFieldValue( window, "defaultLayout" ); //$NON-NLS-1$
				if ( defaultLayout != null )
				{
					if ( defaultLayout.getTrim( trayLinkTrim.getId( ) ) == null )
					{
						defaultLayout.addTrim( SWT.BOTTOM, trayLinkTrim );
					}
					trayLink.setVisible( true );
				}
			}
			else
			{
				TrimLayout defaultLayout = (TrimLayout) ReflectionUtils.getFieldValue( window, "defaultLayout" ); //$NON-NLS-1$
				if ( defaultLayout != null )
				{
					defaultLayout.removeTrim( trayLinkTrim );
					trayLink.setVisible( false );;
				}
			}
		}
	}
}
