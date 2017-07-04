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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.drgarbage.bytecode.constant_pool.AbstractConstantPoolEntry;
import com.drgarbage.bytecode.constant_pool.ConstantUtf8Info;
import com.drgarbage.bytecode.instructions.AbstractInstruction;
import com.drgarbage.bytecode.instructions.BranchInstruction;
import com.drgarbage.bytecode.instructions.BranchWInstruction;
import com.drgarbage.bytecode.instructions.ConstantPoolByteIndexInstruction;
import com.drgarbage.bytecode.instructions.ConstantPoolShortIndexInstruction;
import com.drgarbage.bytecode.instructions.ImmediateByteInstruction;
import com.drgarbage.bytecode.instructions.ImmediateShortInstruction;
import com.drgarbage.bytecode.instructions.IncrementInstruction;
import com.drgarbage.bytecode.instructions.InvokeInterfaceInstruction;
import com.drgarbage.bytecode.instructions.LookupSwitchInstruction;
import com.drgarbage.bytecode.instructions.MultianewarrayInstruction;
import com.drgarbage.bytecode.instructions.NewArrayInstruction;
import com.drgarbage.bytecode.instructions.Opcodes;
import com.drgarbage.bytecode.instructions.RetInstruction;
import com.drgarbage.bytecode.instructions.SimpleInstruction;
import com.drgarbage.bytecode.instructions.TableSwitchInstruction;
import com.drgarbage.bytecode.instructions.XLoadStoreNInstruction.XLoadInstruction;
import com.drgarbage.bytecode.instructions.XLoadStoreNInstruction.XLoadNInstruction;
import com.drgarbage.bytecode.instructions.XLoadStoreNInstruction.XStoreInstruction;
import com.drgarbage.bytecode.instructions.XLoadStoreNInstruction.XStoreNInstruction;

/**
  *  Converts code to a list of instructions.
  *
  *  @author Sergej Alekseev and Peter Palaga
  *  @version $Revision: 187 $
  *  $Id: InstructionParser.java 187 2013-06-03 05:47:37Z salekseev $
  */
public class InstructionParser implements Opcodes {

    /**
	 * The input stream
	 */
	private CountedDataInputStream in;
	
	/**
	 * NUmebr of byte to read from {@link #in}. 
	 */
	private int length;
	private int offset;
	
	private byte[] code;
	
