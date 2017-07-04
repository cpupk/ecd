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
  * Describes a <code>CONSTANT_InvokeDynamic_info</code> constant pool data structure.
  *
  * @author Sergej Alekseev    
  * @version $Revision: 187 $
  * $Id: ConstantInvokeDynamicInfo.java 187 2013-06-03 05:47:37Z salekseev $
  */
public class ConstantInvokeDynamicInfo extends AbstractConstantPoolEntry {

    public ConstantInvokeDynamicInfo(AbstractConstantPoolEntry[] constantPool) {
		super(CONSTANT_INVOKE_DYNAMIC, constantPool);
	}

	/** Length of the constant pool data structure in bytes. */
    public static final int SIZE = 4;
    
    private int bootstrapMethodAttrIndex;
	private int nameAndTypeIndex;    
    
	/**
	 * Get the index of the constant pool entry containing the
	 * bootstrapMethodAttr of this entry.
	 * @return the index
	 */
    public int getBootstrapMethodAttrIndex() {
		return bootstrapMethodAttrIndex;
	}

	/**
	 * Set the index of the constant pool entry containing the
	 * bootstrapMethodAttr of this entry.
	 * @param bootstrapMethodAttrIndex the index
	 */
	public void setBootstrapMethodAttrIndex(int bootstrapMethodAttrIndex) {
		this.bootstrapMethodAttrIndex = bootstrapMethodAttrIndex;
	}

	/**
	 * Get the index of the constant pool entry containing the
	 * nameAndType of this entry.
	 * @return the index
	 */
	public int getNameAndTypeIndex() {
		return nameAndTypeIndex;
	}


	/**
	 * Set the index of the constant pool entry containing the
	 * nameAndType of this entry.
	 * @param nameAndTypeIndex the index
	 */
	public void setNameAndTypeIndex(int nameAndTypeIndex) {
		this.nameAndTypeIndex = nameAndTypeIndex;
	}

    public void read(DataInput in) throws IOException {            
    	bootstrapMethodAttrIndex = in.readUnsignedShort();
    	nameAndTypeIndex = in.readUnsignedShort();
    }
    
    public void write(DataOutput out)
        throws IOException {

        out.writeByte(ByteCodeConstants.TAG_STRING);
        out.writeShort(bootstrapMethodAttrIndex);
        out.writeShort(nameAndTypeIndex);
    }

    public boolean equals(Object object) {
        if (!(object instanceof ConstantInvokeDynamicInfo)) {
            return false;
        }
        ConstantInvokeDynamicInfo constantInvokeDynamicInfo = (ConstantInvokeDynamicInfo)object;
        return super.equals(object) &&
        		constantInvokeDynamicInfo.bootstrapMethodAttrIndex == bootstrapMethodAttrIndex &&
        		constantInvokeDynamicInfo.nameAndTypeIndex == nameAndTypeIndex;
    }

    public int hashCode() {
        return super.hashCode() ^ bootstrapMethodAttrIndex ^ nameAndTypeIndex;
    }

	@Override
	public String getInfo() {
		return ByteCodeConstants.INVOKE_DYNAMIC_BOOTSTRAP_METHOD_ATTR_INDEX + JavaLexicalConstants.EQUALS +  bootstrapMethodAttrIndex 
				+ JavaLexicalConstants.SEMICOLON + JavaLexicalConstants.SPACE 
				+ ByteCodeConstants.INVOKE_DYNAMIC_NAME_AND_TYPE_INDEX + JavaLexicalConstants.EQUALS +  nameAndTypeIndex;
	}
    
}
