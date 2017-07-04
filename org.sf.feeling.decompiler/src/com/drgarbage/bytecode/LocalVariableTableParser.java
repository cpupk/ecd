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

package com.drgarbage.bytecode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.drgarbage.bytecode.instructions.AbstractInstruction;

/**
 * Converts code to a list of instructions.
 * 
 * @author Sergej Alekseev and Peter Palaga
 * @version $Revision: 187 $ $Id: InstructionParser.java 164 2008-01-06
 *          13:00:25Z sa $
 */
public class LocalVariableTableParser {

	/**
	 * The input stream
	 */
	private CountedDataInputStream in;

	/**
	 * NUmebr of byte to read from {@link #in}.
	 */
	private int length;

	/**
	 * @param code
	 *            a byte array containing instruction.
	 * @param offset
	 *            number of bytes from the beginning of the byte-array where the
	 *            parsing should start.
	 * @param length
	 *            number of byte to parse.
	 */
	public LocalVariableTableParser(byte[] code, int offset, int length) {
		super();
		this.length = length;
		InputStream tmp = new ByteArrayInputStream(code);
		if (offset != 0) {
			/*
			 * only risk that the stream does not support seek if there is
			 * something to skip
			 */
			try {
				/* skip the bytes we do not want to count */
				tmp.skip(offset);
			} catch (IOException e) {
				/* should not happen with ByteArrayInputStream */
				throw new RuntimeException(e);
			}
		}
		in = new CountedDataInputStream(tmp);
	}

	/**
	 * @param code
	 */
	public LocalVariableTableParser(byte[] code) {
		this(code, 0, code.length);
	}

	/**
	 * Parses the underlying byte array int a {@link List} of
	 * {@link AbstractInstruction}s.
	 * 
	 * @return the <code>java.util.List</code> with the instructions
	 */
	public LocalVariableTableEntry[] parse() {
		LocalVariableTableEntry[] result = new LocalVariableTableEntry[length];

		try {
			for (int i = 0; i < length; i++) {
				LocalVariableTableEntry entry = new LocalVariableTableEntry();
				entry.read(in);
				result[i] = entry;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return result;
	}
}