	/**
	 * @param code a byte array containing instruction.
	 * @param offset number of bytes from the beginning of the byte-array where the parsing should start.
	 * @param length number of byte to parse.
	 */
	public InstructionParser(byte[] code, int offset, int length) {
		super();
		this.length = length;
		this.code = code;
		this.offset = offset;
		InputStream tmp = new ByteArrayInputStream(code);
		if (offset != 0) {
			/* only risk that the stream does not support seek if there is something to skip */
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
	public InstructionParser(byte[] code) {
		this(code, 0, code.length);
    }
	
    public Map<String, Object> parseAttributes(Set<String> attributeNames, AbstractConstantPoolEntry[] constantPool) {
		HashMap<String, Object> result = new HashMap<String, Object>();
        if (in.getByteCount() < code.length - offset) {
        	/* there is still something to read in the code array */
        	try {
    			int attrCount = in.readUnsignedShort();
    			int attrsRead = 0;
    			for (int i = 0; i < attrCount; i++) {
    				
    				int nameIndex = in.readUnsignedShort();
    				String attrName = ((ConstantUtf8Info)constantPool[nameIndex]).getString();
    				int attrLen = in.readInt();
    				if (attributeNames.contains(attrName)) {
    					/* read */
    					if (attrName.equals(ByteCodeConstants.LINE_NUMBER_TABLE)) {
    						result.put(attrName, parseLineNumberTable(constantPool));
    					}
    					else if (attrName.equals(ByteCodeConstants.LOCAL_VARIABLE_TABLE)) {
    						//FIXME there may be more than one LocalVariableTable; return them all rather than merging them
    						result.put(attrName, parseLocalVariableTable(constantPool, (LocalVariableTableEntry[]) result.get(attrName)));
    					}
    					else if (attrName.equals(ByteCodeConstants.LOCAL_VARIABLE_TYPE_TABLE)) {
    						result.put(attrName, parseLocalVariableTypeTable(constantPool));
    					}
    					
    					attrsRead++;
    					if (attrsRead == attributeNames.size()) {
    						/* we have read everything we wanted skip other attributes */
    						break;
    					}
    				}
    				else {
    					/* skip */
    					in.skipBytes(attrLen);
    				}
    				
    				
    			}
    		} catch (IOException e) {
    			throw new RuntimeException(e);
    		}
        }
		return result;
    }
    
    private LocalVariableTableEntry[] parseLocalVariableTable(AbstractConstantPoolEntry[] constantPool, LocalVariableTableEntry[] result) throws IOException {
        if (in.getByteCount() < code.length - offset) {
        	int len = in.readUnsignedShort();
    		int j = 0;
    		if (result == null) {
    			result = new LocalVariableTableEntry[len];
    		}
    		else {
    			/* allocate more space for the table */
    			LocalVariableTableEntry[] newResult = new LocalVariableTableEntry[len + result.length];
    			System.arraycopy(result, 0, newResult, 0, result.length);
    			j = result.length;
    			result = newResult;
    		}
    		for (; j < len; j++) {
    			LocalVariableTableEntry en = new LocalVariableTableEntry();
    			en.read(in);
    		    result[j] = en;
    		}
        }
        return result;
	}
    private Object parseLocalVariableTypeTable(AbstractConstantPoolEntry[] constantPool) throws IOException {
        if (in.getByteCount() < code.length - offset) {
    		int len = in.readUnsignedShort();
    		LocalVariableTypeTableEntry[] result = new LocalVariableTypeTableEntry[len];
    		for (int j = 0; j < len; j++) {
    			LocalVariableTypeTableEntry en = new LocalVariableTypeTableEntry();
    			en.read(in);
    		    result[j] = en;
    		}
            return result;
        }
        else {
        	return new LocalVariableTypeTableEntry[0];
        }
	}

	private LineNumberTableEntry[] parseLineNumberTable(AbstractConstantPoolEntry[] constantPool) throws IOException {
        if (in.getByteCount() < code.length - offset) {
    		int len = in.readUnsignedShort();
        	LineNumberTableEntry[] result = new LineNumberTableEntry[len];
    		for (int j = 0; j < len; j++) {
    			LineNumberTableEntry en = new LineNumberTableEntry();
    			en.read(in);
    		    result[j] = en;
    		}
            return result;
        }
        else {
        	return new LineNumberTableEntry[0];
        }
    }
	
    public ExceptionTableEntry[] parseExceptionTable() {
        ExceptionTableEntry[] result = null;
        if (in.getByteCount() < code.length - offset) {
        	/* only if there is something to read */
    		try {
    			int len = in.readUnsignedShort();
    			result = new ExceptionTableEntry[len];
    			for (int i = 0; i < len; i++) {
    				ExceptionTableEntry en = new ExceptionTableEntry();
    				en.read(in);
    			    result[i] = en;
    			}
    		} catch (IOException e) {
    			throw new RuntimeException(e);
    		}
        }
        return result;
    }

    
    /**
        Parses the underlying byte array int a {@link List} of {@link AbstractInstruction}s.
        @return the <code>java.util.List</code> with the instructions
     */
    public List<AbstractInstruction> parse() {
   
        List<AbstractInstruction> instructions = new LinkedList<AbstractInstruction>();
        
        boolean wide = false;
        AbstractInstruction currentInstruction;
        try {
			while (in.getByteCount() < length) {
			    currentInstruction = parseNextInstruction(wide);
			    wide = (currentInstruction.getOpcode() == OPCODE_WIDE);
			    instructions.add(currentInstruction);
			}
		} catch (IOException e) {
			/* should not happen with ByteArrayInputStream */
			throw new RuntimeException(e);
		}
        
        return instructions;
    }
    
    protected AbstractInstruction parseNextInstruction(boolean wide)
        throws IOException {
    	
        AbstractInstruction instruction;

        int opcode = in.readUnsignedByte();

        switch (opcode) {
            
            case OPCODE_NOP:
            case OPCODE_ACONST_NULL:
            case OPCODE_ICONST_M1:
            case OPCODE_ICONST_0:
            case OPCODE_ICONST_1:
            case OPCODE_ICONST_2:
            case OPCODE_ICONST_3:
            case OPCODE_ICONST_4:
            case OPCODE_ICONST_5:
            case OPCODE_LCONST_0:
            case OPCODE_LCONST_1:
            case OPCODE_FCONST_0:
            case OPCODE_FCONST_1:
            case OPCODE_FCONST_2:
            case OPCODE_DCONST_0:
            case OPCODE_DCONST_1:
            	
            case OPCODE_IALOAD:
            case OPCODE_LALOAD:
            case OPCODE_FALOAD:
            case OPCODE_DALOAD:
            case OPCODE_AALOAD:
            case OPCODE_BALOAD:
            case OPCODE_CALOAD:
            case OPCODE_SALOAD:

            
            case OPCODE_IASTORE:
            case OPCODE_LASTORE:
            case OPCODE_FASTORE:
            case OPCODE_DASTORE:
            case OPCODE_AASTORE:
            case OPCODE_BASTORE:
            case OPCODE_CASTORE:
            case OPCODE_SASTORE:
            case OPCODE_POP:
            case OPCODE_POP2:
            case OPCODE_DUP:
            case OPCODE_DUP_X1:
            case OPCODE_DUP_X2:
            case OPCODE_DUP2:
            case OPCODE_DUP2_X1:
            case OPCODE_DUP2_X2:
            case OPCODE_SWAP:
            case OPCODE_IADD:
            case OPCODE_LADD:
            case OPCODE_FADD:
            case OPCODE_DADD:
            case OPCODE_ISUB:
            case OPCODE_LSUB:
            case OPCODE_FSUB:
            case OPCODE_DSUB:
            case OPCODE_IMUL:
            case OPCODE_LMUL:
            case OPCODE_FMUL:
            case OPCODE_DMUL:
            case OPCODE_IDIV:
            case OPCODE_LDIV:
            case OPCODE_FDIV:
            case OPCODE_DDIV:
            case OPCODE_IREM:
            case OPCODE_LREM:
            case OPCODE_FREM:
            case OPCODE_DREM:
            case OPCODE_INEG:
            case OPCODE_LNEG:
            case OPCODE_FNEG:
            case OPCODE_DNEG:
            case OPCODE_ISHL:
            case OPCODE_LSHL:
            case OPCODE_ISHR:
            case OPCODE_LSHR:
            case OPCODE_IUSHR:
            case OPCODE_LUSHR:
            case OPCODE_IAND:
            case OPCODE_LAND:
            case OPCODE_IOR:
            case OPCODE_LOR:
            case OPCODE_IXOR:
            case OPCODE_LXOR:
            case OPCODE_I2L:
            case OPCODE_I2F:
            case OPCODE_I2D:
            case OPCODE_L2I:
            case OPCODE_L2F:
            case OPCODE_L2D:
            case OPCODE_F2I:
            case OPCODE_F2L:
            case OPCODE_F2D:
            case OPCODE_D2I:
            case OPCODE_D2L:
            case OPCODE_D2F:
            case OPCODE_I2B:
            case OPCODE_I2C:
            case OPCODE_I2S:
            case OPCODE_LCMP:
            case OPCODE_FCMPL:
            case OPCODE_FCMPG:
            case OPCODE_DCMPL:
            case OPCODE_DCMPG:
            case OPCODE_IRETURN:
            case OPCODE_LRETURN:
            case OPCODE_FRETURN:
            case OPCODE_DRETURN:
            case OPCODE_ARETURN:
            case OPCODE_RETURN:
//            case OPCODE_XXXUNUSEDXXX:
            case OPCODE_ARRAYLENGTH:
            case OPCODE_ATHROW:
            case OPCODE_MONITORENTER:
            case OPCODE_MONITOREXIT:
            case OPCODE_BREAKPOINT:
            case OPCODE_IMPDEP1:
            case OPCODE_IMPDEP2:
            case OPCODE_WIDE:
                
                instruction = new SimpleInstruction(opcode);
                break;

            case OPCODE_LDC:
                instruction = new ConstantPoolByteIndexInstruction(opcode, wide);
                break;
            case OPCODE_LDC_W:
            case OPCODE_LDC2_W:
                instruction = new ConstantPoolShortIndexInstruction(opcode);
                break;

                
            /* load local variable */
            case OPCODE_ILOAD:  // subject to wide
            case OPCODE_LLOAD:  // subject to wide
            case OPCODE_FLOAD:  // subject to wide
            case OPCODE_DLOAD:  // subject to wide
            case OPCODE_ALOAD:  // subject to wide
                instruction = new XLoadInstruction(opcode, wide);
                break;
            case OPCODE_ILOAD_0:
                instruction = new XLoadNInstruction(opcode, 0);
                break;
            case OPCODE_ILOAD_1:
                instruction = new XLoadNInstruction(opcode, 1);
                break;
            case OPCODE_ILOAD_2:
                instruction = new XLoadNInstruction(opcode, 2);
                break;
            case OPCODE_ILOAD_3:
                instruction = new XLoadNInstruction(opcode, 3);
                break;
            case OPCODE_LLOAD_0:
                instruction = new XLoadNInstruction(opcode, 0);
                break;
            case OPCODE_LLOAD_1:
                instruction = new XLoadNInstruction(opcode, 1);
                break;
            case OPCODE_LLOAD_2:
                instruction = new XLoadNInstruction(opcode, 2);
                break;
            case OPCODE_LLOAD_3:
                instruction = new XLoadNInstruction(opcode, 3);
                break;
            case OPCODE_FLOAD_0:
                instruction = new XLoadNInstruction(opcode, 0);
                break;
            case OPCODE_FLOAD_1:
                instruction = new XLoadNInstruction(opcode, 1);
                break;
            case OPCODE_FLOAD_2:
                instruction = new XLoadNInstruction(opcode, 2);
                break;
            case OPCODE_FLOAD_3:
                instruction = new XLoadNInstruction(opcode, 3);
                break;
            case OPCODE_DLOAD_0:
                instruction = new XLoadNInstruction(opcode, 0);
                break;
            case OPCODE_DLOAD_1:
                instruction = new XLoadNInstruction(opcode, 1);
                break;
            case OPCODE_DLOAD_2:
                instruction = new XLoadNInstruction(opcode, 2);
                break;
            case OPCODE_DLOAD_3:
                instruction = new XLoadNInstruction(opcode, 3);
                break;
            case OPCODE_ALOAD_0:
                instruction = new XLoadNInstruction(opcode, 0);
                break;
            case OPCODE_ALOAD_1:
                instruction = new XLoadNInstruction(opcode, 1);
                break;
            case OPCODE_ALOAD_2:
                instruction = new XLoadNInstruction(opcode, 2);
                break;
            case OPCODE_ALOAD_3:
                instruction = new XLoadNInstruction(opcode, 3);
                break;
                
             /* store into local variable */
            case OPCODE_ISTORE: // subject to wide
            case OPCODE_LSTORE: // subject to wide
            case OPCODE_FSTORE: // subject to wide
            case OPCODE_DSTORE: // subject to wide
            case OPCODE_ASTORE: // subject to wide
                instruction = new XStoreInstruction(opcode, wide);
                break;
            case OPCODE_ISTORE_0:
                instruction = new XStoreNInstruction(opcode, 0);
                break;
            case OPCODE_ISTORE_1:
                instruction = new XStoreNInstruction(opcode, 1);
                break;
            case OPCODE_ISTORE_2:
                instruction = new XStoreNInstruction(opcode, 2);
                break;
            case OPCODE_ISTORE_3:
                instruction = new XStoreNInstruction(opcode, 3);
                break;
            case OPCODE_LSTORE_0:
                instruction = new XStoreNInstruction(opcode, 0);
                break;
            case OPCODE_LSTORE_1:
                instruction = new XStoreNInstruction(opcode, 1);
                break;
            case OPCODE_LSTORE_2:
                instruction = new XStoreNInstruction(opcode, 2);
                break;
            case OPCODE_LSTORE_3:
                instruction = new XStoreNInstruction(opcode, 3);
                break;
            case OPCODE_FSTORE_0:
                instruction = new XStoreNInstruction(opcode, 0);
                break;
            case OPCODE_FSTORE_1:
                instruction = new XStoreNInstruction(opcode, 1);
                break;
            case OPCODE_FSTORE_2:
                instruction = new XStoreNInstruction(opcode, 2);
                break;
            case OPCODE_FSTORE_3:
                instruction = new XStoreNInstruction(opcode, 3);
                break;
            case OPCODE_DSTORE_0:
                instruction = new XStoreNInstruction(opcode, 0);
                break;
            case OPCODE_DSTORE_1:
                instruction = new XStoreNInstruction(opcode, 1);
                break;
            case OPCODE_DSTORE_2:
                instruction = new XStoreNInstruction(opcode, 2);
                break;
            case OPCODE_DSTORE_3:
                instruction = new XStoreNInstruction(opcode, 3);
                break;
            case OPCODE_ASTORE_0:
                instruction = new XStoreNInstruction(opcode, 0);
                break;
            case OPCODE_ASTORE_1:
                instruction = new XStoreNInstruction(opcode, 1);
                break;
            case OPCODE_ASTORE_2:
                instruction = new XStoreNInstruction(opcode, 2);
                break;
            case OPCODE_ASTORE_3:
                instruction = new XStoreNInstruction(opcode, 3);
                break;
                
            case OPCODE_RET:    // subject to wide
                instruction = new RetInstruction(opcode, wide);
                break;

            case OPCODE_BIPUSH:
                instruction = new ImmediateByteInstruction(opcode, wide);
                break;
                
            case OPCODE_GETSTATIC:
            case OPCODE_PUTSTATIC:
            case OPCODE_GETFIELD:
            case OPCODE_PUTFIELD:
            case OPCODE_INVOKEVIRTUAL:
            case OPCODE_INVOKESPECIAL:
            case OPCODE_INVOKESTATIC:
            case OPCODE_INVOKEDYNAMIC:
            case OPCODE_NEW:
            case OPCODE_ANEWARRAY:
            case OPCODE_CHECKCAST:
            case OPCODE_INSTANCEOF:
                instruction = new ConstantPoolShortIndexInstruction(opcode);
                break;
                
            case OPCODE_NEWARRAY:
                instruction = new NewArrayInstruction(opcode, wide);
                break;

            	
            	
            case OPCODE_SIPUSH: // the only immediate short instruction that does
                                // not have an immediate constant pool reference

                instruction = new ImmediateShortInstruction(opcode);
                break;

            case OPCODE_IFEQ:
            case OPCODE_IFNE:
            case OPCODE_IFLT:
            case OPCODE_IFGE:
            case OPCODE_IFGT:
            case OPCODE_IFLE:
            case OPCODE_IF_ICMPEQ:
            case OPCODE_IF_ICMPNE:
            case OPCODE_IF_ICMPLT:
            case OPCODE_IF_ICMPGE:
            case OPCODE_IF_ICMPGT:
            case OPCODE_IF_ICMPLE:
            case OPCODE_IF_ACMPEQ:
            case OPCODE_IF_ACMPNE:
            case OPCODE_GOTO:
            case OPCODE_JSR:
            case OPCODE_IFNULL:
            case OPCODE_IFNONNULL:

                instruction = new BranchInstruction(opcode);
                break;

            case OPCODE_GOTO_W:
            case OPCODE_JSR_W:

                instruction = new BranchWInstruction(opcode);
                break;
                
            case OPCODE_IINC: // subject to wide

                instruction = new IncrementInstruction(opcode, wide);
                break;
                
            case OPCODE_TABLESWITCH:

                instruction = new TableSwitchInstruction(opcode);
                break;
                
            case OPCODE_LOOKUPSWITCH:

                instruction = new LookupSwitchInstruction(opcode);
                break;
                
            case OPCODE_INVOKEINTERFACE:

                instruction = new InvokeInterfaceInstruction(opcode);
                break;
                
            case OPCODE_MULTIANEWARRAY:
            
                instruction = new MultianewarrayInstruction(opcode);
                break;
                
            default:
                throw new IOException("invalid opcode 0x" + Integer.toHexString(opcode));
        }
        
        instruction.read(in);
        return instruction;
    }
    
}
