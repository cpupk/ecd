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

import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.sf.feeling.decompiler.util.ReflectionUtils;

public class TrayLinkV1
{

	public static final String TRIM_CONTRIBUTION_URI = "bundleclass://org.sf.feeling.decompiler.update/org.sf.feeling.decompiler.update.widget.StandardTrimExtension"; //$NON-NLS-1$

	public static void displayTrayLink( boolean show )
	{
		IWorkbenchWindow window = PlatformUI.getWorkbench( ).getActiveWorkbenchWindow( );
		EModelService modelService = (EModelService) ReflectionUtils.getFieldValue( window, "modelService" ); //$NON-NLS-1$
		MTrimmedWindow model = (MTrimmedWindow) ReflectionUtils.getFieldValue( window, "model" ); //$NON-NLS-1$
		MTrimBar bottomTrim = modelService.getTrim( model, SideValue.BOTTOM );

		MToolControl hsElement = (MToolControl) modelService.find(
				"org.sf.feeling.decompiler.update.widget.HtmlLinkTrimItem", //$NON-NLS-1$
				model );
		if ( hsElement == null )
		{
			hsElement = MToolControlFactory.getToolControl( modelService );
			if ( hsElement != null )
			{
				hsElement.setElementId( "org.sf.feeling.decompiler.update.widget.HtmlLinkTrimItem" ); //$NON-NLS-1$
				hsElement.setContributionURI( TRIM_CONTRIBUTION_URI );
				hsElement.getTags( ).add( "Draggable" ); //$NON-NLS-1$
				bottomTrim.getChildren( ).add( hsElement );
			}
			else
			{
				return;
			}
		}
		hsElement.setToBeRendered( show );
	}
}
