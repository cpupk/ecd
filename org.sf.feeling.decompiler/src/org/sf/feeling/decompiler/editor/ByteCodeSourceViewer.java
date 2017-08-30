
package org.sf.feeling.decompiler.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.javaeditor.breadcrumb.EditorBreadcrumb;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.texteditor.LineNumberColumn;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.rulers.IColumnSupport;
import org.eclipse.ui.texteditor.rulers.RulerColumnDescriptor;
import org.eclipse.ui.texteditor.rulers.RulerColumnRegistry;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;
import org.sf.feeling.decompiler.actions.ByteCodeAction;
import org.sf.feeling.decompiler.actions.DisassemblerAction;
import org.sf.feeling.decompiler.actions.SourceCodeAction;
import org.sf.feeling.decompiler.util.Logger;
import org.sf.feeling.decompiler.util.ReflectionUtils;
import org.sf.feeling.decompiler.util.UIUtil;

import com.drgarbage.classfile.editors.ClassFileParser;
import com.drgarbage.utils.ClassFileDocumentsUtils;

public class ByteCodeSourceViewer extends AbstractDecoratedTextEditor
{

	private JavaDecompilerClassFileEditor editor;

	private ByteCodeDocument byteCodeDocument;

	private Composite container;

	public ByteCodeSourceViewer( JavaDecompilerClassFileEditor editor )
	{
		this.editor = editor;
	}

	public StyledText getTextWidget( )
	{
		return getSourceViewer( ).getTextWidget( );
	}

	private IPreferenceStore createCombinedPreferenceStore( )
	{
		List<IPreferenceStore> stores = new ArrayList<IPreferenceStore>( 3 );

		stores.add( JavaDecompilerPlugin.getDefault( ).getPreferenceStore( ) );
		stores.add( JavaPlugin.getDefault( ).getPreferenceStore( ) );
		stores.add( EditorsUI.getPreferenceStore( ) );

		return new ChainedPreferenceStore(
				(IPreferenceStore[]) stores.toArray( new IPreferenceStore[stores.size( )] ) );
	}

