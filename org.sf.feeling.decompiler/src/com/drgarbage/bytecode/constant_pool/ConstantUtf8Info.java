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

package com.drgarbage.bytecode.constant_pool;

import com.drgarbage.bytecode.ByteCodeConstants;
import com.drgarbage.bytecode.BytecodeUtils;
import com.drgarbage.javasrc.JavaLexicalConstants;

import java.io.*;

/**
  * Describes a <code>CONSTANT_Utf8_info</code> constant pool data structure.
  *
  * @author Sergej Alekseev and Peter Palaga    
  * @version $Revision:395 $
  * $Id:ConstantUtf8Info.java 395 2008-04-03 15:08:14Z Peter Palaga $
  */
public class ConstantUtf8Info extends AbstractConstantPoolEntry {

    public ConstantUtf8Info(AbstractConstantPoolEntry[] constantPool) {
		super(CONSTANT_UTF8, constantPool);
	}

	private String string;
	private int length = ByteCodeConstants.INVALID_OFFSET;

    /**
     * Get the string in this entry.
     *
     * @return the string
     */
    public String getString() {
        return string;
    }

    /**
     * Set the string in this entry.
     *
     * @param string the string
     */
    public void setString(String string) {
        this.string = string;
        length = ByteCodeConstants.INVALID_OFFSET;
    }

    public void read(DataInput in) throws IOException {

        string = in.readUTF();
        length = ByteCodeConstants.INVALID_OFFSET;

    }

    public void write(DataOutput out)
            throws IOException {

        out.writeByte(ByteCodeConstants.TAG_UTF8);
        out.writeUTF(string);
    }

    public boolean equals(Object object) {
        if (!(object instanceof ConstantUtf8Info)) {
            return false;
        }
        ConstantUtf8Info constantUtf8Info = (ConstantUtf8Info)object;
        return super.equals(object) && constantUtf8Info.string.equals(string);
    }

    public int hashCode() {
        return super.hashCode() ^ string.hashCode();
    }
    
    public int getLength() {
    	if (length == ByteCodeConstants.INVALID_OFFSET) {
        	try {
    			length = string.getBytes("utf-8").length;
    		} catch (UnsupportedEncodingException e) {
    			throw new RuntimeException(e);
    		}
    	}
    	return length;
    }

    @Override
	public String getInfo() {
    	
    	StringBuffer sb = new StringBuffer()
    		.append(ByteCodeConstants.LENGTH)
    		.append(JavaLexicalConstants.EQUALS)
    		.append(getLength())
    		.append(JavaLexicalConstants.SEMICOLON)
    		.append(JavaLexicalConstants.SPACE)
    		.append(ByteCodeConstants.bytes)
    		.append(JavaLexicalConstants.EQUALS)
   		;
		BytecodeUtils.appendString(sb, string);
		return sb.toString();  
	}

}
