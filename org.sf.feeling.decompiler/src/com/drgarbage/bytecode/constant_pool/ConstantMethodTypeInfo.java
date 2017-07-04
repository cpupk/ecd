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

import com.drgarbage.bytecode.ByteCodeConstants;
import com.drgarbage.javasrc.JavaLexicalConstants;

import java.io.*;

/**
  * Describes a <code>CONSTANT_MethodType_info</code> constant pool data structure.
  *
  * @author Sergej Alekseev    
  * @version $Revision: 187 $
  * $Id: ConstantMethodTypeInfo.java 187 2013-06-03 05:47:37Z salekseev $
  */
public class ConstantMethodTypeInfo extends AbstractConstantPoolEntry {

    public ConstantMethodTypeInfo(AbstractConstantPoolEntry[] constantPool) {
		super(CONSTANT_METHOD_TYPE, constantPool);
	}

	/** Length of the constant pool data structure in bytes. */
    public static final int SIZE = 2;
    
    private int descriptorIndex;  
    
	/**
	 * Get the index of the constant pool entry containing the
	 * descriptor of this entry.
	 * @return the index
	 */
    public int getDescriptorIndex() {
		return descriptorIndex;
	}

    /**
	 * Set the index of the constant pool entry containing the
	 * descriptor of this entry.
	 * @param descriptorIndex the index
	 */
	public void setDescriptorIndex(int descriptorIndex) {
		this.descriptorIndex = descriptorIndex;
	}

    public void read(DataInput in) throws IOException {
    	descriptorIndex = in.readUnsignedShort();
    }
    
    public void write(DataOutput out)
        throws IOException {

        out.writeByte(ByteCodeConstants.TAG_STRING);
        out.writeShort(descriptorIndex);
    }

    public boolean equals(Object object) {
        if (!(object instanceof ConstantMethodTypeInfo)) {
            return false;
        }
        ConstantMethodTypeInfo constantMethodTypeInfo = (ConstantMethodTypeInfo)object;
        return super.equals(object) &&
        		constantMethodTypeInfo.descriptorIndex == descriptorIndex;
    }

    public int hashCode() {
        return super.hashCode() ^ descriptorIndex;
    }

	@Override
	public String getInfo() {
		return ByteCodeConstants.DESCRIPTOR_INDEX + JavaLexicalConstants.EQUALS +  descriptorIndex;
	}
    
}
