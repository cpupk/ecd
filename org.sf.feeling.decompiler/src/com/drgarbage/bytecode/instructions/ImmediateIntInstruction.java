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
  *  Describes an instruction that is followed by an immediate int.
  *
  * @author Sergej Alekseev and Peter Palaga  
  *  @version $Revision:395 $
  *  $Id:ImmediateIntInstruction.java 395 2008-04-03 15:08:14Z Peter Palaga $
  */
public class ImmediateIntInstruction extends AbstractInstruction {

    private int immediateInt;
   
    /**
        Constructor.
        @param opcode the opcode.
     */
    public ImmediateIntInstruction(int opcode) {
        super(opcode); 
    }

    /**
        Constructor.
        @param opcode the opcode.
        @param immediateInt the immediate int value.
     */
    public ImmediateIntInstruction(int opcode, int immediateInt) {
        super(opcode); 
        this.immediateInt = immediateInt;
    }
    
    public int getLength() {
        return super.getLength() + 4;
    }

    /**
        Get the immediate int of this instruction.
        @return the int
     */
    public int getImmediateInt() {
        return immediateInt;
    }

    /**
        Set the immediate int of this instruction.
        @param immediateInt the int
     */
    public void setImmediateInt(int immediateInt) {
        this.immediateInt = immediateInt;
    }
    
    public void read(CountedDataInput in) throws IOException {
        super.read(in);

        immediateInt = in.readInt();
    }

    public void write(CountedOutput out) throws IOException {
        super.write(out);

        out.writeInt(immediateInt);
    }
    
}
