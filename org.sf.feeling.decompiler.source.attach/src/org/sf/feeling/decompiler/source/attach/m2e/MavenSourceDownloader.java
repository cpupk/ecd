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

package org.sf.feeling.decompiler.source.attach.m2e;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.m2e.jdt.IClasspathManager;
import org.eclipse.m2e.jdt.MavenJdtPlugin;
import org.eclipse.ui.IEditorPart;
import org.sf.feeling.decompiler.source.attach.handler.AttachSourceHandler;
import org.sf.feeling.decompiler.source.attach.handler.JavaSourceAttacherHandler;
import org.sf.feeling.decompiler.source.attach.utils.SourceAttachUtil;
import org.sf.feeling.decompiler.util.Logger;

@SuppressWarnings("restriction")
public class MavenSourceDownloader
{

	private IPackageFragmentRoot root = null;

	public void downloadSource( IEditorPart part )
	{
		root = null;
		try
		{
			IClasspathManager buildpathManager = MavenJdtPlugin.getDefault( ).getBuildpathManager( );
			IClassFileEditorInput input = (IClassFileEditorInput) part.getEditorInput( );
			IJavaElement element = input.getClassFile( );
			while ( element.getParent( ) != null )
			{
				element = element.getParent( );
				if ( ( element instanceof IPackageFragmentRoot ) )
				{
					root = (IPackageFragmentRoot) element;
					final IPath sourcePath = root.getSourceAttachmentPath( );
					if ( sourcePath != null && sourcePath.toOSString( ) != null )
					{
						File tempfile = new File( sourcePath.toOSString( ) );
						if ( tempfile.exists( ) && tempfile.isFile( ) )
						{
							break;
						}
					}
					buildpathManager.scheduleDownload( root, true, false );

					Thread thread = new Thread( ) {

						public void run( )
						{
							if ( root instanceof PackageFragmentRoot )
							{
								long time = System.currentTimeMillis( );
								PackageFragmentRoot fRoot = (PackageFragmentRoot) root;
								while ( true )
								{
									if ( System.currentTimeMillis( ) - time > 60 * 1000 )
									{
										new AttachSourceHandler( ).execute( root, true );
										break;
									}
									try
									{
										if ( fRoot.getSourceAttachmentPath( ) != null
												&& fRoot.getSourceAttachmentPath( ).toFile( ).exists( ) )
										{
											SourceAttachUtil.updateSourceAttachStatus( fRoot );
											break;
										}
									}
									catch ( JavaModelException e )
									{
										Logger.debug( e );
										break;
									}
								}
							}
						}
					};
					thread.setDaemon( true );
					thread.start( );
				}
			}
		}
		catch ( JavaModelException e )
		{
			Logger.debug( e );
			if ( root != null )
			{
				final List<IPackageFragmentRoot> selections = new ArrayList<IPackageFragmentRoot>( );
				selections.add( root );
				Thread thread = new Thread( ) {

					public void run( )
					{
						JavaSourceAttacherHandler.updateSourceAttachments( selections, null, true );
					}
				};
				thread.setDaemon( true );
				thread.start( );
			}
		}
	}

}
