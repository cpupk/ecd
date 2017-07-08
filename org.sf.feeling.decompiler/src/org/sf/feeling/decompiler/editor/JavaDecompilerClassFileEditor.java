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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.BufferManager;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.ui.actions.CompositeActionGroup;
import org.eclipse.jdt.internal.ui.javaeditor.ClassFileEditor;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jdt.internal.ui.javaeditor.InternalClassFileEditorInput;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.HyperlinkManager;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.search2.internal.ui.text.AnnotationManagers;
import org.eclipse.search2.internal.ui.text.EditorAnnotationManager;
import org.eclipse.search2.internal.ui.text.WindowAnnotationManager;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;
import org.sf.feeling.decompiler.actions.DecompileActionGroup;
import org.sf.feeling.decompiler.util.ClassUtil;
import org.sf.feeling.decompiler.util.DecompileUtil;
import org.sf.feeling.decompiler.util.DecompilerOutputUtil;
import org.sf.feeling.decompiler.util.FileUtil;
import org.sf.feeling.decompiler.util.Logger;
import org.sf.feeling.decompiler.util.MarkUtil;
import org.sf.feeling.decompiler.util.ReflectionUtils;
import org.sf.feeling.decompiler.util.UIUtil;

public class JavaDecompilerClassFileEditor extends ClassFileEditor
{

	public static final String ID = "org.sf.feeling.decompiler.ClassFileEditor"; //$NON-NLS-1$

	private IBuffer classBuffer;
	private PaintListener paintListener;
	private MouseAdapter mouseAdapter;
	private int currentSourceMode = -1;
	private boolean selectionChange = false;
	private ISourceReference selectedElement = null;
	
	public ISourceReference getSelectedElement( )
	{
		return selectedElement;
	}

	private DisassemblerSourceViewer fDisassemblerSourceViewer;
	private ByteCodeSourceViewer fByteCodeSourceViewer;

	public JavaDecompilerClassFileEditor( )
	{
		super( );
	}

	private boolean doOpenBuffer( IEditorInput input, boolean force ) throws JavaModelException
	{
		IPreferenceStore prefs = JavaDecompilerPlugin.getDefault( ).getPreferenceStore( );
		String decompilerType = prefs.getString( JavaDecompilerPlugin.DECOMPILER_TYPE );
		return doOpenBuffer( input, decompilerType, force );
	}

	private boolean doOpenBuffer( IEditorInput input, String type, boolean force ) throws JavaModelException
	{
		IPreferenceStore prefs = JavaDecompilerPlugin.getDefault( ).getPreferenceStore( );
		boolean reuseBuf = prefs.getBoolean( JavaDecompilerPlugin.REUSE_BUFFER );
		boolean always = prefs.getBoolean( JavaDecompilerPlugin.IGNORE_EXISTING );
		return doOpenBuffer( input, type, force, reuseBuf, always );
	}

