package org.sf.feeling.decompiler.util;

import java.lang.reflect.Method;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.core.NamedMember;
import org.eclipse.jdt.internal.core.SourceMapper;

public class SourceMapperUtil
{
	private static final String MAP_SOURCE_METHOD_NAME = "mapSource"; //$NON-NLS-1$

	public static void mapSource(SourceMapper sourceMapper, IType type, char[] source, IBinaryType info)
	{
		Method mapSourceMethod = getMapSourceMethod( sourceMapper );

		// API changed with Java 9 support (#daa227e4f5b7af888572a286c4f973b7a167ff2e)
		if (mapSourceMethod == null) {
			mapSourceMethod = getLegacyMapSourceMethod( sourceMapper );
		}
		
		if (mapSourceMethod != null) {
			Object[] parameters = new Object[] { type, source, info };
			ReflectionUtils.invokeMethod( mapSourceMethod, sourceMapper, parameters );
		} else {
			throw new IllegalStateException( "Unable to invoke mapSource method on sourceMapper" );
		}
	}

	private static Method getMapSourceMethod( SourceMapper sourceMapper )
	{
		return ReflectionUtils.getDeclaredMethod( sourceMapper, MAP_SOURCE_METHOD_NAME, new Class[]{
				NamedMember.class, char[].class, IBinaryType.class
		});
	}

	private static Method getLegacyMapSourceMethod( SourceMapper sourceMapper )
	{
		return ReflectionUtils.getDeclaredMethod( sourceMapper, MAP_SOURCE_METHOD_NAME, new Class[]{
				IType.class, char[].class, IBinaryType.class
		});
	}
}
