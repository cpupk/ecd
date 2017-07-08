
package org.sf.feeling.decompiler.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
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
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
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
import org.sf.feeling.decompiler.util.MarkUtil;
import org.sf.feeling.decompiler.util.ReflectionUtils;
import org.sf.feeling.decompiler.util.UIUtil;

import com.drgarbage.classfile.editors.ClassFileParser;

public class ByteCodeSourceViewer extends AbstractDecoratedTextEditor
{

	private JavaDecompilerClassFileEditor editor;

	private Document byteCodeDocument;

	private Composite container;

	public ByteCodeSourceViewer( JavaDecompilerClassFileEditor editor )
	{
		this.editor = editor;
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
		String mark = MarkUtil.getMark( classContent );
		ByteCodeDocumentProvider provider = new ByteCodeDocumentProvider( mark );
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

		if ( fSourceViewerDecorationSupport != null )
			fSourceViewerDecorationSupport.install( getPreferenceStore( ) );

		StyledText styledText = fSourceViewer.getTextWidget( );

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

		byteCodeDocument = new Document( );

		JavaTextTools tools = JavaPlugin.getDefault( ).getJavaTextTools( );
		tools.setupJavaDocumentPartitioner( byteCodeDocument, IJavaPartitions.JAVA_PARTITIONING );

		try
		{
			ClassFileParser parser = new ClassFileParser( );
			String content = parser.parseClassFile( cf.getBytes( ) );
			byteCodeDocument.set( mark + "\n\n" + ( content == null ? "" : content ) ); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch ( Exception e )
		{
			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler( );
			try
			{
				String content = disassembler.disassemble( cf.getBytes( ), "\n", ClassFileBytesDisassembler.DETAILED ); //$NON-NLS-1$
				byteCodeDocument.set( mark + "\n\n" + ( content == null ? "" : content ) ); //$NON-NLS-1$ //$NON-NLS-2$
			}
			catch ( Exception ex )
			{
				Logger.debug( e );
			}
		}
		fSourceViewer.setDocument( byteCodeDocument );

		IVerticalRuler ruler = getVerticalRuler( );
		if ( ruler instanceof CompositeRuler )
			updateContributedRulerColumns( (CompositeRuler) ruler );

		IColumnSupport columnSupport = getAdapter( IColumnSupport.class );

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

		String ad = mark.replaceAll( "/(\\*)+", "" ) //$NON-NLS-1$ //$NON-NLS-2$
				.replaceAll( "(\\*)+/", "" ) //$NON-NLS-1$ //$NON-NLS-2$
				.trim( );
		int length = ad.length( );
		int offset = mark.indexOf( ad );

		StyleRange textRange = UIUtil.getAdTextStyleRange( styledText, offset, length );
		if ( textRange != null )
		{
			styledText.setStyleRange( textRange );
		}

		URLHyperlinkDetector detector = new URLHyperlinkDetector( );
		final int index = mark.indexOf( "://" ); //$NON-NLS-1$
		final IHyperlink[] links = detector.detectHyperlinks( getSourceViewer( ), new Region( index, 0 ), true );
		for ( int j = 0; j < links.length; j++ )
		{
			IHyperlink link = links[j];
			StyleRange linkRange = UIUtil.getAdLinkStyleRange( styledText,
					link.getHyperlinkRegion( ).getOffset( ),
					link.getHyperlinkRegion( ).getLength( ) );
			if ( linkRange != null )
			{
				styledText.setStyleRange( linkRange );
			}
		}

		return container;
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

	protected IConfigurationElement getConfigurationElement( )
	{
		return (IConfigurationElement) ReflectionUtils.invokeMethod( editor, "getConfigurationElement" ); //$NON-NLS-1$
	}

}
