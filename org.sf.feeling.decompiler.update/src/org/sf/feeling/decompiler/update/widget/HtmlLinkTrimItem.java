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
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.TrimUtil;
import org.eclipse.ui.themes.ColorUtil;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;
import org.sf.feeling.decompiler.update.util.ExecutorUtil;
import org.sf.feeling.decompiler.update.util.TrayLinkUtil;
import org.sf.feeling.decompiler.util.Logger;
import org.sf.feeling.decompiler.util.ReflectionUtils;
import org.sf.feeling.decompiler.util.UIUtil;

@SuppressWarnings("restriction")
public class HtmlLinkTrimItem extends Composite
{

	private double width, height;
	private Browser browser;
	private volatile String browserUrl;
	private volatile boolean isDisposed = false;
	private boolean isUseExternalBrowser = true;

	private Composite container;
	private ScrolledComposite sComposite;
	private String trayLinkUrl;
	private long lastUpdateTime = -1L;

	static class CustomFunction extends BrowserFunction
	{

		private String funcName;
		private HtmlLinkTrimItem linkItem;

		CustomFunction( HtmlLinkTrimItem linkItem, Browser browser, String name )
		{
			super( browser, name );
			this.funcName = name;
			this.linkItem = linkItem;
		}

		public Object function( Object[] arguments )
		{
			if ( "gotoUrl".equals( funcName ) ) //$NON-NLS-1$
			{
				if ( arguments != null && arguments.length > 0 && arguments[0] != null )
				{
					UIUtil.openBrowser( arguments[0].toString( ) );
				}
			}
			else if ( "updateAdCount".equals( funcName ) ) //$NON-NLS-1$
			{
				JavaDecompilerPlugin.getDefault( ).getPreferenceStore( ).setValue( JavaDecompilerPlugin.ADCLICK_COUNT,
						JavaDecompilerPlugin.getDefault( ).getAdClickCount( ).getAndIncrement( ) );
			}
			else if ( "resize".equals( funcName ) ) //$NON-NLS-1$
			{
				linkItem.resize( "" );
			}
			return super.function( arguments );
		}
	}

