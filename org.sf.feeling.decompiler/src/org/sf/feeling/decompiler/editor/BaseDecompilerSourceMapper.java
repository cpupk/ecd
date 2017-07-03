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

package org.sf.feeling.decompiler.editor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.ExternalPackageFragmentRoot;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.SourceMapper;
import org.eclipse.jdt.internal.debug.core.JDIDebugPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jface.preference.IPreferenceStore;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;
import org.sf.feeling.decompiler.fernflower.FernFlowerDecompiler;
import org.sf.feeling.decompiler.util.ClassUtil;
import org.sf.feeling.decompiler.util.DecompileUtil;
import org.sf.feeling.decompiler.util.DecompilerOutputUtil;
import org.sf.feeling.decompiler.util.Logger;
import org.sf.feeling.decompiler.util.MarkUtil;
import org.sf.feeling.decompiler.util.ReflectionUtils;
import org.sf.feeling.decompiler.util.SortMemberUtil;
import org.sf.feeling.decompiler.util.UIUtil;

public abstract class BaseDecompilerSourceMapper extends DecompilerSourceMapper
{

	protected IDecompiler origionalDecompiler;
	private IDecompiler usedDecompiler;
	private String classLocation;

	private static Map<String, String> options = new HashMap<String, String>( );
	static
	{
		CompilerOptions option = new CompilerOptions( );
		options = option.getMap( );
		options.put( CompilerOptions.OPTION_Compliance, DecompilerOutputUtil.getMaxDecompileLevel( ) ); // $NON-NLS-1$
		options.put( CompilerOptions.OPTION_Source, DecompilerOutputUtil.getMaxDecompileLevel( ) ); // $NON-NLS-1$
	}

	public BaseDecompilerSourceMapper( IPath sourcePath, String rootPath )
	{

		this( sourcePath, rootPath, options );
	}

	public BaseDecompilerSourceMapper( IPath sourcePath, String rootPath, Map options )
	{
		super( sourcePath, rootPath, options );
	}

