/**
 * Copyright (c) 2008-2013, Dr. Garbage Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.drgarbage.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.sf.feeling.decompiler.util.Logger;

import com.drgarbage.bytecode.ByteCodeConstants;
import com.drgarbage.javalang.JavaLangUtils;

/**
 * Some utility methods for class file objects.
 *
 * @author Sergej Alekseev
 * @version $Revision: 516 $ $Id: ClassFileDocumentsUtils.java 516 2014-02-05
 *          09:17:35Z salekseev $
 */
public class ClassFileDocumentsUtils
{

	/**
	 * Returns a method object for the given type by the method name and the
	 * method signature.
	 * 
	 * @param type
	 *            type representing a class
	 * @param methodName
	 *            method name
	 * @param methodSignature
	 *            method signature in java class file format
	 * @return m the method object {@link IMethod}
	 * @throws JavaModelException
	 * @see IType
	 */
	public static IMethod findMethod( IType type, String methodName, String methodSignature ) throws JavaModelException
	{
		if ( type == null )
		{
			return null;
		}

		if ( methodName.equals( ByteCodeConstants.INIT ) )
		{
			methodName = type.getElementName( );
		}

		IMethod[] methods = type.getMethods( );
		for ( IMethod m : methods )
		{
			String sig = resolveMethodSignature( m );
			if ( m.getElementName( ).equals( methodName ) && sig.equals( methodSignature ) )
			{

				return m;
			}
		}

		return null;
	}

	/**
	 * Returns the method signature string.
	 * 
	 * @param iMethod
	 *            method object
	 * @return method signature string
	 * 
	 * @see IMethod
	 */
	public static String resolveMethodSignature( IMethod iMethod )
	{

		String mMethodSignature = null;
		try
		{
			/*
			 * Method Signature: NOTE: if class file is selected then the method
			 * signature is resolved.
			 */
			if ( iMethod.isBinary( ) )
			{
				mMethodSignature = iMethod.getSignature( );
			}
			else
			{

				/* resolve parameter signature */
				StringBuffer buf = new StringBuffer( "(" );
				String[] parameterTypes = iMethod.getParameterTypes( );
				String res = null;
				for ( int i = 0; i < parameterTypes.length; i++ )
				{
					res = ActionUtils.getResolvedTypeName( parameterTypes[i], iMethod.getDeclaringType( ) );
					buf.append( res );
				}
				buf.append( ")" );

				res = ActionUtils.getResolvedTypeName( iMethod.getReturnType( ), iMethod.getDeclaringType( ) );
				buf.append( res );

				mMethodSignature = buf.toString( );
			}

		}
		catch ( IllegalArgumentException e )
		{
			Logger.debug( e );
			return null;
		}
		catch ( JavaModelException e )
		{
			Logger.debug( e );
			return null;
		}

		return mMethodSignature;
	}

	/**
	 * Returns the package name as a string for the given compilation unit.
	 * 
	 * @param unit
	 *            compilation unit
	 * @return package name
	 * @throws JavaModelException
	 */
	public static String getPackageNameFromCompilationUnit( ICompilationUnit unit ) throws JavaModelException
	{
		IPackageDeclaration[] p = unit.getPackageDeclarations( );
		if ( p.length > 0 )
		{
			return p[0].getElementName( );
		}

		return null;
	}

	/**
	 * Collects recursively nested classes in the compilation unit. The nested
	 * class names are type-qualified. The classes are stored in the class list
	 * given as argument. For example:
	 * 
	 * <pre>
	 *      HEllo
	 *      HEllo$C1
	 *      HEllo$C1$C3
	 * </pre>
	 * 
	 * @param t
	 *            type representing a class
	 * @param classList
	 *            class list to store nested classes
	 * @throws JavaModelException
	 * @see IType#getTypeQualifiedName()
	 */
	public static void collectNestedClasses( IType t, List<String> classList ) throws JavaModelException
	{
		classList.add( t.getTypeQualifiedName( ) );

		/* start recursion */
		IType[] types = t.getTypes( );
		for ( IType e : types )
		{
			collectNestedClasses( e, classList );
		}
	}

	/**
	 * Returns the list of InputStreams for the given list of classes.
	 * 
	 * @param classList
	 *            list of classes
	 * @param classPath
	 *            classpath
	 * @param packageName
	 *            package name
	 * @return list if InputStreams
	 */
	public static List<InputStream> getInputStreams( List<String> classList, String[] classPath, String packageName )
	{
		List<InputStream> streams = new ArrayList<InputStream>( );
		for ( String l : classList )
		{
			InputStream in;
			try
			{
				in = JavaLangUtils.findResource( classPath, packageName, l );

				if ( in == null )
				{
					String err = "Resource '" + packageName + " " + l + "' not found.";
					Logger.info( err );
				}

				if ( !( in instanceof BufferedInputStream ) )
				{
					/* buffer only if necessary */
					in = new BufferedInputStream( in );
				}

				streams.add( in );
			}
			catch ( IOException e )
			{
				Logger.debug( e );
			}
		}

		return streams;
	}

	/**
	 * Returns the list of InputStreams for all classes defined in the
	 * compilation unit including nested classes.
	 * 
	 * @param unit
	 *            compilation unit
	 * @param jp
	 *            java project
	 * @return list if InputStreams
	 */
	public static List<InputStream> findClasses( ICompilationUnit unit, IJavaProject jp )
	{
		try
		{
			List<String> classList = new ArrayList<String>( );
			IType[] types = unit.getTypes( );
			for ( IType t : types )
			{
				ClassFileDocumentsUtils.collectNestedClasses( t, classList );
			}

			String[] classpath = JavaRuntime.computeDefaultRuntimeClassPath( jp );
			String packageName = getPackageNameFromCompilationUnit( unit );

			return getInputStreams( classList, classpath, packageName );

		}
		catch ( JavaModelException e )
		{
			Logger.debug( e );
		}
		catch ( CoreException e )
		{
			Logger.debug( e );
		}

		return null;

	}
}
