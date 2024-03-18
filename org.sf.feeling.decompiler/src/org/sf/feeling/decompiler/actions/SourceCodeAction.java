/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.IUpdate;
import org.sf.feeling.decompiler.JavaDecompilerConstants;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;
import org.sf.feeling.decompiler.editor.JavaDecompilerClassFileEditor;
import org.sf.feeling.decompiler.i18n.Messages;
import org.sf.feeling.decompiler.util.UIUtil;

public class SourceCodeAction extends Action implements IUpdate {

	public static final String ID = "SourceCode"; //$NON-NLS-1$

	public SourceCodeAction() {
		super(Messages.getString("JavaDecompilerActionBarContributor.Action.SourceCode"), AS_CHECK_BOX); //$NON-NLS-1$
	}

	@Override
	public void run() {
		JavaDecompilerPlugin.getDefault().setSourceMode(JavaDecompilerConstants.SOURCE_MODE);
		setChecked(true);
		final JavaDecompilerClassFileEditor editor = UIUtil.getActiveEditor();
		if (editor != null) {
			editor.showSource();
			editor.notifyPropertiesChange();
			Display.getDefault().asyncExec(new Runnable() {

				public void run() {
					editor.setFocus();
				}
			});
		}
	}

	@Override
	public boolean isEnabled() {
		JavaDecompilerClassFileEditor editor = UIUtil.getActiveEditor();
		return editor != null;
	}

	public boolean isChecked() {
		return JavaDecompilerPlugin.getDefault().getSourceMode() == JavaDecompilerConstants.SOURCE_MODE;
	}

	@Override
	public void update() {
		setChecked(isChecked());
	}
}