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

package org.sf.feeling.decompiler.source.attach.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathSupport;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.sf.feeling.decompiler.source.attach.handler.JavaSourceAttacherHandler;
import org.sf.feeling.decompiler.util.FileUtil;
import org.sf.feeling.decompiler.util.HashUtils;
import org.sf.feeling.decompiler.util.Logger;

@SuppressWarnings("restriction")
public class SourceAttachUtil
{

	public static File getBinFile( IPackageFragmentRoot root )
	{
		File binFile;
		if ( !root.isExternal( ) )
		{
			binFile = root.getResource( ).getLocation( ).toFile( );
		}
		else
		{
			binFile = root.getPath( ).toFile( );
		}
		return binFile;
	}

	public static boolean reattchSource( final IPackageFragmentRoot pkgRoot, File sourceFile, File tempSourceFile,
			String downloadUrl )
	{
		try
		{
			IPath sourcePath = pkgRoot.getSourceAttachmentPath( );
			File tempfile = new File( sourcePath.toOSString( ) );
			File tempFile;

			if ( !tempfile.getAbsolutePath( ).equals( tempSourceFile.getAbsolutePath( ) ) )
			{
				tempFile = tempSourceFile;
			}
			else
			{
				String suffix = "-" + System.currentTimeMillis( ) + ".jar"; //$NON-NLS-1$ //$NON-NLS-2$
				tempFile = new File( SourceConstants.SourceTempDir,
						sourceFile.getName( )
								.replaceAll( "(?i)(\\-)*(\\d)*(\\.)jar", suffix ) //$NON-NLS-1$
								.replaceAll( "(?i)(\\-)*(\\d)*(\\.)zip", suffix ) ); //$NON-NLS-1$
			}

			if ( !tempFile.exists( ) )
			{
				if ( tempSourceFile.exists( ) )
				{
					tempSourceFile.renameTo( tempFile );
				}
				else
				{
					FileUtil.copyFile( sourceFile.getAbsolutePath( ), tempFile.getAbsolutePath( ) );
				}
			}
			JavaSourceAttacherHandler.attachSource( pkgRoot, tempFile );
			tempFile.deleteOnExit( );
			String sha = HashUtils.sha1Hash( SourceAttachUtil.getBinFile( pkgRoot ) );
			SourceBindingUtil.saveSourceBindingRecord( sourceFile, sha, downloadUrl, tempFile );
			return true;
		}
		catch ( Exception e )
		{
			Logger.debug( e );
		}
		return false;
	}

	public static boolean refreshSourceAttachStatus( final IPackageFragmentRoot root )
	{
		try
		{
			String sha = HashUtils.sha1Hash( getBinFile( root ) );
			String[] files = SourceBindingUtil.getSourceFileBySha( sha );
			if ( files != null && files[0] != null )
			{
				File sourceFile = new File( files[0] );
				if ( !sourceFile.exists( ) )
				{
					return false;
				}
				File tempFile = new File( files[1] );
				if ( files[1] != null && tempFile.exists( ) )
				{
					JavaSourceAttacherHandler.attachSource( root, tempFile );
					SourceBindingUtil.saveSourceBindingRecord( sourceFile, sha, null, tempFile );

					if ( sourceFile.getName( ).startsWith( "jre_" ) ) //$NON-NLS-1$
					{
						refreshJRELibrarySources( root );
					}

					if ( sourceFile.getName( ).startsWith( "eclipse_" ) ) //$NON-NLS-1$
					{
						List<String> packages = getEclipsePlugins( sourceFile );
						refreshEclipseLibrarySources( root, packages );
					}

					return true;
				}
				else
				{
					String suffix = "-" + System.currentTimeMillis( ) + ".jar"; //$NON-NLS-1$ //$NON-NLS-2$
					tempFile = new File( SourceConstants.SourceTempDir,
							sourceFile.getName( )
									.replaceAll( "(?i)(\\-)*(\\d)*(\\.)jar", suffix ) //$NON-NLS-1$
									.replaceAll( "(?i)(\\-)*(\\d)*(\\.)zip", suffix ) ); //$NON-NLS-1$
					FileUtil.copyFile( sourceFile.getAbsolutePath( ), tempFile.getAbsolutePath( ) );
					JavaSourceAttacherHandler.attachSource( root, tempFile );
					tempFile.deleteOnExit( );
					SourceBindingUtil.saveSourceBindingRecord( sourceFile, sha, null, tempFile );

					if ( sourceFile.getName( ).startsWith( "jre_" ) ) //$NON-NLS-1$
					{
						refreshJRELibrarySources( root );
					}

					if ( sourceFile.getName( ).startsWith( "eclipse_" ) ) //$NON-NLS-1$
					{
						List<String> packages = getEclipsePlugins( sourceFile );
						refreshEclipseLibrarySources( root, packages );
					}

					return true;
				}
			}

		}
		catch ( Exception e )
		{
			Logger.debug( e );
		}
		return false;
	}

