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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.drgarbage.asm.AnnotationVisitor;
import com.drgarbage.asm.Attribute;
import com.drgarbage.asm.ClassReader;
import com.drgarbage.asm.ClassVisitor;
import com.drgarbage.asm.FieldVisitor;
import com.drgarbage.asm.MethodVisitor;
import com.drgarbage.asm.render.intf.IClassFileDocument;
import com.drgarbage.asm.render.intf.ILocalVariableTable;
import com.drgarbage.asm.signature.SignatureReader;
import com.drgarbage.bytecode.ByteCodeConstants;
import com.drgarbage.bytecode.BytecodeUtils;
import com.drgarbage.bytecode.ExceptionTableEntry;
import com.drgarbage.javasrc.JavaKeywords;

public class ClassFileDocument extends AbstractClassFileDocument {

	/**
	 * Renderer Class for Exception Table Objects.
	 */	
	protected static class ExceptionTableEntryComparator implements java.util.Comparator<ExceptionTableEntry> {
	
		public int compare(ExceptionTableEntry o1, ExceptionTableEntry o2) {
			if (o1 == o2) {
				return 0;
			}
			else if (o1 == null) {
				return -1;
			}
			else if (o2 == null) {
				return 1;
			}
			else {
				/* none of the two is null */
	
				if (o1.getStartPc() < o2.getStartPc()) {
					return -1;
				}
				else if (o1.getStartPc() > o2.getStartPc()) {
					return 1;
				}
				else {
					/* equal start_pc */
					if (o1.getEndPc() > o2.getEndPc()) {
						/* we want wider try blocks to occur first */
						return -1;
					}
					else if (o1.getEndPc() < o2.getEndPc()) {
						return 1;
					}
					else {
						/* they cover the same block */
						if (o1.getHandlerPc() < o2.getHandlerPc()) {
							/* let the handler with the handler 
							 * closer to 0 win */
							return -1;
						}
						else if (o1.getHandlerPc() < o2.getHandlerPc()) {
							return 1;
						}
						else {
							/* they are completely equal */
							return 0;
						}
					}
				}
			}
		}
	
	}
	

	protected static class ListMap<K, V> extends HashMap<K, List<V>> {

		private static final long serialVersionUID = 2842382259729658163L;

		public void putToList(K key, V value) {
			List<V> list = get(key);
			if (list == null) {
				list = new ArrayList<V>();
				super.put(key, list);
			}
			list.add(value);
		}

	}

	protected class MethodRenderer extends AbstractMethodRenderer {

		public MethodRenderer(int access, String name, String descriptor,
				String signature, String[] exceptions) {
			super(access, name, descriptor, signature, exceptions);
		}

		public MethodRenderer(int access, String name, String descriptor,
				String signature, String[] exceptions, MethodVisitor mv) {
			super(access, name, descriptor, signature, exceptions, mv);
		}

		@Override
		protected ILocalVariableTable createLocalVariableTable(boolean available) {
			return new LocalVariableTable(ClassFileDocument.this.constantPool, available);
		}
		
	}

	/**
	 * Prints a disassembled view of the given class to the standard output. <p>
	 * Usage: TraceClassVisitor [-debug] &lt;fully qualified class name or class
	 * file name &gt;
	 * 
	 * @param args the command line arguments.
	 * 
	 * @throws Exception if the class cannot be found, or if an IO exception
	 *         occurs.
	 */
	public static void main(final String[] args) throws Exception {
	    int i = 0;
	    int flags = ClassReader.SKIP_DEBUG;
	
	    boolean ok = true;
	    if (args.length < 1 || args.length > 2) {
	        ok = false;
	    }
	    if (ok && "-debug".equals(args[0])) {
	        i = 1;
	        flags = 0;
	        if (args.length != 2) {
	            ok = false;
	        }
	    }
	    if (!ok) {
	        System.err.println("Prints a disassembled view of the given class.");
	        System.err.println("Usage: TraceClassVisitor [-debug] "
	                + "<fully qualified class name or class file name>");
	        return;
	    }
	    ClassReader cr;
	    if (args[i].endsWith(".class") || args[i].indexOf('\\') > -1
	            || args[i].indexOf('/') > -1)
	    {
	        cr = new ClassReader(new FileInputStream(args[i]));
	    } else {
	        cr = new ClassReader(args[i]);
	    }
	    cr.accept(new ClassFileDocument(null),
	            DEFAULT_ATTRIBUTES,
	            flags);
	}


	public static IClassFileDocument readClass(InputStream in) throws IOException {
        ClassFileDocument doc = new ClassFileDocument();
        ClassReader cr = new ClassReader(in, doc);
        cr.accept(doc, AbstractClassFileDocument.DEFAULT_ATTRIBUTES, 0);
        return doc;
	}
	

	
	/**
	 * The print writer to be used to print the class.
	 */
	//protected final PrintWriter pw;

	private ClassVisitor classVisitor;

	private ClassFileDocument() {
		this(null);
	}
	/**
	 * Constructs a new class file document.
	 * 
	 * @param classVisitor the class visitor.
	 * @see ClassVisitor
	 */
	public ClassFileDocument(ClassVisitor classVisitor) {
	    //this.pw = pw;
		this.classVisitor = classVisitor;
		
	}

