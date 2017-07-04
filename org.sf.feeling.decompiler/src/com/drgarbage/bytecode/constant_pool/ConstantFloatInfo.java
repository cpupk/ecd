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
import com.drgarbage.javasrc.JavaLexicalConstants;

import java.io.*;

/**
  * Describes a <code>CONSTANT_Float_info</code> constant pool data structure.
  *
  * @author Sergej Alekseev and Peter Palaga    
  * @version $Revision:395 $
  * $Id:ConstantFloatInfo.java 395 2008-04-03 15:08:14Z Peter Palaga $
  */
public class ConstantFloatInfo extends ConstantNumeric {

    public ConstantFloatInfo(AbstractConstantPoolEntry[] constantPool) {
		super(CONSTANT_FLOAT, constantPool);
	}

    /**
        Get the float value of this constant pool entry.
        @return the value
     */
    public float getFloat() {
        return Float.intBitsToFloat(bytes);
    }

    /**
        Set the float value of this constant pool entry.
        @param number the value
     */
    public void setFloat(float number) {
        bytes = Float.floatToIntBits(number);
    }

    public void read(DataInput in) throws IOException {
        
        super.read(in);
    }
    
     public void write(DataOutput out)
        throws IOException {
        
        out.writeByte(ByteCodeConstants.TAG_FLOAT);
        super.write(out);
    }

 	@Override
	public String getInfo() {
		return ByteCodeConstants.bytes + JavaLexicalConstants.EQUALS +  String.valueOf(getFloat());
	}

}