	@Override
	public char[] findSource( IType type, IBinaryType info )
	{
		IPreferenceStore prefs = JavaDecompilerPlugin.getDefault( ).getPreferenceStore( );
		boolean always = prefs.getBoolean( JavaDecompilerPlugin.IGNORE_EXISTING );

		Collection exceptions = new LinkedList( );
		IPackageFragment pkgFrag = type.getPackageFragment( );
		IPackageFragmentRoot root = (IPackageFragmentRoot) pkgFrag.getParent( );

		JavaDecompilerPlugin.getDefault( ).syncLibrarySource( root );
		char[] attachedSource = null;

		if ( UIUtil.requestFromJavadocHover( ) && !fromInput( type ) && always )
		{
			sourceRanges.remove( type );
			attachedSource = originalSourceMapper.get( root ).findSource( type, info );
			return attachedSource;
		}

		if ( originalSourceMapper.containsKey( root ) )
		{
			attachedSource = originalSourceMapper.get( root ).findSource( type, info );

			if ( attachedSource != null && !always )
			{
				attachedSource = DecompileUtil.getCopyRightContent( type.getClassFile( ), new String( attachedSource ) )
						.toCharArray( );
				updateSourceRanges( type, attachedSource );
				isAttachedSource = true;
				updateBreakPointStatus( type, new String( attachedSource ) );
				mapSource( type, attachedSource, true );
				( (PackageFragmentRoot) root ).getSourceMapper( ).mapSource( type, attachedSource, info );
				return attachedSource;
			}
		}

		if ( info == null )
		{
			if ( always )
				return null;
			return attachedSource;
		}

		try
		{
			if ( root instanceof PackageFragmentRoot )
			{
				PackageFragmentRoot pfr = (PackageFragmentRoot) root;

				SourceMapper sourceMapper = pfr.getSourceMapper( );

				if ( !originalSourceMapper.containsKey( root ) )
				{
					ReflectionUtils.setFieldValue( this,
							"options", //$NON-NLS-1$
							ReflectionUtils.getFieldValue( sourceMapper, "options" ) ); //$NON-NLS-1$
					originalSourceMapper.put( root, sourceMapper );
				}

				if ( sourceMapper != null && !always && !( sourceMapper instanceof DecompilerSourceMapper ) )
				{
					attachedSource = sourceMapper.findSource( type, info );
					if ( attachedSource != null )
					{
						attachedSource = DecompileUtil
								.getCopyRightContent( type.getClassFile( ), new String( attachedSource ) )
								.toCharArray( );
						updateSourceRanges( type, attachedSource );
						isAttachedSource = true;
						updateBreakPointStatus( type, new String( attachedSource ) );
						mapSource( type, attachedSource, true );
						( (PackageFragmentRoot) root ).getSourceMapper( ).mapSource( type, attachedSource, info );
						return attachedSource;
					}
				}

				if ( sourceMapper != this )
				{
					pfr.setSourceMapper( this );
				}
			}
		}
		catch ( JavaModelException e )
		{
			JavaDecompilerPlugin.logError( e, "Could not set source mapper." ); //$NON-NLS-1$
		}

		isAttachedSource = false;

		if ( JavaDecompilerPlugin.getDefault( ).isAutoAttachSource( ) )
		{
			JavaDecompilerPlugin.getDefault( ).attachSource( root, false );
		}

		String className = new String( info.getName( ) );
		String fullName = new String( info.getFileName( ) );
		className = fullName.substring( fullName.lastIndexOf( className ) );

		int index = className.lastIndexOf( '/' );
		className = className.substring( index + 1 );

		classLocation = ""; //$NON-NLS-1$

		usedDecompiler = decompile( null, type, exceptions, root, className );

		if ( usedDecompiler == null
				|| usedDecompiler.getSource( ) == null
				|| usedDecompiler.getSource( ).length( ) == 0 )
		{
			if ( usedDecompiler == null || !DecompilerType.FernFlower.equals( usedDecompiler.getDecompilerType( ) ) )
			{
				usedDecompiler = decompile( new FernFlowerDecompiler( ), type, exceptions, root, className );
				if ( usedDecompiler == null
						|| usedDecompiler.getSource( ) == null
						|| usedDecompiler.getSource( ).length( ) == 0 )
				{
					return null;
				}
			}
		}

		String code = MarkUtil.getRandomMark( type.getClassFile( ) )
				+ "\r\n" //$NON-NLS-1$
				+ usedDecompiler.getSource( );

		boolean showReport = prefs.getBoolean( JavaDecompilerPlugin.PREF_DISPLAY_METADATA );
		if ( !showReport )
		{
			code = usedDecompiler.removeComment( code );
		}

		boolean showLineNumber = prefs.getBoolean( JavaDecompilerPlugin.PREF_DISPLAY_LINE_NUMBERS );
		boolean align = prefs.getBoolean( JavaDecompilerPlugin.ALIGN );
		if ( ( showLineNumber && align )
				|| UIUtil.isDebugPerspective( )
				|| JavaDecompilerPlugin.getDefault( ).isDebugMode( ) )
		{
			if ( showReport )
				code = usedDecompiler.removeComment( code );
			DecompilerOutputUtil decompilerOutputUtil = new DecompilerOutputUtil( type.getClassFile( ),
					usedDecompiler.getDecompilerType( ),
					code );
			code = decompilerOutputUtil.realign( );
		}

		StringBuffer source = new StringBuffer( );

		if ( !( UIUtil.isDebugPerspective( ) || JavaDecompilerPlugin.getDefault( ).isDebugMode( ) ) )
		{
			boolean useSorter = prefs.getBoolean( JavaDecompilerPlugin.USE_ECLIPSE_SORTER );
			if ( useSorter )
			{
				className = new String( info.getName( ) );
				fullName = new String( info.getFileName( ) );
				if ( fullName.lastIndexOf( className ) != -1 )
				{
					className = fullName.substring( fullName.lastIndexOf( className ) );
				}

				code = SortMemberUtil.sortMember( type.getPackageFragment( ).getElementName( ), className, code );
			}

			source.append( formatSource( code ) );

			if ( showReport )
			{
				printDecompileReport( source, classLocation, exceptions, usedDecompiler.getDecompilationTime( ) );
			}
		}
		else
		{
			source.append( code );
		}

		updateBreakPointStatus( type, source.toString( ) );

		if ( originalSourceMapper.containsKey( root ) )
		{
			if ( originalSourceMapper.get( root ).findSource( type, info ) == null )
			{
				originalSourceMapper.get( root ).mapSource( type, source.toString( ).toCharArray( ), null );
			}
		}

		updateSourceRanges( type, source.toString( ).toCharArray( ) );
		return source.toString( ).toCharArray( );
	}

	private void updateSourceRanges( IType type, char[] attachedSource )
	{
		if ( type.getParent( ) instanceof ClassFile )
		{
			try
			{
				DecompileUtil.updateSourceRanges( ( (ClassFile) type.getParent( ) ), new String( attachedSource ) );
			}
			catch ( JavaModelException e )
			{
				Logger.debug( e );
			}
		}
	}

