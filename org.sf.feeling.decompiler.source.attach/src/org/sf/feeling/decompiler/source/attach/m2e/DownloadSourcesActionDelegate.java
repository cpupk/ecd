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

import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.sf.feeling.decompiler.source.attach.utils.SourceAttachUtil;

@SuppressWarnings("restriction")
public class DownloadSourcesActionDelegate implements IEditorActionDelegate
{

	public void setActiveEditor( IAction action, IEditorPart part )
	{
		if ( ( part != null ) && ( ( part.getEditorInput( ) instanceof IClassFileEditorInput ) ) )
		{
			if ( SourceAttachUtil.enableMavenDownload( ) )
			{
				new MavenSourceDownloader( ).downloadSource( part );
			}
		}
	}

	public void run( IAction action )
	{
	}

	public void selectionChanged( IAction action, ISelection selection )
	{
	}
}
