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

import org.eclipse.jdt.core.IJavaElement;

import com.drgarbage.asm.AnnotationVisitor;
import com.drgarbage.asm.Attribute;
import com.drgarbage.asm.FieldVisitor;
import com.drgarbage.asm.MethodVisitor;
import com.drgarbage.asm.render.intf.IClassFileDocument;
import com.drgarbage.bytecode.ByteCodeConstants;
import com.drgarbage.bytecode.BytecodeUtils;

public class ClassFileOutlineElement extends OutlineElement {
	private IClassFileDocument classFileDocument;
	private int fieldCount = 0;
	private int methodCount = 0;
	/**
	 * The type outline element. Class, Interface or Enum.
	 */
	private OutlineElementType typeElement;
	
	/**
	 * 
	 */
	public ClassFileOutlineElement() {
		super(com.drgarbage.asm.Opcodes.ASM4);
		typeElement = new OutlineElementType();
	}
	
	@Override
	public IJavaElement getPrimaryElement() {
		return typeElement;
	}

	/**
	 * @param classFileDocument the classFileDocument to set
	 */
	public void setClassFileDocument(IClassFileDocument classFileDocument) {
		this.classFileDocument = classFileDocument;
	}

	/* (non-Javadoc)
	 * @see com.drgarbage.asm.ClassVisitor#visit(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
	 */
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {

		typeElement.setFlags(access);

		if(name.contains(String.valueOf(ByteCodeConstants.CLASS_NAME_POINT))){
			name = name.replace(ByteCodeConstants.CLASS_NAME_POINT, ByteCodeConstants.CLASS_NAME_SLASH);
		}
	
		int i = name.lastIndexOf(ByteCodeConstants.CLASS_NAME_SLASH);
	
		if (i >= 0) {
			/* create outline object*/
			OutlineElement packageElement = new OutlineElement(com.drgarbage.asm.Opcodes.ASM4);
			packageElement.setElementType(IJavaElement.PACKAGE_DECLARATION);
			packageElement.setElementName(name.substring(0, i));
			
			// TODO test if the -3 works for all circumstances 
			packageElement.setBytecodeDocumentLine(classFileDocument.getLineCount() - 3);

			/* add package and type outline element */
			addChild(packageElement);
		}

		addChild(typeElement);
		
		/* set the outline property */
		typeElement.setElementName(BytecodeUtils.getSimpleName(name));
		typeElement.setFullyQualifiedName(name);
		typeElement.setBytecodeDocumentLine(classFileDocument.getLineCount() - 1);		
	}

	/* (non-Javadoc)
	 * @see com.drgarbage.asm.ClassVisitor#visitAnnotation(java.lang.String, boolean)
	 */
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.drgarbage.asm.ClassVisitor#visitAttribute(com.drgarbage.asm.Attribute)
	 */
	public void visitAttribute(Attribute attr) {
	}

	/* (non-Javadoc)
	 * @see com.drgarbage.asm.ClassVisitor#visitEnd()
	 */
	public void visitEnd() {
	}

	/* (non-Javadoc)
	 * @see com.drgarbage.asm.ClassVisitor#visitField(int, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
	 */
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		
		/* create outline objects */
		OutlineElementField field = new OutlineElementField(typeElement,classFileDocument, fieldCount++);
		field.setElementName(name);
		field.setFlags(access);
		field.setTypeSignature(desc);
		field.setBytecodeDocumentLine(classFileDocument.getLineCount() - 1);
		typeElement.addChild(field);
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.drgarbage.asm.ClassVisitor#visitInnerClass(java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public void visitInnerClass(String name, String outerName,
			String innerName, int access) {
	}

	/* (non-Javadoc)
	 * @see com.drgarbage.asm.ClassVisitor#visitMethod(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String[])
	 */
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		
		OutlineElementMethod method = new OutlineElementMethod(typeElement, classFileDocument, methodCount++);
		if(name.startsWith(ByteCodeConstants.INIT)){
			method.setConstructor(true);
			method.setElementName(typeElement.getElementName());
		}
		else if(name.startsWith(ByteCodeConstants.CLINIT)){
			method.setConstructor(false);
			method.setElementName(ByteCodeConstants.CLINIT_MNEMONICS);
		}
		else{
			method.setConstructor(false);
			method.setElementName(name);
		}
		method.setFlags(access);
		
		/*
		 * FIX: bug#47 Selection of the last element of javax.servlet.http.HttpServletRequest 
		 *             in the outline not working properly 
		 * DESCRIPTION:
		 * The reason is the key @Deprecated
		 * The first line variable of the method section is set to 70 instead of 71 by
		 * class file document parser. 
		 * 
		 * 70    @Deprecated
		 * 71    public abstract boolean isRequestedSessionIdFromUrl();
		 *
		 * If you are clcking on the corresponding outline element of the method
		 * isRequestedSessionIdFromUrl the line 70 is selected in the bytecode view.
		 * This line is not assigned to any method sections and the focus is set to the
		 * class element. 
		 */
		if ((access & com.drgarbage.asm.Opcodes.ACC_DEPRECATED) != 0) {
			method.setBytecodeDocumentLine(classFileDocument.getLineCount() + 2);
		}
		else{		
			method.setBytecodeDocumentLine(classFileDocument.getLineCount() + 1);
		}
		
		method.setMethodDescriptor(desc);
		typeElement.addChild(method);

		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.drgarbage.asm.ClassVisitor#visitOuterClass(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void visitOuterClass(String owner, String name, String desc) {
	}

	/* (non-Javadoc)
	 * @see com.drgarbage.asm.ClassVisitor#visitSource(java.lang.String, java.lang.String)
	 */
	public void visitSource(String source, String debug) {
	}

}
