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

/**
  * Describes an entry in a <code>LocalVariableTableEntry</code> attribute structure.
  *
  * @author Sergej Alekseev and Peter Palaga    
  * @version $Revision: 187 $
  * $Id: LocalVariableTableEntry.java 187 2013-06-03 05:47:37Z salekseev $
  */
public class LocalVariableTableEntry extends LocalVariableCommonEntry {


    /**
     * Get the index of the constant pool entry containing the descriptor of this
     * local variable.
     *
     * @return the index
     */
    public int getDescriptorIndex() {
        return descriptorOrSignatureIndex;
    }

    /**
     * Get the index of the constant pool entry containing the descriptor of this
     * local variable.
     *
     * @param descriptorIndex the index
     */
    public void setDescriptorIndex(int descriptorIndex) {
        setDescriptorOrSignatureIndex(descriptorIndex);
    }

}
