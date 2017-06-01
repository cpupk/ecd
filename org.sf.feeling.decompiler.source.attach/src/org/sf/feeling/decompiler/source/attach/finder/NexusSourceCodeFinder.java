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

package org.sf.feeling.decompiler.source.attach.finder;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.sf.feeling.decompiler.source.attach.utils.SourceAttachUtil;
import org.sf.feeling.decompiler.source.attach.utils.SourceBindingUtil;
import org.sf.feeling.decompiler.source.attach.utils.UrlDownloader;
import org.sf.feeling.decompiler.util.HashUtils;
import org.sf.feeling.decompiler.util.Logger;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.SearchNGResponse;
import org.sonatype.nexus.rest.model.SearchResponse;

public class NexusSourceCodeFinder extends AbstractSourceCodeFinder implements SourceCodeFinder
{

	private boolean canceled = false;
	private String serviceUrl;

	public NexusSourceCodeFinder( String serviceUrl )
	{
		this.serviceUrl = serviceUrl;
	}

	@Override
	public String toString( )
	{
		return this.getClass( ) + "; serviceUrl=" + serviceUrl; //$NON-NLS-1$
	}

	public void cancel( )
	{
		this.canceled = true;
	}

	public void find( String binFile, List<SourceFileResult> results )
	{
		Collection<GAV> gavs = new HashSet<GAV>( );
		try
		{
			String sha1 = HashUtils.sha1Hash( new File( binFile ) );
			gavs.addAll( findArtifactsUsingNexus( null, null, null, null, sha1, false ) );
		}
		catch ( Throwable e )
		{
			Logger.debug( e );
		}

		if ( canceled )
			return;

		if ( gavs.isEmpty( ) )
		{
			try
			{
				gavs.addAll( findGAVFromFile( binFile ) );
			}
			catch ( Throwable e )
			{
				Logger.debug( e );
			}
		}

		if ( canceled )
			return;

		Map<GAV, String> sourcesUrls = new HashMap<GAV, String>( );
		try
		{
			sourcesUrls.putAll( findSourcesUsingNexus( gavs ) );
		}
		catch ( Throwable e )
		{
			Logger.debug( e );
		}

		for ( Map.Entry<GAV, String> entry : sourcesUrls.entrySet( ) )
		{
			try
			{
				String[] sourceFiles = SourceBindingUtil.getSourceFileByDownloadUrl( entry.getValue( ) );
				if ( sourceFiles != null && sourceFiles[0] != null && new File( sourceFiles[0] ).exists( ) )
				{
					File sourceFile = new File( sourceFiles[0] );
					File tempFile = new File( sourceFiles[1] );
					SourceFileResult result = new SourceFileResult( this, binFile, sourceFile, tempFile, 100 );
					results.add( result );
					return;
				}
			}
			catch ( Throwable e )
			{
				Logger.debug( e );
			}
		}

		for ( Map.Entry<GAV, String> entry : sourcesUrls.entrySet( ) )
		{
			String name = entry.getKey( ).getA( ) + '-' + entry.getKey( ).getV( ) + "-sources.jar"; //$NON-NLS-1$
			try
			{
				String tmpFile = new UrlDownloader( ).download( entry.getValue( ) );
				if ( tmpFile != null
						&& new File( tmpFile ).exists( )
						&& SourceAttachUtil.isSourceCodeFor( tmpFile, binFile ) )
				{
					setDownloadUrl( entry.getValue( ) );
					SourceFileResult object = new SourceFileResult( this, binFile, tmpFile, name, 100 );
					Logger.debug( this.toString( ) + " FOUND: " + object, null ); //$NON-NLS-1$
					results.add( object );

				}
			}
			catch ( Throwable e )
			{
				Logger.debug( e );
			}
		}
	}

