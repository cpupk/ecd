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

package com.drgarbage.bytecode;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import com.drgarbage.asm.MethodVisitor;
import com.drgarbage.asm.Opcodes;
import com.drgarbage.asm.render.intf.ILocalVariableTable;
import com.drgarbage.bytecode.constant_pool.AbstractConstantPoolEntry;
import com.drgarbage.bytecode.constant_pool.ConstantClassInfo;
import com.drgarbage.bytecode.constant_pool.ConstantUtf8Info;
import com.drgarbage.javasrc.JavaKeywords;
import com.drgarbage.javasrc.JavaLexicalConstants;

public class BytecodeUtils {
	public static int getMinor(int classFileFormatVersion) {
		return classFileFormatVersion >>> 16;
	}

	public static int getMajor(int classFileFormatVersion) {
		return classFileFormatVersion & 0xFFFF;
	}

	public static String getLowestJavaPlatformVersion(int major, int minor) {
		if (major < 45) {
			return null;
		} else if (major == 45 && minor >= 0 && minor <= 3) {
			return "1.0.2";
		} else if (major == 45 && minor >= 0 && minor <= 65535) {
			return "1.1";
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("1.");
			sb.append(major - 44);
			return sb.toString();
		}
	}

	public static String toJavaSrcName(String byteCodeName) {
		return byteCodeName.replace(ByteCodeConstants.CLASS_NAME_SLASH,
				JavaLexicalConstants.DOT).replace(
				ByteCodeConstants.CLASS_NAME_DOLLAR, JavaLexicalConstants.DOT);
	}

	public static String getSimpleName(String byteCodeName) {
		if (byteCodeName != null) {
			int i = byteCodeName
					.lastIndexOf(ByteCodeConstants.CLASS_NAME_SLASH);
			if (i >= 0) {
				return byteCodeName.substring(i + 1);
			}
		}
		return byteCodeName;
	}

	public static String getPackage(String thisClassName) {
		if (thisClassName != null) {
			int i = thisClassName
					.lastIndexOf(ByteCodeConstants.CLASS_NAME_SLASH);
			if (i >= 0) {
				return thisClassName.substring(0, i);
			}
		}
		return null;
	}

	/**
	 * @see MethodVisitor#visitIntInsn(int, int)
	 * 
	 * @param operand
	 * @return java keyword as a string
	 */
	public static String getJavaTypeName(int operand) {
		switch (operand) {
		case Opcodes.T_BOOLEAN:
			return JavaKeywords.BOOLEAN;
		case Opcodes.T_CHAR:
			return JavaKeywords.CHAR;
		case Opcodes.T_FLOAT:
			return JavaKeywords.FLOAT;
		case Opcodes.T_DOUBLE:
			return JavaKeywords.DOUBLE;
		case Opcodes.T_BYTE:
			return JavaKeywords.BYTE;
		case Opcodes.T_SHORT:
			return JavaKeywords.SHORT;
		case Opcodes.T_INT:
			return JavaKeywords.INT;
		case Opcodes.T_LONG:
			return JavaKeywords.LONG;
		default:
			throw new IllegalArgumentException("Unexpected option '" + operand
					+ "';");
		}
	}

