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

package com.drgarbage.asm.render.intf;

import java.util.Comparator;
import java.util.List;

import com.drgarbage.bytecode.LocalVariableTableEntry;
import com.drgarbage.bytecode.LocalVariableTypeTableEntry;
import com.drgarbage.bytecode.ByteCodeConstants.Align;

public interface ILocalVariableTable {
	public static class LocalVariableTableEntryEndOffsetComparator implements Comparator<LocalVariableTableEntry> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(LocalVariableTableEntry e1, LocalVariableTableEntry e2) {
			int i1 = e1.getStartPc() + e1.getLength();
			int i2 = e2.getStartPc() + e2.getLength();
			return (i1 < i2 ? -1 : (i1 == i2 ? 0 : 1));
		}
		
	}
	public static final LocalVariableTableEntryEndOffsetComparator END_OFFSET_COMPARATOR = new LocalVariableTableEntryEndOffsetComparator();

	String findArgName(int i, int offset, boolean isConstructor, boolean isStatic);
	int getLength();
	List<LocalVariableTableEntry> getEntries();
	public boolean isAvailable();
	public void addLocalVariableTableEntry(LocalVariableTableEntry entry);
	public void addLocalVariableTypeTableEntry(LocalVariableTypeTableEntry entry);
	
	public String[] getHeader();
	public String[] getRow(int index);
	public Align getAlignment(int column);

}