	private void updateBreakPointStatus( IType type, String source )
	{
		if ( MarkUtil.containsSourceMark( source ) )
		{
			IBreakpointManager manager = DebugPlugin.getDefault( ).getBreakpointManager( );
			String modelId = JDIDebugPlugin.getUniqueIdentifier( );
			IBreakpoint[] breakpoints = manager.getBreakpoints( modelId );
			for ( int i = 0; i < breakpoints.length; i++ )
			{
				if ( !( breakpoints[i] instanceof IJavaLineBreakpoint ) )
					continue;
				try
				{
					IJavaLineBreakpoint breakPoint = (IJavaLineBreakpoint) breakpoints[i];
					String breakpointTypeName = breakPoint.getTypeName( );
					if ( type != null
							&& breakpointTypeName != null
							&& breakpointTypeName.equals( type.getFullyQualifiedName( ) ) )
					{
						int lineNumber = (Integer) breakPoint.getMarker( ).getAttribute( IMarker.LINE_NUMBER );

						String[] lines = source.split( "\n" ); //$NON-NLS-1$

						int charStart = getCharStart( lines, lineNumber - 1 );
						int charEnd = getCharEnd( lines, lineNumber - 1 );

						breakPoint.getMarker( ).setAttribute( IMarker.CHAR_START, new Integer( charStart ) );
						breakPoint.getMarker( ).setAttribute( IMarker.CHAR_END, new Integer( charEnd - 1 ) );
					}
				}
				catch ( CoreException e )
				{
					Logger.debug( e );
				}
			}
		}
		else if ( MarkUtil.containsMark( source ) && UIUtil.isDebug( ) )
		{
			IBreakpointManager manager = DebugPlugin.getDefault( ).getBreakpointManager( );
			String modelId = JDIDebugPlugin.getUniqueIdentifier( );
			IBreakpoint[] breakpoints = manager.getBreakpoints( modelId );
			for ( int i = 0; i < breakpoints.length; i++ )
			{
				if ( !( breakpoints[i] instanceof IJavaLineBreakpoint ) )
					continue;
				try
				{
					IJavaLineBreakpoint breakPoint = (IJavaLineBreakpoint) breakpoints[i];
					String breakpointTypeName = breakPoint.getTypeName( );
					if ( type != null
							&& breakpointTypeName != null
							&& breakpointTypeName.equals( type.getFullyQualifiedName( ) ) )
					{
						int lineNumber = (Integer) breakPoint.getMarker( ).getAttribute( IMarker.LINE_NUMBER );

						String[] lines = source.split( "\n" ); //$NON-NLS-1$

						int charStart = getCharStart( lines, lineNumber - 1 );
						int charEnd = getCharEnd( lines, lineNumber - 1 );

						breakPoint.getMarker( ).setAttribute( IMarker.CHAR_START, new Integer( charStart ) );
						breakPoint.getMarker( ).setAttribute( IMarker.CHAR_END, new Integer( charEnd - 1 ) );
					}
				}
				catch ( CoreException e )
				{
					Logger.debug( e );
				}
			}
		}
	}

	private int getCharEnd( String[] lines, int lineNumber )
	{
		int end = 0;
		if ( lineNumber > -1 && lineNumber < lines.length )
		{
			for ( int i = 0; i <= lineNumber; i++ )
			{
				end += lines[i].length( ) + 1;
			}
		}
		return end;
	}

	private int getCharStart( String[] lines, int lineNumber )
	{
		int start = 0;
		if ( lineNumber > -1 && lineNumber < lines.length )
		{
			for ( int i = 0; i < lineNumber; i++ )
			{
				start += lines[i].length( ) + 1;
			}
		}
		return start;
	}

	private boolean fromInput( IType type )
	{
		JavaDecompilerClassFileEditor editor = UIUtil.getActiveEditor( );
		if ( editor != null && editor.getEditorInput( ) instanceof IClassFileEditorInput )
		{
			IClassFile input = ( (IClassFileEditorInput) editor.getEditorInput( ) ).getClassFile( );
			IType inputType = (IType) ReflectionUtils.invokeMethod( input,
					"getOuterMostEnclosingType", //$NON-NLS-1$
					new Class[0],
					new Object[0] );
			return type.equals( inputType );
		}
		return false;
	}