	public Composite createControl( Composite parent )
	{
		setSite( editor.getSite( ) );

		String classContent = editor.getDocumentProvider( ).getDocument( editor.getEditorInput( ) ).get( );
		DisassemblerDocumentProvider provider = new DisassemblerDocumentProvider( );
		setDocumentProvider( provider );
		setInput( editor.getEditorInput( ) );

		container = new Composite( parent, SWT.NONE );
		container.setLayout( new FillLayout( ) );
		IPreferenceStore store = createCombinedPreferenceStore( );
		setPreferenceStore( store );

		int styles = SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION;

		IVerticalRuler fVerticalRuler = createVerticalRuler( );
		ReflectionUtils.setFieldValue( this, "fVerticalRuler", fVerticalRuler ); //$NON-NLS-1$

		SourceViewer fSourceViewer = new JavaSourceViewer( container, fVerticalRuler, null, false, styles, store );
		ReflectionUtils.setFieldValue( this, "fSourceViewer", fSourceViewer ); //$NON-NLS-1$
		getSourceViewerDecorationSupport( fSourceViewer );

		createActions( );

		ReflectionUtils.invokeMethod( this, "initializeSourceViewer", new Class[]{ //$NON-NLS-1$
				IEditorInput.class
		}, new Object[]{
				getEditorInput( )
		} );

		if ( fSourceViewerDecorationSupport != null )
			fSourceViewerDecorationSupport.install( getPreferenceStore( ) );

		StyledText styledText = fSourceViewer.getTextWidget( );
		styledText.addMouseListener( getCursorListener( ) );
		styledText.addKeyListener( getCursorListener( ) );

		ReflectionUtils.setFieldValue( this, "fEditorContextMenuId", "#TextEditorContext" ); //$NON-NLS-1$ //$NON-NLS-2$
		String id = "#TextEditorContext"; //$NON-NLS-1$
		MenuManager manager = new MenuManager( id, id );
		manager.setRemoveAllWhenShown( true );
		manager.addMenuListener( getContextMenuListener( ) );
		Menu fTextContextMenu = manager.createContextMenu( styledText );
		styledText.setMenu( fTextContextMenu );

		ReflectionUtils.setFieldValue( this, "fRulerContextMenuId", "#TextRulerContext" ); //$NON-NLS-1$ //$NON-NLS-2$
		id = "#TextRulerContext"; //$NON-NLS-1$
		manager = new MenuManager( id, id );
		manager.setRemoveAllWhenShown( true );
		manager.addMenuListener( getContextMenuListener( ) );

		Control rulerControl = fVerticalRuler.getControl( );
		Menu fRulerContextMenu = manager.createContextMenu( rulerControl );
		rulerControl.setMenu( fRulerContextMenu );
		rulerControl.addMouseListener( getRulerMouseListener( ) );

		createOverviewRulerContextMenu( );

		JavaTextTools textTools = JavaPlugin.getDefault( ).getJavaTextTools( );
		IColorManager colorManager = textTools.getColorManager( );
		JavaSourceViewerConfiguration classFileConfiguration = new JavaSourceViewerConfiguration( colorManager,
				store,
				editor,
				IJavaPartitions.JAVA_PARTITIONING );
		fSourceViewer.configure( classFileConfiguration );
		setSourceViewerConfiguration( classFileConfiguration );
		getSourceViewerDecorationSupport( fSourceViewer ).install( getPreferenceStore( ) );
		initializeViewerColors( fSourceViewer );
		ReflectionUtils.invokeMethod( this, "initializeViewerFont", new Class[]{ //$NON-NLS-1$
				ISourceViewer.class
		}, new Object[]{
				fSourceViewer
		} );

		IClassFile cf = (ClassFile) ( (IClassFileEditorInput) editor.getEditorInput( ) ).getClassFile( );

		byteCodeDocument = new ByteCodeDocument( this );

		JavaTextTools tools = JavaPlugin.getDefault( ).getJavaTextTools( );
		tools.setupJavaDocumentPartitioner( byteCodeDocument, IJavaPartitions.JAVA_PARTITIONING );

		try
		{
			ClassFileParser parser = new ClassFileParser( );
			String content = parser.parseClassFile( cf.getBytes( ) );
			byteCodeDocument.set( ( content == null ? "" : content ) ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch ( Exception e )
		{
			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler( );
			try
			{
				String content = disassembler.disassemble( cf.getBytes( ), "\n", ClassFileBytesDisassembler.DETAILED ); //$NON-NLS-1$
				byteCodeDocument.set( ( content == null ? "" : content ) ); //$NON-NLS-1$ //$NON-NLS-2$
			}
			catch ( Exception ex )
			{
				Logger.debug( e );
			}
		}

		final EditorSelectionChangedListener fEditorSelectionChangedListener = new EditorSelectionChangedListener( );
		fEditorSelectionChangedListener.install( getSelectionProvider( ) );

		fSourceViewer.setDocument( byteCodeDocument );

		provider.setDocument( byteCodeDocument );

		IVerticalRuler ruler = getVerticalRuler( );
		if ( ruler instanceof CompositeRuler )
			updateContributedRulerColumns( (CompositeRuler) ruler );

		IColumnSupport columnSupport = (IColumnSupport) getAdapter( IColumnSupport.class );

		RulerColumnDescriptor lineNumberColumnDescriptor = RulerColumnRegistry.getDefault( )
				.getColumnDescriptor( LineNumberColumn.ID );
		if ( lineNumberColumnDescriptor != null )
			columnSupport.setColumnVisible( lineNumberColumnDescriptor, isLineNumberRulerVisible( ) );

		IPropertyChangeListener fFontPropertyChangeListener = (IPropertyChangeListener) ReflectionUtils
				.getFieldValue( this, "fFontPropertyChangeListener" ); //$NON-NLS-1$
		JFaceResources.getFontRegistry( ).addListener( fFontPropertyChangeListener );

		parent.addDisposeListener( new DisposeListener( ) {

			@Override
			public void widgetDisposed( DisposeEvent e )
			{
				ByteCodeSourceViewer.this.dispose( );
			}
		} );

		return container;
	}

	class JdtSelectionProvider extends SelectionProvider
	{

		private List<ISelectionChangedListener> fSelectionListeners = new ArrayList<ISelectionChangedListener>( );
		private List<ISelectionChangedListener> fPostSelectionListeners = new ArrayList<ISelectionChangedListener>( );
		private ITextSelection fInvalidSelection;
		private ISelection fValidSelection;

		/*
		 * @see org.eclipse.jface.viewers.ISelectionProvider#
		 * addSelectionChangedListener(ISelectionChangedListener)
		 */
		@Override
		public void addSelectionChangedListener( ISelectionChangedListener listener )
		{
			super.addSelectionChangedListener( listener );
			if ( getSourceViewer( ) != null )
				fSelectionListeners.add( listener );
		}

		/*
		 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
		 */
		@Override
		public ISelection getSelection( )
		{
			if ( fInvalidSelection != null )
				return fInvalidSelection;
			return super.getSelection( );
		}

		/*
		 * @see org.eclipse.jface.viewers.ISelectionProvider#
		 * removeSelectionChangedListener(ISelectionChangedListener)
		 */
		@Override
		public void removeSelectionChangedListener( ISelectionChangedListener listener )
		{
			super.removeSelectionChangedListener( listener );
			if ( getSourceViewer( ) != null )
				fSelectionListeners.remove( listener );
		}

		/*
		 * @see
		 * org.eclipse.jface.viewers.ISelectionProvider#setSelection(ISelection)
		 */
		@Override
		public void setSelection( ISelection selection )
		{
			if ( selection instanceof ITextSelection )
			{
				if ( fInvalidSelection != null )
				{
					fInvalidSelection = null;

					ITextSelection newSelection = (ITextSelection) selection;
					ITextSelection oldSelection = (ITextSelection) getSelection( );

					if ( newSelection.getOffset( ) == oldSelection.getOffset( )
							&& newSelection.getLength( ) == oldSelection.getLength( ) )
					{
						markValid( );
					}
					else
					{
						super.setSelection( selection );
					}
				}
				else
				{
					super.setSelection( selection );
				}
			}
			else if ( selection instanceof IStructuredSelection
					&& ( (IStructuredSelection) selection ).getFirstElement( ) instanceof EditorBreadcrumb )
			{
				markInvalid( );
			}
		}

		/*
		 * @see org.eclipse.jface.text.IPostSelectionProvider#
		 * addPostSelectionChangedListener(org.eclipse.jface.viewers.
		 * ISelectionChangedListener)
		 */
		@Override
		public void addPostSelectionChangedListener( ISelectionChangedListener listener )
		{
			super.addPostSelectionChangedListener( listener );
			if ( getSourceViewer( ) != null
					&& getSourceViewer( ).getSelectionProvider( ) instanceof IPostSelectionProvider )
				fPostSelectionListeners.add( listener );
		}

		/*
		 * @see org.eclipse.jface.text.IPostSelectionProvider#
		 * removePostSelectionChangedListener(org.eclipse.jface.viewers.
		 * ISelectionChangedListener)
		 */
		@Override
		public void removePostSelectionChangedListener( ISelectionChangedListener listener )
		{
			super.removePostSelectionChangedListener( listener );
			if ( getSourceViewer( ) != null )
				fPostSelectionListeners.remove( listener );
		}

		/*
		 * @see org.eclipse.jface.text.IPostSelectionValidator#isValid()
		 */
		@Override
		public boolean isValid( ISelection postSelection )
		{
			return fInvalidSelection == null && super.isValid( postSelection );
		}

		/**
		 * Marks this selection provider as currently being invalid. An invalid
		 * selection is one which can not be selected in the source viewer.
		 */
		private void markInvalid( )
		{
			fValidSelection = getSelection( );
			fInvalidSelection = new TextSelection( 0, 0 );

			SelectionChangedEvent event = new SelectionChangedEvent( this, fInvalidSelection );

			for ( ISelectionChangedListener listener : fSelectionListeners )
			{
				listener.selectionChanged( event );
			}

			for ( ISelectionChangedListener listener : fPostSelectionListeners )
			{
				listener.selectionChanged( event );
			}
		}

		/**
		 * Marks this selection provider as being valid.
		 */
		private void markValid( )
		{
			fInvalidSelection = null;

			SelectionChangedEvent event = new SelectionChangedEvent( this, fValidSelection );

			for ( ISelectionChangedListener listener : fSelectionListeners )
			{
				listener.selectionChanged( event );
			}

			for ( ISelectionChangedListener listener : fPostSelectionListeners )
			{
				listener.selectionChanged( event );
			}
		}
	}

	private class EditorSelectionChangedListener extends AbstractSelectionChangedListener
	{

		public void selectionChanged( SelectionChangedEvent event )
		{
			doHandleCursorPositionChanged( );
		}

	}

	private void doHandleCursorPositionChanged( )
	{
		StyledText byteCodeText = getSourceViewer( ).getTextWidget( );
		String byteCode = byteCodeText.getText( );
		int selectedRange = byteCodeText.getSelectionRange( ).x;
		IJavaElement element = getJavaElement( byteCode, selectedRange );
		if ( element != null )
		{
			editor.setSelection( element );
		}
	}

	public IJavaElement getJavaElement( String byteCode, int index )
	{
		if ( byteCode.lastIndexOf( "/* Methods: */" ) != -1 ) //$NON-NLS-1$
		{
			int methodStartIndex = byteCode.substring( 0, byteCode.lastIndexOf( "/* Methods: */" ) ).lastIndexOf( "\n" ) //$NON-NLS-1$ //$NON-NLS-2$
					+ 1;
			int methodEndIndex = byteCode.substring( 0, byteCode.lastIndexOf( "attributes_count" ) ) //$NON-NLS-1$
					.lastIndexOf( "\n" ); //$NON-NLS-1$
			if ( index >= methodStartIndex && index <= methodEndIndex )
			{
				return getMethod( byteCode.substring( methodStartIndex, methodEndIndex ), index - methodStartIndex );
			}

			if ( byteCode.lastIndexOf( "/* Fields: */" ) != -1 ) //$NON-NLS-1$
			{
				int fieldStartIndex = byteCode.substring( 0, byteCode.lastIndexOf( "/* Fields: */" ) ).lastIndexOf( //$NON-NLS-1$
						"\n" ) + 1; //$NON-NLS-1$
				int fieldEndIndex = methodStartIndex - 1;
				if ( index >= fieldStartIndex && index <= fieldEndIndex )
				{
					return getField( byteCode.substring( fieldStartIndex, fieldEndIndex ), index - fieldStartIndex );
				}
			}
		}
		else if ( byteCode.lastIndexOf( "/* Fields: */" ) != -1 ) //$NON-NLS-1$
		{
			int fieldStartIndex = byteCode.substring( 0, byteCode.lastIndexOf( "/* Fields: */" ) ).lastIndexOf( "\n" ) //$NON-NLS-1$ //$NON-NLS-2$
					+ 1;
			int fieldEndIndex = byteCode.substring( 0, byteCode.lastIndexOf( "attributes_count" ) ).lastIndexOf( "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
			if ( index >= fieldStartIndex && index <= fieldEndIndex )
			{
				return getField( byteCode.substring( fieldStartIndex, fieldEndIndex ), index - fieldStartIndex );
			}
		}

		ClassFile cf = (ClassFile) ( (IClassFileEditorInput) getEditorInput( ) ).getClassFile( );
		return cf.getType( );
	}

	private IField getField( String text, int index )
	{
		Pattern pattern = Pattern.compile( "Field\\[\\d+\\].+?Field\\[\\d+\\]", Pattern.DOTALL ); //$NON-NLS-1$
		Matcher matcher = pattern.matcher( text );

		int fieldStartIndex = text.substring( 0, index ).lastIndexOf( "\n" ) + 1; //$NON-NLS-1$

		int findIndex = 0;
		while ( matcher.find( findIndex ) )
		{
			int start = text.substring( 0, matcher.start( ) ).lastIndexOf( "\n" ) + 1; //$NON-NLS-1$
			int end = text.substring( 0, matcher.end( ) ).lastIndexOf( "\n" ); //$NON-NLS-1$
			if ( fieldStartIndex >= start && fieldStartIndex <= end )
			{
				String field = text.substring( start, end );
				return getField( field );
			}
			findIndex = end;
		}

		if ( text.lastIndexOf( "Field[" ) != -1 ) //$NON-NLS-1$
		{
			int start = text.substring( 0, text.lastIndexOf( "Field[" ) ).lastIndexOf( "\n" ) + 1; //$NON-NLS-1$ //$NON-NLS-2$
			int end = text.length( );
			if ( fieldStartIndex >= start && fieldStartIndex <= end )
			{
				String field = text.substring( start, end );
				return getField( field );
			}
		}
		return null;
	}

	private IField getField( String field )
	{
		ClassFile cf = (ClassFile) ( (IClassFileEditorInput) getEditorInput( ) ).getClassFile( );
		int nameIndex = field.indexOf( "name_index" ); //$NON-NLS-1$
		if ( nameIndex != -1 )
		{
			String startText = field.substring( nameIndex );
			int firstIndex = startText.indexOf( '"' );
			if ( firstIndex != -1 )
			{
				int lastIndex = startText.indexOf( '"', firstIndex + 1 );
				if ( lastIndex != -1 )
				{
					String fieldName = startText.substring( firstIndex + 1, lastIndex );
					IField f = cf.getType( ).getField( fieldName );
					if ( f != null )
					{
						return f;
					}
				}
			}
		}
		return null;
	}

	private IMethod getMethod( String text, int index )
	{
		Pattern pattern = Pattern.compile( "Method\\[\\d+\\].+?Method\\[\\d+\\]", Pattern.DOTALL ); //$NON-NLS-1$
		Matcher matcher = pattern.matcher( text );

		int methodStartIndex = text.substring( 0, index ).lastIndexOf( "\n" ) + 1; //$NON-NLS-1$

		int findIndex = 0;
		while ( matcher.find( findIndex ) )
		{
			int start = text.substring( 0, matcher.start( ) ).lastIndexOf( "\n" ) + 1; //$NON-NLS-1$
			int end = text.substring( 0, matcher.end( ) ).lastIndexOf( "\n" ); //$NON-NLS-1$
			if ( methodStartIndex >= start && methodStartIndex <= end )
			{
				String method = text.substring( start, end );
				return getMethod( method );
			}
			findIndex = end;
		}

		if ( text.lastIndexOf( "Method[" ) != -1 ) //$NON-NLS-1$
		{
			int start = text.substring( 0, text.lastIndexOf( "Method[" ) ).lastIndexOf( "\n" ) + 1; //$NON-NLS-1$ //$NON-NLS-2$
			int end = text.length( );
			if ( methodStartIndex >= start && methodStartIndex <= end )
			{
				String method = text.substring( start, end );
				return getMethod( method );
			}
		}
		return null;
	}

	private IMethod getMethod( String method )
	{
		ClassFile cf = (ClassFile) ( (IClassFileEditorInput) getEditorInput( ) ).getClassFile( );

		String methodName = null;
		String descriptor = null;

		int nameIndex = method.indexOf( "name_index" ); //$NON-NLS-1$
		if ( nameIndex != -1 )
		{

			String startText = method.substring( nameIndex );
			int firstIndex = startText.indexOf( '"' );
			if ( firstIndex != -1 )
			{
				int lastIndex = startText.indexOf( '"', firstIndex + 1 );
				if ( lastIndex != -1 )
				{
					methodName = startText.substring( firstIndex + 1, lastIndex );
				}
			}

		}

		if ( methodName == null )
			return null;

		if ( "<init>".equals( methodName ) ) //$NON-NLS-1$
		{
			methodName = cf.getTypeName( );
		}

		int descriptorIndex = method.indexOf( "descriptor_index" ); //$NON-NLS-1$
		if ( descriptorIndex != -1 )
		{
			String startText = method.substring( descriptorIndex );
			int firstIndex = startText.indexOf( '"' );
			if ( firstIndex != -1 )
			{
				int lastIndex = startText.indexOf( '"', firstIndex + 1 );
				if ( lastIndex != -1 )
				{
					descriptor = startText.substring( firstIndex + 1, lastIndex );
				}
			}

		}

		if ( descriptor == null )
			return null;

		try
		{
			IMethod m = ClassFileDocumentsUtils.findMethod( cf.getType( ), methodName, descriptor );
			if ( m != null )
			{
				return m;
			}
		}
		catch ( JavaModelException e )
		{
			Logger.debug( e );
		}
		return null;
	}

	protected void editorContextMenuAboutToShow( IMenuManager menu )
	{
		super.editorContextMenuAboutToShow( menu );
		String text = (String) ReflectionUtils.invokeMethod( this, "getShowInMenuLabel" ); //$NON-NLS-1$
		for ( int i = 0; i < menu.getItems( ).length; i++ )
		{
			if ( menu.getItems( )[i] instanceof MenuManager )
			{
				if ( ( (MenuManager) menu.getItems( )[i] ).getMenuText( ).equals( text ) )
				{
					menu.remove( menu.getItems( )[i] );
				}
			}
		}

		menu.appendToGroup( ITextEditorActionConstants.GROUP_OPEN, getAction( SourceCodeAction.ID ) );
		menu.appendToGroup( ITextEditorActionConstants.GROUP_OPEN, getAction( ByteCodeAction.ID ) );
		menu.appendToGroup( ITextEditorActionConstants.GROUP_OPEN, getAction( DisassemblerAction.ID ) );
		menu.addMenuListener( new IMenuListener( ) {

			@Override
			public void menuAboutToShow( IMenuManager manager )
			{
				showMenu( manager );
			}
		} );
	}

	private void showMenu( IMenuManager submenu )
	{
		for ( Iterator<IContributionItem> iter = Arrays.asList( submenu.getItems( ) ).iterator( ); iter.hasNext( ); )
		{
			IContributionItem item = iter.next( );
			if ( item instanceof ActionContributionItem )
			{
				IAction action = ( (ActionContributionItem) item ).getAction( );
				if ( action instanceof IUpdate )
				{
					( (IUpdate) action ).update( );
				}
			}
		}
	}

	@Override
	protected void createActions( )
	{
		super.createActions( );
		setAction( SourceCodeAction.ID, new SourceCodeAction( ) );
		setAction( ByteCodeAction.ID, new ByteCodeAction( ) );
		setAction( DisassemblerAction.ID, new DisassemblerAction( ) );
	}

	@Override
	public String[] collectContextMenuPreferencePages( )
	{
		return editor.collectContextMenuPreferencePages( );
	}

	public boolean isEditorInputModifiable( )
	{
		return false;
	}

	public Control getControl( )
	{
		return container;
	}

	public void setSelectionElement( ISourceReference selectedElement )
	{
		setSelectionElement( selectedElement, false );
	}

	public void setSelectionElement( ISourceReference selectedElement, boolean force )
	{
		final StyledText byteCodeText = getSourceViewer( ).getTextWidget( );
		if ( JavaDecompilerPlugin.getDefault( ).getSourceMode( ) == JavaDecompilerPlugin.BYTE_CODE_MODE
				&& byteCodeText != null
				&& !byteCodeText.isDisposed( ) )
		{
			if ( !force )
			{
				if ( UIUtil.requestFromDisassemblerSelection( ) )
					return;

				if ( !UIUtil.requestFromLinkToSelection( ) )
					return;
			}

			if ( selectedElement instanceof IMethod
					|| selectedElement instanceof IField
					|| selectedElement instanceof BinaryType )
			{
				try
				{
					IRegion element = searchElement( byteCodeText, selectedElement );
					if ( element != null )
					{
						selectElement( byteCodeText, element );
					}
				}
				catch ( Exception e )
				{
					Logger.debug( e );
				}
			}
		}
	}

	private IRegion searchElement( StyledText byteCodeText, ISourceReference reference ) throws CoreException
	{
		ClassFile cf = (ClassFile) ( (IClassFileEditorInput) getEditorInput( ) ).getClassFile( );
		if ( reference instanceof BinaryType )
		{
			String byteCode = byteCodeText.getText( );
			String className = ( (BinaryType) reference ).getElementName( );
			Pattern pattern = Pattern.compile( "this_class.+?\\*", Pattern.DOTALL ); //$NON-NLS-1$
			Matcher matcher = pattern.matcher( byteCode );
			while ( matcher.find( ) )
			{
				String text = matcher.group( );
				int classIndex = text.indexOf( className );
				if ( classIndex != -1 )
				{
					return new Region( matcher.start( ) + classIndex, className.length( ) );
				}
			}
		}
		else if ( reference instanceof IField )
		{
			BinaryType jdtType = (BinaryType) ( (IField) reference ).getParent( );
			if ( jdtType.equals( cf.getType( ) ) )
			{
				String byteCode = byteCodeText.getText( );
				int index = byteCode.indexOf( "/* Fields: */" ); //$NON-NLS-1$
				if ( index != -1 )
				{
					String fieldName = ( (IField) reference ).getElementName( );
					String fieldByteCode = "bytes=\"" + fieldName + "\""; //$NON-NLS-1$ //$NON-NLS-2$
					Pattern pattern = Pattern.compile( "Field\\[\\d+\\].+?attributes_count", Pattern.DOTALL ); //$NON-NLS-1$
					Matcher matcher = pattern.matcher( byteCode );
					while ( matcher.find( ) )
					{
						String text = matcher.group( );
						int fieldIndex = text.indexOf( fieldByteCode );
						if ( fieldIndex != -1 )
						{
							return new Region( matcher.start( ) + fieldIndex + "bytes=\"".length( ), //$NON-NLS-1$
									fieldName.length( ) );
						}
					}
				}
			}
		}
		else if ( reference instanceof IMethod )
		{
			BinaryType jdtType = (BinaryType) ( (IMethod) reference ).getParent( );
			if ( jdtType.equals( cf.getType( ) ) )
			{
				String byteCode = byteCodeText.getText( );
				int index = byteCode.indexOf( "/* Methods: */" ); //$NON-NLS-1$
				if ( index != -1 )
				{
					String methodName = ( (IMethod) reference ).getElementName( );
					if ( ( (IMethod) reference ).isConstructor( ) )
					{
						methodName = "<init>"; //$NON-NLS-1$
					}
					String methodByteCode = "bytes=\"" + methodName + "\""; //$NON-NLS-1$ //$NON-NLS-2$
					String methodSignature = ( (IMethod) reference ).getSignature( );
					Pattern pattern = Pattern.compile( "Method\\[\\d+\\].+?attributes_count", Pattern.DOTALL ); //$NON-NLS-1$
					Matcher matcher = pattern.matcher( byteCode );
					while ( matcher.find( ) )
					{
						String text = matcher.group( );
						int methodIndex = text.indexOf( methodByteCode );
						int methodSignatureIndex = text.indexOf( methodSignature );
						if ( methodIndex != -1 && methodSignatureIndex != -1 )
						{
							return new Region( matcher.start( ) + methodIndex + "bytes=\"".length( ), //$NON-NLS-1$
									methodName.length( ) );
						}
					}
				}
			}
		}
		return null;
	}

	private void selectElement( StyledText byteCodeText, IRegion region )
	{
		if ( region != null && region.getOffset( ) != -1 )
		{
			byteCodeText.setSelection( region.getOffset( ), region.getOffset( ) + region.getLength( ) );
		}
	}

	protected IConfigurationElement getConfigurationElement( )
	{
		return (IConfigurationElement) ReflectionUtils.invokeMethod( editor, "getConfigurationElement" ); //$NON-NLS-1$
	}

	@Override
	public boolean isDirty( )
	{
		return false;
	}

	public boolean isEditable( )
	{
		return false;
	}

	@Override
	public boolean isEditorInputReadOnly( )
	{
		return true;
	}

	protected void handleCursorPositionChanged( )
	{
		ReflectionUtils.invokeMethod( editor, "handleCursorPositionChanged" ); //$NON-NLS-1$
	}
}
