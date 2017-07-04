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
  * Describes an exception table entry in a <code>Code</code> attribute structure.
  *
  * @author Sergej Alekseev and Peter Palaga    
  * @version $Revision: 187 $
  * $Id: ExceptionTableEntry.java 187 2013-06-03 05:47:37Z salekseev $
  */
public class ExceptionTableEntry {

    /**
     * Length in bytes of an exception table entry.
     */
    public static final int LENGTH = 8;

    private int startPc;
    private int endPc;
    private int handlerPc;
    private int catchType;

    /**
     * Constructor.
     */
    public ExceptionTableEntry() {
    }

    /**
     * Constructor.
     *
     * @param startPc   the <code>start_pc</code>
     * @param endPc     the <code>end_pc</code>
     * @param handlerPc the <code>handler_pc</code>
     * @param catchType the constant pool index for the catch type of this exception table entry
     */
    public ExceptionTableEntry(int startPc, int endPc, int handlerPc, int catchType) {
        this.startPc = startPc;
        this.endPc = endPc;
        this.handlerPc = handlerPc;
        this.catchType = catchType;
    }

    /**
     * Get the <code>start_pc</code> of this exception table entry.
     *
     * @return the <code>start_pc</code>
     */
    public int getStartPc() {
        return startPc;
    }

    /**
     * Set the <code>start_pc</code> of this exception table entry.
     *
     * @param startPc the <code>start_pc</code>
     */
    public void setStartPc(int startPc) {
        this.startPc = startPc;
    }

    /**
     * Get the <code>end_pc</code> of this exception table entry.
     *
     * @return the <code>end_pc</code>
     */
    public int getEndPc() {
        return endPc;
    }

    /**
     * Set the <code>end_pc</code> of this exception table entry.
     *
     * @param endPc the <code>end_pc</code>
     */
    public void setEndPc(int endPc) {
        this.endPc = endPc;
    }

    /**
     * Get the <code>handler_pc</code> of this exception table entry.
     *
     * @return the <code>handler_pc</code>
     */
    public int getHandlerPc() {
        return handlerPc;
    }

    /**
     * Set the <code>handler_pc</code> of this exception table entry.
     *
     * @param handlerPc the <code>handler_pc</code>
     */
    public void setHandlerPc(int handlerPc) {
        this.handlerPc = handlerPc;
    }

    /**
     * Get the constant pool index for the catch type of this exception table entry.
     *
     * @return the index
     */
    public int getCatchType() {
        return catchType;
    }

    /**
     * Set the constant pool index for the catch type of this exception table entry.
     *
     * @param catchType the index
     */
    public void setCatchType(int catchType) {
        this.catchType = catchType;
    }

    public void read(DataInput in) throws IOException {

        startPc = in.readUnsignedShort();
        endPc = in.readUnsignedShort();
        handlerPc = in.readUnsignedShort();
        catchType = in.readUnsignedShort();
    }

    public void write(DataOutput out) throws IOException {

        out.writeShort(startPc);
        out.writeShort(endPc);
        out.writeShort(handlerPc);
        out.writeShort(catchType);
    }

    protected String printAccessFlagsVerbose(int accessFlags) {
        if (accessFlags != 0)
            throw new RuntimeException("Access flags should be zero: " + Integer.toHexString(accessFlags));
        return "";
    }

}
