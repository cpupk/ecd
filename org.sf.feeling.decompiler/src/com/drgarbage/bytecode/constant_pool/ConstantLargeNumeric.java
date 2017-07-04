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

/**
  * Base class for large numeric constant pool data structures.
  *
  * @author Sergej Alekseev and Peter Palaga    
  * @version $Revision:395 $
  * $Id:ConstantLargeNumeric.java 395 2008-04-03 15:08:14Z Peter Palaga $
  */
public abstract class ConstantLargeNumeric extends AbstractConstantPoolEntry {

    public ConstantLargeNumeric(byte tag, AbstractConstantPoolEntry[] constantPool) {
		super(tag, constantPool);
	}

	/** Length of the constant pool data structure in bytes. */
    public static final int SIZE = 8;
    
    /** <code>high_bytes</code> field. */
    protected int highBytes;
    /** <code>low_bytes</code> field. */
    protected int lowBytes;
    
    /**
        Get the <code>high_bytes</code> field of this constant pool entry.
        @return the <code>high_bytes</code> field
     */
    public int getHighBytes() {
        return highBytes;
    }

    /**
        Set the <code>high_bytes</code> field of this constant pool entry.
        @param highBytes the <code>high_bytes</code> field
     */
    public void setHighBytes(int highBytes) {
        this.highBytes = highBytes;
    }

    /**
        Get the <code>low_bytes</code> field of this constant pool entry.
        @return the <code>low_bytes</code> field
     */
    public int getLowBytes() {
        return lowBytes;
    }

    /**
        Set the <code>low_bytes</code> field of this constant pool entry.
        @param lowBytes the <code>low_bytes</code> field
     */
    public void setLowBytes(int lowBytes) {
        this.lowBytes = lowBytes;
    }
    
    public void read(DataInput in) throws IOException {
            
        highBytes = in.readInt();
        lowBytes = in.readInt();
    }

    public void write(DataOutput out) throws IOException {
        
        out.writeInt(highBytes);
        out.writeInt(lowBytes);
    }
    
    public boolean equals(Object object) {
        if (!(object instanceof ConstantLargeNumeric)) {
            return false;
        }
        ConstantLargeNumeric constantLargeNumeric = (ConstantLargeNumeric)object;
        return super.equals(object) &&
               constantLargeNumeric.highBytes == highBytes &&
               constantLargeNumeric.lowBytes == lowBytes;
    }

    public int hashCode() {
        return super.hashCode() ^ highBytes ^ lowBytes;
    }
    
}
