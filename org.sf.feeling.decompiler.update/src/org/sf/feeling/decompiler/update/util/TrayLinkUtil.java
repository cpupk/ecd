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

package org.sf.feeling.decompiler.update.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.eclipse.swt.widgets.Display;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;
import org.sf.feeling.decompiler.update.widget.LinkTrimChecker;
import org.sf.feeling.decompiler.util.UserUtil;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class TrayLinkUtil
{

	public static void displayTrayLink( )
	{
		Display.getDefault( ).asyncExec( new Runnable( ) {

			public void run( )
			{
				new LinkTrimChecker( ).displayTrayLink( enableShowTrayLink( ) );
			}
		} );
	}

	public static boolean handleTrayLinkJson( JsonValue trayLinkValue )
	{
		if ( trayLinkValue != null )
		{
			JavaDecompilerPlugin.getDefault( ).getPreferenceStore( ).setValue( "trayLinkStrategy", //$NON-NLS-1$
					trayLinkValue.toString( ) );
			return true;
		}
		return false;
	}

	public static boolean enableShowTrayLink( )
	{
		return JavaDecompilerPlugin.getDefault( ).getPreferenceStore( ).contains( "trayLinkStrategy" ) && UserUtil.matchAdCondition( ); //$NON-NLS-1$
	}

	public static String getTrayUrl( )
	{
		String strategyString = JavaDecompilerPlugin.getDefault( )
				.getPreferenceStore( )
				.getString( "trayLinkStrategy" ); //$NON-NLS-1$
		if ( strategyString == null || "".equals( strategyString ) ) //$NON-NLS-1$
			return null;
		return getRandomTrayUrl( strategyString );
	}

	private static String getRandomTrayUrl( String strategyString )
	{
		JsonValue strategyValue = Json.parse( strategyString );
		if ( strategyValue.isObject( ) )
		{
			JsonObject strategy = strategyValue.asObject( );
			if ( strategy.get( "url" ) != null ) //$NON-NLS-1$
			{
				return strategy.get( "url" ).asString( ); //$NON-NLS-1$
			}
		}
		else if ( strategyValue.isArray( ) )
		{
			JsonArray strategyArray = strategyValue.asArray( );
			List<JsonValue> values = strategyArray.values( );
			Map<Integer, String> urlMap = new TreeMap<Integer, String>( );
			int weight = 0;
			for ( int i = 0; i < values.size( ); i++ )
			{
				JsonObject strategy = values.get( i ).asObject( );
				if ( strategy.get( "url" ) != null ) //$NON-NLS-1$
				{
					String url = strategy.get( "url" ).asString( ); //$NON-NLS-1$
					int priority = 1;
					if ( strategy.get( "priority" ) != null ) //$NON-NLS-1$
					{
						priority = strategy.get( "priority" ).asInt( ); //$NON-NLS-1$
					}
					weight += priority;
					urlMap.put( weight, url );
				}
			}

			int randomWeight = new Random( new Random( System.currentTimeMillis( ) ).nextLong( ) ).nextInt( weight * 100 ) / 100;
			Iterator<Integer> iter = urlMap.keySet( ).iterator( );
			while ( iter.hasNext( ) )
			{
				Integer priority = iter.next( );
				if ( randomWeight < priority )
				{
					return urlMap.get( priority );
				}
			}
		}
		return null;
	}

	public static Integer getTrayUrlDisplayTime( String url )
	{
		String strategyString = JavaDecompilerPlugin.getDefault( )
				.getPreferenceStore( )
				.getString( "trayLinkStrategy" ); //$NON-NLS-1$
		if ( strategyString == null )
			return null;
		JsonValue strategyValue = Json.parse( strategyString );
		if ( strategyValue.isObject( ) )
		{
			JsonObject strategy = strategyValue.asObject( );
			if ( strategy.get( "showTime" ) != null ) //$NON-NLS-1$
			{
				return strategy.get( "showTime" ).asInt( ); //$NON-NLS-1$
			}
		}
		else if ( strategyValue.isArray( ) )
		{
			JsonArray strategyArray = strategyValue.asArray( );
			List<JsonValue> values = strategyArray.values( );
			for ( int i = 0; i < values.size( ); i++ )
			{
				JsonObject strategy = values.get( i ).asObject( );
				if ( strategy.get( "url" ) != null && url.equals( strategy.get( "url" ).asString( ) ) ) //$NON-NLS-1$ //$NON-NLS-2$
				{
					if ( strategy.get( "showTime" ) != null ) //$NON-NLS-1$
					{
						return strategy.get( "showTime" ).asInt( ); //$NON-NLS-1$
					}
				}
			}
		}
		return null;
	}

	public static boolean isUseExternalBrowser( String url )
	{
		String strategyString = JavaDecompilerPlugin.getDefault( )
				.getPreferenceStore( )
				.getString( "trayLinkStrategy" ); //$NON-NLS-1$
		if ( strategyString == null )
			return true;
		JsonValue strategyValue = Json.parse( strategyString );
		if ( strategyValue.isObject( ) )
		{
			JsonObject strategy = strategyValue.asObject( );
			if ( strategy.get( "external" ) != null ) //$NON-NLS-1$
			{
				return strategy.get( "external" ).asBoolean( ); //$NON-NLS-1$
			}
		}
		else if ( strategyValue.isArray( ) )
		{
			JsonArray strategyArray = strategyValue.asArray( );
			List<JsonValue> values = strategyArray.values( );
			for ( int i = 0; i < values.size( ); i++ )
			{
				JsonObject strategy = values.get( i ).asObject( );
				if ( strategy.get( "url" ) != null && url.equals( strategy.get( "url" ).asString( ) ) ) //$NON-NLS-1$ //$NON-NLS-2$
				{
					if ( strategy.get( "external" ) != null ) //$NON-NLS-1$
					{
						return strategy.get( "external" ).asBoolean( ); //$NON-NLS-1$
					}
				}
			}
		}
		return true;
	}
	
	public static boolean useSystemColor( String url )
	{
		String strategyString = JavaDecompilerPlugin.getDefault( )
				.getPreferenceStore( )
				.getString( "trayLinkStrategy" ); //$NON-NLS-1$
		if ( strategyString == null )
			return true;
		JsonValue strategyValue = Json.parse( strategyString );
		if ( strategyValue.isObject( ) )
		{
			JsonObject strategy = strategyValue.asObject( );
			if ( strategy.get( "useSystemColor" ) != null ) //$NON-NLS-1$
			{
				return strategy.get( "useSystemColor" ).asBoolean( ); //$NON-NLS-1$
			}
		}
		else if ( strategyValue.isArray( ) )
		{
			JsonArray strategyArray = strategyValue.asArray( );
			List<JsonValue> values = strategyArray.values( );
			for ( int i = 0; i < values.size( ); i++ )
			{
				JsonObject strategy = values.get( i ).asObject( );
				if ( strategy.get( "url" ) != null && url.equals( strategy.get( "url" ).asString( ) ) ) //$NON-NLS-1$ //$NON-NLS-2$
				{
					if ( strategy.get( "useSystemColor" ) != null ) //$NON-NLS-1$
					{
						return strategy.get( "useSystemColor" ).asBoolean( ); //$NON-NLS-1$
					}
				}
			}
		}
		return true;
	}
}
