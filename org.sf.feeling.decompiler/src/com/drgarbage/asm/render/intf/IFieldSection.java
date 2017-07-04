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

import com.drgarbage.bytecode.ByteCodeConstants;

/**
 * Representation of a filed.
 * 
 * @author Sergej Alekseev
 * @version $Revision: 187 $
 * $Id: IFieldSection.java 187 2013-06-03 05:47:37Z salekseev $
 */
public interface IFieldSection {
	
	/**
	 * Returns the simple name of this field.
	 * @return the simple name of this field.
	 */
	public String getName();
	
	/**
	 * Returns the type signature of this field. For enum constants,
	 * this returns the signature of the declaring enum class.
	 * <p>
	 * @return the type signature of this field
	 */
	public String getDescriptor();

	/**
	 * Returns byte code document line number.
	 * @return byte code document line or {@link ByteCodeConstants#INVALID_OFFSET}
	 */
	public int getBytecodeDocumentLine();

}
