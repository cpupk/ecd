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

/**
  * Base class for numeric constant pool data structures.
  *
  * @author Sergej Alekseev and Peter Palaga    
  * @version $Revision:395 $
  * $Id:ConstantNumeric.java 395 2008-04-03 15:08:14Z Peter Palaga $
  */
public abstract class ConstantNumeric extends AbstractConstantPoolEntry {

    public ConstantNumeric(byte tag, AbstractConstantPoolEntry[] constantPool) {
		super(tag, constantPool);
	}

	/** Length of the constant pool data structure in bytes. */
    public static final int SIZE = 4;
    
    /** <code>bytes</code> field. */
    protected int bytes;
    
    /**
        Get the <code>bytes</code> field of this constant pool entry.
        @return the <code>bytes</code> field
     */
    public int getBytes() {
        return bytes;
    }

    /**
        Set the <code>bytes</code> field of this constant pool entry.
        @param bytes the <code>bytes</code> field
     */
    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    public void read(DataInput in) throws IOException {
        bytes = in.readInt();
    }
    
    public void write(DataOutput out)
        throws IOException {
        
        out.writeInt(bytes);
    }
    
    public boolean equals(Object object) {
        if (!(object instanceof ConstantNumeric)) {
            return false;
        }
        ConstantNumeric constantNumeric = (ConstantNumeric)object;
        return super.equals(object) && constantNumeric.bytes == bytes;
    }

    public int hashCode() {
        return super.hashCode() ^ bytes;
    }
    
}
