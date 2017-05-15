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

package org.sf.feeling.decompiler.util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class MarkUtil
{

	private final static String MARK = "/** <a href=\"http://www.cpupk.com/decompiler\">Eclipse Class Decompiler</a> plugin, Copyright (c) 2017 Chen Chao. **/"; //$NON-NLS-1$
	private final static String SOURCE_MARK = "/** <a href=\"http://www.cpupk.com/decompiler\">Eclipse Class Decompiler</a> plugin, Copyright (c) 2017 Chen Chao. */"; //$NON-NLS-1$

	private final static Map<String, Integer> MARKS = new LinkedHashMap<String, Integer>( );
	private final static Map<String, Integer> SOURCE_MARKS = new LinkedHashMap<String, Integer>( );
	private static int MARK_WEIGHT = 0;
	private static int SOURCE_MARK_WEIGHT = 0;
	static
	{
		MARKS.put( MARK, 1 );
		SOURCE_MARKS.put( SOURCE_MARK, 1 );
	}

	public static boolean containsMark( String source )
	{
		if ( source == null )
		{
			return false;
		}
		int index = source.indexOf( "\n" );
		if ( index != -1 )
		{
			source = source.substring( 0, index );
		}

		Iterator<String> marks = MARKS.keySet( ).iterator( );
		while ( marks.hasNext( ) )
		{
			String mark = marks.next( );
			if ( source.startsWith( mark ) )
				return true;
		}
		return false;
	}

	public static boolean containsSourceMark( String source )
	{
		if ( source == null )
		{
			return false;
		}
		int index = source.indexOf( "\n" );
		if ( index != -1 )
		{
			source = source.substring( 0, index );
		}

		Iterator<String> marks = SOURCE_MARKS.keySet( ).iterator( );
		while ( marks.hasNext( ) )
		{
			String mark = marks.next( );
			if ( source.startsWith( mark ) )
				return true;
		}
		return false;
	}

	public static String getMark( String source )
	{
		if ( source == null )
		{
			return "";
		}
		int index = source.indexOf( "\n" );
		if ( index != -1 )
		{
			source = source.substring( 0, index );
		}

		Iterator<String> marks = SOURCE_MARKS.keySet( ).iterator( );
		while ( marks.hasNext( ) )
		{
			String mark = marks.next( );
			if ( source.startsWith( mark ) )
				return mark;
		}
		return "";
	}

	public static String getRandomSourceMark( )
	{
		if ( SOURCE_MARKS.size( ) == 1 )
		{
			return SOURCE_MARKS.keySet( ).iterator( ).next( );
		}
		else
		{
			int weight = new Random(
					new Random( System.currentTimeMillis( ) ).nextLong( ) )
							.nextInt( SOURCE_MARK_WEIGHT * 100 )
					/ 100;
			Iterator<String> marks = SOURCE_MARKS.keySet( ).iterator( );
			marks.next( );
			int totalWeigth = 0;
			while ( marks.hasNext( ) )
			{
				String mark = marks.next( );
				totalWeigth += SOURCE_MARKS.get( mark );
				if ( weight < totalWeigth )
				{
					return mark;
				}
			}
			return SOURCE_MARKS.keySet( ).iterator( ).next( );
		}
	}

	public static String getRandomMark( )
	{
		if ( MARKS.size( ) == 1 )
		{
			return MARKS.keySet( ).iterator( ).next( );
		}
		else
		{
			int weight = new Random(
					new Random( System.currentTimeMillis( ) ).nextLong( ) )
							.nextInt( MARK_WEIGHT * 100 )
					/ 100;
			Iterator<String> marks = MARKS.keySet( ).iterator( );
			marks.next( );
			int totalWeigth = 0;
			while ( marks.hasNext( ) )
			{
				String mark = marks.next( );
				totalWeigth += MARKS.get( mark );
				if ( weight < totalWeigth )
				{
					return mark;
				}
			}
			return MARKS.keySet( ).iterator( ).next( );
		}
	}

	public static void addMark( String mark, int weight )
	{
		if ( mark != null && !MARKS.containsKey( mark ) )
		{
			MARKS.put( mark, weight );
			MARK_WEIGHT += weight;
		}
	}

	public static void addSourceMark( String mark, int weight )
	{
		if ( mark != null && !SOURCE_MARKS.containsKey( mark ) )
		{
			SOURCE_MARKS.put( mark, weight );
			SOURCE_MARK_WEIGHT += weight;
		}
	}
}
