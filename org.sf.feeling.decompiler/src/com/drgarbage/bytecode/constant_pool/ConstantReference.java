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


import java.io.*;

import com.drgarbage.bytecode.ByteCodeConstants;
import com.drgarbage.javasrc.JavaLexicalConstants;

/**
  * Base class for constant pool data structures which reference class members.
  *
  * @author Sergej Alekseev and Peter Palaga    
  * @version $Revision:395 $
  * $Id:ConstantReference.java 395 2008-04-03 15:08:14Z Peter Palaga $
  */
public abstract class ConstantReference extends AbstractConstantPoolEntry {

	public ConstantReference(byte tag, AbstractConstantPoolEntry[] constantPool) {
		super(tag, constantPool);
	}

	/** Length of the constant pool data structure in bytes. */
    public static final int SIZE = 4;

    /** <code>class_index</code> field. */
    protected int classIndex;
    /** <code>name_and_type_index</code> field. */
    protected int nameAndTypeIndex;

    /**
        Get the index of the constant pool entry containing the
        <code>CONSTANT_Class_info</code> of this entry.
        @return the index
     */
    public int getClassIndex() {
        return classIndex;
    }
    
    /**
        Set the index of the constant pool entry containing the
        <code>CONSTANT_Class_info</code> of this entry.
        @param classIndex the index
     */
    public void setClassIndex(int classIndex) {
        this.classIndex = classIndex;
    }
    
    /**
        Get the index of the constant pool entry containing the
         <code>CONSTANT_NameAndType_info</code> of this entry.
        @return the index
     */
    public int getNameAndTypeIndex() {
        return nameAndTypeIndex;
    }

    /**
        Set the index of the constant pool entry containing the
         <code>CONSTANT_NameAndType_info</code> of this entry.
        @param nameAndTypeIndex the index
     */
    public void setNameAndTypeIndex(int nameAndTypeIndex) {
        this.nameAndTypeIndex = nameAndTypeIndex;
    }
    
    public void read(DataInput in) throws IOException {
            
        classIndex = in.readUnsignedShort();
        nameAndTypeIndex = in.readUnsignedShort();
    }
    
    public void write(DataOutput out)
        throws IOException {
        
        out.writeShort(classIndex);
        out.writeShort(nameAndTypeIndex);
    }
    
    public boolean equals(Object object) {
        if (!(object instanceof ConstantReference)) {
            return false;
        }
        ConstantReference constantReference = (ConstantReference)object;
        return super.equals(object) &&
               constantReference.classIndex == classIndex &&
               constantReference.nameAndTypeIndex == nameAndTypeIndex;
    }

    public int hashCode() {
        return super.hashCode() ^ classIndex ^ nameAndTypeIndex;
    }

    @Override
	public String getInfo() {
		return ByteCodeConstants.CLASS_INDEX + JavaLexicalConstants.EQUALS +  classIndex 
		+ JavaLexicalConstants.SEMICOLON + JavaLexicalConstants.SPACE 
		+ ByteCodeConstants.name_and_type_index + JavaLexicalConstants.EQUALS +  nameAndTypeIndex;
	}

}
