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

package org.sf.feeling.decompiler.cfr.decompiler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.Path;
import org.sf.feeling.decompiler.editor.BaseDecompilerSourceMapper;

public class CfrSourceMapper extends BaseDecompilerSourceMapper
{

	public CfrSourceMapper( )
	{
		super( new Path( "." ), "" ); //$NON-NLS-1$ //$NON-NLS-2$
		origionalDecompiler = new CfrDecompiler( );
	}

	@Override
	protected void printDecompileReport( StringBuffer source, String fileLocation, Collection exceptions,
			long decompilationTime )
	{
		String location = "\tDecompiled from: " //$NON-NLS-1$
				+ fileLocation;
		source.append( "\n\n/*" ); //$NON-NLS-1$
		source.append( "\n\tDECOMPILATION REPORT\n\n" ); //$NON-NLS-1$
		source.append( location ).append( "\n" ); //$NON-NLS-1$
		source.append( "\tTotal time: " ) //$NON-NLS-1$
				.append( decompilationTime )
				.append( " ms\n" ); //$NON-NLS-1$
		source.append( "\t" //$NON-NLS-1$
				+ origionalDecompiler.getLog( )
						.replaceAll( "\t", "" ) //$NON-NLS-1$ //$NON-NLS-2$
						.replaceAll( "\n\\s*", "\n\t" ) ); //$NON-NLS-1$ //$NON-NLS-2$
		exceptions.addAll( origionalDecompiler.getExceptions( ) );
		logExceptions( exceptions, source );
		source.append( "\n*/" ); //$NON-NLS-1$
	}

	protected void logExceptions( Collection exceptions, StringBuffer buffer )
	{
		if ( !exceptions.isEmpty( ) )
		{
			buffer.append( "\n\tCaught exceptions:" ); //$NON-NLS-1$
			if ( exceptions == null || exceptions.size( ) == 0 )
				return; // nothing to do
			buffer.append( "\n" ); //$NON-NLS-1$
			StringWriter stackTraces = new StringWriter( );
			PrintWriter stackTracesP = new PrintWriter( stackTraces );

			Iterator i = exceptions.iterator( );
			while ( i.hasNext( ) )
			{
				( (Exception) i.next( ) ).printStackTrace( stackTracesP );
				stackTracesP.println( "" ); //$NON-NLS-1$
			}

			stackTracesP.flush( );
			stackTracesP.close( );
			buffer.append( stackTraces.toString( ) );
		}
	}
}