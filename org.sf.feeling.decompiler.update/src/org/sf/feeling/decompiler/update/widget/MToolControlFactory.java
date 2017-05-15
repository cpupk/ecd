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

import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.sf.feeling.decompiler.util.Logger;
import org.sf.feeling.decompiler.util.ReflectionUtils;

@SuppressWarnings({
		"rawtypes"
})
public class MToolControlFactory
{

	public static MToolControl getToolControl( EModelService modelService )
	{
		if ( supportV1( modelService ) )
		{
			return MToolControlV1.createToolControl( modelService );
		}
		else if ( supportV2( ) )
		{
			return MToolControlV2.createToolControl( );
		}
		return null;
	}

	private static boolean supportV2( )
	{
		try
		{
			Class clazz = Class.forName( "org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl" );
			return ReflectionUtils.getDeclaredMethod( clazz, "createToolControl", new Class[0] ) != null;
		}
		catch ( Exception e )
		{
			Logger.debug( e );
		}
		return false;
	}

	private static boolean supportV1( EModelService modelService )
	{
		return ReflectionUtils.getDeclaredMethod( modelService, "createModelElement", new Class[]{
				MToolControl.class
		} ) != null;
	}
}
