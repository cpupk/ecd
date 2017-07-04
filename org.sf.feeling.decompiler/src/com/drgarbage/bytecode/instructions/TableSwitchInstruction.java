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
  *  Describes the <code>tableswitch</code> instruction.
  *
  * @author Sergej Alekseev and Peter Palaga
  *  @version $Revision:81 $
  *  $Id:TableSwitchInstruction.java 81 2007-05-10 08:38:47Z Peter Palaga $
  */
public class TableSwitchInstruction extends PaddedInstruction {

    private int defaultOffset;
    private int low;
    private int high;
    private int[] jumpOffsets;
   
    /**
        Constructor.
        @param opcode the opcode.
     */
    public TableSwitchInstruction(int opcode) {
        super(opcode); 
    }
    
    public int getLength() {
        return super.getLength() + 12 + 4 * jumpOffsets.length;
    }

    /**
        Get the default offset of the branch of this instruction.
        @return the offset
     */
    public int getDefaultOffset() {
        return defaultOffset;
    }

    /**
        Set the default offset of the branch of this instruction.
        @param defaultOffset the offset
     */
    public void setDefaultOffset(int defaultOffset) {
        this.defaultOffset = defaultOffset;
    }
    
    /**
        Get the lower bound for the table switch.
        @return the lower bound
     */
    public int getLow() {
        return low;
    }

    /**
        Set the lower bound for the table switch.
        @param low the lower bound
     */
    public void setLow(int low) {
        this.low = low;
    }
    
    /**
        Get the upper bound for the table switch.
        @return the upper bound
     */
    public int getHigh() {
        return high;
    }

    /**
        Set the upper bound for the table switch.
        @param high the upper bound
     */
    public void setHigh(int high) {
        this.high = high;
    }
    
    /**
        Get the array of relative jump offsets for the table switch.
        @return the array
     */
    public int[] getJumpOffsets() {
        return jumpOffsets;
    }

    /**
        Set the array of relative jump offsets for the table switch.
        @param jumpOffsets the array
     */
    public void setJumpOffsets(int[] jumpOffsets) {
        this.jumpOffsets = jumpOffsets;
    }
    
    public void read(CountedDataInput in) throws IOException {
        super.read(in);

        defaultOffset = in.readInt();
        low = in.readInt();
        high = in.readInt();

        int numberOfOffsets = high - low + 1;
        jumpOffsets = new int[numberOfOffsets];
        
        for (int i = 0; i < numberOfOffsets; i++) {
            jumpOffsets[i] = in.readInt();
        }
        
    }

    public void write(CountedOutput out) throws IOException {
        super.write(out);

        out.writeInt(defaultOffset);
        out.writeInt(low);
        out.writeInt(high);

        int numberOfOffsets = jumpOffsets.length;
        
        for (int i = 0; i < numberOfOffsets; i++) {
            out.writeInt(jumpOffsets[i]);
        }
    }

}
