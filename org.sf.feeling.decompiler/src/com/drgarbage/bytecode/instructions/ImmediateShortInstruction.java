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
  *  Describes an instruction that is followed by an immediate unsigned short.
  *
  * @author Sergej Alekseev and Peter Palaga    
  *  @version $Revision:395 $
  *  $Id:ImmediateShortInstruction.java 395 2008-04-03 15:08:14Z Peter Palaga $
  */
public class ImmediateShortInstruction extends AbstractInstruction {

    private int immediateShort;
   
    public int getLength() {
        return super.getLength() + 2;
    }

    /**
        Constructor.
        @param opcode the opcode.
     */
    public ImmediateShortInstruction(int opcode) {
        super(opcode); 
    }
    
    /**
        Constructor.
        @param opcode the opcode.
        @param immediateShort the immediate short value.
     */
    public ImmediateShortInstruction(int opcode, int immediateShort) {
        super(opcode); 
        this.immediateShort = immediateShort;
    }
    
    /**
        Get the immediate unsigned short of this instruction.
        @return the short
     */
    public int getImmediateShort() {
        return immediateShort;
    }

    /**
        Set the immediate unsigned short of this instruction.
        @param immediateShort the short
     */
    public void setImmediateShort(int immediateShort) {
        this.immediateShort = immediateShort;
    }
    
    public void read(CountedDataInput in) throws IOException {
        super.read(in);

        immediateShort = in.readUnsignedShort();
    }

    public void write(CountedOutput out) throws IOException {
        super.write(out);

        out.writeShort(immediateShort);
    }
    
}