	private Map<GAV, String> findSourcesUsingNexus( Collection<GAV> gavs ) throws Exception
	{
		Map<GAV, String> results = new HashMap<GAV, String>( );
		for ( GAV gav : gavs )
		{
			if ( canceled )
				return results;
			Set<GAV> gavs2 = findArtifactsUsingNexus( gav.getG( ), gav.getA( ), gav.getV( ), "sources", null, true ); //$NON-NLS-1$
			for ( GAV gav2 : gavs2 )
			{
				results.put( gav, gav2.getArtifactLink( ) );
			}
		}

		return results;
	}

	private Set<GAV> findArtifactsUsingNexus( String g, String a, String v, String c, String sha1, boolean getLink )
			throws Exception
	{
		// http://repository.sonatype.org/service/local/lucene/search?sha1=686ef3410bcf4ab8ce7fd0b899e832aaba5facf7
		// http://repository.sonatype.org/service/local/data_index?sha1=686ef3410bcf4ab8ce7fd0b899e832aaba5facf7
		Set<GAV> results = new HashSet<GAV>( );
		String nexusUrl = getNexusContextUrl( );

		String[] endpoints = new String[]{
				nexusUrl + "service/local/data_index"/*
														 * //$NON-NLS-1$ ,
														 * nexusUrl +
														 * "service/local/lucene/search"
														 */
		};
		for ( String endpoint : endpoints )
		{
			if ( canceled )
				return results;
			String urlStr = endpoint;
			LinkedHashMap<String, String> params = new LinkedHashMap<String, String>( );
			if ( g != null )
			{
				params.put( "g", g ); //$NON-NLS-1$
			}
			if ( a != null )
			{
				params.put( "a", a ); //$NON-NLS-1$
			}
			if ( v != null )
			{
				params.put( "v", v ); //$NON-NLS-1$
			}
			if ( c != null )
			{
				params.put( "c", c ); //$NON-NLS-1$
			}
			if ( sha1 != null )
			{
				params.put( "sha1", sha1 ); //$NON-NLS-1$
			}
			for ( Map.Entry<String, String> entry : params.entrySet( ) )
			{
				if ( !urlStr.endsWith( "&" ) && !urlStr.endsWith( "?" ) ) //$NON-NLS-1$ //$NON-NLS-2$
				{
					if ( urlStr.indexOf( '?' ) == -1 )
						urlStr += "?"; //$NON-NLS-1$
					else
						urlStr += "&"; //$NON-NLS-1$
				}
				urlStr += URLEncoder.encode( entry.getKey( ), "UTF-8" ) //$NON-NLS-1$
						+ "=" //$NON-NLS-1$
						+ URLEncoder.encode( entry.getValue( ), "UTF-8" ); //$NON-NLS-1$
			}

			JAXBContext context = JAXBContext.newInstance( SearchResponse.class, SearchNGResponse.class );
			Unmarshaller unmarshaller = context.createUnmarshaller( );
			URLConnection connection = new URL( urlStr ).openConnection( );
			connection.setConnectTimeout( 5000 );
			connection.setReadTimeout( 5000 );
			connection.connect( );
			try
			{
				Object resp = unmarshaller.unmarshal( connection.getInputStream( ) );
				if ( resp instanceof SearchResponse )
				{
					SearchResponse srsp = (SearchResponse) resp;
					for ( NexusArtifact ar : srsp.getData( ) )
					{
						GAV gav = new GAV( );
						gav.setG( ar.getGroupId( ) );
						gav.setA( ar.getArtifactId( ) );
						gav.setV( ar.getVersion( ) );
						if ( getLink )
							gav.setArtifactLink( ar.getArtifactLink( ) );
						results.add( gav );
					}
				}
			}
			catch ( Throwable e )
			{
				Logger.debug( e );
			}
		}
		return results;
	}

	private String getNexusContextUrl( )
	{
		String result = serviceUrl.substring( 0, serviceUrl.lastIndexOf( '/' ) );
		if ( !result.endsWith( "/" ) ) //$NON-NLS-1$
		{
			result += '/';
		}
		return result;
	}
}