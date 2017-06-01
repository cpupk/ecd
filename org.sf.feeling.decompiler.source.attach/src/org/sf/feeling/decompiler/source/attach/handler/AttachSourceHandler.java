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

package org.sf.feeling.decompiler.source.attach.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;
import org.sf.feeling.decompiler.source.attach.IAttachSourceHandler;
import org.sf.feeling.decompiler.source.attach.i18n.Messages;
import org.sf.feeling.decompiler.source.attach.utils.SourceAttachUtil;
import org.sf.feeling.decompiler.util.Logger;

public class AttachSourceHandler implements IAttachSourceHandler
{

	public void execute( final IPackageFragmentRoot library, final boolean showUI )
	{
		if ( !JavaDecompilerPlugin.getDefault( ).isEnableExtension( ) )
		{
			return;
		}

		if ( !showUI && SourceAttachUtil.isMavenLibrary( library ) && SourceAttachUtil.enableMavenDownload( ) )
		{
			return;
		}

		final List<IPackageFragmentRoot> selections = new ArrayList<IPackageFragmentRoot>( );
		selections.add( library );
		if ( !selections.isEmpty( ) )
		{
			if ( showUI )
			{
				final Job job = new Job( Messages.getString( "AttachSourceHandler.Job.Name" ) ) { //$NON-NLS-1$

					protected IStatus run( final IProgressMonitor monitor )
					{
						return JavaSourceAttacherHandler.updateSourceAttachments( selections, monitor );
					}
				};
				job.setPriority( 30 );
				job.schedule( );
			}
			else
			{
				Thread thread = new Thread( ) {

					public void run( )
					{
						JavaSourceAttacherHandler.updateSourceAttachments( selections, null );
					}
				};
				thread.setDaemon( true );
				thread.start( );

			}
		}

	}

	@Override
	public boolean syncAttachSource( IPackageFragmentRoot root )
	{
		if ( !JavaDecompilerPlugin.getDefault( ).isEnableExtension( ) )
		{
			return false;
		}

		try
		{
			final IPath sourcePath = root.getSourceAttachmentPath( );

			if ( sourcePath != null && sourcePath.toOSString( ) != null )
			{
				File tempfile = new File( sourcePath.toOSString( ) );
				if ( tempfile.exists( ) && tempfile.isFile( ) )
				{
					return true;
				}
				return SourceAttachUtil.refreshSourceAttachStatus( root );
			}
		}
		catch ( Exception e )
		{
			Logger.debug( e );
		}
		return false;
	}
}
