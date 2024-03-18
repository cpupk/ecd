/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.sf.feeling.decompiler.JavaDecompilerConstants;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;
import org.sf.feeling.decompiler.i18n.Messages;

public class DisassemblerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	class CheckFieldEditor extends BooleanFieldEditor {

		public CheckFieldEditor(String name, String label, Composite parent) {
			super(name, label, parent);
		}

		@Override
		protected void fireStateChanged(String property, boolean oldValue, boolean newValue) {
			fireValueChanged(property, oldValue ? Boolean.TRUE : Boolean.FALSE,
					newValue ? Boolean.TRUE : Boolean.FALSE);
		}

		public void handleSelection(Composite parent) {
			boolean isSelected = getChangeControl(parent).getSelection();
			valueChanged(false, isSelected);
		}

		@Override
		protected void valueChanged(boolean oldValue, boolean newValue) {
			setPresentsDefaultValue(false);
			fireStateChanged(VALUE, oldValue, newValue);
		}

		@Override
		public Button getChangeControl(Composite parent) {
			return super.getChangeControl(parent);
		}
	}

	private Group basicGroup;
	private Group styleGroup;

	public DisassemblerPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		setPreferenceStore(JavaDecompilerPlugin.getDefault().getPreferenceStore());
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
	}

	@Override
	protected void createFieldEditors() {
		basicGroup = new Group(getFieldEditorParent(), SWT.NONE);
		basicGroup.setText(Messages.getString("DisassemblerPreferencePage.Label.DisassemblerSettings")); //$NON-NLS-1$
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		basicGroup.setLayoutData(gd);

		BooleanFieldEditor showConstantPool = new BooleanFieldEditor(
				JavaDecompilerConstants.CLASS_FILE_ATTR_SHOW_CONSTANT_POOL,
				Messages.getString("DisassemblerPreferencePage.Label.ShowConstant"), //$NON-NLS-1$
				basicGroup);

		BooleanFieldEditor showSourceLineNumber = new BooleanFieldEditor(
				JavaDecompilerConstants.CLASS_FILE_ATTR_SHOW_SOURCE_LINE_NUMBERS,
				Messages.getString("DisassemblerPreferencePage.Label.ShowSourceLineNumber"), //$NON-NLS-1$
				basicGroup);

		BooleanFieldEditor showLineNumberTable = new BooleanFieldEditor(
				JavaDecompilerConstants.CLASS_FILE_ATTR_SHOW_LINE_NUMBER_TABLE,
				Messages.getString("DisassemblerPreferencePage.Label.ShowLineNumberTable"), //$NON-NLS-1$
				basicGroup);

		BooleanFieldEditor showVariableTable = new BooleanFieldEditor(
				JavaDecompilerConstants.CLASS_FILE_ATTR_SHOW_VARIABLE_TABLE,
				Messages.getString("DisassemblerPreferencePage.Label.ShowVariableTable"), //$NON-NLS-1$
				basicGroup);

		BooleanFieldEditor showExceptionTable = new BooleanFieldEditor(
				JavaDecompilerConstants.CLASS_FILE_ATTR_SHOW_EXCEPTION_TABLE,
				Messages.getString("DisassemblerPreferencePage.Label.ShowExceptionTable"), //$NON-NLS-1$
				basicGroup);

		BooleanFieldEditor showMaxs = new BooleanFieldEditor(JavaDecompilerConstants.CLASS_FILE_ATTR_SHOW_MAXS,
				Messages.getString("DisassemblerPreferencePage.Label.ShowMaxs"), //$NON-NLS-1$
				basicGroup);

		BooleanFieldEditor showTryCatch = new BooleanFieldEditor(
				JavaDecompilerConstants.CLASS_FILE_ATTR_RENDER_TRYCATCH_BLOCKS,
				Messages.getString("DisassemblerPreferencePage.Label.ShowTryCatch"), //$NON-NLS-1$
				basicGroup);

		addField(showConstantPool);
		addField(showSourceLineNumber);
		addField(showLineNumberTable);
		addField(showVariableTable);
		addField(showExceptionTable);
		addField(showMaxs);
		addField(showTryCatch);

		GridLayout layout = (GridLayout) basicGroup.getLayout();
		layout.marginWidth = layout.marginHeight = 5;
		basicGroup.layout();

		RadioGroupFieldEditor red = new RadioGroupFieldEditor(JavaDecompilerConstants.BRANCH_TARGET_ADDRESS_RENDERING,
				Messages.getString("DisassemblerPreferencePage.Label.BranchTargetAddressSettings"), //$NON-NLS-1$
				1, new String[][] { { Messages.getString("DisassemblerPreferencePage.Label.AbsoluteAddress"), //$NON-NLS-1$
						JavaDecompilerConstants.BRANCH_TARGET_ADDRESS_RELATIVE },
						{ Messages.getString("DisassemblerPreferencePage.Label.RelativeAddress"), //$NON-NLS-1$
								JavaDecompilerConstants.BRANCH_TARGET_ADDRESS_ABSOLUTE } },
				getFieldEditorParent(), true);
		addField(red);

		styleGroup = new Group(getFieldEditorParent(), SWT.NONE);
		styleGroup.setText(Messages.getString("DisassemblerPreferencePage.Label.StyleSettings")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		styleGroup.setLayoutData(gd);

		ColorFieldEditor opcodeColor = new ColorFieldEditor(JavaDecompilerConstants.BYTECODE_MNEMONIC,
				Messages.getString("DisassemblerPreferencePage.Label.StyleColor"), //$NON-NLS-1$
				styleGroup);

		BooleanFieldEditor opcodeFontBold = new BooleanFieldEditor(JavaDecompilerConstants.BYTECODE_MNEMONIC_BOLD,
				Messages.getString("DisassemblerPreferencePage.Label.StyleBold"), //$NON-NLS-1$
				styleGroup);

		BooleanFieldEditor opcodeFontItalic = new BooleanFieldEditor(JavaDecompilerConstants.BYTECODE_MNEMONIC_ITALIC,
				Messages.getString("DisassemblerPreferencePage.Label.StyleItalic"), //$NON-NLS-1$
				styleGroup);

		BooleanFieldEditor opcodeFontStrikethrough = new BooleanFieldEditor(
				JavaDecompilerConstants.BYTECODE_MNEMONIC_STRIKETHROUGH,
				Messages.getString("DisassemblerPreferencePage.Label.StyleStrikethrough"), //$NON-NLS-1$
				styleGroup);

		BooleanFieldEditor showFontUnderline = new BooleanFieldEditor(JavaDecompilerConstants.BYTECODE_MNEMONIC_UNDERLINE,
				Messages.getString("DisassemblerPreferencePage.Label.StyleUnderline"), //$NON-NLS-1$
				styleGroup);

		addField(opcodeColor);
		addField(opcodeFontBold);
		addField(opcodeFontItalic);
		addField(opcodeFontStrikethrough);
		addField(showFontUnderline);

		layout = (GridLayout) styleGroup.getLayout();
		layout.marginWidth = layout.marginHeight = 5;
		layout.numColumns = 2;
		styleGroup.layout();

		getFieldEditorParent().layout();
	}

	@Override
	public void init(IWorkbench arg0) {

	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
	}

}
