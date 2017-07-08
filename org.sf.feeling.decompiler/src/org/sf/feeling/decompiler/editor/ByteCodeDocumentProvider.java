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

package org.sf.feeling.decompiler.editor;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.sf.feeling.decompiler.util.Logger;
import org.sf.feeling.decompiler.util.UIUtil;

import com.drgarbage.asm.ClassReader;
import com.drgarbage.asm.render.impl.ClassFileDocument;
import com.drgarbage.asm.render.impl.ClassFileOutlineElement;
import com.drgarbage.asm.render.intf.IClassFileDocument;
import com.drgarbage.asm.render.intf.IDocumentUpdateListener;

public class ByteCodeDocumentProvider extends FileDocumentProvider
{

	/**
	 * Reference to the class file document object.
	 */
	private IClassFileDocument classFileDocument = null;

	/**
	 * Reference to the class file outline element.
	 */
	private IJavaElement classFileOutlineElement;

	/**
	 * List of the document update listeners.
	 * <code>BytecodeDocumentProvider</code> fires on each update of this
	 * document an event for all registered listeners.
	 */
	private ArrayList<IDocumentUpdateListener> documentUpdateListeners;

	private String mark;

	public String getMark( )
	{
		return mark;
	}

	/**
	 * Constructor.
	 * 
	 * @param mark
	 * 
	 * @param part
	 *            bytecode editor
	 */
	public ByteCodeDocumentProvider( String mark )
	{
		super( );
		this.mark = mark;
	}

	/**
	 * Returns the reference to the outline element.
	 * 
	 * @return the classfile OutlineElement
	 */
	public IJavaElement getClassFileOutlineElement( )
	{
		return classFileOutlineElement;
	}

	/**
	 * returns the reference to the class file document.
	 * 
	 * @return the class file document
	 */
	public IClassFileDocument getClassFileDocument( )
	{
		return classFileDocument;
	}

	/**
	 * Adds <code>listener</code> to the list that will be fired on each update
	 * of this document.
	 * 
	 * @param listener
	 */
	public void addDocumentUpdateListener( IDocumentUpdateListener listener )
	{
		if ( listener != null )
		{
			if ( documentUpdateListeners == null )
			{
				documentUpdateListeners = new ArrayList<IDocumentUpdateListener>( );
			}
			documentUpdateListeners.add( listener );
		}
	}

	/**
	 * Removes the given <code>listener</code> from the list.
	 * 
	 * @param listener
	 */
	public void removeDocumentUpdateListener( IDocumentUpdateListener listener )
	{
		if ( listener != null && documentUpdateListeners != null )
		{
			Iterator<IDocumentUpdateListener> it = documentUpdateListeners.iterator( );
			while ( it.hasNext( ) )
			{
				IDocumentUpdateListener l = it.next( );
				if ( l == listener )
				{
					it.remove( );
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.editors.text.StorageDocumentProvider#createDocument(java.
	 * lang.Object)
	 */
	protected IDocument createDocument( Object element ) throws CoreException
	{
		IDocument document = super.createDocument( element );

		if ( document != null )
		{
			JavaTextTools tools = JavaPlugin.getDefault( ).getJavaTextTools( );
			tools.setupJavaDocumentPartitioner( document, IJavaPartitions.JAVA_PARTITIONING );
		}
		return document;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.editors.text.StorageDocumentProvider#createEmptyDocument()
	 */
	protected IDocument createEmptyDocument( )
	{
		return new ByteCodeDocument( this, UIUtil.getActiveDecompilerEditor( ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.editors.text.StorageDocumentProvider#setDocumentContent(
	 * org.eclipse.jface.text.IDocument, java.io.InputStream, java.lang.String)
	 */
	protected void setDocumentContent( IDocument document, InputStream contentStream, String encoding )
			throws CoreException
	{
		DataInputStream in = null;
		try
		{
			/* buffer only if necessary */
			if ( contentStream instanceof BufferedInputStream )
			{
				in = new DataInputStream( contentStream );
			}
			else
			{
				in = new DataInputStream( new BufferedInputStream( contentStream ) );
			}
			ClassFileOutlineElement outlineElement = new ClassFileOutlineElement( );
			ClassFileDocument doc = new ClassFileDocument( outlineElement );
			outlineElement.setClassFileDocument( doc );
			ClassReader cr = new ClassReader( in, doc );
			cr.accept( doc, 0 );

			document.set( mark + "\n\n" + doc.toString( ) ); //$NON-NLS-1$

			classFileDocument = doc;
			classFileOutlineElement = outlineElement;

		}
		catch ( Exception e )
		{
			Logger.debug( e );
		}
		finally
		{
			if ( in != null )
			{
				try
				{
					in.close( );
				}
				catch ( IOException e )
				{
				}
			}
		}

		/* fire update document events */
		if ( documentUpdateListeners != null )
		{
			Iterator<IDocumentUpdateListener> it = documentUpdateListeners.iterator( );
			while ( it.hasNext( ) )
			{
				IDocumentUpdateListener l = it.next( );
				l.documentUpdated( classFileDocument );
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.editors.text.FileDocumentProvider#isModifiable(java.lang.
	 * Object)
	 */
	@Override
	public boolean isModifiable( Object element )
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.texteditor.AbstractDocumentProvider#getDocument(java.lang.
	 * Object)
	 */
	@Override
	public IDocument getDocument( Object element )
	{
		return getBytecodeDocument( element );
	}

	/**
	 * <p>
	 * Returns the document for the given element. The document contains a
	 * textual presentation of the class file content.
	 * </p>
	 * <p>
	 * This method wraps the {@link IDocumentProvider#getDocument(Object)} to
	 * avoid calling this method directly.
	 * </p>
	 * 
	 * @param input
	 *            the element, or <code>null</code>
	 * @return the document, or <code>null</code> if none
	 * 
	 * @see IDocumentProvider#getDocument(Object)
	 */
	public IDocument getBytecodeDocument( Object input )
	{
		return super.getDocument( input );
	}

}