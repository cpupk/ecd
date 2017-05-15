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

package org.sf.feeling.decompiler.update.widget;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.swt.widgets.Composite;

public class StandardTrimExtension
{

	@Inject
	EModelService modelService;

	private StatusLineManager manager;

	@PostConstruct
	void createWidget( Composite parent, MToolControl toolControl )
	{
		if ( toolControl.getElementId( ).equals( "org.sf.feeling.decompiler.update.widget.HtmlLinkTrimItem" ) ) //$NON-NLS-1$
		{
			createTrayLink( parent, toolControl );
		}
	}

	@PreDestroy
	void destroy( )
	{
		if ( manager != null )
		{
			manager.dispose( );
			manager = null;
		}
	}

	/**
	 * @param parent
	 * @param toolControl
	 */
	private void createTrayLink( Composite parent, MToolControl toolControl )
	{
		new HtmlLinkTrimItem( parent );
	}
}
