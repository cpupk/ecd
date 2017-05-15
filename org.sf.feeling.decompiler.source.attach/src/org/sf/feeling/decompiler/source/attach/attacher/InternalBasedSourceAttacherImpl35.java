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

package org.sf.feeling.decompiler.source.attach.attacher;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathSupport;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.eclipse.swt.widgets.Shell;
import org.sf.feeling.decompiler.source.attach.utils.MethodUtils;
import org.sf.feeling.decompiler.util.Logger;

@SuppressWarnings("restriction")
public class InternalBasedSourceAttacherImpl35 implements SourceAttacher
{

	@Override
	public boolean attachSource( final IPackageFragmentRoot fRoot, final File newSourcePath ) throws CoreException
	{
		try
		{
			IPath fContainerPath = null;
			IClasspathEntry fEntry = null;
			if ( fRoot == null || fRoot.getKind( ) != IPackageFragmentRoot.K_BINARY )
			{
				Logger.debug( "error(!=K_BINARY)", null ); //$NON-NLS-1$
				return false;
			}
			IPath containerPath = null;
			final IJavaProject jproject = fRoot.getJavaProject( );
			IClasspathEntry entry = fRoot.getRawClasspathEntry( );
			if ( entry == null )
			{
				entry = JavaCore.newLibraryEntry( fRoot.getPath( ), (IPath) null, (IPath) null );
			}
			else if ( entry.getEntryKind( ) == IClasspathEntry.CPE_CONTAINER )
			{
				containerPath = entry.getPath( );
				final ClasspathContainerInitializer initializer = JavaCore
						.getClasspathContainerInitializer( containerPath.segment( 0 ) );
				final IClasspathContainer container = JavaCore.getClasspathContainer( containerPath, jproject );
				if ( initializer == null || container == null )
				{
					Logger.debug( "error(initializer == null || container == null)", null ); //$NON-NLS-1$
					return false;
				}
				final IStatus status = initializer.getSourceAttachmentStatus( containerPath, jproject );
				if ( status.getCode( ) == ClasspathContainerInitializer.ATTRIBUTE_NOT_SUPPORTED )
				{
					Logger.debug( "error(ATTRIBUTE_NOT_SUPPORTED)", null ); //$NON-NLS-1$
					return false;
				}
				if ( status.getCode( ) == ClasspathContainerInitializer.ATTRIBUTE_READ_ONLY )
				{
					Logger.debug( "error(ATTRIBUTE_READ_ONLY)", null ); //$NON-NLS-1$
					return false;
				}
				entry = JavaModelUtil.findEntryInContainer( container, fRoot.getPath( ) );
				Assert.isNotNull( (Object) entry );
			}
			fContainerPath = containerPath;
			fEntry = entry;
			final CPListElement elem = CPListElement.createFromExisting( fEntry, (IJavaProject) null );
			IPath srcAttPath = Path.fromOSString( newSourcePath.getAbsolutePath( ) ).makeAbsolute( );
			if ( fEntry.getEntryKind( ) == IClasspathEntry.CPE_VARIABLE )
			{
				final File sourceAttacherDir = newSourcePath.getParentFile( );
				JavaCore.setClasspathVariable( "SOURCE_ATTACHER", //$NON-NLS-1$
						(IPath) new Path( sourceAttacherDir.getAbsolutePath( ) ),
						(IProgressMonitor) null );
				srcAttPath = (IPath) new Path( "SOURCE_ATTACHER/" + newSourcePath.getName( ) ); //$NON-NLS-1$
			}
			elem.setAttribute( "sourcepath", (Object) srcAttPath ); //$NON-NLS-1$
			final IClasspathEntry entry2 = elem.getClasspathEntry( );
			if ( entry2.equals( fEntry ) )
			{
				Logger.debug( "NO CHANGE", null ); //$NON-NLS-1$
				return true;
			}
			final IClasspathEntry newEntry = entry2;
			final String[] changedAttributes = {
					"sourcepath" //$NON-NLS-1$
			};
			try
			{
				MethodUtils.invokeExactStaticMethod( (Class<?>) BuildPathSupport.class,
						"modifyClasspathEntry", //$NON-NLS-1$
						new Object[]{
								null, newEntry, changedAttributes, jproject, fContainerPath, new NullProgressMonitor( )
						},
						new Class[]{
								Shell.class,
								IClasspathEntry.class,
								String[].class,
								IJavaProject.class,
								IPath.class,
								IProgressMonitor.class
						} );
			}
			catch ( NoSuchMethodException e )
			{
				throw new RuntimeException( e );
			}
			catch ( IllegalAccessException e2 )
			{
				throw new RuntimeException( e2 );
			}
		}
		catch ( InvocationTargetException e3 )
		{
			Logger.debug( "error", e3 ); //$NON-NLS-1$
			return false;
		}
		catch ( CoreException e4 )
		{
			Logger.debug( "error", (Throwable) e4 ); //$NON-NLS-1$
			return false;
		}
		return true;
	}
}