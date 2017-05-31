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

package org.sf.feeling.decompiler.editor;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.ImportContainer;
import org.eclipse.jdt.internal.core.ImportContainerInfo;
import org.eclipse.jdt.internal.core.ImportDeclaration;
import org.eclipse.jdt.internal.core.ImportDeclarationElementInfo;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.OpenableElementInfo;
import org.eclipse.jdt.internal.core.SourceMapper;
import org.sf.feeling.decompiler.util.DecompilerOutputUtil;
import org.sf.feeling.decompiler.util.Logger;
import org.sf.feeling.decompiler.util.ReflectionUtils;

public class ImportSourceMapper extends SourceMapper
{

	private static Map<String, String> options = new HashMap<String, String>( );
	static
	{
		CompilerOptions option = new CompilerOptions( );
		options = option.getMap( );
		options.put( CompilerOptions.OPTION_Compliance,
				DecompilerOutputUtil.getMaxDecompileLevel( ) ); // $NON-NLS-1$
		options.put( CompilerOptions.OPTION_Source,
				DecompilerOutputUtil.getMaxDecompileLevel( ) ); // $NON-NLS-1$
	}

	public ImportSourceMapper( IPath sourcePath, String rootPath )
	{

		this( sourcePath, rootPath, options );
	}

	public ImportSourceMapper( IPath sourcePath, String rootPath, Map options )
	{
		super( sourcePath, rootPath, options );
	}

	protected Stack infoStack;
	protected HashMap children;
	protected Stack handleStack;
	protected ClassFile unit;
	protected OpenableElementInfo unitInfo;
	protected ImportContainerInfo importContainerInfo = null;
	protected ImportContainer importContainer;

	private JavaModelManager manager = JavaModelManager.getJavaModelManager( );

	public void enterCompilationUnit( )
	{
		this.infoStack = new Stack( );
		this.children = new HashMap( );
		this.handleStack = new Stack( );
		this.infoStack.push( this.unitInfo );
		this.handleStack.push( this.unit );
	}

	public synchronized ISourceRange mapSource( IType type, char[] contents,
			IBinaryType info, IJavaElement elementToFind )
	{
		this.binaryType = (BinaryType) type;
		this.unit = (ClassFile) binaryType.getClassFile( );
		try
		{
			this.unitInfo = (OpenableElementInfo) this.unit.getElementInfo( );
		}
		catch ( JavaModelException e )
		{
			Logger.debug( e );
		}
		
		return super.mapSource( type, contents, info, elementToFind );
	}

	public void exitCompilationUnit( int declarationEnd )
	{
		IJavaElement[] oldChildren = (IJavaElement[]) ReflectionUtils
				.getFieldValue( this.unitInfo, "children" );

		if ( this.importContainerInfo != null )
		{
			ReflectionUtils.setFieldValue( this.importContainerInfo,
					"children",
					getChildren( this.importContainerInfo ) );
		}

		List<IJavaElement> children = new ArrayList<IJavaElement>( );

		for ( int i = 0; i < oldChildren.length; i++ )
		{
			IJavaElement child = oldChildren[i];
			if ( child instanceof ImportContainer )
				continue;
			children.add( child );
		}

		children.addAll( Arrays.asList( getChildren( this.unitInfo ) ) );

		ReflectionUtils.setFieldValue( this.unitInfo,
				"children",
				children.toArray( new IJavaElement[0] ) );

		if ( this.importContainer != null )
		{
			manager.getTemporaryCache( ).put( this.importContainer, this.importContainerInfo );
		}
		ReflectionUtils.invokeMethod( manager, "putInfos", new Class[]{
				IJavaElement.class, Object.class, boolean.class, Map.class
		}, new Object[]{
				unit, unitInfo, false, manager.getTemporaryCache( )
		} );
	}

	private IJavaElement[] getChildren( Object info )
	{
		ArrayList childrenList = (ArrayList) this.children.get( info );
		if ( childrenList != null )
		{
			return (IJavaElement[]) childrenList
					.toArray( new IJavaElement[childrenList.size( )] );
		}
		return new JavaElement[0];
	}

	protected ImportContainer createImportContainer( ClassFile parent )
	{
		return new ClassImportContainer( parent );
	}

	private void addToChildren( Object parentInfo, JavaElement handle )
	{
		ArrayList childrenList = (ArrayList) this.children.get( parentInfo );
		if ( childrenList == null )
			this.children.put( parentInfo, childrenList = new ArrayList( ) );
		childrenList.add( handle );
	}

	protected ImportDeclaration createImportDeclaration( ImportContainer parent,
			String name, boolean onDemand )
	{
		try
		{
			Constructor c = ImportDeclaration.class.getDeclaredConstructor(
					ImportContainer.class,
					String.class,
					boolean.class );
			c.setAccessible( true );
			ImportDeclaration dec = (ImportDeclaration) c
					.newInstance( parent, name, onDemand );
			return dec;
		}
		catch ( Exception e )
		{
			Logger.debug( e );
		}
		return null;
	}

	public void acceptImport( int declarationStart, int declarationEnd,
			int nameSourceStart, int nameSourceEnd, char[][] tokens,
			boolean onDemand, int modifiers )
	{
		JavaElement parentHandle = (JavaElement) this.handleStack.peek( );
		if ( !( parentHandle.getElementType( ) == IJavaElement.CLASS_FILE ) )
		{
			Assert.isTrue( false ); // Should not happen
		}

		ClassFile parentCU = (ClassFile) parentHandle;
		// create the import container and its info
		if ( this.importContainer == null )
		{
			this.importContainer = createImportContainer( parentCU );
			this.importContainerInfo = new ImportContainerInfo( );
			Object parentInfo = this.infoStack.peek( );
			addToChildren( parentInfo, this.importContainer );
		}

		String elementName = JavaModelManager.getJavaModelManager( ).intern(
				new String( CharOperation.concatWith( tokens, '.' ) ) );
		ImportDeclaration handle = createImportDeclaration(
				this.importContainer,
				elementName,
				onDemand );

		ImportDeclarationElementInfo info = new ImportDeclarationElementInfo( );
		ReflectionUtils.invokeMethod( info,
				"setSourceRangeStart",
				int.class,
				declarationStart );
		ReflectionUtils.invokeMethod( info,
				"setSourceRangeEnd",
				int.class,
				declarationEnd );
		ReflectionUtils.invokeMethod( info,
				"setNameSourceStart",
				int.class,
				nameSourceStart );
		ReflectionUtils.invokeMethod( info,
				"setNameSourceEnd",
				int.class,
				nameSourceEnd );
		ReflectionUtils.invokeMethod( info, "setFlags", int.class, modifiers );

		addToChildren( this.importContainerInfo, handle );
		manager.getTemporaryCache( ).put( handle, info );
	}
}
