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

package org.sf.feeling.decompiler;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.sf.feeling.decompiler.actions.DebugModeAction;
import org.sf.feeling.decompiler.editor.DecompilerType;
import org.sf.feeling.decompiler.editor.IDecompilerDescriptor;
import org.sf.feeling.decompiler.editor.JavaDecompilerBufferManager;
import org.sf.feeling.decompiler.editor.JavaDecompilerClassFileEditor;
import org.sf.feeling.decompiler.extension.DecompilerAdapterManager;
import org.sf.feeling.decompiler.extension.IDecompilerExtensionHandler;
import org.sf.feeling.decompiler.i18n.Messages;
import org.sf.feeling.decompiler.source.attach.IAttachSourceHandler;
import org.sf.feeling.decompiler.update.IDecompilerUpdateHandler;
import org.sf.feeling.decompiler.util.DecompilerOutputUtil;
import org.sf.feeling.decompiler.util.Logger;
import org.sf.feeling.decompiler.util.MarkUtil;
import org.sf.feeling.decompiler.util.SortMemberUtil;
import org.sf.feeling.decompiler.util.UIUtil;

public class JavaDecompilerPlugin extends AbstractUIPlugin implements
		IPropertyChangeListener
{

	public static final String EDITOR_ID = "org.sf.feeling.decompiler.ClassFileEditor"; //$NON-NLS-1$
	public static final String PLUGIN_ID = "org.sf.feeling.decompiler"; //$NON-NLS-1$
	public static final String TEMP_DIR = "org.sf.feeling.decompiler.tempd"; //$NON-NLS-1$

	public static final String REUSE_BUFFER = "org.sf.feeling.decompiler.reusebuff"; //$NON-NLS-1$
	public static final String IGNORE_EXISTING = "org.sf.feeling.decompiler.alwaysuse"; //$NON-NLS-1$
	public static final String USE_ECLIPSE_FORMATTER = "org.sf.feeling.decompiler.use_eclipse_formatter"; //$NON-NLS-1$
	public static final String USE_ECLIPSE_SORTER = "org.sf.feeling.decompiler.use_eclipse_sorter"; //$NON-NLS-1$
	public static final String DECOMPILER_TYPE = "org.sf.feeling.decompiler.type"; //$NON-NLS-1$
	public static final String PREF_DISPLAY_LINE_NUMBERS = "jd.ide.eclipse.prefs.DisplayLineNumbers"; //$NON-NLS-1$
	public static final String DECOMPILE_COUNT = "decompile.count"; //$NON-NLS-1$
	public static final String PREF_DISPLAY_METADATA = "jd.ide.eclipse.prefs.DisplayMetadata"; //$NON-NLS-1$
	public static final String ALIGN = "jd.ide.eclipse.prefs.RealignLineNumbers"; //$NON-NLS-1$
	public static final String DEFAULT_EDITOR = "org.sf.feeling.decompiler.default_editor"; //$NON-NLS-1$ ;
	public static final String CHECK_UPDATE = "org.sf.feeling.decompiler.check_update"; //$NON-NLS-1$ ;
	public static final String EXPORT_ENCODING = "org.sf.feeling.decompiler.export.encoding"; //$NON-NLS-1$ ;
	public static final String ATTACH_SOURCE = "org.sf.feeling.decompiler.attach_source"; //$NON-NLS-1$ ;

	private static JavaDecompilerPlugin plugin;

	private IPreferenceStore preferenceStore;
	private TreeMap<String, IDecompilerDescriptor> decompilerDescriptorMap = new TreeMap<String, IDecompilerDescriptor>( );
	private AtomicInteger decompileCount = new AtomicInteger( 0 );
	private boolean isDebugMode = false;
	private boolean enableExtension = false;

	private IBreakpointListener breakpintListener = new IBreakpointListener( ) {

		@Override
		public void breakpointRemoved( IBreakpoint breakpoint,
				IMarkerDelta delta )
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void breakpointChanged( IBreakpoint breakpoint,
				IMarkerDelta delta )
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void breakpointAdded( IBreakpoint breakpoint )
		{
			JavaDecompilerClassFileEditor editor = UIUtil
					.getActiveDecompilerEditor( );
			if ( editor != null )
			{
				String source = editor.getDocumentProvider( )
						.getDocument( editor.getEditorInput( ) )
						.get( );

				if ( source != null && MarkUtil.containsMark( source ) )
				{

					if ( UIUtil.isDebug( ) )
					{
						try
						{
							int lineNumber = (Integer) breakpoint.getMarker( )
									.getAttribute( IMarker.LINE_NUMBER );
							String[] lines = source.split( "\n" ); //$NON-NLS-1$
							if ( lineNumber - 1 < lines.length )
							{
								String line = lines[lineNumber - 1];
								int number = DecompilerOutputUtil
										.parseJavaLineNumber( line );
								if ( number == -1 )
								{
									Display.getDefault( )
											.asyncExec( new Runnable( ) {

												public void run( )
												{
													MessageDialog
															.openInformation(
																	Display.getDefault( )
																			.getActiveShell( ),
																	Messages.getString(
																			"JavaDecompilerPlugin.BreakpoingWithNumberDialog.Title" ), //$NON-NLS-1$
																	Messages.getString(
																			"JavaDecompilerPlugin.BreakpoingWithNumberDialog.Message" ) ); //$NON-NLS-1$
												}
											} );
									breakpoint.delete( );
								}
							}
						}
						catch ( CoreException e )
						{
							Logger.debug( e );
						}
					}
					else
					{
						try
						{
							Display.getDefault( ).asyncExec( new Runnable( ) {

								public void run( )
								{
									boolean setDebug = MessageDialog
											.openConfirm(
													Display.getDefault( )
															.getActiveShell( ),
													Messages.getString(
															"JavaDecompilerPlugin.BreakpoingDialog.Title" ), //$NON-NLS-1$
													Messages.getString(
															"JavaDecompilerPlugin.BreakpoingDialog.Message" ) ); //$NON-NLS-1$
									if ( setDebug )
									{
										new DebugModeAction( ).run();
									}
								}
							} );
							breakpoint.delete( );
						}
						catch ( CoreException e )
						{
							Logger.debug( e );
						}
					}
				}
			}
		}
	};

	private IBreakpointManager manager = DebugPlugin.getDefault( )
			.getBreakpointManager( );

	public AtomicInteger getDecompileCount( )
	{
		return decompileCount;
	}

	public Map<String, IDecompilerDescriptor> getDecompilerDescriptorMap( )
	{
		return decompilerDescriptorMap;
	}

	public String[] getDecompilerDescriptorTypes( )
	{
		return decompilerDescriptorMap.keySet( ).toArray( new String[0] );
	}

	public IDecompilerDescriptor getDecompilerDescriptor(
			String decompilerType )
	{
		return decompilerDescriptorMap.get( decompilerType );
	}

	public static JavaDecompilerPlugin getDefault( )
	{
		return plugin;
	}

	public static void logError( Throwable t, String message )
	{
		JavaDecompilerPlugin.getDefault( ).getLog( ).log(
				new Status( Status.ERROR, PLUGIN_ID, 0, message, t ) );
	}

	public static void logInfo( String message )
	{
		JavaDecompilerPlugin.getDefault( ).getLog( ).log(
				new Status( Status.INFO, PLUGIN_ID, 0, message, null ) );
	}

	public static void log( int severity, Throwable t, String message )
	{
		JavaDecompilerPlugin.getDefault( ).getLog( ).log(
				new Status( severity, PLUGIN_ID, 0, message, t ) );
	}

	public static ImageDescriptor getImageDescriptor( String path )
	{
		URL base = JavaDecompilerPlugin.getDefault( ).getBundle( ).getEntry(
				"/" ); //$NON-NLS-1$
		URL url = null;
		try
		{
			url = new URL( base, path ); // $NON-NLS-1$
		}
		catch ( MalformedURLException e )
		{
			Logger.debug( e );
		}
		ImageDescriptor actionIcon = null;
		if ( url != null )
			actionIcon = ImageDescriptor.createFromURL( url );
		return actionIcon;
	}

	public JavaDecompilerPlugin( )
	{
		plugin = this;
	}

	protected void initializeDefaultPreferences( IPreferenceStore store )
	{
		store.setDefault( TEMP_DIR,
				System.getProperty( "java.io.tmpdir" ) //$NON-NLS-1$
						+ File.separator
						+ ".org.sf.feeling.decompiler" ); //$NON-NLS-1$
		store.setDefault( REUSE_BUFFER, true );
		store.setDefault( IGNORE_EXISTING, false );
		store.setDefault( USE_ECLIPSE_FORMATTER, true );
		store.setDefault( USE_ECLIPSE_SORTER, false );
		store.setDefault( PREF_DISPLAY_METADATA, false );
		store.setDefault( DEFAULT_EDITOR, true );
		store.setDefault( CHECK_UPDATE, true );
		store.setDefault( ATTACH_SOURCE, true );
		store.setDefault( DECOMPILE_COUNT, 0 );
		store.setDefault( EXPORT_ENCODING, "UTF-8" ); //$NON-NLS-1$
	}

	private void setDefaultDecompiler( IPreferenceStore store )
	{
		if ( isEnableExtension( ) )
		{
			Object[] decompilerAdapters = DecompilerAdapterManager
					.getAdapters( this, IDecompilerDescriptor.class );

			if ( decompilerAdapters != null )
			{
				for ( int i = 0; i < decompilerAdapters.length; i++ )
				{
					Object adapter = decompilerAdapters[i];
					if ( adapter instanceof IDecompilerDescriptor )
					{
						IDecompilerDescriptor descriptor = (IDecompilerDescriptor) adapter;
						if ( descriptor.isEnabled( ) )
						{
							decompilerDescriptorMap.put(
									descriptor.getDecompilerType( ),
									descriptor );
						}
					}
				}
			}
		}
		store.setDefault( DECOMPILER_TYPE, getDefalutDecompilerType( ) );
	}

	public void propertyChange( PropertyChangeEvent event )
	{
		if ( event.getProperty( ).equals( IGNORE_EXISTING ) )
			JavaDecompilerBufferManager.closeDecompilerBuffers( false );
	}

	public void start( BundleContext context ) throws Exception
	{
		super.start( context );
		checkEnableExtension( );
		setDefaultDecompiler( getPreferenceStore( ) );
		getPreferenceStore( ).addPropertyChangeListener( this );
		SortMemberUtil.deleteDecompilerProject( );
		decompileCount.set( getPreferenceStore( ).getInt( DECOMPILE_COUNT ) );
		Display.getDefault( ).asyncExec( new SetupRunnable( ) );

		manager.addBreakpointListener( breakpintListener );
	}

	private void checkEnableExtension( )
	{
		final Object extensionAdapter = DecompilerAdapterManager.getAdapter(
				JavaDecompilerPlugin.getDefault( ),
				IDecompilerExtensionHandler.class );

		if ( extensionAdapter instanceof IDecompilerExtensionHandler )
		{
			enableExtension = true;
		}
		else
		{
			enableExtension = false;
		}
	}

	public boolean isEnableExtension( )
	{
		return enableExtension;
	}

	public IPreferenceStore getPreferenceStore( )
	{
		if ( preferenceStore == null )
		{
			preferenceStore = super.getPreferenceStore( );

			String decompilerType = preferenceStore
					.getString( DECOMPILER_TYPE );
			if ( !DecompilerType.FernFlower.equals( decompilerType ) )
			{
				IDecompilerDescriptor descriptor = getDecompilerDescriptor(
						decompilerType );
				if ( descriptor == null )
				{
					preferenceStore.setDefault( DECOMPILER_TYPE,
							getDefalutDecompilerType( ) );
				}
			}

		}
		return preferenceStore;
	}

	public void stop( BundleContext context ) throws Exception
	{
		manager.removeBreakpointListener( breakpintListener );
		getPreferenceStore( ).setValue( DECOMPILE_COUNT,
				decompileCount.get( ) );
		super.stop( context );
		getPreferenceStore( ).removePropertyChangeListener( this );
		plugin = null;
	}

	public Boolean isDisplayLineNumber( )
	{
		return Boolean.valueOf(
				getPreferenceStore( ).getBoolean( PREF_DISPLAY_LINE_NUMBERS ) );
	}

	public Boolean isDebug( )
	{
		return Boolean.valueOf( getPreferenceStore( ).getBoolean( ALIGN ) );
	}

	public void displayLineNumber( Boolean display )
	{
		getPreferenceStore( ).setValue( PREF_DISPLAY_LINE_NUMBERS,
				display.booleanValue( ) );
	}

	public void setExportEncoding( String encoding )
	{
		getPreferenceStore( ).setValue( EXPORT_ENCODING, encoding );
	}

	public String getExportEncoding( )
	{
		return getPreferenceStore( ).getString( EXPORT_ENCODING );
	}

	public boolean enableCheckUpdateSetting( )
	{
		Object updateAdapter = DecompilerAdapterManager.getAdapter( this,
				IDecompilerUpdateHandler.class );
		if ( updateAdapter instanceof IDecompilerUpdateHandler )
		{
			IDecompilerUpdateHandler updateHandler = (IDecompilerUpdateHandler) updateAdapter;
			return !updateHandler.isForce( null );
		}
		return false;
	}

	public boolean enableAttachSourceSetting( )
	{
		if ( isEnableExtension( ) )
		{
			Object attachSourceAdapter = DecompilerAdapterManager
					.getAdapter( this, IAttachSourceHandler.class );
			if ( attachSourceAdapter instanceof IAttachSourceHandler )
			{
				return true;
			}
		}
		return false;
	}

	private Set<String> librarys = new ConcurrentSkipListSet<String>( );

	public void attachSource( IPackageFragmentRoot library, boolean force )
	{
		if ( isEnableExtension( ) )
		{
			Object attachSourceAdapter = DecompilerAdapterManager
					.getAdapter( this, IAttachSourceHandler.class );
			if ( attachSourceAdapter instanceof IAttachSourceHandler )
			{
				if ( !librarys.contains( library.getPath( ).toOSString( ) )
						|| force )
				{
					librarys.add( library.getPath( ).toOSString( ) );
					( (IAttachSourceHandler) attachSourceAdapter )
							.execute( library, force );
				}
			}
		}
	}

	public void syncLibrarySource( IPackageFragmentRoot library )
	{
		if ( isEnableExtension( ) )
		{
			try
			{
				if ( library.getPath( ) != null
						&& library.getSourceAttachmentPath( ) != null
						&& !librarys
								.contains( library.getPath( ).toOSString( ) ) )
				{
					final IPreferenceStore prefs = JavaDecompilerPlugin
							.getDefault( )
							.getPreferenceStore( );
					if ( prefs
							.getBoolean( JavaDecompilerPlugin.DEFAULT_EDITOR ) )
					{
						final Object attachSourceAdapter = DecompilerAdapterManager
								.getAdapter( JavaDecompilerPlugin.getDefault( ),
										IAttachSourceHandler.class );
						if ( attachSourceAdapter instanceof IAttachSourceHandler )
						{
							librarys.add( library.getPath( ).toOSString( ) );
							if ( !( (IAttachSourceHandler) attachSourceAdapter )
									.syncAttachSource( library ) )
							{
								librarys.remove(
										library.getPath( ).toOSString( ) );
							} ;
						}
					}
				}
			}
			catch ( JavaModelException e )
			{
				Logger.debug( e );
			}
		}
	}

	public boolean isAutoAttachSource( )
	{
		if ( isEnableExtension( ) )
		{
			if ( !enableAttachSourceSetting( ) )
			{
				return false;
			}

			return getPreferenceStore( ).getBoolean( ATTACH_SOURCE );
		}
		else
		{
			return false;
		}
	}

	public String getDefalutDecompilerType( )
	{
		Collection<IDecompilerDescriptor> descriptors = JavaDecompilerPlugin
				.getDefault( )
				.getDecompilerDescriptorMap( )
				.values( );
		if ( descriptors != null )
		{
			for ( Iterator iterator = descriptors.iterator( ); iterator
					.hasNext( ); )
			{
				IDecompilerDescriptor iDecompilerDescriptor = (IDecompilerDescriptor) iterator
						.next( );
				if ( iDecompilerDescriptor.isDefault( ) )
				{
					return iDecompilerDescriptor.getDecompilerType( );
				}
			}
		}
		return DecompilerType.FernFlower;
	}

	public boolean isDebugMode( )
	{
		return isDebugMode;
	}

	public void setDebugMode( boolean isDebugMode )
	{
		this.isDebugMode = isDebugMode;
	}

	public void resetDecompileCount( )
	{
		decompileCount.set( 0 );
		getPreferenceStore( ).setValue( DECOMPILE_COUNT,
				decompileCount.get( ) );
	}

	public String getDefaultExportEncoding( )
	{
		return getPreferenceStore( )
				.getDefaultString( JavaDecompilerPlugin.EXPORT_ENCODING );
	}

}