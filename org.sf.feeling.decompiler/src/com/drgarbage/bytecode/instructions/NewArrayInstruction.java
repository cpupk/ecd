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

import com.drgarbage.javasrc.JavaKeywords;

public class NewArrayInstruction extends ImmediateByteInstruction {

	public static final int T_BOOLEAN = 4;
	public static final int T_CHAR = 5;
	public static final int T_FLOAT = 6;
	public static final int T_DOUBLE = 7;
	public static final int T_BYTE = 8;
	public static final int T_SHORT = 9;
	public static final int T_INT = 10;
	public static final int T_LONG = 11;
	
	public static final String resolveType(int t) {
        switch (t) {
            case T_BOOLEAN:
                return JavaKeywords.BOOLEAN;
            case T_CHAR:
                return JavaKeywords.CHAR;
            case T_FLOAT:
                return JavaKeywords.FLOAT;
            case T_DOUBLE:
                return JavaKeywords.DOUBLE;
            case T_BYTE:
                return JavaKeywords.BYTE;
            case T_SHORT:
                return JavaKeywords.SHORT;
            case T_INT:
                return JavaKeywords.INT;
            case T_LONG:
                return JavaKeywords.LONG;
            default:
                throw new RuntimeException("invalid type "+ t);
        }
	}

	public NewArrayInstruction(int opcode, boolean wide) {
		super(opcode, wide);
	}

	public NewArrayInstruction(int opcode, boolean wide, int immediateByte) {
		super(opcode, wide, immediateByte);
	}

}