	private boolean doOpenBuffer( IEditorInput input, String type, boolean force, boolean reuseBuf, boolean always )
			throws JavaModelException
	{
		if ( UIUtil.isDebugPerspective( ) || JavaDecompilerPlugin.getDefault( ).isDebugMode( ) )
			reuseBuf = false;

		if ( input instanceof IClassFileEditorInput )
		{

			boolean opened = false;
			IClassFile cf = ( (IClassFileEditorInput) input ).getClassFile( );

			String decompilerType = type;
			String origSrc = cf.getSource( );
			if ( origSrc == null
					|| ( origSrc != null
							&& always
							&& ( !MarkUtil.containsMark( origSrc )
									|| ( MarkUtil.containsMark( origSrc ) && !reuseBuf ) ) )
					|| ( origSrc != null && !always && !MarkUtil.containsMark( origSrc ) && !reuseBuf )
					|| debugOptionChange( origSrc )
					|| force )
			{
				DecompilerSourceMapper sourceMapper = SourceMapperFactory.getSourceMapper( decompilerType );
				char[] src = sourceMapper == null ? null : sourceMapper.findSource( cf.getType( ) );
				if ( src == null )
				{
					if ( DecompilerType.FernFlower.equals( decompilerType ) )
					{
						src = SourceMapperFactory.getSourceMapper( DecompilerType.FernFlower )
								.findSource( cf.getType( ) );
					}
					else
					{
						IDecompilerDescriptor decompilerDescriptor = JavaDecompilerPlugin.getDefault( )
								.getDecompilerDescriptor( decompilerType );
						if ( decompilerDescriptor != null )
						{
							src = decompilerDescriptor.getDecompilerSourceMapper( ).findSource( cf.getType( ) );
						}
					}
				}
				if ( src == null )
				{
					return false;
				}
				char[] markedSrc = src;
				classBuffer = BufferManager.createBuffer( cf );
				classBuffer.setContents( markedSrc );
				getBufferManager( ).addBuffer( classBuffer );

				sourceMapper.mapSource( cf.getType( ), markedSrc, true );

				ClassFileSourceMap.updateSource( getBufferManager( ), (ClassFile) cf, markedSrc );

				String content = new String( markedSrc );
				if ( MarkUtil.containsMark( content ) )
				{
					if ( getSourceViewer( ) != null
							&& getSourceViewer( ).getTextWidget( ) != null
							&& !getSourceViewer( ).getTextWidget( ).isDisposed( ) )
					{

						if ( !getSourceViewer( ).getTextWidget( ).getText( ).equals( content ) )
						{
							clearSelection( );
						}
					}
				}

				opened = true;
			}
			else
			{
				DecompileUtil.checkAndUpdateCopyright( this, cf, origSrc );
			}
			return opened;

		}
		return false;
	}

	public void clearSelection( )
	{
		if ( getSourceViewer( ) != null
				&& getSourceViewer( ).getTextWidget( ) != null
				&& !getSourceViewer( ).getTextWidget( ).isDisposed( ) )
		{
			getSourceViewer( ).getTextWidget( ).setSelectionRange( 0, 0 );
		}
	}

	@Override
	public boolean isDirty( )
	{
		return false;
	}

	@Override
	protected void selectionChanged( )
	{
		selectionChange = true;
		super.selectionChanged( );
		selectionChange = false;
	}

	protected void setSelection( ISourceReference reference, boolean moveCursor )
	{
		super.setSelection( reference, moveCursor );

		this.selectedElement = reference;

		if ( fDisassemblerSourceViewer != null )
		{
			fDisassemblerSourceViewer.setSelectionElement( reference );
		}
	}

	@Override
	public void setHighlightRange( int offset, int length, boolean moveCursor )
	{
		super.setHighlightRange( offset, length, moveCursor );

		if ( selectionChange )
			return;

		IClassFileEditorInput classFileEditorInput = (IClassFileEditorInput) getEditorInput( );
		final IClassFile file = classFileEditorInput.getClassFile( );

		Display.getDefault( ).asyncExec( new Runnable( ) {

			@Override
			public void run( )
			{
				try
				{
					DecompileUtil.updateBuffer( file, file.getBuffer( ).getContents( ) );
				}
				catch ( JavaModelException e )
				{
					Logger.debug( e );
				}
			}
		} );

		final StyledText widget = getSourceViewer( ).getTextWidget( );
		widget.getDisplay( ).asyncExec( new Runnable( ) {

			@Override
			public void run( )
			{
				if ( !widget.isDisposed( ) )
				{
					if ( widget.getVerticalBar( ) != null )
					{
						int selection = widget.getVerticalBar( ).getSelection( );

						if ( selection > 0
								&& selection < widget.getBounds( ).height / 2
								&& widget.getLocationAtOffset( widget.getSelection( ).x ).y
										+ selection
										+ widget.getLineHeight( ) * 2 < widget.getBounds( ).height )
						{
							ReflectionUtils.invokeMethod( widget, "scrollVertical", new Class[]{ //$NON-NLS-1$
									int.class, boolean.class
							}, new Object[]{
									-selection, true
							} );
						}
					}
				}
			};
		} );
	}

