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

package org.sf.feeling.decompiler.update;

import java.io.File;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;
import org.sf.feeling.decompiler.update.util.TrayLinkUtil;
import org.sf.feeling.decompiler.util.Logger;
import org.sf.feeling.decompiler.util.ReflectionUtils;

/**
 * The activator class controls the plug-in life cycle
 */
public class DecompilerUpdatePlugin extends AbstractUIPlugin implements IPropertyChangeListener
{

	// The plug-in ID
	public static final String PLUGIN_ID = "org.sf.feeling.decompiler.update"; //$NON-NLS-1$

	// The shared instance
	private static DecompilerUpdatePlugin plugin;

	private IPreferenceStore preferenceStore;

	public static final String NOT_UPDATE_VERSION = "org.sf.feeling.decompiler.not_update_version"; //$NON-NLS-1$ ;

	private File patchFile;

	private IWindowListener windowListener = new IWindowListener( ) {

		@Override
		public void windowOpened( IWorkbenchWindow window )
		{
			TrayLinkUtil.displayTrayLink( window );
		}

		@Override
		public void windowActivated( IWorkbenchWindow window )
		{
		}

		@Override
		public void windowDeactivated( IWorkbenchWindow window )
		{
		}

		@Override
		public void windowClosed( IWorkbenchWindow window )
		{
		}
	};

	/**
	 * The constructor
	 */
	public DecompilerUpdatePlugin( )
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext )
	 */
	@Override
	public void start( BundleContext context ) throws Exception
	{
		super.start( context );
		plugin = this;
		getPreferenceStore( ).addPropertyChangeListener( this );
		PlatformUI.getWorkbench( ).addWindowListener( windowListener );
	}

	@Override
	public IPreferenceStore getPreferenceStore( )
	{
		if ( preferenceStore == null )
		{
			preferenceStore = JavaDecompilerPlugin.getDefault( ).getPreferenceStore( );
			preferenceStore.setDefault( NOT_UPDATE_VERSION, "" ); //$NON-NLS-1$
		}
		return preferenceStore;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext )
	 */
	@Override
	public void stop( BundleContext context ) throws Exception
	{
		if ( PlatformUI.getWorkbench( ) != null )
		{
			PlatformUI.getWorkbench( ).removeWindowListener( windowListener );
		}
		if ( patchFile != null )
		{
			try
			{
				Bundle bundle = (Bundle) ReflectionUtils.invokeMethod( context, "getBundle", new Class[]{ //$NON-NLS-1$
						String.class
				}, new Object[]{
						patchFile.toURI( ).toString( )
				} );
				if ( bundle == null )
				{
					return;
				}
				bundle.uninstall( );
			}
			catch ( BundleException e )
			{
				Logger.debug( e );
			}
		}
		plugin = null;
		super.stop( context );
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static DecompilerUpdatePlugin getDefault( )
	{
		return plugin;
	}

	@Override
	public void propertyChange( PropertyChangeEvent event )
	{

	}

	public File getPatchFile( )
	{
		return patchFile;
	}

	public void setPatchFile( File patchFile )
	{
		this.patchFile = patchFile;
	}
}
