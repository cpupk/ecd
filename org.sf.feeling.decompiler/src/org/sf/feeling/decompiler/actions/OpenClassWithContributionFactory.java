/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.sf.feeling.decompiler.JavaDecompilerConstants;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;
import org.sf.feeling.decompiler.editor.DecompilerType;
import org.sf.feeling.decompiler.editor.IDecompilerDescriptor;
import org.sf.feeling.decompiler.editor.JavaDecompilerClassFileEditor;
import org.sf.feeling.decompiler.i18n.Messages;

public class OpenClassWithContributionFactory extends ExtensionContributionFactory {

	public static final class OpenClassesAction extends Action {

		private final IEditorDescriptor classEditor;
		private final List classes;
		private String decompilerType;

		public OpenClassesAction(IEditorDescriptor classEditor, List classes, String decompilerType) {
			this.classEditor = classEditor;
			this.classes = classes;
			this.decompilerType = decompilerType;
		}

		@Override
		public String getText() {
			IDecompilerDescriptor decompilerDescriptor = JavaDecompilerPlugin.getDefault()
					.getDecompilerDescriptor(decompilerType);
			if (decompilerDescriptor != null) {
				return decompilerDescriptor.getDecompileAction().getText();
			}
			return classEditor.getLabel();
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return JavaDecompilerPlugin.getDefault().getDecompilerDescriptor(decompilerType).getDecompilerIcon();
		}

		@Override
		public void run() {
			// Get UI refs
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window == null) {
				return;
			}
			IWorkbenchPage page = window.getActivePage();
			if (page == null) {
				return;
			}

			// Load each IClassFile into the selected editor
			for (int i = 0; i < classes.size(); i++) {
				IClassFile classfile = (IClassFile) classes.get(i);
				// Convert the IClassFile to an IEditorInput
				IEditorInput input = EditorUtility.getEditorInput(classfile);

				try {
					IEditorPart openEditor = page.openEditor(input, classEditor.getId(), true);

					if ((openEditor != null) && (!classEditor.getId().equals(openEditor.getEditorSite().getId()))) {
						// An existing editor already has this class open. Close
						// it
						// and re-open in the correct editor
						if (!openEditor.isDirty()) {
							openEditor.getSite().getPage().closeEditor(openEditor, false);
							page.openEditor(input, classEditor.getId(), true);
						}
					}
					if (openEditor instanceof JavaDecompilerClassFileEditor) {
						JavaDecompilerClassFileEditor editor = (JavaDecompilerClassFileEditor) openEditor;
						editor.doSetInput(decompilerType, true);
					}
				} catch (PartInitException e) {
					JavaDecompilerPlugin.getDefault().getLog()
							.log(new Status(IStatus.ERROR, JavaDecompilerConstants.PLUGIN_ID, 0, e.getMessage(), e));
				}
			}
		}
	}

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {

		final ISelectionService selService = (ISelectionService) serviceLocator.getService(ISelectionService.class);

		// Define a dynamic set of submenu entries
		String dynamicMenuId = "org.sf.feeling.decompiler.openwith.items"; //$NON-NLS-1$
		IContributionItem dynamicItems = new CompoundContributionItem(dynamicMenuId) {

			@Override
			protected IContributionItem[] getContributionItems() {

				// Get the list of editors that can open a class file
				IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();

				// Get the current selections and return if nothing is selected
				Iterator<Object> selections = getSelections(selService);
				if (selections == null) {
					return new IContributionItem[0];
				}

				List<IClassFile> classes = getSelectedElements(selService, IClassFile.class);

				// List of menu items
				List<ActionContributionItem> list = new ArrayList<>();

				if (classes.size() == 1) {
					IEditorDescriptor editor = registry.findEditor(JavaDecompilerConstants.EDITOR_ID);

					TreeSet<String> decompilerTypes = new TreeSet<>();
					Collections.addAll(decompilerTypes, DecompilerType.getDecompilerTypes());

					for (String decompilerType : decompilerTypes) {
						list.add(new ActionContributionItem(new OpenClassesAction(editor, classes, decompilerType)));
					}
				}

				return (IContributionItem[]) list.toArray(new IContributionItem[list.size()]);
			}
		};

		// Determine menu name
		List selectedClasses = getSelectedElements(selService, IClassFile.class);
		boolean openClassWith = (selectedClasses.size() == 1);
		if (openClassWith) {

			// Define dynamic submenu
			MenuManager submenu = new MenuManager(
					Messages.getString("JavaDecompilerActionBarContributor.Menu.OpenClassWith"), //$NON-NLS-1$
					dynamicMenuId);
			submenu.add(dynamicItems);

			// Add the submenu and show it when classes are selected
			additions.addContributionItem(submenu, new Expression() {

				@Override
				public EvaluationResult evaluate(IEvaluationContext context) throws CoreException {
					boolean menuVisible = isMenuVisible(selService);

					if (menuVisible) {
						return EvaluationResult.TRUE;
					}
					return EvaluationResult.FALSE;
				}
			});
		}
	}

	private boolean isMenuVisible(ISelectionService selService) {

		Iterator selections = getSelections(selService);

		boolean atLeastOneSelection = false;
		boolean allClasses = true;
		boolean singlePackageOrRoot = false;

		while ((selections != null) && selections.hasNext()) {
			atLeastOneSelection = true;

			Object select = selections.next();

			if (!(select instanceof IClassFile)) {
				allClasses = false;
			}

			if (((select instanceof IPackageFragment)
					|| (select instanceof IPackageFragmentRoot) && (!selections.hasNext()))) {
				singlePackageOrRoot = true;
			}
		}

		if (atLeastOneSelection) {
			if (allClasses || singlePackageOrRoot) {
				return true;
			}
		}

		return false;
	}

	private <T> List<T> getSelectedElements(ISelectionService selService, Class<T> eleClass) {

		Iterator selections = getSelections(selService);
		List<T> elements = new ArrayList<>();

		while ((selections != null) && selections.hasNext()) {
			Object select = selections.next();

			if (eleClass.isInstance(select)) {
				elements.add((T) select);
			}
		}

		return elements;
	}

	private Iterator getSelections(ISelectionService selService) {
		ISelection selection = selService.getSelection();

		if (selection != null) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				return structuredSelection.iterator();
			}
		}

		return null;
	}

}