	@Override
	public void selectAndReveal( int start, int length )
	{
		if ( UIUtil.requestFromShowMatch( ) )
		{
			String src = this.getDocumentProvider( ).getDocument( this.getEditorInput( ) ).get( );
			if ( MarkUtil.containsSourceMark( src ) )
			{
				if ( src.indexOf( "\r\n" ) != -1 ) //$NON-NLS-1$
				{
					super.selectAndReveal( start + MarkUtil.getMark( src ).length( ) - 1, length );
				}
				else
				{
					super.selectAndReveal( start + MarkUtil.getMark( src ).length( ), length );
				}
			}
			else
			{
				super.selectAndReveal( start, length );
			}
		}
		else
		{
			super.selectAndReveal( start, length );
		}
	}

	public static boolean debugOptionChange( String source )
	{
		return isDebug( source ) != ClassUtil.isDebug( );
	}

	public static boolean isDebug( String source )
	{
		if ( source == null )
			return false;
		Pattern pattern = Pattern.compile( "/\\*\\s*\\d+\\s*\\*/" ); //$NON-NLS-1$
		Matcher matcher = pattern.matcher( source );
		return matcher.find( ) || source.indexOf( DecompilerOutputUtil.NO_LINE_NUMBER ) != -1;
	}

	public IBuffer getClassBuffer( )
	{
		return classBuffer;
	}

	/**
	 * Sets edditor input only if buffer was actually opened.
	 * 
	 * @param force
	 *            if <code>true</code> initialize no matter what
	 */
	public void doSetInput( boolean force )
	{
		IEditorInput input = getEditorInput( );
		try
		{
			if ( doOpenBuffer( input, force ) )
			{
				super.doSetInput( input );
			}
		}
		catch ( Exception e )
		{
			JavaDecompilerPlugin.logError( e, "" ); //$NON-NLS-1$
		}

		handleMarkLink( );
	}

	public void doSetInput( String type, boolean force )
	{
		IEditorInput input = getEditorInput( );
		try
		{
			if ( doOpenBuffer( input, type, force ) )
			{
				super.doSetInput( input );
			}
		}
		catch ( Exception e )
		{
			JavaDecompilerPlugin.logError( e, "" ); //$NON-NLS-1$
		}

		handleMarkLink( );
	}

