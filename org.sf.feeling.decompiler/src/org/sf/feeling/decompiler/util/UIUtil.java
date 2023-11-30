/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.util;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.navigator.IExtensionStateConstants.Values;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonNavigatorManager;
import org.eclipse.ui.navigator.IExtensionStateModel;
import org.eclipse.ui.navigator.INavigatorContentService;
import org.eclipse.ui.themes.ColorUtil;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;
import org.sf.feeling.decompiler.editor.JavaDecompilerClassFileEditor;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class UIUtil {

	public static void openBrowser(String url) {
		try {
			final URL urlToOpen = new URL(url);
			IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
			if (browser != null) {
				browser.openURL(urlToOpen);
			}
		} catch (Exception e) {
			Logger.debug(e);
		}
	}

	public static JavaDecompilerClassFileEditor getActiveEditor() {
		final JavaDecompilerClassFileEditor[] editors = new JavaDecompilerClassFileEditor[1];
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				IWorkbenchPart editor = getActiveEditor(true);
				if (editor instanceof JavaDecompilerClassFileEditor) {
					editors[0] = (JavaDecompilerClassFileEditor) editor;
				} else {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

					if (window != null) {
						IWorkbenchPage pg = window.getActivePage();

						if (pg != null) {
							IEditorPart editorPart = pg.getActiveEditor();
							if (editorPart instanceof JavaDecompilerClassFileEditor) {
								editors[0] = (JavaDecompilerClassFileEditor) editorPart;
							}
						}
					}
				}
			}
		});
		return editors[0];
	}

	public static JavaDecompilerClassFileEditor getActiveDecompilerEditor() {
		final JavaDecompilerClassFileEditor[] editors = new JavaDecompilerClassFileEditor[1];
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				IWorkbenchPart editor = getActiveEditor(true);
				if (editor instanceof JavaDecompilerClassFileEditor) {
					editors[0] = (JavaDecompilerClassFileEditor) editor;
				}
			}
		});
		return editors[0];
	}

	public static List getActiveSelection() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final List classes = getSelectedElements(window.getSelectionService(), IClassFile.class);
		if (classes != null && !classes.isEmpty()) {
			return classes;
		}
		return null;
	}

	public static String getActivePerspectiveId() {
		final String[] ids = new String[1];
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				IWorkbench wb = PlatformUI.getWorkbench();
				if (wb == null) {
					ids[0] = null;
					return;
				}

				IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
				if (win == null) {
					ids[0] = null;
					return;
				}
				IWorkbenchPage page = win.getActivePage();
				if (page == null) {
					ids[0] = null;
					return;
				}

				IPerspectiveDescriptor perspective = page.getPerspective();
				if (perspective == null) {
					ids[0] = null;
					return;
				}
				ids[0] = perspective.getId();

			}
		});
		return ids[0];
	}

	public static boolean isDebug() {
		IPreferenceStore prefs = JavaDecompilerPlugin.getDefault().getPreferenceStore();
		boolean showLineNumber = prefs.getBoolean(JavaDecompilerPlugin.PREF_DISPLAY_LINE_NUMBERS);
		boolean align = prefs.getBoolean(JavaDecompilerPlugin.ALIGN);
		boolean debug = (showLineNumber && align) || UIUtil.isDebugPerspective()
				|| JavaDecompilerPlugin.getDefault().isDebugMode();
		return debug;
	}

	public static boolean isDebugPerspective() {
		return "org.eclipse.debug.ui.DebugPerspective" //$NON-NLS-1$
				.equals(getActivePerspectiveId());
	}

	public static List getSelectedElements(ISelectionService selService, Class eleClass) {

		Iterator selections = getSelections(selService);
		List elements = new ArrayList();

		while ((selections != null) && selections.hasNext()) {
			Object select = selections.next();

			if (eleClass.isInstance(select)) {
				elements.add(select);
			}
		}

		return elements;
	}

	public static Iterator getSelections(ISelectionService selService) {
		ISelection selection = selService.getSelection();

		if (selection != null) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				return structuredSelection.iterator();
			}
		}

		return null;
	}

	public static IWorkbenchPart getActiveEditor(boolean activePageOnly) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		if (window != null) {
			if (activePageOnly) {
				IWorkbenchPage pg = window.getActivePage();

				if (pg != null) {
					IWorkbenchPart activePart = pg.getActivePart();
					if (activePart instanceof ContentOutline) {
						ContentOutline outline = (ContentOutline) activePart;
						IWorkbenchPart part = (IWorkbenchPart) ReflectionUtils.invokeMethod(outline,
								"getCurrentContributingPart"); //$NON-NLS-1$
						if (part == null) {
							return (IWorkbenchPart) ReflectionUtils.getFieldValue(outline, "hiddenPart"); //$NON-NLS-1$
						}
					} else
						return activePart;
				}
			} else {
				IWorkbenchPage[] pgs = window.getPages();

				for (int i = 0; i < pgs.length; i++) {
					IWorkbenchPage pg = pgs[i];

					if (pg != null) {
						IWorkbenchPart part = pg.getActivePart();

						if (part != null) {
							return part;
						}
					}
				}
			}
		}

		return null;
	}

	public static boolean isOpenClassEditor() {
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();

		if (windows != null) {
			for (int i = 0; i < windows.length; i++) {
				IWorkbenchWindow window = windows[i];
				IWorkbenchPage[] pgs = window.getPages();
				for (int j = 0; j < pgs.length; j++) {
					IWorkbenchPage pg = pgs[j];
					if (pg != null) {
						IEditorPart[] parts = pg.getEditors();
						if (parts != null) {
							for (int k = 0; k < parts.length; k++) {
								if (parts[i] instanceof JavaDecompilerClassFileEditor)
									return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	public static List getExportSelections() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final List selectedJars = getSelectedElements(window.getSelectionService(), IPackageFragmentRoot.class);
		if (selectedJars.size() == 1) {
			return selectedJars;
		}

		if (selectedJars.size() > 1)
			return null;

		final List selectedPackages = getSelectedElements(window.getSelectionService(), IPackageFragment.class);
		final List selectedClasses = getSelectedElements(window.getSelectionService(), IClassFile.class);
		selectedClasses.addAll(selectedPackages);
		if (!selectedClasses.isEmpty()) {
			return selectedClasses;
		}

		return null;
	}

	public static boolean isPackageFlat() {
		boolean isFlat = false;
		try {
			IWorkbenchPart view = getActiveEditor(true);
			if (view != null) {
				if (view.getSite().getId().equals("org.eclipse.ui.navigator.ProjectExplorer")) //$NON-NLS-1$
				{
					CommonNavigator explorer = (CommonNavigator) view;
					Field field = CommonNavigator.class.getDeclaredField("commonManager"); //$NON-NLS-1$
					if (field != null) {
						field.setAccessible(true);
						CommonNavigatorManager manager = (CommonNavigatorManager) field.get(explorer);

						field = CommonNavigatorManager.class.getDeclaredField("contentService"); //$NON-NLS-1$
						if (field != null) {
							field.setAccessible(true);
							INavigatorContentService service = (INavigatorContentService) field.get(manager);
							IExtensionStateModel model = service.findStateModel("org.eclipse.jdt.java.ui.javaContent"); //$NON-NLS-1$
							isFlat = model.getBooleanProperty(Values.IS_LAYOUT_FLAT);
						}
					}
				} else if (view.getSite().getId().equals("org.eclipse.jdt.ui.PackageExplorer")) //$NON-NLS-1$
				{
					PackageExplorerPart explorer = (PackageExplorerPart) view;
					isFlat = explorer.isFlatLayout();
				}
			}
		} catch (Exception e) {
		}
		return isFlat;
	}

	public static boolean isWin32() {
		return Platform.OS_WIN32.equalsIgnoreCase(Platform.getOS());
	}

	public static boolean isMacOS() {
		return Platform.OS_MACOSX.equalsIgnoreCase(Platform.getOS());
	}

	public static String getPathLocation(IPath path) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IResource resource = root.findMember(path);
		if (resource != null) {
			return resource.getLocation().toOSString();
		}
		return null;
	}

	public static boolean requestFromJavadocHover() {
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		for (int i = 0; i < stacks.length && i < 12; i++) {
			if (stacks[i].getClassName().indexOf("BinaryType") != -1 //$NON-NLS-1$
					&& stacks[i].getMethodName().equals("getJavadocRange")) //$NON-NLS-1$
				return false;

			if (stacks[i].getClassName().indexOf("JavadocHover") != -1 //$NON-NLS-1$
					&& stacks[i].getMethodName().equals("getHoverInfo")) //$NON-NLS-1$
				return true;
		}
		return false;
	}

	public static boolean requestFromDisassemblerSelection() {
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		for (int i = 0; i < stacks.length && i < 10; i++) {
			if (stacks[i].getMethodName().equals("doHandleCursorPositionChanged")) //$NON-NLS-1$
				return true;
		}
		return false;
	}

	public static boolean requestCreateBuffer() {
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		for (int i = 0; i < stacks.length && i < 12; i++) {
			if (stacks[i].getClassName().indexOf("BinaryType") != -1 //$NON-NLS-1$
					&& stacks[i].getMethodName().equals("getJavadocRange")) //$NON-NLS-1$
				return false;

			if (stacks[i].getClassName().indexOf("JavadocHover") != -1 //$NON-NLS-1$
					&& stacks[i].getMethodName().equals("getHoverInfo2")) //$NON-NLS-1$
				return true;

			if (stacks[i].getClassName().indexOf("JavaSourceHover") != -1 //$NON-NLS-1$
					&& stacks[i].getMethodName().equals("getHoverInfo")) //$NON-NLS-1$
				return true;

			if (stacks[i].getClassName().indexOf("FindOccurrencesInFileAction") != -1 //$NON-NLS-1$
					&& stacks[i].getMethodName().equals("getMember")) //$NON-NLS-1$
				return true;

			// if ( stacks[i].getClassName( ).indexOf( "HyperlinkManager" ) !=
			// -1 //$NON-NLS-1$
			// && stacks[i].getMethodName( ).equals( "findHyperlinks" ) )
			// //$NON-NLS-1$
			// return true;

			// if ( stacks[i].getClassName( )
			// .indexOf( "DefaultJavaFoldingStructureProvider" ) != -1
			// //$NON-NLS-1$
			// && stacks[i].getMethodName( )
			// .equals( "computeProjectionRanges" ) ) //$NON-NLS-1$
			// return true;
		}
		return false;
	}

	public static boolean requestFromShowMatch() {
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		for (int i = 0; i < stacks.length; i++) {
			if (stacks[i].getClassName().indexOf("JavaSearchResultPage") != -1 //$NON-NLS-1$
					&& stacks[i].getMethodName().equals("showMatch")) //$NON-NLS-1$
				return true;
		}
		return false;
	}

	public static StyleRange getAdTextStyleRange(StyledText textWidget, int offset, int length) {
		JavaTextTools textTools = JavaPlugin.getDefault().getJavaTextTools();
		IPreferenceStore preferences = (IPreferenceStore) ReflectionUtils.getFieldValue(textTools, "fPreferenceStore"); //$NON-NLS-1$
		String textColor = preferences.getString(IJavaColorConstants.JAVADOC_LINK);

		StyleRange styleRange = null;
		if (isDark(textWidget)) {
			String darkStyleValue = JavaDecompilerPlugin.getDefault().getPreferenceStore().getString("darkStyle"); //$NON-NLS-1$
			if (darkStyleValue != null && darkStyleValue.trim().length() > 0) {
				styleRange = handleStyleValue(textWidget, offset, length, darkStyleValue, "text", textColor, false); //$NON-NLS-1$
			}
		} else {
			String brightStyleValue = JavaDecompilerPlugin.getDefault().getPreferenceStore().getString("brightStyle"); //$NON-NLS-1$
			if (brightStyleValue != null && brightStyleValue.trim().length() > 0) {
				styleRange = handleStyleValue(textWidget, offset, length, brightStyleValue, "text", textColor, false); //$NON-NLS-1$
			}

		}

		if (styleRange == null) {
			styleRange = getDefaultCommonRange(textWidget, offset, length);
		}
		return styleRange;
	}

	private static StyleRange handleStyleValue(StyledText textWidget, int offset, int length, String brightStyleValue,
			String property, String defaultColor, boolean isLink) {
		JsonValue styleValue = null;
		try {
			styleValue = Json.parse(brightStyleValue);
		} catch (Exception e) {
			Logger.debug(e);
		}

		if (styleValue != null && styleValue.isObject()) {
			JsonObject style = styleValue.asObject();
			if (style.get(property) != null && style.get(property).isObject()) {
				return getStyleRange(textWidget, offset, length, style.get(property).asObject(), defaultColor, isLink);
			}
		}

		return null;
	}

	private static StyleRange getStyleRange(StyledText textWidget, int offset, int length, JsonObject style,
			String defaultColor, boolean isLink) {
		int contentLength = textWidget.getCharCount();
		if (contentLength - offset < 0)
			return null;
		StyleRange range = new StyleRange(offset, offset + length < contentLength ? length : contentLength - offset,
				defaultColor == null ? null
						: JFaceResources.getResources().createColor(ColorUtil.getColorValue(defaultColor)),
				null);
		JsonValue fontStyleValue = style.get("fontStyle"); //$NON-NLS-1$
		if (fontStyleValue != null && fontStyleValue.isNumber()) {
			range.fontStyle = fontStyleValue.asInt();
		}

		JsonValue fontFamilyValue = style.get("fontFamily"); //$NON-NLS-1$
		if (fontFamilyValue != null && fontFamilyValue.isString()) {
			if (range.font == null) {
				FontData fontData = textWidget.getFont().getFontData()[0];
				fontData.setName(fontFamilyValue.asString());
				Font font = new Font(textWidget.getDisplay(), fontData);
				range.font = font;
			} else {
				FontData fontData = range.font.getFontData()[0];
				fontData.setName(fontFamilyValue.asString());
			}
		}

		JsonValue fontSizeValue = style.get("fontSize"); //$NON-NLS-1$
		if (fontSizeValue != null && fontSizeValue.isNumber()) {
			FontData textFontData = textWidget.getFont().getFontData()[0];
			if (fontSizeValue.asInt() > textFontData.getHeight()) {
				if (range.font == null) {
					FontData fontData = textFontData;
					fontData.setHeight(fontSizeValue.asInt());
					Font font = new Font(textWidget.getDisplay(), fontData);
					range.font = font;
				} else {
					FontData fontData = range.font.getFontData()[0];
					fontData.setHeight(fontSizeValue.asInt());
				}
			}
		}

		if (isLink) {
			range.underline = true;
			range.underlineStyle = SWT.UNDERLINE_LINK;
		}

		JsonValue underlineValue = style.get("underline"); //$NON-NLS-1$
		if (underlineValue != null && underlineValue.isBoolean()) {
			range.underline = underlineValue.asBoolean();
			if (range.underline) {
				range.underlineStyle = SWT.UNDERLINE_LINK;
			}
		}

		JsonValue underlineStyleValue = style.get("underlineStyle"); //$NON-NLS-1$
		if (underlineStyleValue != null && underlineStyleValue.isNumber()) {
			range.underlineStyle = underlineStyleValue.asInt();
		}

		JsonValue hideUnderlineValue = style.get("hideUnderline"); //$NON-NLS-1$
		if (hideUnderlineValue != null && hideUnderlineValue.isBoolean()) {
			if (hideUnderlineValue.asBoolean()) {
				range.underlineColor = textWidget.getBackground();
			}
		}

		JsonValue backgroundValue = style.get("background"); //$NON-NLS-1$
		if (backgroundValue != null && backgroundValue.isString()) {
			String backgroundString = backgroundValue.asString();
			RGB rgb = ColorUtil.getColorValue(backgroundString);
			if (rgb != null) {
				range.background = new Color(textWidget.getDisplay(), rgb);
			}
		}

		JsonValue foregroundValue = style.get("foreground"); //$NON-NLS-1$
		if (foregroundValue != null && foregroundValue.isString()) {
			String foregroundString = foregroundValue.asString();
			RGB rgb = ColorUtil.getColorValue(foregroundString);
			if (rgb != null) {
				range.foreground = new Color(textWidget.getDisplay(), rgb);
				if (range.underlineColor == null) {
					range.underlineColor = range.foreground;
				}
			}
		}

		return range;
	}

	public static boolean isDark(Control textWidget) {
		if (textWidget != null && !textWidget.isDisposed()) {
			Color color = textWidget.getBackground();
			int red = color.getRed();
			int blue = color.getBlue();
			int green = color.getGreen();
			return red < 127 || blue < 127 || green < 127;
		}
		return false;
	}

	public static StyleRange getAdLinkStyleRange(StyledText textWidget, int offset, int length) {
		JavaTextTools textTools = JavaPlugin.getDefault().getJavaTextTools();
		IPreferenceStore preferences = (IPreferenceStore) ReflectionUtils.getFieldValue(textTools, "fPreferenceStore"); //$NON-NLS-1$
		String linkColor = preferences.getString(IJavaColorConstants.JAVADOC_DEFAULT);

		StyleRange styleRange = null;
		if (isDark(textWidget)) {
			String darkStyleValue = JavaDecompilerPlugin.getDefault().getPreferenceStore().getString("darkStyle"); //$NON-NLS-1$
			if (darkStyleValue != null && darkStyleValue.trim().length() > 0) {
				styleRange = handleStyleValue(textWidget, offset, length, darkStyleValue, "link", linkColor, true); //$NON-NLS-1$
			}
		} else {
			String brightStyleValue = JavaDecompilerPlugin.getDefault().getPreferenceStore().getString("brightStyle"); //$NON-NLS-1$
			if (brightStyleValue != null && brightStyleValue.trim().length() > 0) {
				styleRange = handleStyleValue(textWidget, offset, length, brightStyleValue, "link", linkColor, true); //$NON-NLS-1$
			}
		}

		if (styleRange == null) {
			styleRange = getDefaultLinkRange(textWidget, offset, length);
		}
		return styleRange;
	}

	private static StyleRange getDefaultLinkRange(StyledText textWidget, int offset, int length) {
		JavaTextTools textTools = JavaPlugin.getDefault().getJavaTextTools();
		IPreferenceStore preferences = (IPreferenceStore) ReflectionUtils.getFieldValue(textTools, "fPreferenceStore"); //$NON-NLS-1$
		String linkColor = preferences.getString(IJavaColorConstants.JAVADOC_LINK);

		StyleRange range = new StyleRange(offset, length,
				JFaceResources.getResources().createColor(ColorUtil.getColorValue(linkColor)), null);
		range.font = textWidget.getFont();
		range.underline = true;
		range.underlineStyle = SWT.UNDERLINE_LINK;
		return range;
	}

	private static StyleRange getDefaultCommonRange(StyledText textWidget, int offset, int length) {
		JavaTextTools textTools = JavaPlugin.getDefault().getJavaTextTools();
		IPreferenceStore preferences = (IPreferenceStore) ReflectionUtils.getFieldValue(textTools, "fPreferenceStore"); //$NON-NLS-1$
		String commentColor = preferences.getString(IJavaColorConstants.JAVADOC_DEFAULT);

		StyleRange range = new StyleRange(offset, length,
				new Color(textWidget.getDisplay(), ColorUtil.getColorValue(commentColor)), null);
		range.font = textWidget.getFont();
		return range;
	}

	public static boolean requestFromLinkToSelection() {
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		for (int i = 0; i < stacks.length && i < 10; i++) {
			if (stacks[i].getMethodName().equals("linkToEditor") //$NON-NLS-1$
					|| stacks[i].getMethodName().equals("showSource")) //$NON-NLS-1$
				return true;
		}
		return false;
	}

	public static boolean requestFromCopyOperation() {
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		for (int i = 0; i < stacks.length && i < 10; i++) {
			if (stacks[i].getMethodName().equals("doCutCopyWithImportsOperation")) //$NON-NLS-1$
				return true;
		}
		return false;
	}
}
