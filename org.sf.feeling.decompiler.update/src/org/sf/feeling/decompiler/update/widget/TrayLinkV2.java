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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WindowTrimProxy;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.layout.TrimLayout;
import org.sf.feeling.decompiler.util.ReflectionUtils;

@SuppressWarnings("restriction")
public class TrayLinkV2
{

	private static HtmlLinkTrimItem trayLink = null;

	private static WindowTrimProxy trayLinkTrim = null;

	public static void displayTrayLink( boolean show )
	{

		if ( trayLink == null )
		{
			trayLink = new HtmlLinkTrimItem( Display.getDefault( ).getActiveShell( ) );
			trayLinkTrim = new WindowTrimProxy( trayLink,
					"org.sf.feeling.decompiler.update.widget.HtmlLinkTrimItem", //$NON-NLS-1$
					"Tray Link", //$NON-NLS-1$
					SWT.BOTTOM | SWT.TOP ) {

				public void handleClose( )
				{
					getControl( ).dispose( );
				}

				public boolean isCloseable( )
				{
					return true;
				}
			};
		}

		if ( trayLink != null )
		{
			if ( show )
			{
				WorkbenchWindow window = (WorkbenchWindow) PlatformUI.getWorkbench( ).getActiveWorkbenchWindow( );

				if ( trayLink.getLayoutData( ) == null )
				{
					trayLinkTrim.setWidthHint( trayLink.computeSize( SWT.DEFAULT, SWT.DEFAULT ).x );
					trayLinkTrim.setHeightHint(
							window.getStatusLineManager( ).getControl( ).computeSize( SWT.DEFAULT, SWT.DEFAULT ).y );
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
				WorkbenchWindow window = (WorkbenchWindow) PlatformUI.getWorkbench( ).getActiveWorkbenchWindow( );
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