	// ------------------------------------------------------------------------
	// Implementation of the ClassVisitor interface
	// ------------------------------------------------------------------------
	
	
	public void visit(
	    final int version,
	    final int access,
	    final String name,
	    final String signature,
	    final String superName,
	    final String[] interfaces)
	{
	    this.name = BytecodeUtils.toJavaSrcName(name);
	    classSimpleName = BytecodeUtils.getSimpleName(name);
	    
		//appendHeaderComment(ByteCodeConstants.Filesystem, null);
		//appendNewline();
		appendClassFileFormatVersion(version);
		
		appendPackage(name);
		appendNewline();

		appendDeprecated(access);
	
	    appendAccess(access & ~ACC_SUPER);
	    if ((access & ACC_ANNOTATION) != 0) {
	    	appendAnnotation(JavaKeywords.INTERFACE);
	    	appendSpace();
	    } else if ((access & ACC_INTERFACE) != 0) {
	    	sb.append(JavaKeywords.INTERFACE);
	    	appendSpace();
	    } else if ((access & ACC_ENUM) == 0) {
	    	sb.append(JavaKeywords.CLASS);
	    	appendSpace();
	    }

	    sb.append(classSimpleName);
	    classSignatureDocumentLine = lineCount; /* set class file line number reference */
	    if (signature != null) {
	        SignatureRenderer sv = new SignatureRenderer(access);
	        SignatureReader r = new SignatureReader(signature);
	        r.accept(sv);
	        sb.append(sv.getDeclaration());
	    }
	    else {
	    	/* null signature */
		    if (superName != null && !ByteCodeConstants.JAVA_LANG_OBJECT.equals(superName)) {
				appendSpace();
				append(JavaKeywords.EXTENDS);
				appendSpace();
				appendJavaSourcePath(superName);
		    }
		    if (interfaces != null && interfaces.length > 0) {
				appendSpace();
				append(JavaKeywords.IMPLEMENTS);

		        for (int i = 0; i < interfaces.length; ++i) {
					if (i != 0) {
						appendComma();
					}
					appendSpace();
					appendJavaSourcePath(interfaces[i]);
		        }
		    }
	    }
	
		appendSpace();
		appendLeftBrace();
		appendNewline();
		incrementIndent();
		
		appendConstantPool();

		if (classVisitor != null) {
			classVisitor.visit(version,
			    access,
			    name,
			    signature,
			    superName,
			    interfaces);
		}
	}

	public AnnotationVisitor visitAnnotation(
	    final String desc,
	    final boolean visible)
	{
		return visitAnnotationImpl(desc, visible);
	}

	
	public void visitAttribute(final Attribute attr) {
		//FIXME Attribute rendering
		visitAttributeImpl(attr);
	
		if (classVisitor != null) {
			classVisitor.visitAttribute(attr);
		}

	}

	public void visitEnd() {
		decrementIndent();
		appendRightBrace();
		appendNewline();
		
		if (classVisitor != null) {
			classVisitor.visitEnd();
		}
		
		methodBorderLines = new int[methodBorderLinesList.size()];
		for (int i = 0; i < methodBorderLinesList.size(); i++) {
			methodBorderLines[i] = methodBorderLinesList.get(i).intValue();
		}
		methodBorderLinesList = null;

	}

	public FieldVisitor visitField(
	    final int access,
	    final String name,
	    final String desc,
	    final String signature,
	    final Object value)
	{
	    FieldVisitor fieldVisitor = super.visitField(access, name, desc, signature);
		if (classVisitor != null) {
			classVisitor.visitField(access, name, desc, signature, value);
		}
	
	    return fieldVisitor;
	}

	public void visitInnerClass(
	    final String name,
	    final String outerName,
	    final String innerName,
	    final int access)
	{
		
		if (name == null || !BytecodeUtils.toJavaSrcName(name).equals(this.name)) {
			/* only show as inner class 
			 * if the name of thisClass is unequal with name */
			appendNewline();
	    	appendCommentBegin();
	    	appendSpace();
	    	sb.append(ByteCodeConstants.INNER_CLASS);
	    	appendSpace();
	    	appendCommentEnd();
	    	appendNewline();
	    	
	    	appendCommentBegin();
	    	appendSpace();
		    appendAccess(access & ~ACC_SUPER);
	    	sb.append(innerName);
	    	appendSpace();
	    	appendCommentEnd();
	    	appendNewline();
			
		}
	    
		if (classVisitor != null) {
			classVisitor.visitInnerClass(name, outerName, innerName, access);
		}
	}

	public MethodVisitor visitMethod(
	    final int access,
	    final String name,
	    final String desc,
	    final String signature,
	    final String[] exceptions)
	{
		if (classVisitor != null) {
			classVisitor.visitMethod(
				    access,
				    name,
				    desc,
				    signature,
				    exceptions);
		}
		return new MethodRenderer(access, name, desc, signature, exceptions);
	}

	public void visitOuterClass(
	    final String owner,
	    final String name,
	    final String desc)
	{
		// TODO test outer class
		appendNewline();
    	appendCommentBegin();
    	appendSpace();
    	sb.append(ByteCodeConstants.OUTER_CLASS);
    	appendSpace();
    	appendCommentEnd();
    	appendNewline();
    	
    	appendCommentBegin();
    	appendSpace();
    	//FIXME format the owner
    	sb.append(owner);
    	appendSpace();
    	appendCommentEnd();
    	appendNewline();
    	
    	if (name != null) {
        	appendCommentBegin();
        	appendSpace();
        	sb.append(ByteCodeConstants.IN_METHOD);
        	appendSpace();
        	
        	//FIXME format the name
        	sb.append(name);
        	appendSpace();
        	//FIXME format the descriptor
        	sb.append(desc);
        	appendSpace();
        	appendCommentEnd();
        	appendNewline();
    	}
    	
		if (classVisitor != null) {
			classVisitor.visitOuterClass(owner, name, desc);
		}
	}

	public void visitSource(final String file, final String debugInfo) {
	    if (file != null) {
	    	appendSourcePathComment(file);
	    }
	    if (debugInfo != null) {
	    	appendDebugInfoComment(debugInfo);
	    }
	
		if (classVisitor != null) {
			classVisitor.visitSource(file, debugInfo);
		}

	}

}
