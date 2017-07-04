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

public class BranchWInstruction extends BranchInstruction {

	public BranchWInstruction(int opcode) {
		super(opcode);
	}

	public BranchWInstruction(int opcode, int branchOffset) {
		super(opcode, branchOffset);
	}

	public void read(CountedDataInput in) throws IOException {
	    super.read(in);
	
	    branchOffset = in.readInt();
	}

	public void write(CountedOutput out) throws IOException {
	    super.write(out);
	
	    out.writeInt(branchOffset);
	}
	
	

}
