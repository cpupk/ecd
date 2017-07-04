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
  * Describes a <code>CONSTANT_NameAndType_info</code> constant pool data structure.
  *
  * @author Sergej Alekseev and Peter Palaga    
  * @version $Revision:395 $
  * $Id:ConstantNameAndTypeInfo.java 395 2008-04-03 15:08:14Z Peter Palaga $
  */
public class ConstantNameAndTypeInfo extends AbstractConstantPoolEntry {

    public ConstantNameAndTypeInfo(AbstractConstantPoolEntry[] constantPool) {
		super(CONSTANT_NAME_AND_TYPE, constantPool);
	}

	/** Length of the constant pool data structure in bytes. */
    public static final int SIZE = 4;
    
    private int nameIndex;
    private int descriptorIndex;
    
    /**
        Get the index of the constant pool entry containing the name of this entry.
        @return the index
     */
    public int getNameIndex() {
        return nameIndex;
    }

    /**
        Set the index of the constant pool entry containing the name of this entry.
        @param nameIndex the index
     */
    public void setNameIndex(int nameIndex) {
        this.nameIndex = nameIndex;
    }

    /**
        Get the index of the constant pool entry containing the descriptor of this entry.
        @return the index
     */
    public int getDescriptorIndex() {
        return descriptorIndex;
    }

    /**
        Set the index of the constant pool entry containing the descriptor of this entry.
        @param descriptorIndex the index
     */
    public void setDescriptorIndex(int descriptorIndex) {
        this.descriptorIndex = descriptorIndex;
    }

    /**
        Get the name.
        @return the name.
     */
    public String getName() {
        return ((ConstantUtf8Info)constantPool[nameIndex]).getString();
    }

    /**
        Get the descriptor string.
        @return the string.
     */
    public String getDescriptor() {
        return ((ConstantUtf8Info)constantPool[descriptorIndex]).getString();
    }

    public void read(DataInput in) throws IOException {
            
        nameIndex = in.readUnsignedShort();
        descriptorIndex = in.readUnsignedShort();
        
    }
    
    public void write(DataOutput out)
        throws IOException {

        out.writeByte(ByteCodeConstants.TAG_NAME_AND_TYPE);
        out.writeShort(nameIndex);
        out.writeShort(descriptorIndex);
    }

    public boolean equals(Object object) {
        if (!(object instanceof ConstantNameAndTypeInfo)) {
            return false;
        }
        ConstantNameAndTypeInfo constantNameAndTypeInfo = (ConstantNameAndTypeInfo)object;
        return super.equals(object) &&
               constantNameAndTypeInfo.nameIndex == nameIndex &&
               constantNameAndTypeInfo.descriptorIndex == descriptorIndex;
    }

    public int hashCode() {
        return super.hashCode() ^ nameIndex ^ descriptorIndex;
    }
    
    @Override
	public String getInfo() {
		return ByteCodeConstants.NAME_INDEX + JavaLexicalConstants.EQUALS +  nameIndex 
		+ JavaLexicalConstants.SEMICOLON + JavaLexicalConstants.SPACE 
		+ ByteCodeConstants.DESCRIPTOR_INDEX + JavaLexicalConstants.EQUALS +  descriptorIndex;
	}

    
}
