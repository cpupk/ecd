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

import java.io.IOException;

import com.drgarbage.bytecode.ByteCodeConstants;
import com.drgarbage.bytecode.CountedDataInput;
import com.drgarbage.bytecode.CountedOutput;

/**
  *  Abstract superclass for all other instructions.
  *
  * @author Sergej Alekseev and Peter Palaga  
  *  @version $Revision:395 $
  *  $Id:AbstractInstruction.java 395 2008-04-03 15:08:14Z Peter Palaga $
  */
public abstract class AbstractInstruction implements Opcodes {

    private int offset;
    private int opcode;
	/**
        Constructor.
        @param opcode the opcode.
     */
    protected AbstractInstruction(int opcode) {
        this.opcode = opcode; 
    }
    
    /**
        Byte length of this instruction.
        @return length in bytes
     */
    public int getLength() {
        return 1;
    }

    /**
        Returns the offset in bytes, actualy the distance 
        from the beginning of the first instruction of the given method.
        @return offset in bytes.
     */
    public int getOffset() {
        return offset;
    }
    
    /**
     * Returns the opcode of this instruction.
     * 
     * @see Opcodes
     * @see ByteCodeConstants#OPCODE_MNEMONICS 
     * @return the opcode
     */
    public int getOpcode() {
        return opcode;
    }
    
    /**
     * Returns the opcode mnemonic of this instruction.
     * @see Opcodes
     * @see ByteCodeConstants#OPCODE_MNEMONICS
     * @return the opcode mnemonic
     */
    public String getOpcodeMnemonic() {
    	return ByteCodeConstants.OPCODE_MNEMONICS[opcode];
    }
    
    /**
        Reads this instruction from a {@link CountedDataInput}.
     	Does not read the {@link #opcode}. The {@link #opcode} should be
     	read in the calling method, just before this instruction is constructed 
     	with {@link #AbstractInstruction(int)}.
        
        @param in an input.
        @throws IOException 
     */
    public void read(CountedDataInput in) throws IOException {
        /* The opcode has already been read 
         * in the calling method and passed in through the 
         * constructor */
        offset = in.getByteCount() - 1;
    }
    
    /**
        Sets the offset of this instruction. 
        This method is thought rather for bytecode instrumentation, 
        e.g. when inserting instructions at positions before this instruction.
        In cases when the instructions are read from some kind of <code>InputStream</code>
        the {@link #offset} attribute is set within the {@link #read(CountedDataInput)} method
           
        @see #getOffset()
        @see #read(CountedDataInput)
        @param offset in bytes.
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
        Writes this instruction into a {@link CountedOutput}.
        @param out an output
        @throws IOException
     */
    public void write(CountedOutput out) throws IOException {
        out.writeByte(opcode);
    }
    
}
