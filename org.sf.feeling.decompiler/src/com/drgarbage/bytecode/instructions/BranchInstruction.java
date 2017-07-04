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

import com.drgarbage.bytecode.CountedDataInput;
import com.drgarbage.bytecode.CountedOutput;

/**
  *  Describes an instruction that branches to a different offset.
  *
  * @author Sergej Alekseev and Peter Palaga  
  *  @version $Revision:81 $
  *  $Id:BranchInstruction.java 81 2007-05-10 08:38:47Z Peter Palaga $
  */
public class BranchInstruction extends AbstractInstruction {

    protected int branchOffset;

    /**
        Constructor.
        @param opcode the opcode.
     */
    public BranchInstruction(int opcode) {
        super(opcode); 
    }

    /**
        Constructor.
        @param opcode the opcode.
        @param branchOffset the branch offset.
     */
    public BranchInstruction(int opcode, int branchOffset) {
        super(opcode); 
        this.branchOffset = branchOffset;
    }
    
    public int getLength() {
        return super.getLength() + 2;
    }

    /**
        Get the relative offset of the branch of this instruction.
        @return the offset
     */
    public int getBranchOffset() {
        return branchOffset;
    }

    /**
        Set the relative offset of the branch of this instruction.
        @param branchOffset the offset
     */
    public void setBranchOffset(int branchOffset) {
        this.branchOffset = branchOffset;
    }
    
    public void read(CountedDataInput in) throws IOException {
        super.read(in);

        branchOffset = in.readShort();
    }

    public void write(CountedOutput out) throws IOException {
        super.write(out);

        out.writeShort(branchOffset);
    }

}
