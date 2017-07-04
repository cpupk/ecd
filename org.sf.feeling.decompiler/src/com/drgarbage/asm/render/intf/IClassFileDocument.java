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

package com.drgarbage.asm.render.intf;

import java.util.List;

import com.drgarbage.bytecode.ByteCodeConstants;
import com.drgarbage.bytecode.constant_pool.AbstractConstantPoolEntry;

/**
 * A visualized class file.
 * 
 * @author Peter Palaga
 * @version $Revision: 516 $
 * $Id: IClassFileDocument.java 516 2014-02-05 09:17:35Z salekseev $
 */
public interface IClassFileDocument extends Appendable {
	
	/**
	 * Adds a field Section to the document.
	 * @param f the field section
	 */
	public void addFieldSection(IFieldSection f);

	/**
	 * Appends '\n' to this {@link IClassFileDocument} and increments the internal
	 * line counter.
	 */
	public void appendNewline();

	/**
	 * Decrements the current indentation by 1.
	 */
	public void decrementIndent();

	/**
	 * Returns a {@link IFieldSection} of the given line number
	 * or <code>null</code> if no such exists.
	 * 
 	 * @param line 0-based line number
	 * @return a {@link IFieldSection} of the given line number
	 * or <code>null</code> if no such exists.
	 */
	public IFieldSection findFieldSection(int line);

	/**
	 * Search for a a {@link IFieldSection} by field name. 
	 * @param fieldName field name
	 * @return a {@link IFieldSection} or <code>null</code> if no such exists.
	 */
	public IFieldSection findFieldSection(String fieldName);

	/**
	 * Returns the first instruction of the given source code line
	 * or null if none found.
	 * 
	 * @param sourceCodeLine 0-based line number
	 * @return the instruction line object
	 * @see IInstructionLine
	 */
	public IInstructionLine findInstructionLine(int sourceCodeLine);

	/**
	 * Returns a {@link IMethodSection} containing the given line number
	 * or <code>null</code> if no such exists.
	 * 
 	 * @param line 0-based line number
	 * @return a {@link IMethodSection} containing the given line number
	 * or <code>null</code> if no such exists.
	 */
	public IMethodSection findMethodSection(int line);	

	/**
	 * Search for a a {@link IMethodSection} by method name and method signature.
	 * @param methodName method name
	 * @param methodSignature method signature
	 * @return a {@link IMethodSection} or <code>null</code> if no such exists.
	 */
	public IMethodSection findMethodSection(String methodName, String methodSignature);

	/**
	 * Returns a  fully qualified class name.
	 * @return class name
	 */
	public String getClassName();

	/**
	 * Returns byte code document line number where the class signature begins.
	 * @return byte code document line or {@link ByteCodeConstants#INVALID_OFFSET}
	 */
	public int getClassSignatureDocumentLine();

	/**
	 * Returns a simple unqualified class name.
	 * @return class name
	 */
	public String getClassSimpleName();

	/**
	 * Returns a {@link List} of {@link IFeldSection}s representing
	 * the fields of this {@link IClassFileDocument}.
	 *  
	 * @return {@link List} of {@link IFieldSection}s.
	 */
	public List<IFieldSection> getFieldSections();

	/**
	 * Returns a string used for indentation in this document.
	 * @return an indentation string.
	 */
	public String getIndentationString();
	
	/**
	 * Returns the number of lines in this {@link IClassFileDocument}.
	 * @return th eline count.
	 */
	public int getLineCount();

	/**
	 * Returns a {@link List} of {@link IMethodSection}s representing
	 * the methods of this {@link IClassFileDocument}, excluding <code>abstract</code>
	 * methods.
	 *  
	 * @return {@link List} of {@link IMethodSection}s representing
	 * the methods of this {@link IClassFileDocument}, excluding abstract
	 * methods; or <code>null</code> if this {@link IClassFileDocument} has
	 * no non-abstract methods.
	 */
	public List<IMethodSection> getMethodSections();
	/**
	 * Increments the current indentation by 1.
	 */
	public void incrementIndent();

	/**
	 * Tells if the given line number includes a field declaration. 
	 * The implementations should return quickly, e.g. should use some 
	 * sort of index or similar.
	 * 
	 * @param line 0-based line number
	 * @return <code>true</code> if the given line number includes a field declaration, <code>false</code> otherwise.
	 */
	public boolean isLineInField(int line);
	
	/**
	 * Tells if the given line number is enclosed in some method. 
	 * The implementations should return quickly, e.g. should use some 
	 * sort of index or similar.
	 * 
	 * @param line 0-based line number
	 * @return <code>true</code> if the given line number is enclosed in some method, <code>false</code> otherwise.
	 */
	public boolean isLineInMethod(int line);

	/**
	 * Sets a string used for indentation in this document, 
	 * e.g. "\t", or "    ". This method should be used 
	 * before nay rendering is done; the 
	 * results will not look consistent otherwise.
	 * 
	 * @param indentationString
	 */
	public void setIndentationString(String indentationString);
	
	/**
	 * Returns the constant pool object. 
	 * @return constant pool reference
	 */
	public AbstractConstantPoolEntry[] getConstantPool();

}
