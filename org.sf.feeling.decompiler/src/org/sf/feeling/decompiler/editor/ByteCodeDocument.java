
package org.sf.feeling.decompiler.editor;

import org.eclipse.jface.text.Document;

public class ByteCodeDocument extends Document
{

	private ByteCodeSourceViewer editor;

	public ByteCodeSourceViewer getEditor( )
	{
		return editor;
	}

	public ByteCodeDocument( ByteCodeSourceViewer editor )
	{
		super( );
		this.editor = editor;
	}

}
