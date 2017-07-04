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
  * Describes a <code>CONSTANT_MethodHandle_info</code> constant pool data structure.
  *
  * @author Sergej Alekseev    
  * @version $Revision: 187 $
  * $Id: ConstantMethodHandleInfo.java 187 2013-06-03 05:47:37Z salekseev $
  */
public class ConstantMethodHandleInfo extends AbstractConstantPoolEntry {

    public ConstantMethodHandleInfo(AbstractConstantPoolEntry[] constantPool) {
		super(CONSTANT_METHOD_HANDLE, constantPool);
	}

	/** Length of the constant pool data structure in bytes. */
    public static final int SIZE = 3;
    
    private byte referenceKind;
	private int referenceIndex;    
    
    /**
     * Get the value in the range 1 to 9.
     * @return the kind
     */
    public byte getReferenceKind() {
		return referenceKind;
	}

	/**
	 * Set the value in the range 1 to 9. The value denotes
     * the kind of this method handle, which characterizes 
     * its bytecode behavior 
	 * @param referenceKind the kind of the reference
	 */
	public void setReferenceKind(byte referenceKind) {
		this.referenceKind = referenceKind;
	}

	/**
	 * Get the index of the constant pool entry containing the
	 * reference of this entry.
	 * @return the index
	 */
	public int getReferenceIndex() {
		return referenceIndex;
	}

	/**
	 * Set the index of the constant pool entry containing the
	 * reference of this entry.
	 * @param referenceIndex the index
	 */
	public void setReferenceIndex(int referenceIndex) {
		this.referenceIndex = referenceIndex;
	}

    public void read(DataInput in) throws IOException {            
    	referenceKind = in.readByte();
    	referenceIndex = in.readUnsignedShort();
    }
    
    public void write(DataOutput out)
        throws IOException {

        out.writeByte(ByteCodeConstants.TAG_STRING);
        out.writeByte(referenceKind);
        out.writeShort(referenceIndex);
    }

    public boolean equals(Object object) {
        if (!(object instanceof ConstantMethodHandleInfo)) {
            return false;
        }
        ConstantMethodHandleInfo constantMethodHandleInfo = (ConstantMethodHandleInfo)object;
        return super.equals(object) &&
        		constantMethodHandleInfo.referenceKind == referenceKind &&
        		constantMethodHandleInfo.referenceIndex == referenceIndex;
    }

    public int hashCode() {
        return super.hashCode() ^ referenceKind ^ referenceIndex;
    }

	@Override
	public String getInfo() {
		return ByteCodeConstants.METHOD_HANDLE_REF_KIND + JavaLexicalConstants.EQUALS +  referenceKind 
				+ JavaLexicalConstants.SEMICOLON + JavaLexicalConstants.SPACE 
				+ ByteCodeConstants.METHOD_HANDLE_INDEX + JavaLexicalConstants.EQUALS +  referenceIndex;
	}
    
}
