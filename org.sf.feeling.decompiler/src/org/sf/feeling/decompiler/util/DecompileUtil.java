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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.core.BufferManager;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.SourceMapper;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.sf.feeling.decompiler.editor.DecompilerSourceMapper;
import org.sf.feeling.decompiler.editor.DecompilerType;
import org.sf.feeling.decompiler.editor.JavaDecompilerClassFileEditor;
import org.sf.feeling.decompiler.editor.SourceMapperFactory;

public class DecompileUtil
{

	public static String decompile( IClassFile cf, String type, boolean always, boolean reuseBuf, boolean force )
			throws CoreException
	{
		String decompilerType = type;
		String origSrc = cf.getSource( );
		// have to check our mark since all line comments are stripped
		// in debug align mode
		if ( origSrc == null
				|| always && !MarkUtil.containsMark( origSrc )
				|| ( MarkUtil.containsMark( origSrc ) && ( !reuseBuf || force ) ) )
		{
			DecompilerSourceMapper sourceMapper = SourceMapperFactory.getSourceMapper( decompilerType );
			char[] src = sourceMapper.findSource( cf.getType( ) );

			if ( src == null && !DecompilerType.FernFlower.equals( decompilerType ) )
			{
				src = SourceMapperFactory.getSourceMapper( DecompilerType.FernFlower ).findSource( cf.getType( ) );
			}
			if ( src == null )
			{
				return origSrc;
			}
			else
				return new String( src );
		}

		return origSrc;
	}

	public static String decompiler( FileStoreEditorInput input, String decompilerType )
	{
		DecompilerSourceMapper sourceMapper = SourceMapperFactory.getSourceMapper( decompilerType );
		File file = new File( input.getURI( ) );
		return sourceMapper.decompile( decompilerType, file );

	}

	public static String getPackageName( String source )
	{
		Pattern p = Pattern.compile( "(?i)package\\s+\\S+" ); //$NON-NLS-1$

		Matcher m = p.matcher( source );
		if ( m.find( ) )
		{
			return m.group( )
					.replace( "package", "" ) //$NON-NLS-1$ //$NON-NLS-2$
					.replace( ";", "" ) //$NON-NLS-1$ //$NON-NLS-2$
					.trim( );
		}
		return null;
	}

	public static String checkAndUpdateCopyright( JavaDecompilerClassFileEditor editor, IClassFile cf, String origSrc )
			throws JavaModelException
	{
		if ( MarkUtil.containsMark( new String( origSrc ) ) )
		{
			editor.clearSelection( );
		}

		if ( !MarkUtil.containsSourceMark( origSrc ) && !MarkUtil.containsMark( origSrc ) )
		{
			IBuffer buffer = cf.getBuffer( );
			ReflectionUtils.invokeMethod( buffer, "setReadOnly", new Class[]{ //$NON-NLS-1$
					boolean.class
			}, new Object[]{
					false
			} );

			String contents = getCopyRightContent( cf, origSrc );

			buffer.setContents( contents );

			ReflectionUtils.invokeMethod( BufferManager.getDefaultBufferManager( ), "addBuffer", new Class[]{ //$NON-NLS-1$
					IBuffer.class
			}, new Object[]{
					buffer
			} );

			updateSourceRanges( cf, contents );
			return contents;
		}
		else
		{
			updateSourceRanges( cf, origSrc );
		}
		return origSrc;
	}

	public static String updateBuffer( IClassFile cf, String origSrc ) throws JavaModelException
	{
		if ( !MarkUtil.containsSourceMark( origSrc ) && !MarkUtil.containsMark( origSrc ) )
		{
			IBuffer buffer = cf.getBuffer( );
			if ( buffer != null )
			{
				ReflectionUtils.invokeMethod( buffer, "setReadOnly", new Class[]{ //$NON-NLS-1$
						boolean.class
				}, new Object[]{
						false
				} );

				String contents = getCopyRightContent( cf, origSrc );

				buffer.setContents( contents );

				ReflectionUtils.invokeMethod( BufferManager.getDefaultBufferManager( ), "addBuffer", new Class[]{ //$NON-NLS-1$
						IBuffer.class
				}, new Object[]{
						buffer
				} );
				updateSourceRanges( cf, contents );
				return contents;
			}
		}
		else
		{
			updateSourceRanges( cf, origSrc );
		}
		return origSrc;
	}

