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


package com.drgarbage.bytecode.constant_pool;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.drgarbage.bytecode.ByteCodeConstants;


/**
  * Base class for all constant pool entries in the <code>constants</code> package.
  *
  * @author Sergej Alekseev and Peter Palaga  
  * @version $Revision:395 $
  * $Id:AbstractConstantPoolEntry.java 395 2008-04-03 15:08:14Z Peter Palaga $
  */
public abstract class AbstractConstantPoolEntry implements ConstantPoolTags {

    protected AbstractConstantPoolEntry[] constantPool;
    protected byte tag;

    public AbstractConstantPoolEntry(byte tag, AbstractConstantPoolEntry[] constantPool) {
		super();
		this.constantPool = constantPool;
		this.tag = tag;
	}

	/**
     * Get the value of the <code>tag</code> field of the <code>cp_info</code> structure.
     *
     * @return the tag
     */
    public final byte getTag() {
    	return tag;
    }

    /**
     * Gets the <code>tag</code> field's mnemonics
     *
     * @return tag mnemonics
     * 
     * @see ByteCodeConstants#CONSTANT_POOL_TAG_MNEMONICS
     */
    public final String getTagMnemonics() {
    	return ByteCodeConstants.CONSTANT_POOL_TAG_MNEMONICS[tag];
    }


    public boolean equals(Object object) {
        return object instanceof AbstractConstantPoolEntry;
    }

    public int hashCode() {
        return 0;
    }

	public abstract void read(DataInput in) throws IOException;
    public abstract void write(DataOutput out) throws IOException;

	public abstract String getInfo();

}
