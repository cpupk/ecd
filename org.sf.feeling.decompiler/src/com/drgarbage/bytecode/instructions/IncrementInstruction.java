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
  *  Describes the <code>iinc</code> instruction.
  *
  * @author Sergej Alekseev and Peter Palaga
  *  @version $Revision:81 $
  *  $Id:IncrementInstruction.java 81 2007-05-10 08:38:47Z Peter Palaga $
  */
public class IncrementInstruction extends ImmediateByteInstruction implements ILocalVariableIndexProvider {

    private int incrementConst;
    
    /**
        Constructor.
        @param opcode the opcode
        @param wide whether the instruction is a wide instruction.
     */
    public IncrementInstruction(int opcode, boolean wide) {
        super(opcode, wide); 
    }

    /**
        Constructor.
        @param opcode the opcode
        @param wide whether the instruction is a wide instruction.
        @param immediateByte the immediate byte value.
        @param incrementConst the increment.
     */
    public IncrementInstruction(int opcode, boolean wide, int immediateByte, int incrementConst) {
        super(opcode, wide, immediateByte); 
        this.incrementConst = incrementConst;
    }
    
    
    public int getLength() {
        return super.getLength() + (wide ? 2 : 1);
    }

    /**
        Get the increment of this instruction.
        @return the increment
     */
    public int getIncrementConst() {
        return incrementConst;
    }

    /**
        Set the increment of this instruction.
        @param incrementConst the increment
     */
    public void setIncrementConst(int incrementConst) {
        this.incrementConst = incrementConst;
    }
    
    public void read(CountedDataInput in) throws IOException {
        super.read(in);

        if (wide) {
            incrementConst = in.readShort();
        } else {
            incrementConst = in.readByte();
        }
    }

    public void write(CountedOutput out) throws IOException {
        super.write(out);

        if (wide) {
            out.writeShort(incrementConst);
        } else {
            out.writeByte(incrementConst);
        }
    }

	public int getLocalVariableIndex() {
		return getImmediateByte();
	}
    
}
