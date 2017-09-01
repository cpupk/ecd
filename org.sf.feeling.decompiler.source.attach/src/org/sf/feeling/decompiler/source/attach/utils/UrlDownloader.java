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

package org.sf.feeling.decompiler.source.attach.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.manager.BasicScmManager;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.accurev.AccuRevScmProvider;
import org.apache.maven.scm.provider.bazaar.BazaarScmProvider;
import org.apache.maven.scm.provider.clearcase.ClearCaseScmProvider;
import org.apache.maven.scm.provider.cvslib.cvsexe.CvsExeScmProvider;
import org.apache.maven.scm.provider.cvslib.cvsjava.CvsJavaScmProvider;
import org.apache.maven.scm.provider.git.jgit.JGitScmProvider;
import org.apache.maven.scm.provider.hg.HgScmProvider;
import org.apache.maven.scm.provider.jazz.JazzScmProvider;
import org.apache.maven.scm.provider.local.LocalScmProvider;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.starteam.StarteamScmProvider;
import org.apache.maven.scm.provider.svn.svnexe.SvnExeScmProvider;
import org.apache.maven.scm.provider.synergy.SynergyScmProvider;
import org.apache.maven.scm.provider.vss.VssScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Zip;
import org.sf.feeling.decompiler.util.HashUtils;
import org.sf.feeling.decompiler.util.Logger;

public class UrlDownloader
{

	public String download( final String url ) throws Exception
	{
		String result;
		if ( StringUtils.startsWith( url, "scm:" ) ) //$NON-NLS-1$
		{
			result = this.downloadFromScm( url );
		}
		else if ( new File( url ).exists( ) )
		{
			result = url;
		}
		else
		{
			result = this.downloadFromUrl( url );
		}
		return result;
	}

