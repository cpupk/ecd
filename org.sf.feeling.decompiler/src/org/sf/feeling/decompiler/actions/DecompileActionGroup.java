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

package org.sf.feeling.decompiler.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.PerformanceStats;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.javaeditor.JavaTextSelection;
import org.eclipse.jdt.ui.IContextMenuConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.part.Page;
import org.sf.feeling.decompiler.editor.JavaDecompilerClassFileEditor;
import org.sf.feeling.decompiler.i18n.Messages;

public class DecompileActionGroup extends ActionGroup
{

	public static final String MENU_ID = "org.sf.feeling.decompiler.menu"; //$NON-NLS-1$

	private static final String PERF_DECOMPILE_ACTION_GROUP = "org.sf.feeling.decompiler/perf/DecompileActionGroup"; //$NON-NLS-1$

	private static final String QUICK_MENU_ID = "org.sf.feeling.decompiler.quickMenu"; //$NON-NLS-1$

	private final List<SelectionDispatchAction> fActions = new ArrayList<SelectionDispatchAction>( );

	private String fGroupName = IContextMenuConstants.GROUP_REORGANIZE;

	private JavaDecompilerClassFileEditor fEditor;

	public DecompileActionGroup( IViewPart part )
	{
		this( part.getSite( ), null );
	}

	public DecompileActionGroup( Page page )
	{
		this( page.getSite( ), null );
	}

	public DecompileActionGroup( JavaDecompilerClassFileEditor editor, String groupName, boolean binary )
	{

		final PerformanceStats stats = PerformanceStats.getStats( PERF_DECOMPILE_ACTION_GROUP, this );
		stats.startRun( );

		fEditor = editor;
		fGroupName = groupName;

		stats.endRun( );
	}

	public DecompileActionGroup( IWorkbenchSite site, ISelectionProvider selectionProvider )
	{
		final PerformanceStats stats = PerformanceStats.getStats( PERF_DECOMPILE_ACTION_GROUP, this );
		stats.startRun( );

		stats.endRun( );
	}

	@Override
	public void fillContextMenu( IMenuManager menu )
	{
		super.fillContextMenu( menu );
		addDecompileSubmenu( menu );
	}

	/*
	 * @see ActionGroup#dispose()
	 */
	@Override
	public void dispose( )
	{
		super.dispose( );
	}

	private void addDecompileSubmenu( IMenuManager menu )
	{
		MenuManager decompileSubmenu = new MenuManager(
				Messages.getString( "JavaDecompilerActionBarContributor.Menu.Decompiler" ), //$NON-NLS-1$
				MENU_ID );
		decompileSubmenu.setActionDefinitionId( QUICK_MENU_ID );
		if ( fEditor != null )
		{
			final ITypeRoot element = getEditorInput( );
			if ( element != null && ActionUtil.isOnBuildPath( element ) )
			{
				decompileSubmenu.addMenuListener( new IMenuListener( ) {

					@Override
					public void menuAboutToShow( IMenuManager manager )
					{
						decompileMenuShown( manager );
					}
				} );
				menu.appendToGroup( fGroupName, decompileSubmenu );
			}
		}
	}

	private void decompileMenuShown( IMenuManager decompileSubmenu )
	{
		Menu menu = ( (MenuManager) decompileSubmenu ).getMenu( );
		menu.addMenuListener( new MenuAdapter( ) {

			@Override
			public void menuHidden( MenuEvent e )
			{
				decompileMenuHidden( );
			}
		} );
		ITextSelection textSelection = (ITextSelection) fEditor.getSelectionProvider( ).getSelection( );
		JavaTextSelection javaSelection = new JavaTextSelection( getEditorInput( ),
				getDocument( ),
				textSelection.getOffset( ),
				textSelection.getLength( ) );

		for ( Iterator<SelectionDispatchAction> iter = fActions.iterator( ); iter.hasNext( ); )
		{
			SelectionDispatchAction action = iter.next( );
			action.update( javaSelection );
		}
		decompileSubmenu.removeAll( );
	}

	private void decompileMenuHidden( )
	{
		ITextSelection textSelection = (ITextSelection) fEditor.getSelectionProvider( ).getSelection( );
		for ( Iterator<SelectionDispatchAction> iter = fActions.iterator( ); iter.hasNext( ); )
		{
			SelectionDispatchAction action = iter.next( );
			action.update( textSelection );
		}
	}

	private ITypeRoot getEditorInput( )
	{
		return JavaUI.getEditorInputTypeRoot( fEditor.getEditorInput( ) );
	}

	private IDocument getDocument( )
	{
		return JavaUI.getDocumentProvider( ).getDocument( fEditor.getEditorInput( ) );
	}
}