	@Override
	protected void doSetInput( IEditorInput input ) throws CoreException
	{
		JavaDecompilerPlugin.getDefault( ).getDecompileCount( ).getAndIncrement( );
		if ( input instanceof IFileEditorInput )
		{
			String filePath = UIUtil.getPathLocation( ( (IFileEditorInput) input ).getStorage( ).getFullPath( ) );
			if ( filePath == null || !new File( filePath ).exists( ) )
			{
				super.doSetInput( input );
			}
			else
			{
				doSetInput(
						new DecompilerClassEditorInput( EFS.getLocalFileSystem( ).getStore( new Path( filePath ) ) ) );
			}
		}
		else if ( input instanceof FileStoreEditorInput )
		{

			FileStoreEditorInput storeInput = (FileStoreEditorInput) input;
			IPreferenceStore prefs = JavaDecompilerPlugin.getDefault( ).getPreferenceStore( );
			String decompilerType = prefs.getString( JavaDecompilerPlugin.DECOMPILER_TYPE );
			String source = DecompileUtil.decompiler( storeInput, decompilerType );

			if ( source != null )
			{
				String packageName = DecompileUtil.getPackageName( source );
				String classFullName = packageName == null ? storeInput.getName( )
						: packageName
								+ "." //$NON-NLS-1$
								+ storeInput.getName( ).replaceAll( "(?i)\\.class", //$NON-NLS-1$
										"" ); //$NON-NLS-1$

				File file = new File( System.getProperty( "java.io.tmpdir" ), //$NON-NLS-1$
						storeInput.getName( ).replaceAll( "(?i)\\.class", //$NON-NLS-1$
								System.currentTimeMillis( ) + ".java" ) ); //$NON-NLS-1$
				FileUtil.writeToFile( file, source, ResourcesPlugin.getEncoding( ) );
				file.deleteOnExit( );

				DecompilerClassEditorInput editorInput = new DecompilerClassEditorInput(
						EFS.getLocalFileSystem( ).getStore( new Path( file.getAbsolutePath( ) ) ) );
				editorInput.setToolTipText( classFullName );

				IEditorPart editor = PlatformUI.getWorkbench( ).getActiveWorkbenchWindow( ).getActivePage( ).openEditor(
						editorInput,
						"org.eclipse.jdt.ui.CompilationUnitEditor" ); //$NON-NLS-1$
				try
				{
					ReflectionUtils.invokeMethod( editor,
							"setPartName", //$NON-NLS-1$
							new Class[]{
									String.class
							},
							new String[]{
									storeInput.getName( )
							} );

					ReflectionUtils.invokeMethod( editor,
							"setTitleImage", //$NON-NLS-1$
							new Class[]{
									Image.class
							},
							new Object[]{
									JavaDecompilerPlugin.getImageDescriptor( "icons/decompiler.png" ) //$NON-NLS-1$
											.createImage( )
							} );

					ReflectionUtils.setFieldValue( editor,
							"fIsEditingDerivedFileAllowed", //$NON-NLS-1$
							Boolean.valueOf( false ) );
				}
				catch ( Exception e )
				{
					JavaDecompilerPlugin.logError( e, "" ); //$NON-NLS-1$
				}
			}
			Display.getDefault( ).asyncExec( new Runnable( ) {

				@Override
				public void run( )
				{
					JavaDecompilerClassFileEditor.this.getEditorSite( )
							.getPage( )
							.closeEditor( JavaDecompilerClassFileEditor.this, false );
				}
			} );

			throw new CoreException( new Status( 8,
					JavaDecompilerPlugin.PLUGIN_ID,
					1,
					"", //$NON-NLS-1$
					null ) );
		}
		else
		{
			if ( input instanceof InternalClassFileEditorInput )
			{
				InternalClassFileEditorInput classInput = (InternalClassFileEditorInput) input;

				if ( classInput.getClassFile( ).getParent( ) instanceof PackageFragment )
				{
					doOpenBuffer( input, false );
				}
				else
				{
					IPath relativePath = classInput.getClassFile( ).getParent( ).getPath( );
					String location = UIUtil.getPathLocation( relativePath );
					if ( !( FileUtil.isZipFile( location ) || FileUtil.isZipFile( relativePath.toOSString( ) ) ) )
					{
						String filePath = UIUtil.getPathLocation( classInput.getClassFile( ).getPath( ) );
						if ( filePath != null )
						{
							DecompilerClassEditorInput editorInput = new DecompilerClassEditorInput(
									EFS.getLocalFileSystem( ).getStore( new Path( filePath ) ) );
							doSetInput( editorInput );
						}
						else
						{
							doSetInput( new DecompilerClassEditorInput(
									EFS.getLocalFileSystem( ).getStore( classInput.getClassFile( ).getPath( ) ) ) );
						}
						return;
					}
				}
			}
			try
			{
				doOpenBuffer( input, false );
			}
			catch ( JavaModelException e )
			{
				IClassFileEditorInput classFileEditorInput = (IClassFileEditorInput) input;
				IClassFile file = classFileEditorInput.getClassFile( );

				if ( file.getSourceRange( ) == null && file.getBytes( ) != null )
				{
					if ( ClassUtil.isClassFile( file.getBytes( ) ) )
					{
						File classFile = new File(
								JavaDecompilerPlugin.getDefault( ).getPreferenceStore( ).getString(
										JavaDecompilerPlugin.TEMP_DIR ),
								file.getElementName( ) );
						try
						{
							FileOutputStream fos = new FileOutputStream( classFile );
							fos.write( file.getBytes( ) );
							fos.close( );

							doSetInput( new DecompilerClassEditorInput(
									EFS.getLocalFileSystem( ).getStore( new Path( classFile.getAbsolutePath( ) ) ) ) );
							classFile.delete( );
							return;
						}
						catch ( IOException e1 )
						{
							JavaDecompilerPlugin.logError( e, "" ); //$NON-NLS-1$
						}
						finally
						{
							if ( classFile != null && classFile.exists( ) )
								classFile.delete( );
						}
					}
				}
			}

			super.doSetInput( input );
		}

		handleMarkLink( );
	}

