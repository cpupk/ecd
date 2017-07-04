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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import com.drgarbage.asm.render.intf.IClassFileDocument;
import com.drgarbage.asm.render.intf.IMethodSection;
import com.drgarbage.bytecode.ByteCodeConstants;

/**
 * Outline structure for methods.
 *
 * @author Sergej Alekseev
 * @version $Revision: 600 $
 * $Id: OutlineElementMethod.java 600 2014-07-02 09:52:35Z kvbx $
 */
public class OutlineElementMethod extends OutlineElement implements IMethod {
	
	public boolean isLambdaMethod() {
		return false;
	}
	
	private IClassFileDocument classFileDocument;
	
	/**
	 * The declaring type object is a reference 
	 * to the class or interface object.
	 */
	private IType declaringType;
	
	/**
	 * Method flags.
	 */
	private int flags;
	
	/**
	 * Array of the method parameters.
	 */
	private String[] methodDescriptor;
	
	
	/**
	 * Method signature.
	 */
	private String signature;
	
	/**
	 * Flag constructor.
	 */
	private boolean constructor = false;
	
	/**
	 * Flag main method.
	 */
	private boolean mainMethod = false;
	
	private static final String[] NO_STRINGS = new String[0];
	private int methodIndex = ByteCodeConstants.INVALID_OFFSET;
	/**
	 * Constructs a method outline object.
	 * @param parent
	 */
	public OutlineElementMethod(IType parent, IClassFileDocument document, int methodIndex) {
		super(com.drgarbage.asm.Opcodes.ASM4);
		this.classFileDocument = document;
		this.methodIndex = methodIndex;
		setElementType(IJavaElement.METHOD);
		setDeclaringType(parent);
	}
	
	

	/**
	 * Sets the declaring type. The declaring type object is
	 * a reference to the class or interface object.
	 * @param declaringType
	 */
	public void setDeclaringType(IType declaringType) {
		this.declaringType = declaringType;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMember#getDeclaringType()
	 */
	public IType getDeclaringType() {
		return declaringType;
	}	
	
	/**
	 * Sets the method flags.
	 * @param flags
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
	
	/**
	 * Sets the method descriptor.
	 * @param signature the method signature to set
	 */
	public void setMethodDescriptor(String signature) {
		this.signature = signature;
		
		/* create parameter array */
		String[] list=  Signature.getParameterTypes(signature);

		if(list.length == 0){
			methodDescriptor = NO_STRINGS;
		}
		else{
			//TODO: unqualified names
			methodDescriptor = list;
		}

	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#getParameterTypes()
	 */
	public String[] getParameterTypes() {
		return methodDescriptor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#getExceptionTypes()
	 */
	public String[] getExceptionTypes() throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#getKey()
	 */
	public String getKey() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#getNumberOfParameters()
	 */
	public int getNumberOfParameters() {
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#getParameterNames()
	 */
	public String[] getParameterNames() throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#getRawParameterNames()
	 */
	public String[] getRawParameterNames() throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#getReturnType()
	 */
	public String getReturnType() throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#getSignature()
	 */
	public String getSignature() throws JavaModelException {
		return signature;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#getTypeParameter(java.lang.String)
	 */
	public ITypeParameter getTypeParameter(String name) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#getTypeParameterSignatures()
	 */
	public String[] getTypeParameterSignatures() throws JavaModelException {
		return methodDescriptor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#getTypeParameters()
	 */
	public ITypeParameter[] getTypeParameters() throws JavaModelException {
		return null;
	}

	/**
	 * Sets the flag constructor.
	 * @param b <code>true</code> or <code>false</code>
	 */
	public void setConstructor(boolean b) {
		this.constructor = b;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#isConstructor()
	 */
	public boolean isConstructor() throws JavaModelException {
		return constructor;
	}

	/**
	 * Sets the main method flag.
	 * @param b <code>true</code> or <code>false</code>
	 */
	public void setMainMethod(boolean b) {
		this.mainMethod = b;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#isMainMethod()
	 */
	public boolean isMainMethod() throws JavaModelException {
		return mainMethod;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#isResolved()
	 */
	public boolean isResolved() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#isSimilar(org.eclipse.jdt.core.IMethod)
	 */
	public boolean isSimilar(IMethod method) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMember#getCategories()
	 */
	public String[] getCategories() throws JavaModelException {
		return null;
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
		return null;
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
	 * @see org.eclipse.jdt.core.IMember#getTypeRoot()
	 */
	public ITypeRoot getTypeRoot() {
		/*nothing todo*/
		return null;
	}

	/* (non-Javadoc)
	 * @see com.drgarbage.classfile.render.intf.IOutlineElement#getSourceCodeDocumentLine()
	 */
	public int getSourceCodeDocumentLine() {
		IMethodSection m = classFileDocument.getMethodSections().get(methodIndex);
		int byteCodeDocLine = m.getFirstLine();
		
		if(byteCodeDocLine == ByteCodeConstants.INVALID_OFFSET){
			return ByteCodeConstants.INVALID_OFFSET;
		}
		
		return m.getSourceCodeLine(byteCodeDocLine);
	}

	/**
	 * Returns the reference to the method section.
	 * @return method section
	 */
	public IMethodSection getMethodSection(){ 
		return classFileDocument.getMethodSections().get(methodIndex);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMethod#getDefaultValue()
	 */
	public IMemberValuePair getDefaultValue() throws JavaModelException {
		/* since eclipse 3.4 (ganymede) */
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



	public ILocalVariable[] getParameters() throws JavaModelException {
		/* only for 3.7 compatibility */
		return null;
	}
}