	public static int appendMethodDescriptor(String methodName,
			boolean isConstructor, boolean isStatic, String descr, int offset,
			ILocalVariableTable localVariableTable,
			AbstractConstantPoolEntry[] constantPool, Appendable doc)
			throws IOException {

		/*
		 * parse the method arguments first and save them in a temporarary
		 * StringBuilder
		 */
		StringBuilder sb = new StringBuilder();
		int i = offset;
		switch (descr.charAt(i++)) {
		case ByteCodeConstants.METHOD_DESCRIPTOR_LEFT_PARENTHESIS:
			break;
		default:
			i--;
			throw new IllegalArgumentException(
					"could not parse the descriptor '" + descr
							+ "'. '(' expected, found '" + descr.charAt(i)
							+ "' at the position " + i + ".");
		}
		int argi = 0;
		while ((descr.charAt(i)) != ByteCodeConstants.METHOD_DESCRIPTOR_RIGHT_PARENTHESIS) {
			if (argi != 0) {
				sb.append(JavaLexicalConstants.COMMA);
				sb.append(JavaLexicalConstants.SPACE);
			}
			i = appendFieldDescriptor(descr, i, sb);
			sb.append(JavaLexicalConstants.SPACE);

			String argName = null;

			if (localVariableTable != null) {
				argName = localVariableTable.findArgName(isStatic ? argi
						: argi + 1, 0, isConstructor, isStatic);
			}
			if (argName != null) {
				sb.append(argName);
			} else {
				sb.append(ByteCodeConstants.ARG);
				sb.append(argi);
			}
			argi++;
		}
		/* consume the right parenthesis */
		i++;

		/*
		 * now parse the return type; and append it directly to the doc
		 */
		if (!isConstructor) {
			/* constructors do not have return types */
			i = appendFieldDescriptor(descr, i, doc);
			doc.append(JavaLexicalConstants.SPACE);
		}

		doc.append(methodName);
		doc.append(JavaLexicalConstants.LEFT_PARENTHESIS);

		/* append the arguments stored in the sb */
		doc.append(sb.toString());
		doc.append(JavaLexicalConstants.RIGHT_PARENTHESIS);

		return i;
	}

	public static int appendArrayDescriptor(String descr, int offset,
			Appendable doc) throws IOException {
		int i = offset;
		switch (descr.charAt(i++)) {
		case ByteCodeConstants.ARRAY_BEGINNING_BRACKET:
			int dims = 1;
			while (descr.charAt(i) == ByteCodeConstants.ARRAY_BEGINNING_BRACKET) {
				i++;
				dims++;
			}
			i = appendFieldDescriptor(descr, i, doc);
			for (int j = 0; j < dims; j++) {
				doc.append(JavaLexicalConstants.LEFT_SQUARE_BRACKET);
				doc.append(JavaLexicalConstants.RIGHT_SQUARE_BRACKET);
			}
			break;
		default:
			i--;
			throw new IllegalArgumentException(
					"could not parse the descriptor '" + descr
							+ "'. Unexpected character '" + descr.charAt(i)
							+ "' at the position " + i);
		}

		return i;
	}

	public static int appendFieldDescriptor(String descr, int offset,
			Appendable doc) throws IOException {

		int i = offset;
		switch (descr.charAt(i++)) {
		case ByteCodeConstants.ARRAY_BEGINNING_BRACKET:
			i = appendArrayDescriptor(descr, i - 1, doc);
			break;
		case ByteCodeConstants.L_REFERENCE:
			char c;
			while ((c = descr.charAt(i++)) != ByteCodeConstants.REFERENCE_TERMINATING_SEMICOLON) {
				if (c == ByteCodeConstants.CLASS_NAME_SLASH) {
					doc.append(JavaLexicalConstants.DOT);
				} else {
					doc.append(c);
				}
			}
			break;
		case ByteCodeConstants.B_BYTE:
			doc.append(JavaKeywords.BYTE);
			break;
		case ByteCodeConstants.C_CHAR:
			doc.append(JavaKeywords.CHAR);
			break;
		case ByteCodeConstants.D_DOUBLE:
			doc.append(JavaKeywords.DOUBLE);
			break;
		case ByteCodeConstants.F_FLOAT:
			doc.append(JavaKeywords.FLOAT);
			break;
		case ByteCodeConstants.I_INT:
			doc.append(JavaKeywords.INT);
			break;
		case ByteCodeConstants.J_LONG:
			doc.append(JavaKeywords.LONG);
			break;
		case ByteCodeConstants.S_SHORT:
			doc.append(JavaKeywords.SHORT);
			break;
		case ByteCodeConstants.Z_BOOLEAN:
			doc.append(JavaKeywords.BOOLEAN);
			break;
		case ByteCodeConstants.V_VOID:
			doc.append(JavaKeywords.VOID);
			break;
		default:
			i--;
			throw new IllegalArgumentException(
					"could not parse the descriptor '" + descr
							+ "'. Unexpected character '" + descr.charAt(i)
							+ "' at the position " + i);
		}

		return i;

	}

