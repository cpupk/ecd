/**
 * Copyright (c) 2008-2012, Dr. Garbage Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.drgarbage.asm.render.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ICompletionRequestor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;

import com.drgarbage.bytecode.BytecodeUtils;


/**
 * Outline structure for class, interface or enum elements.
 *
 * @author Sergej Alekseev
 * @version $Revision: 600 $
 * $Id: OutlineElementType.java 600 2014-07-02 09:52:35Z kvbx $
 */
public class OutlineElementType extends OutlineElement implements IType {
	
	public boolean isLambda() {
		return false;
	}

	/* flags */
	private int flags = 0;

	/* empty constants */
	@SuppressWarnings("unused")
	private static final IField[] NO_FIELDS = new IField[0];
	private static final IMethod[] NO_METHODS = new IMethod[0];
	private static final IType[] NO_TYPES = new IType[0];
	private static final IInitializer[] NO_INITIALIZERS = new IInitializer[0];
	private static final String[] NO_STRINGS = new String[0];
	public static final String EMPTY_JAVADOC = new String();
	
	/**
	 * Fully qualified name. For exampe: org.test.Test
	 */
	private String fullyQualifiedName;
	private PackageFragment packageFragment;
	
	/**
	 * 
	 */
	public OutlineElementType() {
		super(com.drgarbage.asm.Opcodes.ASM4);
		setElementType(IJavaElement.TYPE);
	}

	/**
	 * Super class name.
	 */
	private String superclassName;

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#isAnnotation()
	 */
	public boolean isAnnotation() throws JavaModelException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#isClass()
	 */
	public boolean isClass() throws JavaModelException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#isEnum()
	 */
	public boolean isEnum() throws JavaModelException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#isInterface()
	 */
	public boolean isInterface() throws JavaModelException {
		return false;
	}