	public void createPartControl( Composite parent )
	{
		super.createPartControl( parent );
		showSource( );
	}

	private void handleMarkLink( )
	{
		final int index = this.getDocumentProvider( ).getDocument( getEditorInput( ) ).get( ).indexOf( "://" ); //$NON-NLS-1$
		if ( index != -1 )
		{
			Display.getDefault( ).asyncExec( new Runnable( ) {

				@Override
				public void run( )
				{
					updateMatchAnnonation( );
					handleMarkLink( index );
				}
			} );
		}
	}

	protected JavaDecompilerBufferManager getBufferManager( )
	{
		JavaDecompilerBufferManager manager;
		BufferManager defManager = BufferManager.getDefaultBufferManager( );
		if ( defManager instanceof JavaDecompilerBufferManager )
			manager = (JavaDecompilerBufferManager) defManager;
		else
			manager = new JavaDecompilerBufferManager( defManager );
		return manager;
	}

	public void notifyPropertiesChange( )
	{
		ReflectionUtils.invokeMethod( this.getViewer( ),
				"fireSelectionChanged", //$NON-NLS-1$
				new Class[]{
						SelectionChangedEvent.class
				},
				new Object[]{
						new SelectionChangedEvent( (ISelectionProvider) this.getViewer( ), new StructuredSelection( ) )
				} );
	}

	private void handleMarkLink( final int index )
	{
		ISourceViewer sourceViewer = getSourceViewer( );
		if ( sourceViewer == null )
			return;

		final StyledText text = sourceViewer.getTextWidget( );
		if ( text.isDisposed( ) )
		{
			return;
		}

		IHyperlinkDetector[] descriptors = getSourceViewerConfiguration( ).getHyperlinkDetectors( sourceViewer );
		for ( int i = 0; i < descriptors.length; i++ )
		{
			final IHyperlink[] links = descriptors[i].detectHyperlinks( sourceViewer, new Region( index, 0 ), true );

			if ( links != null && links.length > 0 && links[0] instanceof URLHyperlink )
			{
				final IHyperlinkPresenter fHyperlinkPresenter = (IHyperlinkPresenter) ReflectionUtils
						.getFieldValue( sourceViewer, "fHyperlinkPresenter" ); //$NON-NLS-1$

				final HyperlinkManager fHyperlinkManager = (HyperlinkManager) ReflectionUtils
						.getFieldValue( sourceViewer, "fHyperlinkManager" ); //$NON-NLS-1$

				if ( fHyperlinkPresenter == null || fHyperlinkManager == null )
					continue;

				fHyperlinkPresenter.showHyperlinks( links );

				updateLinkRanges( links );

				final boolean[] flags = new boolean[1];

				if ( !text.isDisposed( ) && paintListener != null )
				{
					text.removePaintListener( paintListener );
				}

				paintListener = new PaintListener( ) {

					private Boolean isActive( final HyperlinkManager fHyperlinkManager )
					{
						return (Boolean) ReflectionUtils.getFieldValue( fHyperlinkManager, "fActive" );//$NON-NLS-1$
					}

					private void updateHyperlinks( final IHyperlink[] links,
							final IHyperlinkPresenter fHyperlinkPresenter )
					{
						if ( ReflectionUtils.getFieldValue( fHyperlinkPresenter, "fTextViewer" ) != null ) //$NON-NLS-1$
						{
							updateLinkRanges( links );
						}
					}

					private void addPaintListener( final StyledText text )
					{
						Display.getDefault( ).asyncExec( new Runnable( ) {

							@Override
							public void run( )
							{
								if ( !text.isDisposed( ) && paintListener != null )
								{
									text.removePaintListener( paintListener );
									text.addPaintListener( paintListener );
								}
							}
						} );
					}

					@Override
					public void paintControl( PaintEvent e )
					{
						if ( flags[0] )
						{
							return;
						}

						flags[0] = true;

						Display.getDefault( ).asyncExec( new Runnable( ) {

							@Override
							public void run( )
							{
								boolean fActive = isActive( fHyperlinkManager );
								if ( !fActive && !text.isDisposed( ) )
								{
									text.removePaintListener( paintListener );
									updateHyperlinks( links, fHyperlinkPresenter );
									updateMatchAnnonation( );
									addPaintListener( text );
								}
								flags[0] = false;
							}
						} );
					}
				};

				text.addPaintListener( paintListener );

				if ( mouseAdapter != null )
				{
					text.removeMouseListener( mouseAdapter );
				}
				mouseAdapter = new MouseAdapter( ) {

					@Override
					public void mouseUp( MouseEvent e )
					{

						int offset = text.getCaretOffset( );
						if ( offset == -1 )
							return;
						for ( int j = 0; j < links.length; j++ )
						{
							int linkOffset = links[j].getHyperlinkRegion( ).getOffset( );
							int linkLength = links[j].getHyperlinkRegion( ).getLength( );
							if ( offset >= linkOffset && offset < linkOffset + linkLength )
							{
								if ( links[j] instanceof URLHyperlink )
								{
									String url = ( (URLHyperlink) links[j] ).getURLString( );
									UIUtil.openBrowser( url );
								}
								return;
							}
						}

					}
				};
				text.addMouseListener( mouseAdapter );
			}
		}
	}