	public static void padd(String s, int length, char ch, Appendable sb)
			throws IOException {
		int toPadd = length - s.length();
		while (toPadd-- > 0) {
			sb.append(ch);
		}
		sb.append(s);
	}

	/**
	 * Appends a quoted string to a given buffer.
	 * 
	 * @param buf
	 *            the buffer where the string must be added.
	 * @param s
	 *            the string to be added.
	 */
	public static void appendString(final StringBuffer buf, final String s) {
		buf.append(JavaLexicalConstants.QUOTE);
		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if (c == '\n') {
				buf.append("\\n");
			} else if (c == '\r') {
				buf.append("\\r");
			} else if (c == '\\') {
				buf.append("\\\\");
			} else if (c == '"') {
				buf.append("\\\"");
			} else if (c < 0x20 || c > 0x7f) {
				buf.append("\\u");
				if (c < 0x10) {
					buf.append("000");
				} else if (c < 0x100) {
					buf.append("00");
				} else if (c < 0x1000) {
					buf.append('0');
				}
				buf.append(Integer.toString(c, 16));
			} else {
				buf.append(c);
			}
		}
		buf.append(JavaLexicalConstants.QUOTE);
	}
	
	public static boolean isSourceOf(IResource srcFile, IClassFile classFile) {
		
		if (srcFile == null || classFile == null) {
			return false;
		}
		
		IResource resource = classFile.getResource();
		if(resource == null){
			return false;
		}

		String classFileTypeName = toFullyQualifiedTypeName(resource);
		
		if (classFileTypeName == null) {
			return false;
		}
		
		String srcPath = srcFile.getFullPath().removeFileExtension().toString();
		srcPath = toJavaSrcName(srcPath);
		
		return srcPath.endsWith(classFileTypeName);
	}

	/**
	 * Returns the fully qualified type name for a given {@link IResource}. 
	 * For resources which are not included in the project's build path 
	 * returns always <code>null</code>.
	 * 
	 * @param classFileResource
	 * @return the fully qualified name as a string or <code>null</code>
	 */
	public static String toFullyQualifiedTypeName(IResource classFileResource) {
		IProject project = classFileResource.getProject();

		/* create java project */
		IJavaProject javaProject = JavaCore.create(project);

		IPath wspacePath = classFileResource.getFullPath();
		IPath outputDir = null;
		
		try {
			outputDir = javaProject.getOutputLocation();
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}

		IPath classToFind = null;
		if (outputDir.matchingFirstSegments(wspacePath) == outputDir
				.segmentCount()) {
			/*
			 * if we are in the output directory strip the output directory as
			 * we want the project relative path
			 */
			classToFind = wspacePath.removeFirstSegments(outputDir
					.segmentCount());
		}

		if (classToFind != null) {
			classToFind = classToFind.removeFileExtension();
			String typeToFind = classToFind.toString();

			typeToFind = toJavaSrcName(typeToFind);
			
			return typeToFind;

		}
		return null;
	}

	/**
	 * returns the type name of the given {@link ConstantClassInfo}
	 * @param constantClassInfo the {@link ConstantClassInfo} to check
	 * @param constantPool the associated ConstantPool
	 * @return type name as a string
	 */
	public static String resolveConstantPoolTypeName(
			ConstantClassInfo constantClassInfo,
			AbstractConstantPoolEntry[] constantPool) {
		
		return ((ConstantUtf8Info)constantPool[constantClassInfo.getNameIndex()]).getString();
	}

}