	public static void updateSourceRanges( IClassFile cf, String contents ) throws JavaModelException
	{
		if ( cf instanceof ClassFile )
		{
			ClassFile classFile = (ClassFile) cf;
			Object info = classFile.getElementInfo( );
			IBinaryType typeInfo = info instanceof IBinaryType ? (IBinaryType) info : null;
			SourceMapper mapper = classFile.getSourceMapper( );
			IType type = (IType) ReflectionUtils.invokeMethod( classFile,
					"getOuterMostEnclosingType", //$NON-NLS-1$
					new Class[0],
					new Object[0] );
			HashMap sourceRange = (HashMap) ReflectionUtils.getFieldValue( mapper, "sourceRanges" ); //$NON-NLS-1$
			sourceRange.remove( type );
			mapper.mapSource( type, contents.toCharArray( ), typeInfo );

			// List rootPaths = (List) ReflectionUtils.getFieldValue( mapper,
			// "rootPaths" );
			// String rootPath = null;
			// if ( rootPaths != null && rootPaths.size( ) > 0 )
			// {
			// rootPath = (String) rootPaths.get( 0 );
			// }
			//
			// if ( "".equals( rootPath ) )
			// rootPath = null;
			//
			// ImportSourceMapper importMapper = new ImportSourceMapper(
			// (IPath) ReflectionUtils.getFieldValue( mapper,
			// "sourcePath" ),
			// rootPath,
			// (Map) ReflectionUtils.getFieldValue( mapper, "options" ) );
			// importMapper.mapSource( type, contents.toCharArray( ), typeInfo
			// );
		}
	}

	public static String getCopyRightContent( IClassFile classFile, String origSrc )
	{
		if ( origSrc != null && !MarkUtil.containsSourceMark( origSrc ) )
		{
			String src = DecompileUtil.deleteOneEmptyLine( origSrc );
			String contents = MarkUtil.getRandomSourceMark( classFile )
					+ "\n" //$NON-NLS-1$
					+ src;
			if ( src == null )
			{
				contents = MarkUtil.getRandomSourceMark( classFile )
						+ "\n" //$NON-NLS-1$
						+ DecompileUtil.foldOneLine( origSrc );
			}
			return contents;
		}
		return origSrc;
	}

	private static String foldOneLine( String origSrc )
	{
		int index = origSrc.indexOf( "\r\n" ); //$NON-NLS-1$
		if ( index != -1 )
		{
			return origSrc.substring( 0, index ) + origSrc.substring( index + 2 );
		}

		index = origSrc.indexOf( "\n" ); //$NON-NLS-1$
		if ( index != -1 )
		{
			return origSrc.substring( 0, index ) + origSrc.substring( index + 1 );
		}
		return origSrc;
	}

	public static String deleteOneEmptyLine( String origSrc )
	{
		int index = origSrc.indexOf( "{" ); //$NON-NLS-1$
		if ( index == -1 )
		{
			return origSrc;
		}

		String prefix = origSrc.substring( 0, index + 1 );
		String suffix = origSrc.substring( index + 1 );

		List<String> splits = new ArrayList( Arrays.asList( prefix.split( "\n" ) ) ); //$NON-NLS-1$
		boolean flag = false;
		for ( int i = 0; i < splits.size( ); i++ )
		{
			String split = splits.get( i );
			if ( split.trim( ).length( ) == 0 )
			{
				splits.remove( i );
				flag = true;
				break;
			}
		}

		if ( flag )
		{
			StringBuffer buffer = new StringBuffer( );
			for ( int i = 0; i < splits.size( ); i++ )
			{
				String split = splits.get( i );
				buffer.append( split );
				if ( i < splits.size( ) - 1 )
				{
					buffer.append( "\n" ); //$NON-NLS-1$
				}
			}
			buffer.append( suffix );
			return buffer.toString( );
		}
		return null;
	}
}
