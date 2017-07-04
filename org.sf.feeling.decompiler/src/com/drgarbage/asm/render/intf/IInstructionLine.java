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

import com.drgarbage.bytecode.instructions.AbstractInstruction;

/**
 * Represenation of a line in a method.
 * 
 * @author Peter Palaga
 * @version $Revision:25 $
 * $Id:ByteCodeDocumentProvider.java 25 2007-04-01 17:56:22Z aleks $
 */
public interface IInstructionLine {
	
	/**
	 * A constant used to initialize the implementor's field <code>line</code>.
	 */
	public static final int INVALID_LINE = -1;
	
	/**
	 * Returns the instruction occuring at this line.
	 * 
	 * @return the instruction
	 */
	public AbstractInstruction getInstruction();
	
	
	/**
	 * Returns 0-based line number.
	 * @return line number
	 */
	public int getLine();
}