	public static List<String> getEclipsePlugins( final File file ) throws IOException
	{
		final Set<String> plugins = new HashSet<String>( );
		ZipFile zf = new ZipFile( file );
		for ( Enumeration<? extends ZipEntry> entries = zf.entries( ); entries.hasMoreElements( ); )
		{
			String zipEntryName = ( (ZipEntry) entries.nextElement( ) ).getName( );
			if ( zipEntryName.endsWith( ".project" ) ) //$NON-NLS-1$
			{
				String[] segements = zipEntryName.replace( "/.project", "" ).split( "/" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				plugins.add( segements[segements.length - 1] );
			}
		}
		zf.close( );
		return new ArrayList<String>( plugins );
	}

	private static boolean refreshLibrarySources( final IPackageFragmentRoot root, String[] files )
	{
		try
		{
			String sha = HashUtils.sha1Hash( getBinFile( root ) );
			File sourceFile = new File( files[0] );
			if ( !sourceFile.exists( ) )
				return false;
			File tempFile = new File( files[1] );
			if ( files[1] != null && tempFile.exists( ) )
			{
				JavaSourceAttacherHandler.attachSource( root, tempFile );
				tempFile.deleteOnExit( );
				SourceBindingUtil.saveSourceBindingRecord( sourceFile, sha, null, tempFile );
				return true;
			}
			else
			{
				String suffix = "-" + System.currentTimeMillis( ) + ".jar"; //$NON-NLS-1$ //$NON-NLS-2$
				tempFile = new File( SourceConstants.SourceTempDir,
						sourceFile.getName( )
								.replaceAll( "(?i)(\\-)*(\\d)*(\\.)jar", suffix ) //$NON-NLS-1$
								.replaceAll( "(?i)(\\-)*(\\d)*(\\.)zip", suffix ) ); //$NON-NLS-1$
				FileUtil.copyFile( sourceFile.getAbsolutePath( ), tempFile.getAbsolutePath( ) );
				JavaSourceAttacherHandler.attachSource( root, tempFile );
				tempFile.deleteOnExit( );
				SourceBindingUtil.saveSourceBindingRecord( sourceFile, sha, null, tempFile );
				return true;
			}
		}
		catch ( Exception e )
		{
			Logger.debug( e );
		}
		return false;
	}

	private static void refreshJRELibrarySources( final IPackageFragmentRoot pkgRoot )
	{
		Thread thread = new Thread( ) {

			public void run( )
			{
				try
				{
					String sha = HashUtils.sha1Hash( getBinFile( pkgRoot ) );
					String[] files = SourceBindingUtil.getSourceFileBySha( sha );

					IPackageFragmentRoot[] roots = pkgRoot.getJavaProject( ).getAllPackageFragmentRoots( );
					for ( int i = 0; i < roots.length; i++ )
					{
						IPackageFragmentRoot element = roots[i];
						if ( element.equals( pkgRoot ) )
							continue;
						List<String> paths = Arrays.asList( element.getPath( ).segments( ) );
						if ( paths.contains( "jre" ) ) //$NON-NLS-1$
						{
							refreshLibrarySources( element, files );
						}
					} ;
				}
				catch ( JavaModelException e )
				{
					e.printStackTrace( );
				}
			}
		};
		thread.setDaemon( true );
		thread.start( );
	}

	private static void refreshEclipseLibrarySources( final IPackageFragmentRoot pkgRoot,
			final List<String> plugins )
	{
		Thread thread = new Thread( ) {

			public void run( )
			{
				try
				{
					String sha = HashUtils.sha1Hash( getBinFile( pkgRoot ) );
					String[] files = SourceBindingUtil.getSourceFileBySha( sha );

					IPackageFragmentRoot[] roots = pkgRoot.getJavaProject( ).getAllPackageFragmentRoots( );
					for ( int i = 0; i < roots.length; i++ )
					{
						IPackageFragmentRoot element = roots[i];
						if ( element.equals( pkgRoot ) )
							continue;
						String fileName = element.getPath( ).lastSegment( );
						if ( plugins.contains( fileName.split( "_" )[0] ) ) //$NON-NLS-1$
						{
							refreshLibrarySources( element, files );
						}
					} ;
				}
				catch ( JavaModelException e )
				{
					e.printStackTrace( );
				}
			}
		};
		thread.start( );
	}

	public static void updateSourceAttachStatus( final IPackageFragmentRoot root )
	{
		try
		{
			final IClasspathEntry entry = JavaModelUtil.getClasspathEntry( root );

			IPath containerPath = null;
			if ( entry.getEntryKind( ) == IClasspathEntry.CPE_CONTAINER )
			{
				containerPath = entry.getPath( );
			}

			String[] changedAttributes = {
					CPListElement.SOURCEATTACHMENT, "source_encoding"
			};
			BuildPathSupport.modifyClasspathEntry( null,
					entry,
					changedAttributes,
					root.getJavaProject( ),
					containerPath,
					entry.getReferencingEntry( ) != null,
					new NullProgressMonitor( ) );
		}
		catch ( Exception e )
		{
			Logger.debug( e );
		}
	}

	@SuppressWarnings("rawtypes")
	public static boolean isSourceCodeFor( String src, String bin )
	{
		boolean result = false;
		try
		{
			List<String> binList = new ArrayList<String>( );
			ZipFile zf = new ZipFile( bin );
			for ( Enumeration entries = zf.entries( ); entries.hasMoreElements( ); )
			{
				String zipEntryName = ( (ZipEntry) entries.nextElement( ) ).getName( );
				binList.add( zipEntryName );
			}
			zf.close( );

			zf = new ZipFile( src );
			for ( Enumeration entries = zf.entries( ); entries.hasMoreElements( ); )
			{
				String zipEntryName = ( (ZipEntry) entries.nextElement( ) ).getName( );
				String fileBaseName = FilenameUtils.getBaseName( zipEntryName );
				String fileExt = FilenameUtils.getExtension( zipEntryName );
				if ( "java".equals( fileExt ) && fileBaseName != null ) //$NON-NLS-1$
				{
					for ( String zipEntryName2 : binList )
					{
						String fileBaseName2 = FilenameUtils.getBaseName( zipEntryName2 );
						String fileExt2 = FilenameUtils.getExtension( zipEntryName2 );
						if ( "class".equals( fileExt2 ) && fileBaseName.equals( fileBaseName2 ) ) //$NON-NLS-1$
						{
							result = true;
							zf.close( );
							return result;
						}
					}
				}
				binList.add( zipEntryName );
			}
			zf.close( );
		}
		catch ( Exception e )
		{
			Logger.debug( e );
		}

		return result;
	}

	public static boolean isMavenLibrary( IPackageFragmentRoot library )
	{
		try
		{
			IClasspathEntry entry = JavaModelUtil.getClasspathEntry( library );
			return entry.getPath( ).toString( ).indexOf( "MAVEN2_CLASSPATH_CONTAINER" ) != -1; //$NON-NLS-1$
		}
		catch ( Exception e )
		{
			Logger.debug( e );
		}
		return false;
	}

	public static boolean enableMavenDownload( )
	{
		try
		{
			Class<?> clazz = Class.forName( "org.eclipse.m2e.jdt.IClasspathManager" ); //$NON-NLS-1$
			Class<?>[] parameterTypes = new Class[]{
					IPackageFragmentRoot.class, boolean.class, boolean.class
			};
			if ( clazz.getMethod( "scheduleDownload", parameterTypes ) != null ) //$NON-NLS-1$
			{
				return true;
			}
		}
		catch ( ClassNotFoundException ex )
		{
			Logger.debug( "Class org.eclipse.m2e.jdt.IClasspathManager not found.", null ); //$NON-NLS-1$
		}
		catch ( NoSuchMethodException ex )
		{
			Logger.debug( "Method scheduleDownload not found.", null ); //$NON-NLS-1$
		}
		catch ( Exception e )
		{
			Logger.debug( e );
		}
		return false;
	}
}
