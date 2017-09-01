/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Chen Chao  - initial API and implementation
 *******************************************************************************/

package org.sf.feeling.decompiler.jad.decompiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class StreamRedirectThread extends Thread
{

	private final InputStream in;
	private final OutputStream out;
	private Exception ex;

	private static final int BUFFER_SIZE = 2048;

	StreamRedirectThread( String name, InputStream in, OutputStream out )
	{
		super( name );
		this.in = in;
		this.out = out;
		setPriority( Thread.MAX_PRIORITY - 1 );
	}

	public Exception getException( )
	{
		return ex;
	}

	/**
	 * Copy.
	 */
	@Override
	public void run( )
	{
		try
		{
			byte[] cbuf = new byte[BUFFER_SIZE];
			int count;
			while ( ( count = in.read( cbuf, 0, BUFFER_SIZE ) ) >= 0 )
			{
				out.write( cbuf, 0, count );
				out.flush( );
			}
		}
		catch ( IOException exc )
		{
			// System.err.println("Child I/O Transfer - " + exc);
			ex = exc;
		}
	}
}