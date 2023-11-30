/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionDelegate;
import org.sf.feeling.decompiler.editor.JavaDecompilerClassFileEditor;

public class ExportSourceActionDelegate extends ActionDelegate implements IEditorActionDelegate {

	JavaDecompilerClassFileEditor editor;

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof JavaDecompilerClassFileEditor) {
			editor = (JavaDecompilerClassFileEditor) targetEditor;
		}
	}

	@Override
	public void run(IAction action) {
		new ExportEditorSourceAction().run();
	}

}
