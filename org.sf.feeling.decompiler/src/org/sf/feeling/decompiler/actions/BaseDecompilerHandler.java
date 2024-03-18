/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.actions;

import java.util.List;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.sf.feeling.decompiler.JavaDecompilerConstants;
import org.sf.feeling.decompiler.actions.OpenClassWithContributionFactory.OpenClassesAction;
import org.sf.feeling.decompiler.editor.JavaDecompilerClassFileEditor;
import org.sf.feeling.decompiler.util.UIUtil;

public class BaseDecompilerHandler extends DecompileHandler {

	protected Object handleDecompile(String decompilerType) {
		final List classes = UIUtil.getActiveSelection();
		if (classes != null && !classes.isEmpty()) {
			IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
			IEditorDescriptor editorDescriptor = registry.findEditor(JavaDecompilerConstants.EDITOR_ID);
			if (editorDescriptor == null) {
				JavaDecompilerClassFileEditor editor = UIUtil.getActiveEditor();
				if (editor != null) {
					editor.doSetInput(decompilerType, true);
				}
			} else {
				new OpenClassesAction(editorDescriptor, classes, decompilerType).run();
			}
		} else {
			JavaDecompilerClassFileEditor editor = UIUtil.getActiveEditor();
			if (editor != null) {
				editor.doSetInput(decompilerType, true);
			}
		}
		return null;
	}
}
