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

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.TrimUtil;
import org.sf.feeling.decompiler.update.util.TrayLinkUtil;
import org.sf.feeling.decompiler.util.Logger;
import org.sf.feeling.decompiler.util.UIUtil;

@SuppressWarnings("restriction")
public class HtmlLinkTrimItem extends Composite
{

	private double width, height;
	private Browser browser;
	private boolean isUseExternalBrowser = true;

	private Composite container;
	private ScrolledComposite sComposite;

	static class CustomFunction extends BrowserFunction
	{

		CustomFunction( Browser browser, String name )
		{
			super( browser, name );
		}

		public Object function( Object[] arguments )
		{
			if ( arguments != null && arguments.length > 0 && arguments[0] != null )
			{
				UIUtil.openBrowser( arguments[0].toString( ) );
			}
			return super.function( arguments );
		}
	}

	public HtmlLinkTrimItem( Composite parent )
	{
		super( parent, SWT.NONE );
		this.setBackgroundMode( SWT.INHERIT_FORCE );

		GridLayout trimLayout = new GridLayout( );
		trimLayout.marginTop = 1;

		this.setLayout( trimLayout );

		sComposite = new ScrolledComposite( this, SWT.NONE );
		sComposite.setBackgroundMode( SWT.INHERIT_FORCE );
		sComposite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
		sComposite.setExpandHorizontal( true );
		sComposite.setExpandVertical( true );
		sComposite.setAlwaysShowScrollBars( false );

		container = new Composite( sComposite, SWT.NONE );
		container.setBackgroundMode( SWT.INHERIT_FORCE );
		sComposite.setContent( container );
		GridLayout layout = new GridLayout( );
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		container.setLayout( layout );

		browser = new Browser( container, SWT.NONE );
		browser.setVisible( false );
		browser.setBackgroundMode( SWT.INHERIT_FORCE );

		updateTrimUrl( );

		new CustomFunction( browser, "gotoUrl" ); //$NON-NLS-1$

		final ProgressListener[] listeners = new ProgressListener[1];
		ProgressListener listener = new ProgressListener( ) {

			public void completed( ProgressEvent event )
			{
				handleEvent( );
			}

			private void handleEvent( )
			{

				updateBrowserColor( );
				updateBrowserFontColor( );
				updateBrowserFontFamily( );
				updateBrowserFontSize( );

				Display.getDefault( ).asyncExec( new Runnable( ) {

					public void run( )
					{
						try
						{
							Object[] area = (Object[]) browser.evaluate( "return getContentArea();" ); //$NON-NLS-1$

							double tempWidth = Double.valueOf( area[0].toString( ) );
							double tempHeight = Double.valueOf( area[1].toString( ) );
							if ( tempWidth > 0 && tempHeight > 0 && ( tempWidth != width || tempHeight != height ) )
							{
								width = tempWidth;
								height = tempHeight;
								HtmlLinkTrimItem.this.pack( );
								HtmlLinkTrimItem.this.setSize( computeSize( -1, -1, true ) );
								HtmlLinkTrimItem.this.layout( true, true );
								HtmlLinkTrimItem.this.getParent( ).layout( true, true );
								if ( HtmlLinkTrimItem.this.getParent( ).getParent( ) != null )
								{
									HtmlLinkTrimItem.this.getParent( ).getParent( ).layout( true, true );
									if ( HtmlLinkTrimItem.this.getParent( ).getParent( ).getParent( ) != null )
									{
										HtmlLinkTrimItem.this.getParent( ).getParent( ).getParent( ).layout( true,
												true );
									}
								}
								registerLinkClickListener( );
								browser.setVisible( true );
							}
						}
						catch ( Exception e )
						{
							browser.setVisible( false );
							Logger.debug( e );
						}
					}
				} );
			}

			public void changed( ProgressEvent event )
			{
				handleEvent( );
			}
		};
		listeners[0] = listener;
		browser.addProgressListener( listener );

		GridData gd = new GridData( );
		gd.heightHint = getDisplay( ).getBounds( ).height;
		gd.widthHint = getDisplay( ).getBounds( ).width;
		browser.setLayoutData( gd );

		sComposite.addControlListener( new ControlAdapter( ) {

			private void computeSize( )
			{
				sComposite.setMinSize( container.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
				container.layout( );
			}

			public void controlResized( ControlEvent e )
			{
				computeSize( );
			}
		} );
		container.layout( );
	}

	private void updateTrimUrl( )
	{
		if ( browser == null || browser.isDisposed( ) )
			return;

		final String url = TrayLinkUtil.getTrayUrl( );
		if ( url == null )
		{
			return;
		}

		Integer time = TrayLinkUtil.getTrayUrlDisplayTime( url );

		if ( time == null )
		{
			time = 10;
		}

		if ( UIUtil.isWin32( ) )
		{
			isUseExternalBrowser = TrayLinkUtil.isUseExternalBrowser( url );
		}
		else
		{
			isUseExternalBrowser = true;
		}
		if ( !url.equals( browser.getUrl( ) ) )
		{
			ExecutorService poll = Executors.newFixedThreadPool( 1 );
			poll.submit( new Callable<Boolean>( ) {

				public Boolean call( ) throws Exception
				{
					Socket socket = new Socket( );
					try
					{
						socket.connect( new InetSocketAddress( new URL( url ).getHost( ), 80 ), 10000 );
						HtmlLinkTrimItem.this.getDisplay( ).asyncExec( new Runnable( ) {

							public void run( )
							{
								browser.setData( "linkClick", false ); //$NON-NLS-1$
								browser.setVisible( false );
								browser.setUrl( url );
							}
						} );
						return true;
					}
					catch ( Exception e )
					{
						Logger.debug( e );
						return false;
					}
					finally
					{
						socket.close( );
					}
				}
			} );
		}

		this.getDisplay( ).timerExec( time * 60 * 1000, new Runnable( ) {

			public void run( )
			{
				updateTrimUrl( );
			}
		} );
	}

	@Override
	public Point computeSize( int wHint, int hHint, boolean changed )
	{
		int trimWidth = (int) width + 5 * 2 + 4;
		int trimHeight = (int) height + 5 * 2 + 4;
		trimHeight = Math.max( TrimUtil.TRIM_DEFAULT_HEIGHT, trimHeight );
		return new Point( trimWidth, trimHeight );
	}

	private void updateBrowserColor( )
	{
		try
		{
			Color color = getBackground( );
			String script = "return eval('document.body.style.background=\"rgb(" //$NON-NLS-1$
					+ color.getRed( )
					+ "," //$NON-NLS-1$
					+ color.getGreen( )
					+ "," //$NON-NLS-1$
					+ color.getBlue( )
					+ ")\"');"; //$NON-NLS-1$
			browser.evaluate( script );
		}
		catch ( Exception e1 )
		{
			Logger.debug( e1 );
		}
	}

	/**
	 * On Windows: points(SWT) = points(browser) != pixels(SWT) =
	 * pixels(browser) </br>
	 * On Mac: points(SWT) = pixels(SWT) = pixels(browser) != points(browser)
	 */
	protected void updateBrowserFontSize( )
	{
		try
		{
			int fontHeight = HtmlLinkTrimItem.this.getFont( ).getFontData( )[0].getHeight( );
			String script = "return eval('document.getElementById(\"link\").style.fontSize=\"" //$NON-NLS-1$
					+ fontHeight
					+ ( UIUtil.isMacOS( ) ? "px" : "pt" ) //$NON-NLS-1$ //$NON-NLS-2$
					+ "\"');"; //$NON-NLS-1$
			browser.evaluate( script );
		}
		catch ( Exception e1 )
		{
			Logger.debug( e1 );
		}
	}

	protected void updateBrowserFontFamily( )
	{
		try
		{
			String fontFamily = HtmlLinkTrimItem.this.getFont( ).getFontData( )[0].getName( );
			if ( UIUtil.isMacOS( ) )
			{
				fontFamily = "sans-serif"; //$NON-NLS-1$
			}
			String script = "return eval('document.getElementById(\"link\").style.fontFamily=\"" + fontFamily + "\"');"; //$NON-NLS-1$ //$NON-NLS-2$
			browser.evaluate( script );
		}
		catch ( Exception e1 )
		{
			Logger.debug( e1 );
		}
	}

	protected void updateBrowserFontColor( )
	{
		try
		{
			Color color = HtmlLinkTrimItem.this.getParent( ).getForeground( );
			if ( color.equals( getDisplay( ).getSystemColor( SWT.COLOR_WIDGET_FOREGROUND ) ) )
			{
				color = getDisplay( ).getSystemColor( SWT.COLOR_BLUE );
			}
			String script = "return eval('document.getElementById(\"link\").style.color=\"rgb(" //$NON-NLS-1$
					+ color.getRed( )
					+ "," //$NON-NLS-1$
					+ color.getGreen( )
					+ "," //$NON-NLS-1$
					+ color.getBlue( )
					+ ")\"');"; //$NON-NLS-1$
			browser.evaluate( script );
		}
		catch ( Exception e1 )
		{
			Logger.debug( e1 );
		}
	}

	private void registerLinkClickListener( ) throws Exception
	{
		if ( isUseExternalBrowser )
		{
			if ( !Boolean.TRUE.equals( browser.getData( "linkClick" ) ) ) //$NON-NLS-1$
			{
				if ( browser.execute(
						"$('#link').click( function(e) {e.preventDefault(); gotoUrl(this.href); return false; } );" ) ) //$NON-NLS-1$
				{
					browser.setData( "linkClick", true ); //$NON-NLS-1$
				}
			}
		}
	}
}
