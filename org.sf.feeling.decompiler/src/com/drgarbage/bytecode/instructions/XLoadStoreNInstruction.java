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


/**
 * Used for 
iload
lload
fload
dload
aload
iload_0
iload_1
iload_2
iload_3
lload_0
lload_1
lload_2
lload_3
fload_0
fload_1
fload_2
fload_3
dload_0
dload_1
dload_2
dload_3
aload_0
aload_1
aload_2
aload_3
iaload
laload
faload
daload
aaload
baload
caload
saload

 * 
 * @author Peter Palaga
 *
 */

public class XLoadStoreNInstruction extends AbstractInstruction implements ILocalVariableIndexProvider {
	
	
	public static class XLoadInstruction extends ImmediateByteInstruction implements ILocalVariableIndexProvider {

		public XLoadInstruction(int opcode, boolean wide, int immediateByte) {
			super(opcode, wide, immediateByte);
		}

		public XLoadInstruction(int opcode, boolean wide) {
			super(opcode, wide);
		}

		public int getLocalVariableIndex() {
			return getImmediateByte();
		}
		
	}
	public static class XStoreInstruction extends ImmediateByteInstruction implements ILocalVariableIndexProvider {

		public XStoreInstruction(int opcode, boolean wide, int immediateByte) {
			super(opcode, wide, immediateByte);
		}

		public XStoreInstruction(int opcode, boolean wide) {
			super(opcode, wide);
		}

		public int getLocalVariableIndex() {
			return getImmediateByte();
		}
		
	}
	public static class XLoadNInstruction extends XLoadStoreNInstruction {

		public XLoadNInstruction(int opcode) {
			super(opcode);
		}

		public XLoadNInstruction(int opcode, int localVariableIndex) {
			super(opcode, localVariableIndex);
		}
		
	}
	public static class XStoreNInstruction extends XLoadStoreNInstruction {

		public XStoreNInstruction(int opcode) {
			super(opcode);
		}

		public XStoreNInstruction(int opcode, int localVariableIndex) {
			super(opcode, localVariableIndex);
		}
		
	}
	
	protected int localVariableIndex = INVALID_INDEX;

	/**
	 * @param opcode
	 */
	public XLoadStoreNInstruction(int opcode) {
		super(opcode);
	}

	public XLoadStoreNInstruction(int opcode, int localVariableIndex) {
		super(opcode);
		this.localVariableIndex = localVariableIndex;
	}
	
	/* (non-Javadoc)
	 * @see com.drgarbage.classfile.opcodes.LocalVariableIndexProvider#getLocalVariableIndex()
	 */
	public int getLocalVariableIndex() {
		return localVariableIndex;
	}

}
