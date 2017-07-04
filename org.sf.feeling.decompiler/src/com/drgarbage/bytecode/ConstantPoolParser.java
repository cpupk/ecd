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
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.drgarbage.bytecode.constant_pool.AbstractConstantPoolEntry;
import com.drgarbage.bytecode.constant_pool.ConstantClassInfo;
import com.drgarbage.bytecode.constant_pool.ConstantDoubleInfo;
import com.drgarbage.bytecode.constant_pool.ConstantFieldrefInfo;
import com.drgarbage.bytecode.constant_pool.ConstantFloatInfo;
import com.drgarbage.bytecode.constant_pool.ConstantIntegerInfo;
import com.drgarbage.bytecode.constant_pool.ConstantInterfaceMethodrefInfo;
import com.drgarbage.bytecode.constant_pool.ConstantInvokeDynamicInfo;
import com.drgarbage.bytecode.constant_pool.ConstantLargeNumeric;
import com.drgarbage.bytecode.constant_pool.ConstantLongInfo;
import com.drgarbage.bytecode.constant_pool.ConstantMethodHandleInfo;
import com.drgarbage.bytecode.constant_pool.ConstantMethodTypeInfo;
import com.drgarbage.bytecode.constant_pool.ConstantMethodrefInfo;
import com.drgarbage.bytecode.constant_pool.ConstantNameAndTypeInfo;
import com.drgarbage.bytecode.constant_pool.ConstantStringInfo;
import com.drgarbage.bytecode.constant_pool.ConstantUtf8Info;
import com.drgarbage.bytecode.instructions.AbstractInstruction;

/**
 * Converts code to a list of instructions.
 * 
 * @author Sergej Alekseev and Peter Palaga
 * @version $Revision: 187 $ $Id: InstructionParser.java 164 2008-01-06
 *          13:00:25Z sa $
 */
public class ConstantPoolParser {

	/**
	 * The input stream
	 */
	private DataInputStream in;

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
	public ConstantPoolParser(byte[] code, int offset, int length) {
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
		in = new DataInputStream(tmp);
	}

	/**
	 * @param code
	 */
	public ConstantPoolParser(byte[] code) {
		this(code, 0, code.length);
	}

	/**
	 * Parses the underlying byte array int a {@link List} of
	 * {@link AbstractInstruction}s.
	 * 
	 * @return the <code>java.util.List</code> with the instructions
	 */
	public AbstractConstantPoolEntry[] parse() {
		AbstractConstantPoolEntry[] result = new AbstractConstantPoolEntry[length];

		try {
			for (int i = 1; i < length; i++) {
				AbstractConstantPoolEntry entry;

				int int_ = in.readUnsignedByte();
				byte tag = (byte)int_;

				switch (tag) {
				case ByteCodeConstants.TAG_CLASS:
					entry = new ConstantClassInfo(result);
					break;
				case ByteCodeConstants.TAG_FIELDREF:
					entry = new ConstantFieldrefInfo(result);
					break;
				case ByteCodeConstants.TAG_METHODREF:
					entry = new ConstantMethodrefInfo(result);
					break;
				case ByteCodeConstants.TAG_INTERFACE_METHODREF:
					entry = new ConstantInterfaceMethodrefInfo(result);
					break;
				case ByteCodeConstants.TAG_STRING:
					entry = new ConstantStringInfo(result);
					break;
				case ByteCodeConstants.TAG_INTEGER:
					entry = new ConstantIntegerInfo(result);
					break;
				case ByteCodeConstants.TAG_FLOAT:
					entry = new ConstantFloatInfo(result);
					break;
				case ByteCodeConstants.TAG_LONG:
					entry = new ConstantLongInfo(result);
					break;
				case ByteCodeConstants.TAG_DOUBLE:
					entry = new ConstantDoubleInfo(result);
					break;
				case ByteCodeConstants.TAG_NAME_AND_TYPE:
					entry = new ConstantNameAndTypeInfo(result);
					break;
				case ByteCodeConstants.TAG_UTF8:
					entry = new ConstantUtf8Info(result);
					break;
				case ByteCodeConstants.TAG_METHOD_HANDLE:
					entry = new ConstantMethodHandleInfo(result);
					break;
				case ByteCodeConstants.TAG_METHOD_TYPE:
					entry = new ConstantMethodTypeInfo(result);
					break;
				case ByteCodeConstants.TAG_INVOKE_DYNAMIC:
					entry = new ConstantInvokeDynamicInfo(result);
					break;
				default:
					throw new IllegalStateException(
							"Unexpected constant pool entry tag '" + tag + "'");
				}
				entry.read(in);
				result[i] = entry;
				
	            if (result[i] instanceof ConstantLargeNumeric) {
	            	/* some entries are wider */
	            	i++;
	            }

			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return result;
	}
}
