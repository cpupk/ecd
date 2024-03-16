/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.editor;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.SourceMapper;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;
import org.sf.feeling.decompiler.util.DecompilerOutputUtil;
import org.sf.feeling.decompiler.util.EclipseCompatibilityHelper;
import org.sf.feeling.decompiler.util.SourceMapperUtil;

public abstract class DecompilerSourceMapper extends SourceMapper {

	protected static Map<IPackageFragmentRoot, SourceMapper> originalSourceMapper = new ConcurrentHashMap<>();

	protected boolean isAttachedSource;

	public DecompilerSourceMapper(IPath sourcePath, String rootPath, Map options) {
		super(sourcePath, rootPath, options);
	}

	public char[] findSource(IType type) throws JavaModelException {
		if (!type.isBinary()) {
			return null;
		}
		BinaryType parent = (BinaryType) type.getDeclaringType();
		BinaryType declType = (BinaryType) type;
		while (parent != null) {
			declType = parent;
			parent = (BinaryType) declType.getDeclaringType();
		}
		IBinaryType info = EclipseCompatibilityHelper.binaryTypeGetElementInfo(declType);
		if (info == null) {
			return null;
		}
		return findSource(type, info);
	}

	/**
	 * With the time this API has changed and the original method which accepted
	 * {@link IType} as first parameter has been superseded and replaced by the
	 * which does accept {@link NamedParameter} as first parameter.
	 * <p>
	 * But we do need to support both APIs here so we will try to invoke the correct
	 * method using reflection instead of a hard coded reference.
	 */
	public void mapSourceSwitch(IType type, char[] contents, boolean force) {
		if (force) {
			sourceRanges.remove(type);
		}

		try {
			SourceMapperUtil.mapSource(this, type, contents, null);
		} catch (Exception e) {
			// Method was found but invocation failed, this shouldn't happen.
		}
	}

	/**
	 * @return Does the source returned by {@link #findSource(IType, IBinaryType)}
	 *         originate from a source attachment?
	 */
	public boolean isAttachedSource() {
		return isAttachedSource;
	}

	protected String formatSource(String source) {
		String result = null;

		IPreferenceStore prefs = JavaDecompilerPlugin.getDefault().getPreferenceStore();
		boolean useFormatter = prefs.getBoolean(JavaDecompilerPlugin.USE_ECLIPSE_FORMATTER);

		if (source != null && useFormatter) {
			CompilerOptions option = new CompilerOptions();
			Map<String, String> options = option.getMap();
			options.put(CompilerOptions.OPTION_Compliance, DecompilerOutputUtil.getMaxDecompileLevel()); // $NON-NLS-1$
			options.put(CompilerOptions.OPTION_Source, DecompilerOutputUtil.getMaxDecompileLevel()); // $NON-NLS-1$
			CodeFormatter formatter = ToolFactory.createCodeFormatter(options);
			TextEdit textEdit = formatter.format(CodeFormatter.K_COMPILATION_UNIT, source, 0, source.length(), 0, null);
			if (textEdit != null) {
				IDocument document = new Document(source);
				try {
					textEdit.apply(document);
				} catch (BadLocationException e) {
					JavaDecompilerPlugin.log(IStatus.WARNING, e, "Unable to apply text formatting."); //$NON-NLS-1$
				}
				result = document.get();
			}

			if (result == null) {
				JavaDecompilerPlugin.log(IStatus.WARNING, null, "Could not format code, it will remain unformatted."); //$NON-NLS-1$
				result = source;
			}
		} else {
			result = source;
		}

		return result.trim();
	}

	protected String getArchivePath(IPackageFragmentRoot root) {
		String archivePath = null;
		IResource resource;

		try {
			if ((resource = root.getUnderlyingResource()) != null) {
				// jar in workspace
				archivePath = resource.getLocation().toOSString();
			} else {
				// external jar
				archivePath = root.getPath().toOSString();
			}
		} catch (JavaModelException e) {
			throw new RuntimeException("Unexpected Java model exception: " //$NON-NLS-1$
					+ e.toString());
		}
		return archivePath;
	}

	/**
	 * Finds the deepest <code>IJavaElement</code> in the hierarchy of
	 * <code>elt</elt>'s children (including <code>elt</code> itself) which has a
	 * source range that encloses <code>position</code> according to
	 * <code>mapper</code>.
	 * 
	 * Code mostly taken from 'org.eclipse.jdt.internal.core.ClassFile'
	 */
	protected IJavaElement findElement(IJavaElement elt, int position) {
		ISourceRange range = getSourceRange(elt);
		if (range == null || position < range.getOffset() || range.getOffset() + range.getLength() - 1 < position) {
			return null;
		}
		if (elt instanceof IParent) {
			try {
				IJavaElement[] children = ((IParent) elt).getChildren();
				for (int i = 0; i < children.length; i++) {
					IJavaElement match = findElement(children[i], position);
					if (match != null) {
						return match;
					}
				}
			} catch (JavaModelException npe) {
			}
		}
		return elt;
	}

	public abstract String decompile(String decompilerType, File file);
}
