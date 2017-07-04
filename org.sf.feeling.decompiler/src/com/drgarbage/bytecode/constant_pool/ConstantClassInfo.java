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
  * Describes a <code>CONSTANT_Class_info</code> constant pool data structure.
  *
  * @author Sergej Alekseev and Peter Palaga    
  * @version $Revision:395 $
  * $Id:ConstantClassInfo.java 395 2008-04-03 15:08:14Z Peter Palaga $
  */
public class ConstantClassInfo extends AbstractConstantPoolEntry {

    public ConstantClassInfo(AbstractConstantPoolEntry[] constantPool) {
		super(CONSTANT_CLASS, constantPool);
	}

	/** Length of the constant pool data structure in bytes. */
    public static final int SIZE = 2;
    
    private int nameIndex;
    
    /**
        Get the index of the constant pool entry containing the name of the class.
        @return the index
     */
    public int getNameIndex() {
        return nameIndex;
    }

    /**
        Set the index of the constant pool entry containing the name of the class.
        @param nameIndex the index
     */
    public void setNameIndex(int nameIndex) {
        this.nameIndex = nameIndex;
    }

    /**
        Get the name of the class.
        @return name of the class
     */
    public String getName() {
        return ((ConstantUtf8Info)constantPool[nameIndex]).getString();
    }

    public void read(DataInput in) throws IOException {
            
        nameIndex = in.readUnsignedShort();
    }

    public void write(DataOutput out) throws IOException {
        
        out.writeByte(ByteCodeConstants.TAG_CLASS);
        out.writeShort(nameIndex);
    }
    
    public boolean equals(Object object) {
        if (!(object instanceof ConstantClassInfo)) {
            return false;
        }
        ConstantClassInfo constantClassInfo = (ConstantClassInfo)object;
        return super.equals(object) && constantClassInfo.nameIndex == nameIndex;
    }

    public int hashCode() {
        return super.hashCode() ^ nameIndex;
    }

	@Override
	public String getInfo() {
		return ByteCodeConstants.NAME_INDEX + JavaLexicalConstants.EQUALS +  String.valueOf(nameIndex);
	}
    
}
