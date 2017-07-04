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
  * Describes an entry in a <code>LineNumberTable</code> attribute structure.
  *
  * @author Sergej Alekseev and Peter Palaga   
  * @version $Revision: 187 $
  * $Id: LineNumberTableEntry.java 187 2013-06-03 05:47:37Z salekseev $
  */
public class LineNumberTableEntry {

    /**
     * Length in bytes of a line number association.
     */
    public static final int LENGTH = 4;

    private int startPc;
    private int lineNumber;

    /**
     * Get the <code>start_pc</code> of this line number association.
     *
     * @return the <code>start_pc</code>
     */
    public int getStartPc() {
        return startPc;
    }

    /**
     * Set the <code>start_pc</code> of this line number association.
     *
     * @param startPc the <code>start_pc</code>
     */
    public void setStartPc(int startPc) {
        this.startPc = startPc;
    }

    /**
     * Get the line number of this line number association.
     *
     * @return the line number
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Set the line number of this line number association.
     *
     * @param lineNumber the line number
     */
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void read(DataInput in) throws IOException {

        startPc = in.readUnsignedShort();
        lineNumber = in.readUnsignedShort();
    }

    public void write(DataOutput out) throws IOException {

        out.writeShort(startPc);
        out.writeShort(lineNumber);
    }

    protected String printAccessFlagsVerbose(int accessFlags) {
        if (accessFlags != 0)
            throw new RuntimeException("Access flags should be zero: " + Integer.toHexString(accessFlags));
        return "";
    }


}