	private void updateLinkRanges( final IHyperlink[] links )
	{
		String content = getDocumentProvider( ).getDocument( getEditorInput( ) ).get( );

		String mark = MarkUtil.getMark( content );
		String ad = mark.replaceAll( "/(\\*)+", "" ) //$NON-NLS-1$ //$NON-NLS-2$
				.replaceAll( "(\\*)+/", "" ) //$NON-NLS-1$ //$NON-NLS-2$
				.trim( );
		int length = ad.length( );
		int offset = mark.indexOf( ad );

		StyledText textWidget = getViewer( ).getTextWidget( );
		StyleRange textRange = UIUtil.getAdTextStyleRange( textWidget, offset, length );
		if ( textRange != null )
		{
			textWidget.setStyleRange( textRange );
		}

		for ( int j = 0; j < links.length; j++ )
		{
			IHyperlink link = links[j];
			StyleRange linkRange = UIUtil.getAdLinkStyleRange( textWidget,
					link.getHyperlinkRegion( ).getOffset( ),
					link.getHyperlinkRegion( ).getLength( ) );
			if ( linkRange != null )
			{
				getViewer( ).getTextWidget( ).setStyleRange( linkRange );
			}
		}
	}

	private void updateMatchAnnonation( )
	{
		WindowAnnotationManager mgr = (WindowAnnotationManager) ReflectionUtils.invokeMethod( AnnotationManagers.class,
				"getWindowAnnotationManager", //$NON-NLS-1$
				new Class[]{
						IWorkbenchWindow.class
				},
				new Object[]{
						PlatformUI.getWorkbench( ).getActiveWorkbenchWindow( )
				} );
		if ( mgr == null )
			return;
		Map<IEditorPart, EditorAnnotationManager> fAnnotationManagers = (Map<IEditorPart, EditorAnnotationManager>) ReflectionUtils
				.getFieldValue( mgr, "fAnnotationManagers" ); //$NON-NLS-1$
		if ( fAnnotationManagers == null )
			return;
		EditorAnnotationManager amgr = fAnnotationManagers.get( JavaDecompilerClassFileEditor.this );
		if ( amgr == null )
			return;
		ReflectionUtils.invokeMethod( amgr,
				"removeAllAnnotations", //$NON-NLS-1$
				new Class[0],
				new Object[0] );
	}

