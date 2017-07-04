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
  *  Describes the <code>invokeinterface</code> instruction.
  *
  * @author Sergej Alekseev and Peter Palaga
  *  @version $Revision:81 $
  *  $Id:InvokeInterfaceInstruction.java 81 2007-05-10 08:38:47Z Peter Palaga $
  */
public class InvokeInterfaceInstruction extends ImmediateShortInstruction implements IConstantPoolIndexProvider {

    private int count;
    
    /**
        Constructor.
        @param opcode the opcode.
     */
    public InvokeInterfaceInstruction(int opcode) {
        super(opcode); 
    }
    
    /**
        Constructor.
        @param opcode the opcode
        @param immediateShort the immediate short value.
        @param count the argument count.
     */
    public InvokeInterfaceInstruction(int opcode, int immediateShort, int count) {
        super(opcode, immediateShort); 
        this.count = count;
    }
    
    
    public int getLength() {
        return super.getLength() + 2;
    }

    /**
        Get the argument count of this instruction.
        @return the argument count
     */
    public int getCount() {
        return count;
    }

    /**
        Set the argument count of this instruction.
        @param count the argument count
     */
    public void setCount(int count) {
        this.count = count;
    }

    public void read(CountedDataInput in) throws IOException {
        super.read(in);

        count = in.readUnsignedByte();
        // Next byte is always 0 and thus discarded
        in.readByte();
    }

    public void write(CountedOutput out) throws IOException {
        super.write(out);

        out.writeByte(count);
        out.writeByte(0);
    }

	public int getConstantPoolIndex() {
		return getImmediateShort();
	}
    
}
