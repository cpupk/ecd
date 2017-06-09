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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;
import org.sf.feeling.decompiler.util.Logger;

@SuppressWarnings({
		"rawtypes", "unchecked"
})
public class AttachSourceContributionFactory extends ExtensionContributionFactory
{

	@Override
	public void createContributionItems( IServiceLocator serviceLocator, IContributionRoot additions )
	{
		if ( !JavaDecompilerPlugin.getDefault( ).isEnableExtension( ) )
		{
			return;
		}

		final ISelectionService selService = serviceLocator.getService( ISelectionService.class );
		final List selectedJars = getSelectedElements( selService, IPackageFragmentRoot.class );
		boolean attachRoot = ( selectedJars.size( ) == 1 );
		if ( attachRoot )
		{
			additions.addContributionItem( new ActionContributionItem( new AttachSourceAction( selectedJars ) ),
					new Expression( ) {

						@Override
						public EvaluationResult evaluate( IEvaluationContext context ) throws CoreException
						{
							boolean menuVisible = isMenuVisible( selectedJars );

							if ( menuVisible )
								return EvaluationResult.TRUE;

							return EvaluationResult.FALSE;
						}
					} );
			return;
		}

		if ( selectedJars.size( ) > 1 )
			return;

		final List selectedPackages = getSelectedElements( selService, IPackageFragment.class );
		final List selectedClasses = getSelectedElements( selService, IClassFile.class );
		selectedClasses.addAll( selectedPackages );
		boolean attachClasses = ( !selectedClasses.isEmpty( ) );
		if ( attachClasses )
		{
			additions.addContributionItem( new ActionContributionItem( new AttachSourceAction( selectedClasses ) ),
					new Expression( ) {

						@Override
						public EvaluationResult evaluate( IEvaluationContext context ) throws CoreException
						{
							boolean menuVisible = isMenuVisible( selectedClasses );

							if ( menuVisible )
								return EvaluationResult.TRUE;

							return EvaluationResult.FALSE;
						}
					} );
			return;
		}

	}

	private boolean isMenuVisible( List selection )
	{
		IPackageFragmentRoot root = null;
		for ( int i = 0; i < selection.size( ); i++ )
		{
			IPackageFragmentRoot packRoot = null;
			Object obj = selection.get( i );
			if ( obj instanceof IPackageFragment )
			{
				IPackageFragment packageFragment = (IPackageFragment) obj;
				packRoot = (IPackageFragmentRoot) packageFragment.getParent( );
			}
			else if ( obj instanceof IClassFile )
			{
				IClassFile classFile = (IClassFile) obj;
				packRoot = (IPackageFragmentRoot) classFile.getParent( ).getParent( );
			}
			else if ( obj instanceof IPackageFragmentRoot )
			{
				packRoot = (IPackageFragmentRoot) obj;
			}
			else
				return false;
			if ( root == null )
			{
				root = packRoot;
			}
			else
			{
				if ( root != packRoot )
					return false;
			}
		}
		try
		{
			if ( root != null
					&& root.getSourceAttachmentPath( ) != null
					&& root.getSourceAttachmentPath( ).toFile( ).exists( )
					&& !root.getPath( ).toFile( ).equals( root.getSourceAttachmentPath( ).toFile( ) ) )
			{
				return false;
			}
		}
		catch ( JavaModelException e )
		{
			Logger.debug( e );
		}
		return true;
	}

	private List getSelectedElements( ISelectionService selService, Class eleClass )
	{

		Iterator selections = getSelections( selService );
		List elements = new ArrayList( );

		while ( ( selections != null ) && selections.hasNext( ) )
		{
			Object select = selections.next( );

			if ( eleClass.isInstance( select ) )
				elements.add( select );
		}

		return elements;
	}

	private Iterator getSelections( ISelectionService selService )
	{
		ISelection selection = selService.getSelection( );

		if ( selection != null )
		{
			if ( selection instanceof IStructuredSelection )
			{
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				return structuredSelection.iterator( );
			}
		}

		return null;
	}

}