	@Override
	public String[] collectContextMenuPreferencePages( )
	{
		String[] inheritedPages = super.collectContextMenuPreferencePages( );
		int length = 1;
		String[] result = new String[inheritedPages.length + length];
		if ( JavaDecompilerPlugin.getDefault( ).getSourceMode( ) == JavaDecompilerPlugin.SOURCE_MODE )
		{
			result[0] = "org.sf.feeling.decompiler.Main"; //$NON-NLS-1$
		}
		else if ( JavaDecompilerPlugin.getDefault( ).getSourceMode( ) == JavaDecompilerPlugin.DISASSEMBLER_MODE )
		{
			result[0] = "org.sf.feeling.decompiler.Disassembler"; //$NON-NLS-1$
		}
		else if ( JavaDecompilerPlugin.getDefault( ).getSourceMode( ) == JavaDecompilerPlugin.BYTE_CODE_MODE )
		{
			result[0] = "org.eclipse.ui.preferencePages.ColorsAndFonts"; //$NON-NLS-1$
		}
		System.arraycopy( inheritedPages, 0, result, length, inheritedPages.length );
		return result;
	}

	@Override
	protected void createActions( )
	{
		super.createActions( );
		final ActionGroup group = new DecompileActionGroup( this, ITextEditorActionConstants.GROUP_SAVE, true );
		CompositeActionGroup fContextMenuGroup = (CompositeActionGroup) ReflectionUtils.getFieldValue( this,
				"fContextMenuGroup" ); //$NON-NLS-1$
		fContextMenuGroup.addGroup( group );
	}

	public void showSource( )
	{
		if ( getEditorInput( ) instanceof IClassFileEditorInput )
		{
			showSource( (IClassFileEditorInput) getEditorInput( ) );
		}
	}

	protected void showSource( IClassFileEditorInput classFileEditorInput )
	{
		if ( currentSourceMode == JavaDecompilerPlugin.getDefault( ).getSourceMode( ) )
			return;

		try
		{
			StackLayout fStackLayout = (StackLayout) ReflectionUtils.getFieldValue( this, "fStackLayout" ); //$NON-NLS-1$
			Composite fParent = (Composite) ReflectionUtils.getFieldValue( this, "fParent" ); //$NON-NLS-1$
			Composite fViewerComposite = (Composite) ReflectionUtils.getFieldValue( this, "fViewerComposite" ); //$NON-NLS-1$
			if ( JavaDecompilerPlugin.getDefault( ).getSourceMode( ) == JavaDecompilerPlugin.SOURCE_MODE )
			{
				if ( fStackLayout != null && fViewerComposite != null && fParent != null )
				{
					fStackLayout.topControl = fViewerComposite;
					fParent.layout( );
				}
			}
			else if ( JavaDecompilerPlugin.getDefault( ).getSourceMode( ) == JavaDecompilerPlugin.DISASSEMBLER_MODE )
			{
				if ( fStackLayout != null && fParent != null )
				{
					if ( fDisassemblerSourceViewer == null )
					{
						fDisassemblerSourceViewer = new DisassemblerSourceViewer( this );
						fDisassemblerSourceViewer.createControl( fParent );
					}
					fDisassemblerSourceViewer.setSelectionElement( selectedElement );
					fStackLayout.topControl = fDisassemblerSourceViewer.getControl( );
					fParent.layout( );
				}
			}
			else if ( JavaDecompilerPlugin.getDefault( ).getSourceMode( ) == JavaDecompilerPlugin.BYTE_CODE_MODE )
			{
				if ( fStackLayout != null && fParent != null )
				{
					if ( fByteCodeSourceViewer == null )
					{
						fByteCodeSourceViewer = new ByteCodeSourceViewer( this );
						fByteCodeSourceViewer.createControl( fParent );
					}
					fStackLayout.topControl = fByteCodeSourceViewer.getControl( );
					fParent.layout( );
				}
			}
		}
		catch ( Exception e )
		{
			Logger.debug( e );
		}
		currentSourceMode = JavaDecompilerPlugin.getDefault( ).getSourceMode( );
	}
}