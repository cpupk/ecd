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


package com.drgarbage.bytecode.instructions;

import com.drgarbage.bytecode.CountedDataInput;
import com.drgarbage.bytecode.CountedOutput;

import java.io.IOException;

/**
  *  Describes an instruction that is followed by an immediate unsigned byte.
  *
  * @author Sergej Alekseev and Peter Palaga  
  *  @version $Revision:395 $
  *  $Id:ImmediateByteInstruction.java 395 2008-04-03 15:08:14Z Peter Palaga $
  */
public class ImmediateByteInstruction extends AbstractInstruction {

    /** Indicates whether the instuction is subject to a wide instruction or not. */
    protected boolean wide;
    
    private int immediateByte;

    /**
        Constructor.
        @param opcode the opcode
        @param wide whether the instruction is a wide instruction.
     */
    public ImmediateByteInstruction(int opcode, boolean wide) {
        super(opcode); 
        this.wide = wide;
    }

    /**
        Constructor.
        @param opcode the opcode
        @param wide whether the instruction is a wide instruction.
        @param immediateByte the immediate byte value.
     */
    public ImmediateByteInstruction(int opcode, boolean wide, int immediateByte) {
        this(opcode, wide); 
        this.immediateByte = immediateByte;
    }
    
    public int getLength() {
        return super.getLength() + (wide ? 2 : 1);
    }

    /**
        Get the immediate unsigned byte of this instruction.
        @return the byte
     */
    public int getImmediateByte() {
        return immediateByte;
    }

    /**
        Set the immediate unsigned byte of this instruction.
        @param immediateByte the byte
     */
     public void setImmediateByte(int immediateByte) {
        this.immediateByte = immediateByte;
    }
    
    /**
        Check whether the instuction is subject to a wide instruction or not.
        @return wide or not
     */
    public boolean isWide() {
        return wide;
    }
    
    /**
        Set whether the instuction is subject to a wide instruction or not.
        @param wide wide or not
     */
    public void setWide(boolean wide) {
        this.wide = wide;
    }
    
    public void read(CountedDataInput in) throws IOException {
        super.read(in);

        if (wide) {
            immediateByte = in.readUnsignedShort();
        } else {
            immediateByte = in.readUnsignedByte();
        }
    }

    public void write(CountedOutput out) throws IOException {
        super.write(out);

        if (wide) {
            out.writeShort(immediateByte);
        } else {
            out.writeByte(immediateByte);
        }
    }
    
}
