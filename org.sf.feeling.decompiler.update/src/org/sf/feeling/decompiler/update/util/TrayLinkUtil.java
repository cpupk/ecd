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
import org.sf.feeling.decompiler.update.DecompilerUpdatePlugin;
import org.sf.feeling.decompiler.update.widget.LinkTrimChecker;

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
			DecompilerUpdatePlugin.getDefault( ).getPreferenceStore( ).setValue( "trayLinkStrategy", //$NON-NLS-1$
					trayLinkValue.toString( ) );
			return true;
		}
		return false;
	}

	public static boolean enableShowTrayLink( )
	{
		return DecompilerUpdatePlugin.getDefault( ).getPreferenceStore( ).contains( "trayLinkStrategy" ) && UserUtil.matchAdCondition( ); //$NON-NLS-1$
	}

	public static String getTrayUrl( )
	{
		String strategyString = DecompilerUpdatePlugin.getDefault( )
				.getPreferenceStore( )
				.getString( "trayLinkStrategy" );
		if ( strategyString == null || "".equals( strategyString ) )
			return null;
		return getRandomTrayUrl( strategyString );
	}

	private static String getRandomTrayUrl( String strategyString )
	{
		JsonValue strategyValue = Json.parse( strategyString );
		if ( strategyValue.isObject( ) )
		{
			JsonObject strategy = strategyValue.asObject( );
			if ( strategy.get( "url" ) != null )
			{
				return strategy.get( "url" ).asString( );
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
				if ( strategy.get( "url" ) != null )
				{
					String url = strategy.get( "url" ).asString( );
					int priority = 1;
					if ( strategy.get( "priority" ) != null )
					{
						priority = strategy.get( "priority" ).asInt( );
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
		String strategyString = DecompilerUpdatePlugin.getDefault( )
				.getPreferenceStore( )
				.getString( "trayLinkStrategy" );
		if ( strategyString == null )
			return null;
		JsonValue strategyValue = Json.parse( strategyString );
		if ( strategyValue.isObject( ) )
		{
			JsonObject strategy = strategyValue.asObject( );
			if ( strategy.get( "showTime" ) != null )
			{
				return strategy.get( "showTime" ).asInt( );
			}
		}
		else if ( strategyValue.isArray( ) )
		{
			JsonArray strategyArray = strategyValue.asArray( );
			List<JsonValue> values = strategyArray.values( );
			for ( int i = 0; i < values.size( ); i++ )
			{
				JsonObject strategy = values.get( i ).asObject( );
				if ( strategy.get( "url" ) != null && url.equals( strategy.get( "url" ).asString( ) ) )
				{
					if ( strategy.get( "showTime" ) != null )
					{
						return strategy.get( "showTime" ).asInt( );
					}
				}
			}
		}
		return null;
	}

	public static boolean isUseExternalBrowser( String url )
	{
		String strategyString = DecompilerUpdatePlugin.getDefault( )
				.getPreferenceStore( )
				.getString( "trayLinkStrategy" );
		if ( strategyString == null )
			return true;
		JsonValue strategyValue = Json.parse( strategyString );
		if ( strategyValue.isObject( ) )
		{
			JsonObject strategy = strategyValue.asObject( );
			if ( strategy.get( "external" ) != null )
			{
				return strategy.get( "external" ).asBoolean( );
			}
		}
		else if ( strategyValue.isArray( ) )
		{
			JsonArray strategyArray = strategyValue.asArray( );
			List<JsonValue> values = strategyArray.values( );
			for ( int i = 0; i < values.size( ); i++ )
			{
				JsonObject strategy = values.get( i ).asObject( );
				if ( strategy.get( "url" ) != null && url.equals( strategy.get( "url" ).asString( ) ) )
				{
					if ( strategy.get( "external" ) != null )
					{
						return strategy.get( "external" ).asBoolean( );
					}
				}
			}
		}
		return true;
	}
}
