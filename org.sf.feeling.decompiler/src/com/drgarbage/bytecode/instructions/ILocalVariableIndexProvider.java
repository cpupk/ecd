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

package com.drgarbage.bytecode.instructions;


/**
 * Implemented by instructions, which load from or store into a local variable. 
 * The index can be looked up in the the local variable table (if available), of the 
 * <code>Code</code> attribute.  
 * 
 * @author Peter Palaga
 *
 */
public interface ILocalVariableIndexProvider {
	
	/**
	 * A constant experssing an invalid index which can be returned by {@link getLocalVariableIndex()}.
	 */
	public static final int INVALID_INDEX = -1;
	
	/**
	 * Returns an index of of a local variable in the local variables array.
	 * 
	 * @return An index of the local variables array
	 */
	public int getLocalVariableIndex();
}
