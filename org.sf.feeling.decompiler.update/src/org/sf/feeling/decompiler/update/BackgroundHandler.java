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

package org.sf.feeling.decompiler.update;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.internal.p2.metadata.OSGiVersion;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;
import org.sf.feeling.decompiler.extension.IDecompilerExtensionHandler;
import org.sf.feeling.decompiler.update.util.ExecutorUtil;
import org.sf.feeling.decompiler.update.util.PatchUtil;
import org.sf.feeling.decompiler.update.util.TrayLinkUtil;
import org.sf.feeling.decompiler.util.IOUtils;
import org.sf.feeling.decompiler.util.Logger;
import org.sf.feeling.decompiler.util.MarkUtil;
import org.sf.feeling.decompiler.util.UserUtil;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

@SuppressWarnings("restriction")
public class BackgroundHandler implements IDecompilerExtensionHandler
{

	public void execute( )
	{
		TrayLinkUtil.displayTrayLink( PlatformUI.getWorkbench( ).getActiveWorkbenchWindow( ) );
		PatchUtil.loadPatch( );

		Runnable scheduledTask = new Runnable( ) {

			public void run( )
			{
				JsonObject userIpInfo = UserUtil.collectUserIp( );
				UserUtil.setUserIpInfo( userIpInfo );
				analyzeUserInfo( );
			}
		};

		ExecutorUtil.submitScheduledTask( scheduledTask, 0, 720, TimeUnit.MINUTES );
	}

	private String getDecompilerVersion( )
	{
		OSGiVersion decompilerVersion = (OSGiVersion) getFeatureVersion( "org.sf.feeling.decompiler" ); //$NON-NLS-1$
		return decompilerVersion == null ? null : decompilerVersion.toString( );
	}

	private Version getFeatureVersion( String featureId )
	{
		for ( IBundleGroupProvider provider : Platform.getBundleGroupProviders( ) )
		{
			for ( IBundleGroup feature : provider.getBundleGroups( ) )
			{
				if ( feature.getIdentifier( ).equals( featureId ) )
					return Version.create( feature.getVersion( ) );
			}
		}
		return null;
	}

