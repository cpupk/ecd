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
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import com.drgarbage.asm.render.intf.IClassFileDocument;
import com.drgarbage.asm.render.intf.IFieldSection;
import com.drgarbage.asm.render.intf.IOutlineElementField;

/**
 * Outline structure for fields.
 *
 * @author Sergej Alekseev
 * @version $Revision: 203 $
 * $Id: OutlineElementField.java 203 2013-06-07 09:27:41Z salekseev $
 */
public class OutlineElementField extends OutlineElement implements IField, IOutlineElementField {
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
	 * Type signature.
	 */
	private String typeSignature;
	
	private int fieldIndex;
	private IClassFileDocument classFileDocument;

	/**
	 * Constructs a method outline object.
	 * @param parent
	 */
	public OutlineElementField(IType parent, IClassFileDocument document, int fieldIndex) {
		super(com.drgarbage.asm.Opcodes.ASM4);
		this.classFileDocument = document;
		this.fieldIndex = fieldIndex;
		setElementType(IJavaElement.FIELD);
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IMember#getDeclaringType()
	 */
	public IType getDeclaringType() {
		return declaringType;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IField#getConstant()
	 */
	public Object getConstant() throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IField#getKey()
	 */
	public String getKey() {
		return null;
	}

	/**
	 * Sets the type signature.
	 * @param typeSignature
	 */
	public void setTypeSignature(String typeSignature) {
		this.typeSignature = typeSignature;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IField#getTypeSignature()
	 */
	public String getTypeSignature() throws JavaModelException {
		return typeSignature;
	}

	/* (non-Javadoc)
	 * @see com.drgarbage.classfile.render.impl.OutlineElement#getElementName()
	 */
	@Override
	public String getElementName() {
		StringBuffer buf = new StringBuffer(super.getElementName());
		buf.append(" : ");
		String sig = Signature.getSignatureSimpleName(typeSignature);
		//TODO: convert to unqualified name, Define preference
		buf.append(sig);
		return buf.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IField#isEnumConstant()
	 */
	public boolean isEnumConstant() throws JavaModelException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IField#isResolved()
	 */
	public boolean isResolved() {
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
		//FIXME outline selection not shown in source because of this
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
        /* nothing todo*/
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.drgarbage.asm.render.intf.IOutlineElementField#getFieldSection()
	 */
	public IFieldSection getFieldSection() {
		return classFileDocument.getFieldSections().get(fieldIndex);
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
	
}
