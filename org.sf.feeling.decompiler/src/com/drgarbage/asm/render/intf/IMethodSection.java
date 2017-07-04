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
import com.drgarbage.bytecode.ExceptionTableEntry;

/**
 * Representation of a method.
 * 
 * @author Peter Palaga
 * @version $Revision: 516 $
 * $Id: IMethodSection.java 516 2014-02-05 09:17:35Z salekseev $
 */
public interface IMethodSection {

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
	 * Returns 0-based bytecode document line number for the given offset of an instruction.
	 * @param offset offset of an instruction
	 * @return 0-based bytecode document line.
	 */
	public int findOffsetLine(int offset);
	
	/**
	 * Returns 0-based bytecode document line number for the given source code.
	 * @param sourceCodeLine the source code line number
	 * @return the 0-based bytecode document line.
	 */
	public int getBytecodeLine(int sourceCodeLine);
	
	/**
	 * Returns the method descriptor.
	 * @return the method descriptor
	 */
	public String getDescriptor();
	
	/**
	 * Returns 0-based line number.
	 * @return line number
	 */
	public int getFirstLine();

	/**
	 * Returns a {@link List} of {@link IInstructionLine}s.
	 * 
	 * @return {@link List} of {@link IInstructionLine}s 
	 * or <code>null</code> if the method does not have any instructions. 
	 */
	public List<IInstructionLine> getInstructionLines();
	
	/**
	 * Returns 0-based line number.
	 * @return line number
	 */
	public int getLastLine();
	
	/**
	 * Returns the method name.
	 * @return method name
	 */
	public String getName();
	
	/**
	 * Returns 0-based source code line number for the given byte
	 * code document line or {@link ByteCodeConstants#INVALID_OFFSET} if the line info
	 * is unavailable. 
	 * 
	 * @param byteCodeDocLine 0-based line number
	 * @return 0-based source code or {@link ByteCodeConstants#INVALID_OFFSET}
	 */
	public int getSourceCodeLine(int byteCodeDocLine);
	
	/**
	 * Returns a {@link List} of {@link ITryBlock}s.
	 * 
	 * @return {@link List} of {@link ITryBlock}s 
	 * or <code>null</code> if the method does not have any try blocks. 
	 */
	public List<ITryBlock> getTryBlocks();
	
	/**
	 * Returns true if the method has code,
	 * false otherwise. Typically a method
	 * has no code if it is abstract or native.
	 * @return true or false
	 */
	public boolean hasCode();
	
	/**
	 * True if the LineNumberTable is available
	 * otherwise false.
	 * @return true or false
	 */
	public boolean hasLineNumberTable();

	/**
	 * Returns true if the method is abstract,
	 * false otherwise.
	 * 
	 * @return true or false
	 */
	public boolean isAbstract();

	/**
	 * Returns true if the method is a constructor,
	 * false otherwise.
	 * 
	 * @return true or false
	 */
	public boolean isConstructor();
	
	/**
	 * Returns true if the method is native,
	 * false otherwise.
	 * @return true or false
	 */
	public boolean isNative();
	
	/**
	 * Returns the local variable table if exists or null.
	 * @return table or null 
	 */
	public ILocalVariableTable getLocalVariableTable();
	
	/**
	 * Returns true if the local variable table is available
	 * otherwise false.
	 * @return true or false
	 */
	public boolean isLocalVariableTableAvailable();
	
	/**
	 * Returns the value of the <code>max_stack</code> item calculated by the compiler 
	 * and stored in the class file. The <code>max_stack</code> gives the maximum depth 
	 * of the operand stack of this method.
	 * @return The value of the <code>max_stack</code>
	 */
	public int getMaxStack();
	
	/**
	 * Returns the value of the <code>max_locals</code> item calculated by the compiler 
	 * and stored in the class file. The <code>max_locals</code> gives the number of 
	 * local variables in the local variable array allocated upon invocation of this method,
	 * including the local variables used to pass parameters to the method on its invocation.
	 * @return The value of the <code>max_locals</code>
	 */
	public int getMaxLocals();
	
	
	/**
	 * Returns the exception table if exists or null.
	 * @return table or null 
	 */
	public ExceptionTableEntry[] getExceptionTable();
	
	/**
	 * Returns true if the exception table is available
	 * otherwise false.
	 * @return true or false
	 */
	public boolean isExceptionTableAvailable();
	

}
