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
  *  Describes the <code>multianewarray</code> instruction.
  *
  * @author Sergej Alekseev and Peter Palaga
  *  @version $Revision:81 $
  *  $Id:MultianewarrayInstruction.java 81 2007-05-10 08:38:47Z Peter Palaga $
  */
public class MultianewarrayInstruction extends ConstantPoolShortIndexInstruction {

    private int dimensions;
    
    /**
        Constructor.
        @param opcode the opcode.
     */
    public MultianewarrayInstruction(int opcode) {
        super(opcode); 
    }
    
    public int getLength() {
        return super.getLength() + 1;
    }

    /**
        Get the number of dimensions for the new array.
        @return the number of dimensions
     */
    public int getDimensions() {
        return dimensions;
    }

    /**
        Set the number of dimensions for the new array.
        @param dimensions the number of dimensions
     */
    public void setDimensions(int dimensions) {
        this.dimensions = dimensions;
    }

    public void read(CountedDataInput in) throws IOException {
        super.read(in);

        dimensions = in.readUnsignedByte();
    }

    public void write(CountedOutput out) throws IOException {
        super.write(out);

        out.writeByte(dimensions);
    }
    
}