	private String downloadFromScm( String url ) throws Exception
	{
		if ( url.indexOf( "cvs:pserver:" ) != -1 && url.indexOf( "@" ) == -1 ) //$NON-NLS-1$ //$NON-NLS-2$
		{
			url = url.replace( "cvs:pserver:", "cvs:pserver:anonymous:@" ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		final File tmpDir = new File( System.getProperty( "java.io.tmpdir" ) ); //$NON-NLS-1$
		final File checkoutDirectory = new File( tmpDir,
				SourceConstants.TEMP_SOURCE_PREFIX + "_" + HashUtils.md5Hash( url ) ); //$NON-NLS-1$
		final File file = new File( tmpDir,
				SourceConstants.TEMP_SOURCE_PREFIX + "_" + HashUtils.md5Hash( url ) + ".zip" ); //$NON-NLS-1$ //$NON-NLS-2$
		if ( !file.exists( ) )
		{
			if ( !checkoutDirectory.exists( )
					|| checkoutDirectory.list( ).length == 0
					|| ( checkoutDirectory.list( ).length == 1 && checkoutDirectory.list( )[0].startsWith( "." ) ) ) //$NON-NLS-1$
			{
				final ScmManager scmManager = new BasicScmManager( );
				scmManager.setScmProvider( "accurev", new AccuRevScmProvider( ) ); //$NON-NLS-1$
				scmManager.setScmProvider( "bazaar", new BazaarScmProvider( ) ); //$NON-NLS-1$
				scmManager.setScmProvider( "clearcase", new ClearCaseScmProvider( ) ); //$NON-NLS-1$
				scmManager.setScmProvider( "hg", new HgScmProvider( ) ); //$NON-NLS-1$
				scmManager.setScmProvider( "local", new LocalScmProvider( ) ); //$NON-NLS-1$
				scmManager.setScmProvider( "perforce", new PerforceScmProvider( ) ); //$NON-NLS-1$
				scmManager.setScmProvider( "cvs_native", new CvsExeScmProvider( ) ); //$NON-NLS-1$
				scmManager.setScmProvider( "cvs", new CvsJavaScmProvider( ) ); //$NON-NLS-1$
				scmManager.setScmProvider( "git", new JGitScmProvider( ) ); //$NON-NLS-1$
				scmManager.setScmProvider( "svn", new SvnExeScmProvider( ) ); //$NON-NLS-1$
				scmManager.setScmProvider( "starteam", new StarteamScmProvider( ) ); //$NON-NLS-1$
				scmManager.setScmProvider( "synergy", new SynergyScmProvider( ) ); //$NON-NLS-1$
				scmManager.setScmProvider( "vss", new VssScmProvider( ) ); //$NON-NLS-1$
				scmManager.setScmProvider( "jazz", new JazzScmProvider( ) ); //$NON-NLS-1$
				String scmUrl;
				ScmVersion scmVersion;
				if ( url.indexOf( '#' ) != -1 )
				{
					scmUrl = StringUtils.trimToEmpty( url.substring( 0, url.indexOf( '#' ) ) );
					final String fragment = StringUtils.trimToEmpty( url.substring( url.indexOf( '#' ) + 1 ) );
					if ( fragment.indexOf( '=' ) != -1 )
					{
						final String[] versionTypeAndVersion = StringUtils.split( fragment, '=' );
						final String version = StringUtils.trim( versionTypeAndVersion[1] );
						final String type = StringUtils.trim( versionTypeAndVersion[0] );
						if ( "tag".equals( type ) ) //$NON-NLS-1$
						{
							scmVersion = new ScmTag( version );
						}
						else if ( "branch".equals( type ) ) //$NON-NLS-1$
						{
							scmVersion = new ScmBranch( version );
						}
						else if ( "revision".equals( type ) ) //$NON-NLS-1$
						{
							scmVersion = new ScmRevision( version );
						}
						else
						{
							if ( !"commitId".equals( type ) ) //$NON-NLS-1$
							{
								throw new IllegalArgumentException( "'" + type + "' version type isn't known." ); //$NON-NLS-1$ //$NON-NLS-2$
							}
							scmVersion = new ScmRevision( version );
						}
					}
					else
					{
						scmVersion = new ScmTag( fragment );
					}
				}
				else if ( url.indexOf( ';' ) != -1 )
				{
					scmUrl = StringUtils.trimToEmpty( url.substring( 0, url.indexOf( ';' ) ) );
					final String fragment = StringUtils.trimToEmpty( url.substring( url.indexOf( ';' ) + 1 ) );
					scmVersion = null;
					if ( fragment.indexOf( '=' ) != -1 )
					{
						final String[] properties = StringUtils.split( fragment, ';' );
						String[] array;
						for ( int length = ( array = properties ).length, i = 0; i < length; ++i )
						{
							final String property = array[i];
							final String[] versionTypeAndVersion2 = StringUtils.split( property, '=' );
							final String version2 = StringUtils.strip( versionTypeAndVersion2[1], " \"" ); //$NON-NLS-1$
							final String type2 = StringUtils.trim( versionTypeAndVersion2[0] );
							if ( "tag".equals( type2 ) ) //$NON-NLS-1$
							{
								scmVersion = new ScmTag( version2 );
								break;
							}
							if ( "branch".equals( type2 ) ) //$NON-NLS-1$
							{
								scmVersion = new ScmBranch( version2 );
								break;
							}
							if ( "revision".equals( type2 ) ) //$NON-NLS-1$
							{
								scmVersion = new ScmRevision( version2 );
								break;
							}
							if ( "commitId".equals( type2 ) ) //$NON-NLS-1$
							{
								scmVersion = new ScmRevision( version2 );
								break;
							}
						}
					}
					else
					{
						scmVersion = new ScmTag( fragment );
					}
				}
				else
				{
					scmUrl = url;
					scmVersion = null;
				}
				if ( !checkoutDirectory.exists( ) )
				{
					checkoutDirectory.mkdir( );
				}
				final ScmRepository repository = scmManager.makeScmRepository( scmUrl );

				try
				{
					scmManager.checkOut( repository, new ScmFileSet( checkoutDirectory ), scmVersion );
				}
				catch ( Exception e )
				{
					Logger.debug( e );
					if ( checkoutDirectory.exists( ) )
					{
						FileUtils.deleteDirectory( checkoutDirectory );
					}
				}
			}
			if ( checkoutDirectory.exists( ) && checkoutDirectory.list( ).length > 0 )
			{
				this.zipFolder( checkoutDirectory, file );
				FileUtils.deleteDirectory( checkoutDirectory );
			}
		}
		String result = null;
		if ( file.exists( ) )
		{
			result = file.getAbsolutePath( );
		}
		return result;
	}

	public void zipFolder( final File srcFolder, final File destZipFile )
	{
		final Zip zipper = new Zip( );
		zipper.setLevel( 1 );
		zipper.setDestFile( destZipFile );
		zipper.setBasedir( srcFolder );
		zipper.setIncludes( "**/*.java" ); //$NON-NLS-1$
		zipper.setTaskName( "zip" ); //$NON-NLS-1$
		zipper.setTaskType( "zip" ); //$NON-NLS-1$
		zipper.setProject( new Project( ) );
		zipper.setOwningTarget( new Target( ) );
		zipper.execute( );
	}

	public void delete( final File folder )
	{
		final Delete delete = new Delete( );
		delete.setDir( folder );
		delete.setTaskName( "delete" ); //$NON-NLS-1$
		delete.setTaskType( "delete" ); //$NON-NLS-1$
		delete.setProject( new Project( ) );
		delete.setOwningTarget( new Target( ) );
		delete.execute( );
	}

	private String downloadFromUrl( final String url ) throws IOException
	{
		final File file = File.createTempFile( SourceConstants.TEMP_SOURCE_PREFIX, ".tmp" ); //$NON-NLS-1$
		InputStream is = null;
		OutputStream os = null;
		try
		{
			final URLConnection conn = new URL( url ).openConnection( );
			conn.setConnectTimeout( 5000 );
			conn.setReadTimeout( 5000 );
			is = this.openConnectionCheckRedirects( conn );
			os = FileUtils.openOutputStream( file );
			IOUtils.copy( is, os );
		}
		catch ( Exception ex )
		{
			IOUtils.closeQuietly( os );
			file.delete( );
			return file.getAbsolutePath( );
		}
		finally
		{
			IOUtils.closeQuietly( os );
			IOUtils.closeQuietly( is );
		}
		IOUtils.closeQuietly( os );
		IOUtils.closeQuietly( is );
		final String result = file.getAbsolutePath( );
		return result;
	}

	private InputStream openConnectionCheckRedirects( URLConnection c ) throws IOException
	{
		int redirects = 0;
		InputStream in = null;
		boolean redir;
		do
		{
			if ( c instanceof HttpURLConnection )
			{
				( (HttpURLConnection) c ).setInstanceFollowRedirects( false );
				c.setRequestProperty( "User-Agent", //$NON-NLS-1$
						"Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.7 (KHTML, like Gecko) Chrome/16.0.912.63 Safari/535.7" ); //$NON-NLS-1$
			}
			in = c.getInputStream( );
			redir = false;
			if ( c instanceof HttpURLConnection )
			{
				final HttpURLConnection http = (HttpURLConnection) c;
				final int stat = http.getResponseCode( );
				if ( stat < 300 || stat > 307 || stat == 306 || stat == 304 )
				{
					continue;
				}
				final URL base = http.getURL( );
				final String loc = http.getHeaderField( "Location" ); //$NON-NLS-1$
				URL target = null;
				if ( loc != null )
				{
					target = new URL( base, loc );
				}
				http.disconnect( );
				if ( target == null
						|| ( !target.getProtocol( ).equals( "http" ) && !target.getProtocol( ).equals( "https" ) ) //$NON-NLS-1$ //$NON-NLS-2$
						|| redirects >= 5 )
				{
					throw new SecurityException( "illegal URL redirect" ); //$NON-NLS-1$
				}
				redir = true;
				c = target.openConnection( );
				c.setConnectTimeout( 5000 );
				c.setReadTimeout( 5000 );
				++redirects;
			}
		} while ( redir );
		return in;
	}
}