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

package com.drgarbage.bytecode;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
  * Contains common attributes to a local variable table entry structure.
  *
  * @author Sergej Alekseev and Peter Palaga    
  * @version $Revision: 187 $
  * $Id: LocalVariableCommonEntry.java 187 2013-06-03 05:47:37Z salekseev $
  */
public abstract class LocalVariableCommonEntry {

	/**
     * Length in bytes of a local variable association.
     */
    public static final int LENGTH = 10;

    protected int startPc;
    protected int length;
    protected int nameIndex;
    protected int descriptorOrSignatureIndex;
    protected int index;

    /**
     * Get the <code>start_pc</code> of this local variable association.
     *
     * @return the <code>start_pc</code>
     */
    final public int getStartPc() {
        return startPc;
    }

    /**
     * Set the <code>start_pc</code> of this local variable association.
     *
     * @param startPc the <code>start_pc</code>
     */
    final public void setStartPc(int startPc) {
        this.startPc = startPc;
    }

    /**
     * Get the length in bytes of this local variable association.
     *
     * @return the length
     */
    final public int getLength() {
        return length;
    }

    /**
     * Set the length in bytes of this local variable association.
     *
     * @param length the length
     */
    final public void setLength(int length) {
        this.length = length;
    }

    /**
     * Get the index of the constant pool entry containing the name of this
     * local variable.
     *
     * @return the index
     */
    final public int getNameIndex() {
        return nameIndex;
    }

    /**
     * Set the index of the constant pool entry containing the name of this
     * local variable.
     *
     * @param nameIndex the index
     */
    final public void setNameIndex(int nameIndex) {
        this.nameIndex = nameIndex;
    }

    /**
     * Get the index of the constant pool entry containing the descriptor of this
     * local variable.
     *
     * @return the index
     */
    final public int getDescriptorOrSignatureIndex() {
        return descriptorOrSignatureIndex;
    }

    /**
     * Get the index of the constant pool entry containing the descriptor of this
     * local variable.
     *
     * @param descriptorIndex the index
     */
    final public void setDescriptorOrSignatureIndex(int descriptorIndex) {
        this.descriptorOrSignatureIndex = descriptorIndex;
    }

    /**
     * Get the index of this local variable.
     *
     * @return the index
     */
    final public int getIndex() {
        return index;
    }

    /**
     * Set the index of this local variable.
     * Set the index of this local variable.
     */
    final public void setIndex(int index) {
        this.index = index;
    }

    final public void read(DataInput in) throws IOException {

        startPc = in.readUnsignedShort();
        length = in.readUnsignedShort();
        nameIndex = in.readUnsignedShort();
        descriptorOrSignatureIndex = in.readUnsignedShort();
        index = in.readUnsignedShort();

    }

    final public void write(DataOutput out) throws IOException {

        out.writeShort(startPc);
        out.writeShort(length);
        out.writeShort(nameIndex);
        out.writeShort(descriptorOrSignatureIndex);
        out.writeShort(index);

    }

    protected String printAccessFlagsVerbose(int accessFlags) {
        if (accessFlags != 0)
            throw new RuntimeException("Access flags should be zero: " +
                    Integer.toHexString(accessFlags));
        return "";
    }
}
