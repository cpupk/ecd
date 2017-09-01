/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.source.attach.finder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;

import org.apache.commons.io.IOUtils;
import org.sf.feeling.decompiler.util.Logger;

public abstract class AbstractSourceCodeFinder implements SourceCodeFinder
{

	protected String downloadUrl;

	@Override
	public String getDownloadUrl( )
	{
		return downloadUrl;
	}

	public void setDownloadUrl( String downloadUrl )
	{
		this.downloadUrl = downloadUrl;
	}

	protected String getString( URL url ) throws Exception
	{
		String result = null;
		try
		{
			URLConnection con = url.openConnection( );
			con.setRequestProperty( "User-Agent", //$NON-NLS-1$
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36" ); //$NON-NLS-1$
			con.setRequestProperty( "Accept-Encoding", "gzip,deflate" ); //$NON-NLS-1$ //$NON-NLS-2$
			con.setConnectTimeout( 5000 );
			con.setReadTimeout( 5000 );
			InputStream is = null;
			byte[] bytes = null;
			InputStream conIs = null;
			try
			{
				try
				{
					conIs = con.getInputStream( );
					bytes = IOUtils.toByteArray( conIs );
					is = new GZIPInputStream( new ByteArrayInputStream( bytes ) );
					result = IOUtils.toString( is );
				}
				catch ( Exception e )
				{
					if ( bytes != null )
					{
						is = new ByteArrayInputStream( bytes );
						result = IOUtils.toString( is );
					}
				}
			}
			finally
			{
				IOUtils.closeQuietly( conIs );
				IOUtils.closeQuietly( is );
			}
		}
		catch ( Exception e )
		{
			Logger.debug( e );
		}
		return result == null ? "" : result; //$NON-NLS-1$
	}

	protected Collection<GAV> findGAVFromFile( String binFile ) throws Exception
	{
		Set<GAV> gavs = new HashSet<GAV>( );

		// META-INF/maven/commons-beanutils/commons-beanutils/pom.properties
		ZipInputStream in = new ZipInputStream( new FileInputStream( binFile ) );
		byte[] data = new byte[2048];
		do
		{
			ZipEntry entry = in.getNextEntry( );
			if ( entry == null )
			{
				break;
			}

			String zipEntryName = entry.getName( );
			if ( zipEntryName.startsWith( "META-INF/maven/" ) && zipEntryName.endsWith( "/pom.properties" ) ) //$NON-NLS-1$ //$NON-NLS-2$
			{
				ByteArrayOutputStream os = new ByteArrayOutputStream( );
				do
				{
					int read = in.read( data );
					if ( read < 0 )
						break;
					os.write( data, 0, read );
				} while ( true );
				Properties props = new Properties( );
				props.load( new ByteArrayInputStream( os.toByteArray( ) ) );
				String version = props.getProperty( "version" ); //$NON-NLS-1$
				String groupId = props.getProperty( "groupId" ); //$NON-NLS-1$
				String artifactId = props.getProperty( "artifactId" ); //$NON-NLS-1$
				if ( version != null && groupId != null && artifactId != null )
				{
					GAV gav = new GAV( );
					gav.setG( groupId );
					gav.setA( artifactId );
					gav.setV( version );
					gavs.add( gav );
				}
			}
		} while ( true );
		in.close( );

		if ( gavs.size( ) > 1 )
			gavs.clear( ); // a merged file, the result will not be correct
		return gavs;
	}

	protected String getText( HTMLDocument doc, HTMLDocument.Iterator iterator ) throws BadLocationException
	{
		int startOffset = iterator.getStartOffset( );
		int endOffset = iterator.getEndOffset( );
		int length = endOffset - startOffset;
		String text = doc.getText( startOffset, length );
		return text;
	}
}