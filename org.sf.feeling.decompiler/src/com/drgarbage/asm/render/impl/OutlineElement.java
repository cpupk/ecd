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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;

import com.drgarbage.asm.ClassVisitor;
import com.drgarbage.asm.render.intf.IOutlineElement;
import com.drgarbage.bytecode.ByteCodeConstants;


/**
 * The basis class for an outline element.
 *
 * @author Sergej Alekseev
 * @version $Revision: 203 $
 * $Id: OutlineElement.java 203 2013-06-07 09:27:41Z salekseev $
 */
public class OutlineElement extends ClassVisitor implements IJavaElement, IParent, IOutlineElement {

	public OutlineElement(int arg0) {
		super(arg0);
	}

	/**
	 * The name of the element
	 */
	private String elementName;

	/**
	 * The type of the element from IJavaElement interface.
	 * CLASS_FILE -> TYPE -> METHOD ...
	 * Default is 0 (unspecified).
	 */
	private int type = 0;

	/**
	 * Children of the element.
	 */
	private List<IJavaElement> children = new ArrayList<IJavaElement>();
	
	/**
	 * Document line.
	 */
	private int documentLine = -1;

	/**
	 * Sets the elemet name.
	 * @param elementName
	 */
	public void setElementName(String elementName) {
		this.elementName = elementName;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IJavaElement#getElementName()
	 */
	public String getElementName() {
		return elementName;
	}	
	
	/**
	 * Add an element.
	 * @param child
	 */
	public void  addChild(IJavaElement child){
		children.add(child);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IParent#getChildren()
	 */
	public IJavaElement[] getChildren() throws JavaModelException {
		IJavaElement[] c = new IJavaElement[children.size()];
		for (int i = 0; i < children.size(); i++){
			c[i]=children.get(i);
		}

		return c;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IParent#hasChildren()
	 */
	public boolean hasChildren() throws JavaModelException {
		return children.size()!= 0;
	}

	/**
	 * Sets the element type.
	 * @param type
	 */
	public void setElementType(int type) {
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IJavaElement#getElementType()
	 */
	public int getElementType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IJavaElement#exists()
	 */
	public boolean exists() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IJavaElement#getAncestor(int)
	 */
	public IJavaElement getAncestor(int ancestorType) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IJavaElement#getAttachedJavadoc(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public String getAttachedJavadoc(IProgressMonitor monitor) throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IJavaElement#getCorrespondingResource()
	 */
	public IResource getCorrespondingResource() throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IJavaElement#getHandleIdentifier()
	 */
	public String getHandleIdentifier() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IJavaElement#getJavaModel()
	 */
	public IJavaModel getJavaModel() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IJavaElement#getJavaProject()
	 */
	public IJavaProject getJavaProject() {
		/* workaround to avoid an exception */
		return new MJavaProject();
	}
	
	/* workaround to avoid an exception during rendering of the outline */
	private class MJavaProject extends JavaProject {
		/*
		 * @see IJavaProject
		 */
		public boolean isOnClasspath(IJavaElement element) {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IJavaElement#getOpenable()
	 */
	public IOpenable getOpenable() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IJavaElement#getParent()
	 */
	public IJavaElement getParent() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IJavaElement#getPath()
	 */
	public IPath getPath() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IJavaElement#getPrimaryElement()
	 */
	public IJavaElement getPrimaryElement() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IJavaElement#getResource()
	 */
	public IResource getResource() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IJavaElement#getSchedulingRule()
	 */
	public ISchedulingRule getSchedulingRule() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IJavaElement#getUnderlyingResource()
	 */
	public IResource getUnderlyingResource() throws JavaModelException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IJavaElement#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IJavaElement#isStructureKnown()
	 */
	public boolean isStructureKnown() throws JavaModelException {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.drgarbage.classfile.render.intf.IOutlineElement#getBytecodeDocumentLine()
	 */
	public int getBytecodeDocumentLine() {
		return documentLine;
	}

	/**
	 * @param documentLine the documentLine to set
	 */
	public void setBytecodeDocumentLine(int documentLine) {
		this.documentLine = documentLine;
	}

	/* (non-Javadoc)
	 * @see com.drgarbage.classfile.render.intf.IOutlineElement#getSourceCodeDocumentLine()
	 */
	public int getSourceCodeDocumentLine() {
		return ByteCodeConstants.INVALID_OFFSET;
	}
}
