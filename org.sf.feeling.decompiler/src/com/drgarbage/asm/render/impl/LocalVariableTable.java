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

package com.drgarbage.asm.render.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.drgarbage.asm.render.intf.ILocalVariableTable;
import com.drgarbage.bytecode.ByteCodeConstants;
import com.drgarbage.bytecode.LocalVariableTableEntry;
import com.drgarbage.bytecode.LocalVariableTypeTableEntry;
import com.drgarbage.bytecode.ByteCodeConstants.Align;
import com.drgarbage.bytecode.constant_pool.AbstractConstantPoolEntry;
import com.drgarbage.bytecode.constant_pool.ConstantUtf8Info;

public class LocalVariableTable implements ILocalVariableTable {
	
	private static String[] HEADER = new String[] {
			ByteCodeConstants.INDEX,
			ByteCodeConstants.START_PC, 
			ByteCodeConstants.LENGTH,
			ByteCodeConstants.NAME_INDEX,
			ByteCodeConstants.DESCRIPTOR_INDEX,
//					ByteCodeConstants.NAME,
//					ByteCodeConstants.DESCRIPTOR
	};

	private boolean available = true;
	protected AbstractConstantPoolEntry[] constantPool;

	private HashMap<Integer, LocalVariableTableEntry[]> indexedLocalVariableTable = new HashMap<Integer, LocalVariableTableEntry[]>();

	private List<LocalVariableTableEntry> localVariableTable;
	
	private List<LocalVariableTypeTableEntry> localVariableTypeTable;
	public LocalVariableTable(AbstractConstantPoolEntry[] constantPool,
			boolean available) {
		super();
		this.constantPool = constantPool;
		this.available = available;
	}

	public void addLocalVariableTableEntry(LocalVariableTableEntry entry) {
		if (localVariableTable == null) {
			localVariableTable = new LinkedList<LocalVariableTableEntry>();
		}
		localVariableTable.add(entry);
		
		Integer key = Integer.valueOf(entry.getIndex());
		LocalVariableTableEntry[] entries = indexedLocalVariableTable.get(key);
		if (entries == null) {
			entries = new LocalVariableTableEntry[1];
			entries[0] = entry;
		}
		else {
			/* realloc */
			LocalVariableTableEntry[] newEntries = new LocalVariableTableEntry[entries.length +1];
			System.arraycopy(entries, 0, newEntries, 0, entries.length);
			entries = newEntries;
			entries[entries.length - 1] = entry;
			
			/* check if reordering is necessary */
			LocalVariableTableEntry last = entries[entries.length -2];
			if (ILocalVariableTable.END_OFFSET_COMPARATOR.compare(last, entry) > 0) {
				/* last > entry: reordering needed */
				Arrays.sort(entries, ILocalVariableTable.END_OFFSET_COMPARATOR);
			}
		}
		indexedLocalVariableTable.put(key, entries);
	}
	public void addLocalVariableTypeTableEntry(LocalVariableTypeTableEntry entry) {
		if (localVariableTypeTable == null) {
			localVariableTypeTable = new LinkedList<LocalVariableTypeTableEntry>();
		}
		localVariableTypeTable.add(entry);
	}

	public String findArgName(int i, int offset, boolean isConstructor, boolean isStatic) {
		if (constantPool == null) {
			return null;
		}
		int argi = isStatic || isConstructor ? i : i+1;

		LocalVariableTableEntry[] entries = indexedLocalVariableTable.get(Integer.valueOf(argi));
		LocalVariableTableEntry match = null;
		if (entries != null) {
			if (entries.length == 1) {
				match = entries[0];
			}
			else if (entries.length > 1) {
				/* search */
				for (LocalVariableTableEntry e : entries) {
					if (offset < e.getStartPc() + e.getLength()) {
						/* take the first which is just lower than
						 * we can do it, as entries are sorted according to 
						 * their end offsets. 
						 * We do not check the start offset:
						 * 
						 *   "The given local variable must have a value at indices into 
						 *   the code array in the interval [start_pc, start_pc+length], 
						 *   that is, between start_pc and start_pc+length inclusive."
						 *   
						 *   http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#5956
						 *   
						 *  Note that the specification mentions only where the variable 
						 *  must have a value. As a matter of fact, a variable is initialized
						 *  at offsets where it is still does not have a value. 
						 *  So the name of the variable is valid even before the start_pc.
						 *  
						 *  This has to do with BUG#118
						 *  
						 *   */
						match = e;
						break;
					}
				}
			}
		}
		
		if (match != null) {
			return ((ConstantUtf8Info)constantPool[match.getNameIndex()]).getString();
		}
		return null;
	}
	
	

	public Align getAlignment(int column) {
		return Align.RIGHT;
	}

	public List<LocalVariableTableEntry> getEntries() {
		return localVariableTable;
	}
	
	public String[] getHeader() {
		return HEADER;
	}

	public int getLength() {
		return localVariableTable == null ? 0 : localVariableTable.size();
	}

	public String[] getRow(int index) {
		LocalVariableTableEntry en = localVariableTable.get(index);
		return new String[] {
				String.valueOf(en.getIndex()),
				String.valueOf(en.getStartPc()), 
				String.valueOf(en.getLength()),
				String.valueOf(en.getNameIndex()),
				String.valueOf(en.getDescriptorIndex())

//									((ConstantUtf8Info)constantPool[en.getNameIndex()]).getString(),
//									((ConstantUtf8Info)constantPool[en.getDescriptorIndex()]).getString()
		};
	}

	public boolean isAvailable() {
		return available;
	}

}