	/**
	 * Sets flags.
	 * @param flags the flags to set
	 */
	public void setFlags(int flags) {
		this.flags = flags;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMember#getFlags()
	 */
	public int getFlags() throws JavaModelException {
		return flags;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#codeComplete(char[], int, int, char[][], char[][], int[], boolean, org.eclipse.jdt.core.ICompletionRequestor)
	 */
	public void codeComplete(char[] snippet, int insertion, int position, char[][] localVariableTypeNames, char[][] localVariableNames, int[] localVariableModifiers, boolean isStatic, ICompletionRequestor requestor) throws JavaModelException {
		/* ignore */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#codeComplete(char[], int, int, char[][], char[][], int[], boolean, org.eclipse.jdt.core.CompletionRequestor)
	 */
	public void codeComplete(char[] snippet, int insertion, int position, char[][] localVariableTypeNames, char[][] localVariableNames, int[] localVariableModifiers, boolean isStatic, CompletionRequestor requestor) throws JavaModelException {
		/* ignore */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#codeComplete(char[], int, int, char[][], char[][], int[], boolean, org.eclipse.jdt.core.ICompletionRequestor, org.eclipse.jdt.core.WorkingCopyOwner)
	 */
	public void codeComplete(char[] snippet, int insertion, int position, char[][] localVariableTypeNames, char[][] localVariableNames, int[] localVariableModifiers, boolean isStatic, ICompletionRequestor requestor, WorkingCopyOwner owner) throws JavaModelException {
		/* ignore */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#codeComplete(char[], int, int, char[][], char[][], int[], boolean, org.eclipse.jdt.core.CompletionRequestor, org.eclipse.jdt.core.WorkingCopyOwner)
	 */
	public void codeComplete(char[] snippet, int insertion, int position, char[][] localVariableTypeNames, char[][] localVariableNames, int[] localVariableModifiers, boolean isStatic, CompletionRequestor requestor, WorkingCopyOwner owner) throws JavaModelException {
		/* ignore */		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#createField(java.lang.String, org.eclipse.jdt.core.IJavaElement, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IField createField(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#createInitializer(java.lang.String, org.eclipse.jdt.core.IJavaElement, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IInitializer createInitializer(String contents, IJavaElement sibling, IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#createMethod(java.lang.String, org.eclipse.jdt.core.IJavaElement, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IMethod createMethod(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#createType(java.lang.String, org.eclipse.jdt.core.IJavaElement, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IType createType(String contents, IJavaElement sibling, boolean force, IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#findMethods(org.eclipse.jdt.core.IMethod)
	 */
	public IMethod[] findMethods(IMethod method) {
		return NO_METHODS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getChildrenForCategory(java.lang.String)
	 */
	public IJavaElement[] getChildrenForCategory(String category) throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getField(java.lang.String)
	 */
	public IField getField(String name) {
		for (IField f : fields) {
			if (name.equals(f.getElementName())) {
				return f;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getFields()
	 */
	public IField[] getFields() throws JavaModelException {
		return fields.toArray(new IField[fields.size()]);
	}

	/**
	 * @param fullyQualifiedName the fullyQualifiedName to set
	 */
	public void setFullyQualifiedName(String fullyQualifiedName) {
		this.fullyQualifiedName = fullyQualifiedName;
		
		this.packageFragment = new PackageFragment(com.drgarbage.asm.Opcodes.ASM4);
		this.packageFragment.setElementName(BytecodeUtils.getPackage(fullyQualifiedName));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getFullyQualifiedName()
	 */
	public String getFullyQualifiedName() {
		return fullyQualifiedName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getFullyQualifiedName(char)
	 */
	public String getFullyQualifiedName(char enclosingTypeSeparator) {
		return fullyQualifiedName.replace('/', enclosingTypeSeparator);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getFullyQualifiedParameterizedName()
	 */
	public String getFullyQualifiedParameterizedName() throws JavaModelException {
		return fullyQualifiedName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getInitializer(int)
	 */
	public IInitializer getInitializer(int occurrenceCount) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getInitializers()
	 */
	public IInitializer[] getInitializers() throws JavaModelException {
		return NO_INITIALIZERS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getKey()
	 */
	public String getKey() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getMethod(java.lang.String, java.lang.String[])
	 */
	public IMethod getMethod(String name, String[] parameterTypeSignatures) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getMethods()
	 */
	public IMethod[] getMethods() throws JavaModelException {
		return methods.toArray(new IMethod[methods.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getPackageFragment()
	 */
	public IPackageFragment getPackageFragment() {
		return packageFragment;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getSuperInterfaceNames()
	 */
	public String[] getSuperInterfaceNames() throws JavaModelException {
		return NO_STRINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getSuperInterfaceTypeSignatures()
	 */
	public String[] getSuperInterfaceTypeSignatures() throws JavaModelException {
		return NO_STRINGS;
	}

	/**
	 * Sets super class name.
	 * @param superclassName
	 */
	public void setSuperclassName(String superclassName) {
		this.superclassName = superclassName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getSuperclassName()
	 */
	public String getSuperclassName() throws JavaModelException {
		return superclassName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getSuperclassTypeSignature()
	 */
	public String getSuperclassTypeSignature() throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getType(java.lang.String)
	 */
	public IType getType(String name) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getTypeParameter(java.lang.String)
	 */
	public ITypeParameter getTypeParameter(String name) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getTypeParameterSignatures()
	 */
	public String[] getTypeParameterSignatures() throws JavaModelException {
		return NO_STRINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getTypeParameters()
	 */
	public ITypeParameter[] getTypeParameters() throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getTypeQualifiedName()
	 */
	public String getTypeQualifiedName() {
		return fullyQualifiedName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getTypeQualifiedName(char)
	 */
	public String getTypeQualifiedName(char enclosingTypeSeparator) {
		return fullyQualifiedName.replace('/', enclosingTypeSeparator);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#getTypes()
	 */
	public IType[] getTypes() throws JavaModelException {
		return NO_TYPES;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#isAnonymous()
	 */
	public boolean isAnonymous() throws JavaModelException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#isLocal()
	 */
	public boolean isLocal() throws JavaModelException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#isMember()
	 */
	public boolean isMember() throws JavaModelException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#isResolved()
	 */
	public boolean isResolved() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#loadTypeHierachy(java.io.InputStream, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ITypeHierarchy loadTypeHierachy(InputStream input, IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#newSupertypeHierarchy(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ITypeHierarchy newSupertypeHierarchy(IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#newSupertypeHierarchy(org.eclipse.jdt.core.ICompilationUnit[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ITypeHierarchy newSupertypeHierarchy(ICompilationUnit[] workingCopies, IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#newSupertypeHierarchy(org.eclipse.jdt.core.IWorkingCopy[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ITypeHierarchy newSupertypeHierarchy(IWorkingCopy[] workingCopies, IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#newSupertypeHierarchy(org.eclipse.jdt.core.WorkingCopyOwner, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ITypeHierarchy newSupertypeHierarchy(WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#newTypeHierarchy(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ITypeHierarchy newTypeHierarchy(IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#newTypeHierarchy(org.eclipse.jdt.core.IJavaProject, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ITypeHierarchy newTypeHierarchy(IJavaProject project, IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#newTypeHierarchy(org.eclipse.jdt.core.ICompilationUnit[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ITypeHierarchy newTypeHierarchy(ICompilationUnit[] workingCopies, IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#newTypeHierarchy(org.eclipse.jdt.core.IWorkingCopy[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ITypeHierarchy newTypeHierarchy(IWorkingCopy[] workingCopies, IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#newTypeHierarchy(org.eclipse.jdt.core.WorkingCopyOwner, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ITypeHierarchy newTypeHierarchy(WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#newTypeHierarchy(org.eclipse.jdt.core.IJavaProject, org.eclipse.jdt.core.WorkingCopyOwner, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ITypeHierarchy newTypeHierarchy(IJavaProject project, WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#resolveType(java.lang.String)
	 */
	public String[][] resolveType(String typeName) throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#resolveType(java.lang.String, org.eclipse.jdt.core.WorkingCopyOwner)
	 */
	public String[][] resolveType(String typeName, WorkingCopyOwner owner) throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMember#getCategories()
	 */
	public String[] getCategories() throws JavaModelException {
		return NO_STRINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMember#getClassFile()
	 */
	public IClassFile getClassFile() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMember#getCompilationUnit()
	 */
	public ICompilationUnit getCompilationUnit() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMember#getJavadocRange()
	 */
	public ISourceRange getJavadocRange() throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMember#getNameRange()
	 */
	public ISourceRange getNameRange() throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMember#getOccurrenceCount()
	 */
	public int getOccurrenceCount() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMember#getType(java.lang.String, int)
	 */
	public IType getType(String name, int occurrenceCount) {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMember#isBinary()
	 */
	public boolean isBinary() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ISourceReference#getSource()
	 */
	public String getSource() throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ISourceReference#getSourceRange()
	 */
	public ISourceRange getSourceRange() throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ISourceManipulation#copy(org.eclipse.jdt.core.IJavaElement, org.eclipse.jdt.core.IJavaElement, java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void copy(IJavaElement container, IJavaElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws JavaModelException {
		/* ignore */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ISourceManipulation#delete(boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void delete(boolean force, IProgressMonitor monitor) throws JavaModelException {
		/* ignore */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ISourceManipulation#move(org.eclipse.jdt.core.IJavaElement, org.eclipse.jdt.core.IJavaElement, java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void move(IJavaElement container, IJavaElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws JavaModelException {
		/* ignore */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ISourceManipulation#rename(java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void rename(String name, boolean replace, IProgressMonitor monitor) throws JavaModelException {
		/* ignore */		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMember#getDeclaringType()
	 */
	public IType getDeclaringType() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMember#getTypeRoot()
	 */
	public ITypeRoot getTypeRoot() {
		/*nothing todo*/
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IAnnotatable#getAnnotation(java.lang.String)
	 */
	public IAnnotation getAnnotation(String name) {
		/* since eclipse 3.4 (ganymede) */
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IAnnotatable#getAnnotations()
	 */
	public IAnnotation[] getAnnotations() throws JavaModelException {
		/* since eclipse 3.4 (ganymede) */
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#codeComplete(char[], int, int, char[][], char[][], int[], boolean, org.eclipse.jdt.core.CompletionRequestor, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void codeComplete(char[] snippet, int insertion, int position,
			char[][] localVariableTypeNames, char[][] localVariableNames,
			int[] localVariableModifiers, boolean isStatic,
			CompletionRequestor requestor, IProgressMonitor monitor)
			throws JavaModelException {
		/* since eclipse 3.5 (galileo) */
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IType#codeComplete(char[], int, int, char[][], char[][], int[], boolean, org.eclipse.jdt.core.CompletionRequestor, org.eclipse.jdt.core.WorkingCopyOwner, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void codeComplete(char[] snippet, int insertion, int position,
			char[][] localVariableTypeNames, char[][] localVariableNames,
			int[] localVariableModifiers, boolean isStatic,
			CompletionRequestor requestor, WorkingCopyOwner owner,
			IProgressMonitor monitor) throws JavaModelException {
		/* since eclipse 3.5 (galileo) */
	}
	
	private List<IField> fields = new ArrayList<IField>(4);
	private List<IMethod> methods = new ArrayList<IMethod>(4);

	@Override
	public void addChild(IJavaElement child) {
		
		if (child instanceof IField) {
			fields.add((IField)child);
		}
		else if (child instanceof IMethod) {
			methods.add((IMethod)child);
		}
		
		super.addChild(child);
	}
	
	

}