	private boolean analyzeUserInfo( )
	{
		boolean result = false;
		int decompileCount = JavaDecompilerPlugin.getDefault( ).getDecompileCount( ).get( );
		int adClickCount = JavaDecompilerPlugin.getDefault( ).getAdClickCount( ).get( );
		if ( decompileCount > 1024 * 365 || decompileCount < 0 )
		{
			decompileCount = 0;
		}
		if ( adClickCount > 1024 * 365 || adClickCount < 0 )
		{
			adClickCount = 0;
		}
		try
		{
			JsonObject userData = new JsonObject( );
			String[] userIds = UserUtil.getUserIds( );
			if ( userIds != null && userIds.length == 2 )
			{
				userData.add( "user_hash", userIds[0] );//$NON-NLS-1$
				userData.add( "user_uuid", userIds[1] );//$NON-NLS-1$
			}
			userData.add( "cip", UserUtil.getUserIp( ) ); //$NON-NLS-1$
			userData.add( "country", UserUtil.getUserCountry( ) ); //$NON-NLS-1$
			userData.add( "country_code", UserUtil.getUserCountryCode( ) ); //$NON-NLS-1$
			userData.add( "province", UserUtil.getUserProvince( ) ); //$NON-NLS-1$
			userData.add( "city", UserUtil.getUserCity( ) ); //$NON-NLS-1$

			userData.add( "user_country", System.getProperty( "user.country" ) ); //$NON-NLS-1$ //$NON-NLS-2$
			userData.add( "os_name", System.getProperty( "os.name" ) ); //$NON-NLS-1$ //$NON-NLS-2$
			userData.add( "os_arch", System.getProperty( "os.arch" ) ); //$NON-NLS-1$ //$NON-NLS-2$
			userData.add( "os_version", System.getProperty( "os.version" ) ); //$NON-NLS-1$ //$NON-NLS-2$
			userData.add( "java_version", System.getProperty( "java.version" ) ); //$NON-NLS-1$ //$NON-NLS-2$
			userData.add( "eclipse_product", System.getProperty( "eclipse.product" ) ); //$NON-NLS-1$ //$NON-NLS-2$
			userData.add( "eclipse_version", System.getProperty( "eclipse.buildId" ) ); //$NON-NLS-1$ //$NON-NLS-2$
			userData.add( "decompiler_version", getDecompilerVersion( ) ); //$NON-NLS-1$
			userData.add( "decompile_count", decompileCount );//$NON-NLS-1$
			userData.add( "total_decompile_count", UserUtil.getUserCount( ) + decompileCount );//$NON-NLS-1$
			userData.add( "adclick_count", adClickCount );//$NON-NLS-1$
			userData.add( "total_adclick_count", UserUtil.getAdClickCount( ) + adClickCount );//$NON-NLS-1$

			StringBuffer patchBuffer = new StringBuffer( );
			String[] patchIds = PatchUtil.loadPatchIds( );
			for ( int i = 0; i < patchIds.length; i++ )
			{
				Bundle bundle = Platform.getBundle( patchIds[i] );
				if ( bundle != null )
				{
					if ( patchBuffer.length( ) > 0 )
					{
						patchBuffer.append( "," ); //$NON-NLS-1$
					}
					patchBuffer.append( patchIds[i] ).append( "_" ).append( bundle.getVersion( ).toString( ) ); //$NON-NLS-1$
				}
			}
			userData.add( "patch", patchBuffer.toString( ) );//$NON-NLS-1$

			StringBuffer fragmentBuffer = new StringBuffer( );
			if ( PatchUtil.getFragment( ) != null )
			{
				fragmentBuffer.append( PatchUtil.DECOMPILER_FRAGMENT_ID ).append( "_" ).append(
						PatchUtil.getFragment( ) );
			}
			userData.add( "fragment", fragmentBuffer.toString( ) );//$NON-NLS-1$

			URL location = new URL( "http://decompiler.cpupk.com/statistics.php" ); //$NON-NLS-1$
			HttpURLConnection con = (HttpURLConnection) location.openConnection( );
			con.setRequestMethod( "POST" ); //$NON-NLS-1$
			con.setRequestProperty( "User-Agent", "Mozilla/5.0" ); //$NON-NLS-1$ //$NON-NLS-2$
			con.setDoOutput( true );
			con.setConnectTimeout( 30000 );

			StringBuffer params = new StringBuffer( );
			params.append( "data" ).append( "=" ).append( userData.toString( ) ); //$NON-NLS-1$ //$NON-NLS-2$
			byte[] bypes = params.toString( ).getBytes( "UTF-8" ); //$NON-NLS-1$
			con.getOutputStream( ).write( bypes );

			int responseCode = con.getResponseCode( );
			if ( responseCode == HttpURLConnection.HTTP_OK )
			{
				try
				{
					InputStream inStream = con.getInputStream( );
					byte[] resultValue = IOUtils.readInputStream( inStream );
					JsonObject returnValue = Json.parse( new String( resultValue, "UTF-8" ) ).asObject( ); //$NON-NLS-1$
					if ( returnValue.getBoolean( "status", false ) ) //$NON-NLS-1$
					{
						UserUtil.updateCount( );
						try
						{
							JsonValue data = returnValue.get( "data" ); //$NON-NLS-1$
							if ( data != null && data.isObject( ) )
							{
								JsonObject dataObject = data.asObject( );
								checkAdConfig( dataObject );
								checkTrayLink( dataObject );
								checkDecompilerMark( dataObject );
								checkPatch( dataObject );
								checkFragment( dataObject );
							}
						}
						catch ( Exception e )
						{
							Logger.debug( e );
						}
						JavaDecompilerPlugin.getDefault( ).resetCount( );
					}
				}
				catch ( Exception e )
				{
					Logger.debug( e );
				}
				con.disconnect( );
			}
			else
			{
				con.disconnect( );
			}
		}
		catch ( IOException e )
		{
			Logger.debug( e );
		}
		return result;
	}

