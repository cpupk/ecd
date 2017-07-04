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
import java.util.ArrayList;
import java.util.List;

/**
  *  Describes the <code>lookupswitch</code> instruction.
  *
  * @author Sergej Alekseev and Peter Palaga
  *  @version $Revision:81 $
  *  $Id:LookupSwitchInstruction.java 81 2007-05-10 08:38:47Z Peter Palaga $
  */
public class LookupSwitchInstruction extends PaddedInstruction {
	
	/**
	  *  An entry of the match-offset table.
	  *
	  */
	public static class MatchOffsetEntry {

	    private int match;
	    private int offset;

	    /**
	     * Constructor.
	     * @param match the match value.
	     * @param offset the branch offset.
	     */
	    public MatchOffsetEntry(int match, int offset) {
	        this.match = match;
	        this.offset = offset;
	    }

	    /**
	        Get the match value of this match-offset pair.
	        @return the value
	     */
	    public int getMatch() {
	        return match;
	    }

	    /**
	        Set the match value of this match-offset pair.
	        @param match the value
	     */
	    public void setMatch(int match) {
	        this.match = match;
	    }

	    /**
	        Get the offset of the branch for this match-offset pair.
	        @return the offset
	     */
	    public int getOffset() {
	        return offset;
	    }

	    /**
	        Set the offset of the branch for this match-offset pair.
	        @param offset the offset
	     */
	    public void setOffset(int offset) {
	        this.offset = offset;
	    }

	}

    private int defaultOffset;
    private List<MatchOffsetEntry> matchOffsetPairs = new ArrayList<MatchOffsetEntry>();
   
    /**
        Constructor.
        @param opcode the opcode.
     */
    public LookupSwitchInstruction(int opcode) {
        super(opcode); 
    }
    
    public int getLength() {
        return super.getLength() + 8 + 8 * matchOffsetPairs.size();
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
        Get the match-offset pairs of the branch of this instruction as
        a <code>java.util.List</code> of <code>MatchOffsetPair</code>
        elements.
        @return the list
     */
    public List<MatchOffsetEntry> getMatchOffsetPairs() {
        return matchOffsetPairs;
    }
    
    /**
        Set the match-offset pairs of the branch of this instruction as
        a <code>java.util.List</code> of <code>LookupSwitchInstruction.MatchOffsetPair</code>
        elements.
        @param matchOffsetPairs the list
     */
    public void setMatchOffsetPairs(List<MatchOffsetEntry> matchOffsetPairs) {
        this.matchOffsetPairs = matchOffsetPairs;
    }

    public void read(CountedDataInput in) throws IOException {
        super.read(in);

        matchOffsetPairs.clear();
        
        defaultOffset = in.readInt();
        int numberOfPairs = in.readInt();
        
        int match, offset;
        for (int i = 0; i < numberOfPairs; i++) {
            match = in.readInt();
            offset = in.readInt();
            
            matchOffsetPairs.add(new MatchOffsetEntry(match, offset));
        }
        
    }

    public void write(CountedOutput out) throws IOException {
        super.write(out);

        out.writeInt(defaultOffset);

        int numberOfPairs = matchOffsetPairs.size();
        out.writeInt(numberOfPairs);
        
        MatchOffsetEntry currentMatchOffsetPair;
        for (int i = 0; i < numberOfPairs; i++) {
            currentMatchOffsetPair = (MatchOffsetEntry)matchOffsetPairs.get(i);
            out.writeInt(currentMatchOffsetPair.getMatch());
            out.writeInt(currentMatchOffsetPair.getOffset());
        }
    }

    
}