	public HtmlLinkTrimItem( Composite parent )
	{
		super( parent, SWT.NONE );
		this.setBackgroundMode( SWT.INHERIT_FORCE );

		GridLayout trimLayout = new GridLayout( );
		trimLayout.marginWidth = 0;
		trimLayout.marginHeight = 0;
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

		new CustomFunction( this, browser, "gotoUrl" ); //$NON-NLS-1$
		new CustomFunction( this, browser, "updateAdCount" ); //$NON-NLS-1$
		new CustomFunction( this, browser, "resize" ); //$NON-NLS-1$

		final ProgressListener[] listeners = new ProgressListener[1];
		ProgressListener listener = new ProgressListener( ) {

			public void completed( ProgressEvent event )
			{
				handleEvent( );
			}

			private void handleEvent( )
			{
				if ( browser == null || browser.isDisposed( ) )
					return;

				getDisplay( ).asyncExec( new Runnable( ) {

					public void run( )
					{
						if ( browser == null || browser.isDisposed( ) )
							return;
						try
						{
							resize( updateBrowserStyle( ) );
							browserUrl = browser.getUrl( );
						}
						catch ( Exception e )
						{
							if ( browser.isVisible( ) )
							{
								browser.setVisible( false );
							}
							Logger.debug( e );
						}
						finally
						{
							if ( browser == null || browser.isDisposed( ) )
								return;
							getDisplay( ).timerExec( 1000 * 600, new Runnable( ) {

								public void run( )
								{
									if ( browser == null || browser.isDisposed( ) )
										return;
									if ( !browser.isVisible( ) )
									{
										updateTrimUrl( true );
									}
								}
							} );
						}
					}
				} );

			}

			public void changed( ProgressEvent event )
			{
				// handleEvent( );
			}
		};
		listeners[0] = listener;
		browser.addProgressListener( listener );

		GridData gd = new GridData( );
		gd.heightHint = getDisplay( ).getBounds( ).height;
		gd.widthHint = getDisplay( ).getBounds( ).width;
		browser.setLayoutData( gd );

		updateTrimUrl( false );

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

	private String updateBrowserStyle( )
	{
		StringBuilder buffer = new StringBuilder( );
		buffer.append( updateBrowserColor( ) );
		if ( trayLinkUrl == null || TrayLinkUtil.useSystemColor( trayLinkUrl ) || UIUtil.isDark( getParent( ) ) )
		{
			buffer.append( updateBrowserFontColor( ) );
		}
		buffer.append( updateBrowserFontFamily( ) );
		buffer.append( updateBrowserFontSize( ) );

		String listener = registerLinkClickListener( );
		if ( listener != null )
		{
			buffer.append( listener );
		}
		return buffer.toString( );
	}

	private void updateTrimUrl( boolean force )
	{
		if ( isDisposed )
		{
			return;
		}

		trayLinkUrl = TrayLinkUtil.getTrayUrl( );
		if ( trayLinkUrl == null )
		{
			return;
		}

		Integer time = TrayLinkUtil.getTrayUrlDisplayTime( trayLinkUrl );

		if ( time == null )
		{
			time = 10;
		}

		if ( UIUtil.isWin32( ) )
		{
			isUseExternalBrowser = TrayLinkUtil.isUseExternalBrowser( trayLinkUrl );
		}
		else
		{
			isUseExternalBrowser = true;
		}
		if ( !trayLinkUrl.equals( browserUrl ) || force )
		{

			ExecutorUtil.submitTask( new Callable<Boolean>( ) {

				public Boolean call( ) throws Exception
				{
					Socket socket = new Socket( );
					try
					{
						if ( HtmlLinkTrimItem.this.isDisposed( ) )
							return true;
						socket.connect( new InetSocketAddress( new URL( trayLinkUrl ).getHost( ), 80 ), 5000 );
						HtmlLinkTrimItem.this.getDisplay( ).asyncExec( new Runnable( ) {

							public void run( )
							{
								if ( browser != null && !browser.isDisposed( ) )
								{
									browser.setData( "linkClick", false ); //$NON-NLS-1$
									browser.setVisible( false );
									browser.setUrl( trayLinkUrl );
								}
								else
								{
									isDisposed = true;
								}
							}
						} );
						return true;
					}
					catch ( Exception e )
					{
						browserUrl = null;
						HtmlLinkTrimItem.this.getDisplay( ).asyncExec( new Runnable( ) {

							public void run( )
							{
								if ( browser != null && !browser.isDisposed( ) )
								{
									browser.setVisible( false );
								}
								else
								{
									isDisposed = true;
								}
							}
						} );
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

		if ( lastUpdateTime == -1 || ( System.currentTimeMillis( ) - lastUpdateTime ) / ( 60 * 1000 ) >= time - 1 )
		{
			lastUpdateTime = System.currentTimeMillis( );
			ExecutorUtil.submitScheduledTask( new Runnable( ) {

				public void run( )
				{
					updateTrimUrl( false );
				}
			}, time, TimeUnit.MINUTES );
		}
	}

	@Override
	public Point computeSize( int wHint, int hHint, boolean changed )
	{
		int trimWidth = (int) width + 5 * 2 + 4;
		int trimHeight = getParent( ) instanceof Shell ? (int) height : getParent( ).getBounds( ).height;
		trimHeight = Math.max( TrimUtil.TRIM_DEFAULT_HEIGHT, trimHeight );
		return new Point( trimWidth, trimHeight );
	}

	private String updateBrowserColor( )
	{
		Color color = getBackground( );
		String script = "document.body.style.background=\"rgb(" //$NON-NLS-1$
				+ color.getRed( )
				+ "," //$NON-NLS-1$
				+ color.getGreen( )
				+ "," //$NON-NLS-1$
				+ color.getBlue( )
				+ ")\";"; //$NON-NLS-1$
		// script += "$('div').css('background-color','rgb(" //$NON-NLS-1$
		// + color.getRed( )
		// + "," //$NON-NLS-1$
		// + color.getGreen( )
		// + "," //$NON-NLS-1$
		// + color.getBlue( )
		// + ")');"; //$NON-NLS-1$
		return script;
	}

	/**
	 * On Windows: points(SWT) = points(browser) != pixels(SWT) =
	 * pixels(browser) </br>
	 * On Mac: points(SWT) = pixels(SWT) = pixels(browser) != points(browser)
	 */
	protected String updateBrowserFontSize( )
	{
		int fontHeight = HtmlLinkTrimItem.this.getFont( ).getFontData( )[0].getHeight( );
		String script = "$('a').css('font-size','" //$NON-NLS-1$
				+ fontHeight
				+ ( UIUtil.isMacOS( ) ? "px" : "pt" ) //$NON-NLS-1$ //$NON-NLS-2$
				+ "');"; //$NON-NLS-1$
		return script;
	}

	protected String updateBrowserFontFamily( )
	{
		String fontFamily = HtmlLinkTrimItem.this.getFont( ).getFontData( )[0].getName( );
		if ( UIUtil.isMacOS( ) )
		{
			fontFamily = "sans-serif"; //$NON-NLS-1$
		}
		String script = "$('a').css('font-family','" + fontFamily + "');"; //$NON-NLS-1$ //$NON-NLS-2$
		return script;
	}

	protected String updateBrowserFontColor( )
	{

		Color color = HtmlLinkTrimItem.this.getParent( ).getForeground( );
		if ( color.equals( getDisplay( ).getSystemColor( SWT.COLOR_WIDGET_FOREGROUND ) ) )
		{
			color = getDisplay( ).getSystemColor( SWT.COLOR_BLUE );
		}
		else if ( UIUtil.isDark( this ) )
		{
			JavaTextTools textTools = JavaPlugin.getDefault( ).getJavaTextTools( );
			IPreferenceStore preferences = (IPreferenceStore) ReflectionUtils.getFieldValue( textTools,
					"fPreferenceStore" ); //$NON-NLS-1$
			String defaultColorSetting = preferences.getString( IJavaColorConstants.JAVA_DEFAULT );
			color = JFaceResources.getResources( ).createColor( ColorUtil.getColorValue( defaultColorSetting ) );
		}
		String script = "$('a').css('color','rgb(" //$NON-NLS-1$
				+ color.getRed( )
				+ "," //$NON-NLS-1$
				+ color.getGreen( )
				+ "," //$NON-NLS-1$
				+ color.getBlue( )
				+ ")');"; //$NON-NLS-1$
		return script;
	}

	private String registerLinkClickListener( )
	{
		if ( isUseExternalBrowser )
		{
			if ( !Boolean.TRUE.equals( browser.getData( "linkClick" ) ) ) //$NON-NLS-1$
			{

				String script = "$('a').click( function(e) {e.preventDefault(); gotoUrl(this.href); return false; } );"; //$NON-NLS-1$
				browser.setData( "linkClick", true ); //$NON-NLS-1$
				return script;
			}
		}
		else
		{
			if ( !Boolean.TRUE.equals( browser.getData( "linkClick" ) ) ) //$NON-NLS-1$
			{
				String script = "$('a').click( function(e) { updateAdCount(); return true; } );"; //$NON-NLS-1$
				browser.setData( "linkClick", true ); //$NON-NLS-1$
				return script;
			}
		}
		return null;
	}

	private void resize( final String updatedStyle )
	{
		if ( browser.isDisposed( ) )
		{
			return;
		}
		if ( updatedStyle != null && updatedStyle.length( ) > 0 )
		{
			browser.execute( updatedStyle ); // $NON-NLS-1$
		}
		getDisplay( ).asyncExec( new Runnable( ) {

			public void run( )
			{
				if ( browser.isDisposed( ) )
				{
					return;
				}
				getDisplay( ).timerExec( 50, new Runnable( ) {

					public void run( )
					{
						try
						{
							if ( browser.isDisposed( ) )
							{
								return;
							}

							browser.execute( updatedStyle ); // $NON-NLS-1$
							Object[] area = (Object[]) browser.evaluate( "return getContentArea();" ); //$NON-NLS-1$
							if ( area == null || area[0] == null || area[1] == null )
								return;
							double tempWidth = Double.valueOf( area[0].toString( ) );
							double tempHeight = Double.valueOf( area[1].toString( ) );
							if ( tempWidth > 0 && tempHeight > 0 )
							{
								width = tempWidth;
								height = tempHeight;

								Point computeSize = computeSize( -1, -1, true );
								HtmlLinkTrimItem.this.pack( );
								HtmlLinkTrimItem.this.setSize( computeSize );
								GridData gd = (GridData) browser.getLayoutData( );
								if ( gd == null )
								{
									gd = new GridData( );
								}
								gd.verticalIndent = (int) Math
										.ceil( HtmlLinkTrimItem.this.getBounds( ).height - height ) / 2 - 1;
								gd.heightHint = (int) height + 1;
								browser.setLayoutData( gd );
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
								showBrowser( );
							}
							else if ( browser.isVisible( ) )
							{
								browser.setVisible( false );
							}
						}
						catch ( Exception e )
						{
							Logger.debug( e );
						}
					}
				} );
			}
		} );

	}

	private void showBrowser( )
	{
		if ( browser.isDisposed( ) )
		{
			return;
		}
		getDisplay( ).asyncExec( new Runnable( ) {

			public void run( )
			{
				if ( browser.isDisposed( ) )
				{
					return;
				}
				getDisplay( ).timerExec( 50, new Runnable( ) {

					public void run( )
					{
						if ( !browser.isDisposed( ) && !browser.isVisible( ) )
						{
							browser.setVisible( true );
						}
					}
				} );
			}
		} );
	}
}