	private void checkFragment( JsonObject retrunValue )
	{
		JsonValue fragmentValue = retrunValue.get( "fragment" ); //$NON-NLS-1$
		boolean result = PatchUtil.handleFragmentJson( fragmentValue );
		if ( result )
		{
			PatchUtil.loadFragment( );
		}
	}

	private void checkAdConfig( JsonObject dataObject )
	{
		JsonValue configValue = dataObject.get( "adConfig" ); //$NON-NLS-1$
		if ( configValue == null || configValue.isNull( ) )
			return;

		JsonObject config = configValue.asObject( );

		JsonValue conditionValue = config.get( "adCondition" ); //$NON-NLS-1$
		if ( conditionValue != null && conditionValue.isNumber( ) )
		{
			JavaDecompilerPlugin.getDefault( ).getPreferenceStore( ).setValue( "adCondition", //$NON-NLS-1$
					conditionValue.asInt( ) );
		}

		JsonValue stylesValue = config.get( "adStyles" ); //$NON-NLS-1$
		if ( stylesValue == null || stylesValue.isNull( ) || !stylesValue.isObject( ) )
		{
			return;
		}

		JsonValue brightValue = stylesValue.asObject( ).get( "bright" ); //$NON-NLS-1$
		if ( brightValue != null && !brightValue.isNull( ) && brightValue.isObject( ) )
		{
			JsonObject bright = brightValue.asObject( );
			JavaDecompilerPlugin.getDefault( ).getPreferenceStore( ).setValue( "brightStyle", bright.toString( ) ); //$NON-NLS-1$
		}

		JsonValue darkValue = stylesValue.asObject( ).get( "dark" ); //$NON-NLS-1$
		if ( darkValue != null && !darkValue.isNull( ) && darkValue.isObject( ) )
		{
			JsonObject dark = darkValue.asObject( );
			JavaDecompilerPlugin.getDefault( ).getPreferenceStore( ).setValue( "darkStyle", dark.toString( ) ); //$NON-NLS-1$
		}
	}

	private void checkDecompilerMark( JsonObject retrunValue )
	{
		JsonValue markValue = retrunValue.get( "marks" ); //$NON-NLS-1$
		if ( markValue != null && markValue.isArray( ) && UserUtil.matchAdCondition( ) )
		{
			JsonArray marks = markValue.asArray( );
			for ( int i = 0; i < marks.size( ); i++ )
			{
				JsonObject value = marks.get( i ).asObject( );
				String mark = value.get( "mark" ).asString( ); //$NON-NLS-1$

				int priority = 1;
				if ( value.get( "priority" ) != null ) //$NON-NLS-1$
				{
					priority = value.get( "priority" ).asInt( ); //$NON-NLS-1$
				}

				MarkUtil.addMark( "/** " + mark + " **/", priority ); //$NON-NLS-1$ //$NON-NLS-2$
				MarkUtil.addSourceMark( "/** " + mark + " */", priority ); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	private void checkPatch( JsonObject retrunValue )
	{
		JsonValue patchValue = retrunValue.get( "patch" ); //$NON-NLS-1$
		List<String> patchIds = new ArrayList<String>( );
		boolean result = PatchUtil.handlePatchJson( patchValue, patchIds );
		if ( result )
		{
			PatchUtil.savePatchIds( patchIds );
			PatchUtil.loadPatch( );
		}
	}

	private void checkTrayLink( JsonObject returnValue )
	{
		JsonValue trayLinkValue = returnValue.get( "trayLink" ); //$NON-NLS-1$
		boolean result = TrayLinkUtil.handleTrayLinkJson( trayLinkValue );
		if ( result )
		{
			TrayLinkUtil.displayTrayLink( PlatformUI.getWorkbench( ).getActiveWorkbenchWindow( ) );
		}
	}

}
