
package org.sf.feeling.decompiler.editor;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocBrowserInformationControlInput;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocHover;
import org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.sf.feeling.decompiler.util.HelpUtils;
import org.sf.feeling.decompiler.util.Logger;
import org.sf.feeling.decompiler.util.ReflectionUtils;

import com.drgarbage.asm.render.intf.IClassFileDocument;
import com.drgarbage.asm.render.intf.IFieldSection;
import com.drgarbage.asm.render.intf.IMethodSection;
import com.drgarbage.bytecode.ByteCodeConstants;
import com.drgarbage.utils.ClassFileDocumentsUtils;

public class DisassemblerJavadocHover extends JavadocHover implements IJavaEditorTextHover
{

	public Object getHoverInfo2( ITextViewer textViewer, IRegion hoverRegion )
	{
		try
		{
			if ( textViewer.getDocument( ) instanceof ByteCodeDocument )
			{
				ByteCodeDocument bytecodeDocument = (ByteCodeDocument) textViewer.getDocument( );
				String text = textViewer.getDocument( ).get( hoverRegion.getOffset( ), hoverRegion.getLength( ) );

				for ( int i = 0; i < ByteCodeConstants.OPCODE_MNEMONICS.length; i++ )
				{
					if ( text.equals( ByteCodeConstants.OPCODE_MNEMONICS[i] ) )
					{
						StringBuilder sb = HelpUtils.getOpcodeHelpFor( i );
						if ( sb.length( ) > 0 )
						{
							JavadocBrowserInformationControlInput input = new JavadocBrowserInformationControlInput(
									null,
									null,
									sb.toString( ),
									0 );
							return input;
						}
					}
				}

				if ( text.indexOf( "$" ) != -1 ) //$NON-NLS-1$
				{
					text = text.substring( text.lastIndexOf( "$" ) + 1 ); //$NON-NLS-1$
				}
				int line = bytecodeDocument.getLineOfOffset( hoverRegion.getOffset( ) );
				ByteCodeDocumentProvider provider = bytecodeDocument.getDocumentProvider( );
				IClassFileDocument disassemblerClassDocument = provider.getClassFileDocument( );
				JavaDecompilerClassFileEditor editor = bytecodeDocument.getEditor( );
				ClassFile cf = (ClassFile) ( (IClassFileEditorInput) editor.getEditorInput( ) ).getClassFile( );

				int offset = -1;

				if ( disassemblerClassDocument.isLineInMethod( line - 2 ) )
				{
					IMethodSection method = disassemblerClassDocument.findMethodSection( line - 2 );

					if ( method != null )
					{
						IMethod m = ClassFileDocumentsUtils
								.findMethod( cf.getType( ), method.getName( ), method.getDescriptor( ) );
						if ( m != null )
						{
							ISourceRange range = m.getSourceRange( );
							if ( m.getJavadocRange( ) != null )
							{
								range = new SourceRange(
										m.getJavadocRange( ).getOffset( ) + m.getJavadocRange( ).getLength( ),
										range.getLength( ) - m.getJavadocRange( ).getLength( ) );
							}
							if ( range != null && range.getOffset( ) != -1 )
							{
								offset = m.getClassFile( )
										.getBuffer( )
										.getText( range.getOffset( ), range.getLength( ) )
										.indexOf( text );
								if ( offset != -1 )
								{
									hoverRegion = new Region( range.getOffset( ) + offset, text.length( ) );
								}
							}
						}
					}
				}
				else if ( disassemblerClassDocument.isLineInField( line - 2 ) )
				{
					IFieldSection field = disassemblerClassDocument.findFieldSection( line - 2 );
					if ( field != null )
					{
						IField f = cf.getType( ).getField( field.getName( ) );
						if ( f != null )
						{
							ISourceRange range = f.getSourceRange( );
							if ( f.getJavadocRange( ) != null )
							{
								range = new SourceRange(
										f.getJavadocRange( ).getOffset( ) + f.getJavadocRange( ).getLength( ),
										range.getLength( ) - f.getJavadocRange( ).getLength( ) );
							}
							if ( range != null && range.getOffset( ) != -1 )
							{
								offset = f.getClassFile( )
										.getBuffer( )
										.getText( range.getOffset( ), range.getLength( ) )
										.indexOf( text );
								if ( offset != -1 )
								{
									hoverRegion = new Region( range.getOffset( ) + offset, text.length( ) );
								}
							}
							if ( offset == -1 )
							{
								range = cf.getType( ).getSourceRange( );
								offset = cf.getType( )
										.getClassFile( )
										.getBuffer( )
										.getText( 0, range.getOffset( ) + range.getLength( ) )
										.indexOf( text );
								if ( offset != -1 )
								{
									hoverRegion = new Region( offset, text.length( ) );
								}
							}
						}
					}
				}

				if ( offset == -1 )
				{
					ISourceRange range = cf.getType( ).getSourceRange( );
					if ( range != null && range.getOffset( ) + range.getLength( ) > -1 )
					{
						offset = cf.getType( )
								.getClassFile( )
								.getBuffer( )
								.getText( 0, range.getOffset( ) + range.getLength( ) )
								.indexOf( text );
						if ( offset != -1 )
						{
							hoverRegion = new Region( offset, text.length( ) );
						}
					}
				}

			}
		}
		catch ( Exception e )
		{
			Logger.debug( e );
		}

		IJavaElement[] elements = getJavaElementsAt( textViewer, hoverRegion );
		if ( elements == null || elements.length == 0 )
		{
			return null;
		}

		return ReflectionUtils.invokeMethod( this, "getHoverInfo", new Class[]{
				IJavaElement[].class, ITypeRoot.class, IRegion.class, JavadocBrowserInformationControlInput.class
		}, new Object[]{
				elements, getEditorInputJavaElement( ), hoverRegion, null
		} );
	}
}