	private IDecompiler decompile( IDecompiler decompiler, IType type, Collection exceptions, IPackageFragmentRoot root,
			String className )
	{
		IDecompiler result = decompiler;

		String pkg = type.getPackageFragment( ).getElementName( ).replace( '.', '/' );

		Boolean displayNumber = null;
		if ( UIUtil.isDebugPerspective( ) || JavaDecompilerPlugin.getDefault( ).isDebugMode( ) )
		{
			displayNumber = JavaDecompilerPlugin.getDefault( ).isDisplayLineNumber( );
			JavaDecompilerPlugin.getDefault( ).displayLineNumber( Boolean.TRUE );
		}

		try
		{
			if ( root.isArchive( ) )
			{
				String archivePath = getArchivePath( root );
				classLocation += archivePath;

				if ( result == null )
				{
					try
					{
						result = ClassUtil.checkAvailableDecompiler( origionalDecompiler,
								new ByteArrayInputStream( type.getClassFile( ).getBytes( ) ) );
					}
					catch ( JavaModelException e )
					{
						result = origionalDecompiler;
					}
				}
				result.decompileFromArchive( archivePath, pkg, className );
			}
			else
			{
				String rootLocation = null;
				try
				{
					if ( root.getUnderlyingResource( ) != null )
					{
						rootLocation = root.getUnderlyingResource( ).getLocation( ).toOSString( );
						classLocation += rootLocation
								+ "/" //$NON-NLS-1$
								+ pkg
								+ "/" //$NON-NLS-1$
								+ className;
					}
					else if ( root instanceof ExternalPackageFragmentRoot )
					{
						rootLocation = ( (ExternalPackageFragmentRoot) root ).getPath( ).toOSString( );
						classLocation += rootLocation
								+ "/" //$NON-NLS-1$
								+ pkg
								+ "/" //$NON-NLS-1$
								+ className;
					}
					else
					{
						rootLocation = root.getPath( ).toOSString( );
						classLocation += rootLocation
								+ "/" //$NON-NLS-1$
								+ pkg
								+ "/" //$NON-NLS-1$
								+ className;
					}

					if ( result == null )
					{
						result = ClassUtil.checkAvailableDecompiler( origionalDecompiler, new File( classLocation ) );
					}
					result.decompile( rootLocation, pkg, className );
				}
				catch ( JavaModelException e )
				{
					exceptions.add( e );
				}
			}
		}
		catch ( Exception e )
		{
			exceptions.add( e );
		}

		if ( displayNumber != null )
		{
			JavaDecompilerPlugin.getDefault( ).displayLineNumber( displayNumber );
		}
		return result;
	}

	@Override
	public String decompile( String decompilerType, File file )
	{
		IPreferenceStore prefs = JavaDecompilerPlugin.getDefault( ).getPreferenceStore( );

		Boolean displayNumber = null;
		if ( UIUtil.isDebugPerspective( ) || JavaDecompilerPlugin.getDefault( ).isDebugMode( ) )
		{
			displayNumber = JavaDecompilerPlugin.getDefault( ).isDisplayLineNumber( );
			JavaDecompilerPlugin.getDefault( ).displayLineNumber( Boolean.TRUE );
		}

		IDecompiler currentDecompiler = ClassUtil.checkAvailableDecompiler( origionalDecompiler, file );

		currentDecompiler.decompile( file.getParentFile( ).getAbsolutePath( ),
				"", //$NON-NLS-1$
				file.getName( ) );

		if ( displayNumber != null )
		{
			JavaDecompilerPlugin.getDefault( ).displayLineNumber( displayNumber );
		}

		if ( currentDecompiler.getSource( ) == null || currentDecompiler.getSource( ).length( ) == 0 )
			return null;

		String code = MarkUtil.getRandomMark( file.getAbsolutePath( ) )
				+ "\r\n" //$NON-NLS-1$
				+ currentDecompiler.getSource( );

		boolean showReport = prefs.getBoolean( JavaDecompilerPlugin.PREF_DISPLAY_METADATA );
		if ( !showReport )
		{
			code = currentDecompiler.removeComment( code );
		}

		boolean showLineNumber = prefs.getBoolean( JavaDecompilerPlugin.PREF_DISPLAY_LINE_NUMBERS );
		boolean align = prefs.getBoolean( JavaDecompilerPlugin.ALIGN );
		if ( ( showLineNumber && align )
				|| UIUtil.isDebugPerspective( )
				|| JavaDecompilerPlugin.getDefault( ).isDebugMode( ) )
		{
			if ( showReport )
				code = currentDecompiler.removeComment( code );
			DecompilerOutputUtil decompilerOutputUtil = new DecompilerOutputUtil( currentDecompiler
					.getDecompilerType( ), currentDecompiler.getDecompilerType( ), code );
			code = decompilerOutputUtil.realign( );
		}

		StringBuffer source = new StringBuffer( );

		if ( !UIUtil.isDebugPerspective( ) )
		{
			source.append( formatSource( code ) );

			if ( showReport )
			{
				Collection exceptions = new LinkedList( );
				exceptions.addAll( currentDecompiler.getExceptions( ) );
				printDecompileReport( source,
						file.getAbsolutePath( ),
						exceptions,
						currentDecompiler.getDecompilationTime( ) );
			}
		}
		else
		{
			source.append( code );
		}

		return source.toString( );
	}

	protected abstract void printDecompileReport( StringBuffer source, String location, Collection exceptions,
			long decompilationTime );
}
