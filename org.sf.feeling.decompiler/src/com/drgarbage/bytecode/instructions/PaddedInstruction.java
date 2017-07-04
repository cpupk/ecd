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
  *  Base class for intstructions which need a four byte padding relative
  *  to the start of the enclosing code of the parent <code>Code</code>
  *  attribute before reading immediate arguments.
  *
  * @author Sergej Alekseev and Peter Palaga
  *  @version $Revision:395 $
  *  $Id:PaddedInstruction.java 395 2008-04-03 15:08:14Z Peter Palaga $
  */
public class PaddedInstruction extends AbstractInstruction {

    /**
        Constructor.
        @param opcode the opcode.
     */
    public PaddedInstruction(int opcode) {
        super(opcode); 
    }

    /**
        Get the padded size in bytes of this instruction.
        @param offset the offset at which this instruction is found.
        @return the padded size in bytes
     */
    public int getPaddedSize(int offset) {
        return getLength() + paddingBytes(offset + 1);
    }

    public void read(CountedDataInput in) throws IOException {
        super.read(in);
        
        int bytesToRead = paddingBytes(in.getByteCount());
        for (int i = 0; i < bytesToRead; i++) {
            in.readByte();
        }
    }

    public void write(CountedOutput out) throws IOException {
        super.write(out);
        
        int bytesToWrite = paddingBytes(out.getByteCount());
        for (int i = 0; i < bytesToWrite; i++) {
            out.writeByte(0);
        }
    }
    
    private int paddingBytes(int bytesCount) {
        
        int bytesToPad = 4 - bytesCount % 4;
        return (bytesToPad == 4) ? 0 : bytesToPad;
    }
}
