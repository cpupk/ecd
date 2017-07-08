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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;

import com.drgarbage.asm.AnnotationVisitor;
import com.drgarbage.asm.Attribute;
import com.drgarbage.asm.ClassVisitor;
import com.drgarbage.asm.FieldVisitor;
import com.drgarbage.asm.Label;
import com.drgarbage.asm.MethodVisitor;
import com.drgarbage.asm.Type;
import com.drgarbage.asm.render.impl.ClassFileDocument.ExceptionTableEntryComparator;
import com.drgarbage.asm.render.impl.ClassFileDocument.ListMap;
import com.drgarbage.asm.render.intf.IClassFileDocument;
import com.drgarbage.asm.render.intf.IFieldSection;
import com.drgarbage.asm.render.intf.IInstructionLine;
import com.drgarbage.asm.render.intf.ILocalVariableTable;
import com.drgarbage.asm.render.intf.IMethodSection;
import com.drgarbage.asm.render.intf.ITryBlock;
import com.drgarbage.asm.signature.SignatureReader;
import com.drgarbage.asm.signature.SignatureVisitor;
import com.drgarbage.asm_ext.ICodeVisitor;
import com.drgarbage.asm_ext.IConstantPoolVisitor;
import com.drgarbage.asm_ext.ILocalVariableTableVisitor;
import com.drgarbage.bytecode.ByteCodeConstants;
import com.drgarbage.bytecode.ByteCodeConstants.Align;
import com.drgarbage.bytecode.BytecodeUtils;
import com.drgarbage.bytecode.ConstantPoolParser;
import com.drgarbage.bytecode.ExceptionTableEntry;
import com.drgarbage.bytecode.InstructionParser;
import com.drgarbage.bytecode.LineNumberTableEntry;
import com.drgarbage.bytecode.LocalVariableTableEntry;
import com.drgarbage.bytecode.LocalVariableTypeTableEntry;
import com.drgarbage.bytecode.constant_pool.AbstractConstantPoolEntry;
import com.drgarbage.bytecode.constant_pool.ConstantClassInfo;
import com.drgarbage.bytecode.constant_pool.ConstantDoubleInfo;
import com.drgarbage.bytecode.constant_pool.ConstantFieldrefInfo;
import com.drgarbage.bytecode.constant_pool.ConstantFloatInfo;
import com.drgarbage.bytecode.constant_pool.ConstantIntegerInfo;
import com.drgarbage.bytecode.constant_pool.ConstantInterfaceMethodrefInfo;
import com.drgarbage.bytecode.constant_pool.ConstantInvokeDynamicInfo;
import com.drgarbage.bytecode.constant_pool.ConstantLongInfo;
import com.drgarbage.bytecode.constant_pool.ConstantMethodHandleInfo;
import com.drgarbage.bytecode.constant_pool.ConstantMethodTypeInfo;
import com.drgarbage.bytecode.constant_pool.ConstantMethodrefInfo;
import com.drgarbage.bytecode.constant_pool.ConstantNameAndTypeInfo;
import com.drgarbage.bytecode.constant_pool.ConstantStringInfo;
import com.drgarbage.bytecode.constant_pool.ConstantUtf8Info;
import com.drgarbage.bytecode.instructions.AbstractInstruction;
import com.drgarbage.bytecode.instructions.BranchInstruction;
import com.drgarbage.bytecode.instructions.IConstantPoolIndexProvider;
import com.drgarbage.bytecode.instructions.ILocalVariableIndexProvider;
import com.drgarbage.bytecode.instructions.ImmediateByteInstruction;
import com.drgarbage.bytecode.instructions.ImmediateIntInstruction;
import com.drgarbage.bytecode.instructions.ImmediateShortInstruction;
import com.drgarbage.bytecode.instructions.IncrementInstruction;
import com.drgarbage.bytecode.instructions.InvokeInterfaceInstruction;
import com.drgarbage.bytecode.instructions.LookupSwitchInstruction;
import com.drgarbage.bytecode.instructions.LookupSwitchInstruction.MatchOffsetEntry;
import com.drgarbage.bytecode.instructions.MultianewarrayInstruction;
import com.drgarbage.bytecode.instructions.NewArrayInstruction;
import com.drgarbage.bytecode.instructions.SimpleInstruction;
import com.drgarbage.bytecode.instructions.TableSwitchInstruction;
import com.drgarbage.javasrc.JavaAnnotations;
import com.drgarbage.javasrc.JavaKeywords;
import com.drgarbage.javasrc.JavaLexicalConstants;
import com.drgarbage.javasrc.JavaSourceUtils;

public abstract class AbstractClassFileDocument extends ClassVisitor implements
		com.drgarbage.asm.Opcodes,
		IClassFileDocument,
		IConstantPoolVisitor
{

	protected abstract class AbstractClassFileElement extends MethodVisitor
	{

		// ------------------------------------------------------------------------
		// Utility methods
		// ------------------------------------------------------------------------

		public AbstractClassFileElement( int arg0 )
		{
			super( arg0 );
		}

		/**
		 * Appends an internal name, a type descriptor or a type signature to
		 * {@link #buf buf}.
		 * 
		 * @param type
		 *            indicates if desc is an internal name, a field descriptor,
		 *            a method descriptor, a class signature, ...
		 * @param desc
		 *            an internal name, type descriptor, or type signature. May
		 *            be <tt>null</tt>.
		 */
		protected void appendDescriptor( final int type, final String desc )
		{
			// TODO: test this
			sb.append( desc );
		}

	}

	protected abstract class AbstractMethodRenderer extends AbstractClassFileElement implements
			com.drgarbage.asm.Opcodes,
			ICodeVisitor,
			ILocalVariableTableVisitor,
			IMethodSection
	{

		protected class Block
		{

			protected TryBlock enclosingTryBlock;
			protected int endLine = ByteCodeConstants.INVALID_OFFSET;
			protected int endOffset = ByteCodeConstants.INVALID_OFFSET;
			protected String startKeyWord;
			protected int startLine = ByteCodeConstants.INVALID_OFFSET;
			protected int startOffset = ByteCodeConstants.INVALID_OFFSET;

			public Block( TryBlock parent, String startKeyWord, int start )
			{
				super( );
				this.enclosingTryBlock = parent;
				this.startKeyWord = startKeyWord;
				this.startOffset = start;
			}

			public Block( TryBlock parent, String startKeyWord, int startOffset, int endOffset )
			{
				super( );
				this.enclosingTryBlock = parent;
				this.startKeyWord = startKeyWord;
				this.startOffset = startOffset;
				this.endOffset = endOffset;
			}

			public void appendEnd( )
			{

				endLine = AbstractClassFileDocument.this.lineCount;

				if ( renderTryCatchFinallyBlocks )
				{
					decrementIndent( );
					appendRightBrace( );
					// append(" /* endOffset of try */");
					appendNewline( );
				}
			}

			public void appendStart( )
			{

				startLine = AbstractClassFileDocument.this.lineCount;

				if ( renderTryCatchFinallyBlocks )
				{
					append( startKeyWord );
					appendSpace( );
					appendLeftBrace( );
					appendNewline( );
					incrementIndent( );
				}
			}

			public boolean encloses( ExceptionTableEntry en )
			{
				if ( en == null )
				{
					return false;
				}
				else
				{
					return en.getStartPc( ) >= startOffset && en.getEndPc( ) <= endOffset;
				}
			}

			public int getEndLine( )
			{
				return endLine;
			}

			public int getStartLine( )
			{
				return startLine;
			}

		}

		protected class CatchBlock extends FinallyBlock
		{

			protected String catchType;
			protected String variableName;

			public CatchBlock( TryBlock parent, ExceptionTableEntry en )
			{
				super( parent, en.getHandlerPc( ) );
				startKeyWord = JavaKeywords.CATCH;

				if ( constantPool == null )
				{
					catchType = String.valueOf( ByteCodeConstants.UNKNOWN_INFORMATION );
				}
				else
				{
					AbstractConstantPoolEntry cpInfo = constantPool[en.getCatchType( )];
					if ( cpInfo instanceof ConstantClassInfo )
					{
						ConstantClassInfo constantClassInfo = (ConstantClassInfo) cpInfo;
						String name = ( (ConstantUtf8Info) constantPool[constantClassInfo.getNameIndex( )] )
								.getString( );
						catchType = name.replace( ByteCodeConstants.CLASS_NAME_SLASH, JavaLexicalConstants.DOT );
					}
				}
			}

			@Override
			public void appendStart( )
			{
				startLine = AbstractClassFileDocument.this.lineCount;

				if ( renderTryCatchFinallyBlocks )
				{
					append( JavaKeywords.CATCH );
					appendSpace( );
					appendLeftParenthesis( );

					if ( catchType != null )
					{
						append( catchType );
					}

					appendRightParenthesis( );
					appendSpace( );
					appendLeftBrace( );

					appendStartComment( );

					appendNewline( );
					incrementIndent( );
				}
			}

		}

		protected class FinallyBlock extends Block
		{

			public FinallyBlock( TryBlock parent, int start )
			{
				super( parent, JavaKeywords.FINALLY, start );
			}

			public FinallyBlock( TryBlock parent, int start, int end )
			{
				super( parent, JavaKeywords.FINALLY, start, end );
			}

			@Override
			public void appendStart( )
			{
				startLine = AbstractClassFileDocument.this.lineCount;

				if ( renderTryCatchFinallyBlocks )
				{
					append( startKeyWord );
					appendSpace( );
					appendLeftBrace( );

					appendStartComment( );

					appendNewline( );
					incrementIndent( );
				}
			}

			protected void appendStartComment( )
			{
				appendPaddedCommentBegin( );
				appendSpace( );
				append( formatCoversBytesXToY.format( new Object[]{
						enclosingTryBlock.startOffset, enclosingTryBlock.endOffset
				} ) );
				appendSpace( );
				appendCommentEnd( );
			}

		}

		protected class TryBlock extends Block implements ITryBlock
		{

			protected List<CatchBlock> catchBlocks;
			protected int enclosedGotoTarget = ByteCodeConstants.INVALID_OFFSET;
			protected List<TryBlock> enclosedTryBlocks;

			private FinallyBlock finallyBlock;

			public TryBlock( ExceptionTableEntry en )
			{
				this( null, en.getStartPc( ), en.getEndPc( ) );
				// if (tryBlocks == null) {
				// tryBlocks = new ArrayList<ITryBlock>();
				// }
				// tryBlocks.add(this);

				adoptHandler( en );
			}

			public TryBlock( TryBlock parent, int start, int end )
			{
				super( parent, JavaKeywords.TRY, start, end );
				// if (tryBlocks == null) {
				// tryBlocks = new ArrayList<ITryBlock>();
				// }
				// tryBlocks.add(this);
			}

			private void adoptHandler( ExceptionTableEntry en )
			{
				if ( en.getCatchType( ) == 0 )
				{
					/* finally handler */
					if ( finallyBlock != null )
					{
						/* this should not happen */
						throw new IllegalStateException( );
					}
					finallyBlock = new FinallyBlock( this, en.getHandlerPc( ) );
				}
				else
				{
					/* the catch block */
					if ( catchBlocks == null )
					{
						catchBlocks = new ArrayList<CatchBlock>( );
					}
					catchBlocks.add( new CatchBlock( this, en ) );
				}
			}

			public void computeHandlersEnds( Map<Integer, AbstractInstruction> offsetInstructionMap )
			{
				if ( enclosedTryBlocks != null )
				{
					for ( TryBlock t : enclosedTryBlocks )
					{
						t.computeHandlersEnds( offsetInstructionMap );
					}
				}

				if ( catchBlocks != null )
				{
					CatchBlock lastCatchBlock = null;
					for ( CatchBlock c : catchBlocks )
					{
						if ( lastCatchBlock != null && lastCatchBlock.endOffset == ByteCodeConstants.INVALID_OFFSET )
						{
							lastCatchBlock.endOffset = c.startOffset;

							blockStarts.putToList( lastCatchBlock.startOffset, lastCatchBlock );
							blockEnds.putToList( lastCatchBlock.endOffset, lastCatchBlock );
						}
						lastCatchBlock = c;
					}
					if ( lastCatchBlock != null && lastCatchBlock.endOffset == ByteCodeConstants.INVALID_OFFSET )
					{

						AbstractInstruction inst = offsetInstructionMap.get( endOffset );
						int gotoTarget = Integer.MAX_VALUE;
						if ( inst instanceof BranchInstruction )
						{
							BranchInstruction bi = (BranchInstruction) inst;
							gotoTarget = bi.getOffset( ) + bi.getBranchOffset( );
							if ( enclosingTryBlock.enclosedGotoTarget == ByteCodeConstants.INVALID_OFFSET )
							{
								enclosingTryBlock.enclosedGotoTarget = gotoTarget;
							}
						}
						lastCatchBlock.endOffset = Math.min( enclosingTryBlock.endOffset, gotoTarget );
						blockStarts.putToList( lastCatchBlock.startOffset, lastCatchBlock );
						blockEnds.putToList( lastCatchBlock.endOffset, lastCatchBlock );

					}
				}

				if ( finallyBlock != null )
				{
					AbstractInstruction inst = offsetInstructionMap.get( endOffset );
					int gotoTarget = Integer.MAX_VALUE;
					if ( inst instanceof BranchInstruction )
					{
						if ( inst instanceof BranchInstruction )
						{
							BranchInstruction bi = (BranchInstruction) inst;
							gotoTarget = bi.getOffset( ) + bi.getBranchOffset( );
						}
					}
					if ( enclosingTryBlock.endOffset >= gotoTarget )
					{
						finallyBlock.endOffset = gotoTarget;
					}
					else
					{
						/*
						 * the above did not function we have to find some
						 * better way
						 */
						if ( this.enclosedGotoTarget != ByteCodeConstants.INVALID_OFFSET )
						{
							finallyBlock.endOffset = Math.min( enclosingTryBlock.endOffset, this.enclosedGotoTarget );
						}
						else
						{
							finallyBlock.endOffset = enclosingTryBlock.endOffset;
						}
					}
					blockStarts.putToList( finallyBlock.startOffset, finallyBlock );
					blockEnds.putToList( finallyBlock.endOffset, finallyBlock );
				}

				blockStarts.putToList( startOffset, this );
				blockEnds.putToList( endOffset, this );

			}

			public TryBlock createEnclosedTryBlock( ExceptionTableEntry en )
			{
				if ( en.getStartPc( ) < startOffset || en.getEndPc( ) > endOffset )
				{
					throw new IllegalArgumentException( );
				}
				TryBlock result = null;
				if ( en.getStartPc( ) == startOffset
						&& en.getEndPc( ) == endOffset
						&& ( en.getCatchType( ) != 0 || this.finallyBlock == null ) )
				{
					/*
					 * this covers the same area and the handler to add is
					 * either not finally or finallyBlock is not set yet only
					 * add handlers
					 */
					result = this;
				}
				else
				{
					/* enclosed try block */
					result = new TryBlock( this, en.getStartPc( ), en.getEndPc( ) );
					if ( enclosedTryBlocks == null )
					{
						enclosedTryBlocks = new ArrayList<TryBlock>( );
					}
					enclosedTryBlocks.add( result );
				}

				result.adoptHandler( en );

				return result;
			}

			public List<Integer> getExceptionHandlerLines( )
			{
				int cnt = 0;
				if ( catchBlocks != null )
				{
					cnt += catchBlocks.size( );
				}
				if ( finallyBlock != null )
				{
					cnt++;
				}
				if ( cnt > 0 )
				{
					ArrayList<Integer> result = new ArrayList<Integer>( cnt );
					if ( catchBlocks != null )
					{
						for ( CatchBlock cb : catchBlocks )
						{
							result.add( Integer.valueOf( cb.getStartLine( ) ) );
						}
					}
					if ( finallyBlock != null )
					{
						result.add( Integer.valueOf( finallyBlock.getStartLine( ) ) );
					}
					return result;
				}
				else
				{
					return null;
				}
			}

		}

		protected int access;
		private ListMap<Integer, Block> blockEnds = new ListMap<Integer, Block>( );
		private ListMap<Integer, Block> blockStarts = new ListMap<Integer, Block>( );

		protected int currentLineNumberTableIndex = 0;

		protected String descriptor;

		protected String[] exceptions;

		protected int firstLine = ByteCodeConstants.INVALID_LINE;

		private ArrayList<IInstructionLine> instructionLines = new ArrayList<IInstructionLine>( );

		private List<AbstractInstruction> instructions;

		protected boolean isConstructor = false;

		protected int lastLine = ByteCodeConstants.INVALID_LINE;

		private LineNumberTableEntry[] lineNumberTable;
		private ExceptionTableEntry[] exceptionTable;
		protected ILocalVariableTable localVariableTable;

		/**
		 * The {@link MethodVisitor} to which this visitor delegates calls. May
		 * be <tt>null</tt>.
		 */
		protected MethodVisitor mv;
		protected String name;
		private Map<Integer, AbstractInstruction> offsetInstructionMap;
		private TryBlock openedTryBlock;
		private List<TryBlock> rootTryBlocks;
		protected String signature;
		private int max_stack = ByteCodeConstants.INVALID_OFFSET;
		private int max_locals = ByteCodeConstants.INVALID_OFFSET;

		private List<ITryBlock> tryBlocks;

		/**
		 * Constructs a new {@link AbstractMethodRenderer}.
		 */
		public AbstractMethodRenderer( int access, String name, String descriptor, String signature,
				String[] exceptions )
		{
			this( access, name, descriptor, signature, exceptions, null );
		}

		/**
		 * Constructs a new {@link AbstractMethodRenderer}.
		 * 
		 * @param mv
		 *            the {@link MethodVisitor} to which this visitor delegates
		 *            calls. May be <tt>null</tt>.
		 */
		public AbstractMethodRenderer( int access, String name, String descriptor, String signature,
				String[] exceptions, final MethodVisitor mv )
		{
			super( com.drgarbage.asm.Opcodes.ASM4 );
			this.access = access;
			this.name = name;
			this.descriptor = descriptor;
			this.exceptions = exceptions;
			this.mv = mv;
		}

		protected abstract ILocalVariableTable createLocalVariableTable( boolean available );

		private TryBlock findEnclosingTryBlock( ExceptionTableEntry en )
		{
			TryBlock b = openedTryBlock;
			while ( b != null )
			{
				if ( b.encloses( en ) )
				{
					return b;
				}
				b = b.enclosingTryBlock;
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.drgarbage.asm.render.intf.IMethodSection#getLocalVariableTable()
		 */
		public ILocalVariableTable getLocalVariableTable( )
		{
			return localVariableTable;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.drgarbage.asm.render.intf.IMethodSection#
		 * isLocalVariableTableAvailable()
		 */
		public boolean isLocalVariableTableAvailable( )
		{
			if ( localVariableTable == null )
				return false;

			return localVariableTable.isAvailable( );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.drgarbage.asm.render.intf.IMethodSection#getExceptionTable()
		 */
		public ExceptionTableEntry[] getExceptionTable( )
		{
			return exceptionTable;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.drgarbage.asm.render.intf.IMethodSection#
		 * isExceptionTableAvailable()
		 */
		public boolean isExceptionTableAvailable( )
		{
			if ( exceptionTable == null || exceptionTable.length == 0 )
			{
				return false;
			}

			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.drgarbage.asm.render.intf.IMethodSection#getMaxStack()
		 */
		public int getMaxStack( )
		{
			return max_stack;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.drgarbage.asm.render.intf.IMethodSection#getMaxLocals()
		 */
		public int getMaxLocals( )
		{
			return max_locals;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.drgarbage.asm.render.intf.IMethodSection#findOffsetLine(int)
		 */
		public int findOffsetLine( int offset )
		{
			List<IInstructionLine> ils = getInstructionLines( );
			if ( ils != null && ils.size( ) > 0 )
			{
				for ( IInstructionLine il : ils )
				{
					if ( il.getInstruction( ).getOffset( ) == offset )
					{
						return il.getLine( );
					}
				}
			}
			return IInstructionLine.INVALID_LINE;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.drgarbage.asm.render.intf.IMethodSection#getDocLine(int)
		 */
		public int getBytecodeLine( int sourceCodeLine )
		{
			if ( lineNumberTable == null
					|| lineNumberTable.length == 0
					|| sourceCodeLine == ByteCodeConstants.INVALID_OFFSET

					/*
					 * if there are no instructions we will find nothing either
					 */
					|| instructionLines.size( ) == 0 )
			{
				return ByteCodeConstants.INVALID_OFFSET;
			}

			int offset = lookUpLineNumberTableForStartPC( sourceCodeLine );

			int byteCodeDocLine = ByteCodeConstants.INVALID_OFFSET;
			int n = instructionLines.size( );
			for ( int i = 0; i < n; i++ )
			{
				IInstructionLine il = instructionLines.get( i );

				if ( offset == il.getInstruction( ).getOffset( ) )
				{
					byteCodeDocLine = il.getLine( );
					break;
				}
			}

			/* not found */
			return byteCodeDocLine;
		}

		/**
		 * @return the descriptor
		 */
		public String getDescriptor( )
		{
			return descriptor;
		}

		// ------------------------------------------------------------------------
		// Implementation of the MethodVisitor interface
		// ------------------------------------------------------------------------

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.drgarbage.classfile.render.intf.IMethodSection#getFirstLine()
		 */
		public int getFirstLine( )
		{
			return firstLine;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.drgarbage.classfile.render.intf.IMethodSection#
		 * getInstructionLines()
		 */
		public List<IInstructionLine> getInstructionLines( )
		{
			return instructionLines;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.drgarbage.classfile.render.intf.IMethodSection#getLastLine()
		 */
		public int getLastLine( )
		{
			return lastLine;
		}

		/**
		 * @return the name
		 */
		public String getName( )
		{
			return name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.drgarbage.asm.render.intf.IMethodSection#getSourceCodeLine(int)
		 */
		public int getSourceCodeLine( int byteCodeDocLine )
		{

			if ( lineNumberTable == null
					|| lineNumberTable.length == 0
					|| byteCodeDocLine < firstLine
					|| byteCodeDocLine > lastLine

					/*
					 * if there are no instructions we will find nothing either
					 */
					|| instructionLines.size( ) == 0 )
			{
				return ByteCodeConstants.INVALID_OFFSET;
			}

			int offset = ByteCodeConstants.INVALID_OFFSET;
			int n = instructionLines.size( );
			for ( int i = 0; i < n; i++ )
			{
				IInstructionLine il = instructionLines.get( i );
				if ( byteCodeDocLine <= il.getLine( ) )
				{
					offset = il.getInstruction( ).getOffset( );
					break;
				}
			}
			if ( offset == ByteCodeConstants.INVALID_OFFSET )
			{
				/*
				 * if we have not found it yet we are after the last instruction
				 * let us use the offset of the last instruction
				 */
				offset = instructionLines.get( n - 1 ).getInstruction( ).getOffset( );
			}

			return lookUpLineNumberTable( offset );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.drgarbage.classfile.render.intf.IMethodSection#getTryBlocks()
		 */
		public List<ITryBlock> getTryBlocks( )
		{
			return tryBlocks;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.drgarbage.asm.render.intf.IMethodSection#hasCode()
		 */
		public boolean hasCode( )
		{
			return !isNative( ) && !isAbstract( );
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.drgarbage.asm.render.intf.IMethodSection#hasLineNumberTable()
		 */
		public boolean hasLineNumberTable( )
		{
			return lineNumberTable != null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.drgarbage.classfile.render.intf.IMethodSection#isAbstract()
		 */
		public boolean isAbstract( )
		{
			return ( access & ACC_ABSTRACT ) != 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.drgarbage.asm.render.intf.IMethodSection#isConstructor()
		 */
		public boolean isConstructor( )
		{
			return isConstructor;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.drgarbage.asm.render.intf.IMethodSection#isNative()
		 */
		public boolean isNative( )
		{
			return ( access & ACC_NATIVE ) != 0;
		}

		private int lookUpLineNumberTable( int offset )
		{
			if ( offset != ByteCodeConstants.INVALID_OFFSET )
			{

				/*
				 * Added by Sergej on 08.05.2008 FIX: if the lineNumberTable
				 * includes only one entry, then this entry is valid for all
				 * offsets.
				 */
				if ( lineNumberTable.length == 1 )
				{
					LineNumberTableEntry en = lineNumberTable[0];
					return en.getLineNumber( );
				}

				/*
				 * Added by Sergej on 09.05.2008 FIX: Check if the current
				 * offset > the startPC of the last entry. The last entry is not
				 * checked in the loop.
				 */
				LineNumberTableEntry en = lineNumberTable[lineNumberTable.length - 1];
				if ( offset > en.getStartPc( ) )
				{
					return en.getLineNumber( );
				}

				/* find offset */
				LineNumberTableEntry lastEntry = null;
				for ( int i = 0; i < lineNumberTable.length; i++ )
				{
					en = lineNumberTable[i];
					if ( offset == en.getStartPc( ) )
					{
						return en.getLineNumber( );
					}
					else if ( lastEntry != null && offset < en.getStartPc( ) )
					{
						return lastEntry.getLineNumber( );
					}
					lastEntry = en;
				}
			}

			/* not found */
			return ByteCodeConstants.INVALID_OFFSET;
		}

		/**
		 * Returns an offset of the startPC for the given sourceCodeLine.
		 * 
		 * @param sourceCodeLine
		 *            1-based source code line
		 * @return the offset of the startPC
		 */
		private int lookUpLineNumberTableForStartPC( int oneBasedsourceCodeLine )
		{
			if ( oneBasedsourceCodeLine != ByteCodeConstants.INVALID_OFFSET )
			{

				/* find startPointCode */
				LineNumberTableEntry lastEntry = null;
				for ( int i = 0; i < lineNumberTable.length; i++ )
				{
					LineNumberTableEntry en = lineNumberTable[i];
					if ( oneBasedsourceCodeLine == en.getLineNumber( ) )
					{
						return en.getStartPc( );
					}
					else if ( lastEntry != null && oneBasedsourceCodeLine < en.getLineNumber( ) )
					{
						return lastEntry.getStartPc( );
					}
					lastEntry = en;
				}
			}

			/* not found */
			return ByteCodeConstants.INVALID_OFFSET;
		}

		private void renderLineNumberTable( LineNumberTableEntry[] lineNumberTable )
		{
			if ( lineNumberTable == null )
			{
				appendNewline( );
				appendCommentBegin( );
				appendSpace( );
				sb.append( ByteCodeConstants.LINE_NUMBER_TABLE_NOT_AVAILABLE );
				appendSpace( );
				appendCommentEnd( );
				appendNewline( );
			}
			else
			{
				appendNewline( );

				TextTable tbl = new TextTable( new ByteCodeConstants.Align[]{
						ByteCodeConstants.Align.RIGHT, ByteCodeConstants.Align.RIGHT
				} );

				tbl.addRow( new String[]{
						ByteCodeConstants.START_PC, ByteCodeConstants.LINE_NUMBER
				} );

				for ( int i = 0; i < lineNumberTable.length; i++ )
				{
					LineNumberTableEntry en = lineNumberTable[i];
					tbl.addRow( new String[]{
							String.valueOf( en.getStartPc( ) ), String.valueOf( en.getLineNumber( ) )
					} );
				}

				tbl.recomputeColumnWidths( );
				tbl.beginRow( );
				tbl.appendValue( ByteCodeConstants.LINE_NUMBER_TABLE,
						ByteCodeConstants.Align.CENTER,
						tbl.computeWidth( ) );
				tbl.endRow( );
				tbl.render( );

			}
		}

		private void renderLocalVariableTable( )
		{
			if ( localVariableTable == null || !localVariableTable.isAvailable( ) )
			{
				appendNewline( );
				appendCommentBegin( );
				appendSpace( );
				sb.append( ByteCodeConstants.LOCAL_VARIABLE_TABLE_NOT_AVAILABLE );
				appendSpace( );
				appendCommentEnd( );
				appendNewline( );
			}
			else if ( localVariableTable.getLength( ) == 0 )
			{
				appendNewline( );
				appendCommentBegin( );
				appendSpace( );
				sb.append( ByteCodeConstants.LOCAL_VARIABLE_TABLE_EMPTY );
				appendSpace( );
				appendCommentEnd( );
				appendNewline( );
			}
			else
			{
				appendNewline( );

				String[] header = localVariableTable.getHeader( );
				ByteCodeConstants.Align[] alignments = new ByteCodeConstants.Align[header.length];
				for ( int i = 0; i < alignments.length; i++ )
				{
					alignments[i] = localVariableTable.getAlignment( i );
				}

				TextTable tbl = new TextTable( alignments );

				tbl.addRow( header );
				int len = localVariableTable.getLength( );
				if ( len > 0 )
				{
					for ( int i = 0; i < len; i++ )
					{
						tbl.addRow( localVariableTable.getRow( i ) );
					}
				}

				tbl.recomputeColumnWidths( );

				tbl.beginRow( );
				tbl.appendValue( ByteCodeConstants.LOCAL_VARIABLE_TABLE,
						ByteCodeConstants.Align.CENTER,
						tbl.computeWidth( ) );
				tbl.endRow( );

				tbl.render( );

			}
		}

		private void renderExceptionTable( )
		{
			if ( exceptionTable == null )
			{
				appendNewline( );
				appendCommentBegin( );
				appendSpace( );
				sb.append( ByteCodeConstants.EXCEPTION_TABLE_NOT_AVAILABLE );
				appendSpace( );
				appendCommentEnd( );
				appendNewline( );
			}
			else if ( exceptionTable.length == 0 )
			{
				appendNewline( );
				appendCommentBegin( );
				appendSpace( );
				sb.append( ByteCodeConstants.EXCEPTION_TABLE_EMPTY );
				appendSpace( );
				appendCommentEnd( );
				appendNewline( );
			}
			else
			{
				appendNewline( );

				TextTable tbl = new TextTable( new ByteCodeConstants.Align[]{
						ByteCodeConstants.Align.RIGHT,
						ByteCodeConstants.Align.RIGHT,
						ByteCodeConstants.Align.RIGHT,
						ByteCodeConstants.Align.RIGHT
				} );

				/*
				 * u2 exception_table_length; { u2 start_pc; u2 end_pc; u2
				 * handler_pc; u2 catch_type; }
				 * exception_table[exception_table_length];
				 */
				tbl.addRow( new String[]{
						ByteCodeConstants.START_PC,
						ByteCodeConstants.END_PC,
						ByteCodeConstants.HANDLER_PC,
						ByteCodeConstants.CATCH_TYPE
				} );

				for ( int i = 0; i < exceptionTable.length; i++ )
				{
					ExceptionTableEntry en = exceptionTable[i];

					String catch_type_class = String.valueOf( ByteCodeConstants.UNKNOWN_INFORMATION );

					int catch_type = en.getCatchType( );
					if ( catch_type == 0 )
					{
						catch_type_class = ByteCodeConstants.ANY_EXCEPTION;
					}
					else
					{
						AbstractConstantPoolEntry cpInfo = constantPool[catch_type];
						if ( cpInfo instanceof ConstantClassInfo )
						{
							ConstantClassInfo constantClassInfo = (ConstantClassInfo) cpInfo;
							String name = ( (ConstantUtf8Info) constantPool[constantClassInfo.getNameIndex( )] )
									.getString( );
							catch_type_class = name.replace( ByteCodeConstants.CLASS_NAME_SLASH,
									JavaLexicalConstants.DOT );
						}
					}

					tbl.addRow( new String[]{
							String.valueOf( en.getStartPc( ) ),
							String.valueOf( en.getEndPc( ) ),
							String.valueOf( en.getHandlerPc( ) ),
							catch_type_class
					} );
				}

				tbl.recomputeColumnWidths( );
				tbl.beginRow( );
				tbl.appendValue( ByteCodeConstants.EXCEPTION_TABLE,
						ByteCodeConstants.Align.CENTER,
						tbl.computeWidth( ) );
				tbl.endRow( );
				tbl.render( );

			}
		}

		private void renderMaxs( )
		{
			appendNewline( );
			appendCommentBegin( );
			appendSpace( );
			sb.append( ByteCodeConstants.MAX_STACK );
			appendColon( );
			appendSpace( );
			sb.append( max_stack );
			appendSpace( );
			sb.append( ByteCodeConstants.MAX_LOCALS );
			appendColon( );
			appendSpace( );
			sb.append( max_locals );
			appendSpace( );
			appendCommentEnd( );
			appendNewline( );
		}

		private void renderSignature( )
		{

			appendNewline( );

			if ( ( access & ACC_BRIDGE ) != 0 )
			{
				appendCommentBegin( );
				appendSpace( );
				sb.append( ByteCodeConstants.BRIDGE_METHOD_GENERATED_BY_THE_COMPILER );
				appendSpace( );
				appendCommentEnd( );
				appendNewline( );
			}
			appendDeprecated( access );

			/* changed to 0-based */
			this.firstLine = lineCount;

			appendAccess( access );
			if ( ( access & ACC_NATIVE ) != 0 )
			{
				sb.append( JavaKeywords.NATIVE );
				appendSpace( );
			}
			if ( ( access & ACC_VARARGS ) != 0 )
			{
				appendCommentBegin( );
				appendSpace( );
				sb.append( ByteCodeConstants.VARARGS );
				appendSpace( );
				appendCommentEnd( );
				appendSpace( );
			}

			if ( signature != null )
			{

				// TODO test generic signature
				SignatureRenderer v = new SignatureRenderer( 0 );
				SignatureReader r = new SignatureReader( signature );
				r.accept( v );
				String genericDecl = v.getDeclaration( );
				String genericReturn = v.getReturnType( );
				String genericExceptions = v.getExceptions( );

				sb.append( genericReturn );
				appendSpace( );
				sb.append( name );
				sb.append( genericDecl );

				// TODO will this generic exceptions get doubled with the normal
				// ones below?
				if ( genericExceptions != null )
				{
					appendSpace( );
					sb.append( genericExceptions );
				}

			}
			else
			{
				/* null signature */
				isConstructor = true;

				try
				{
					if ( ByteCodeConstants.INIT.equals( name ) )
					{
						/* constructor */
						BytecodeUtils.appendMethodDescriptor( classSimpleName,
								true,
								( access & ACC_STATIC ) != 0,
								descriptor,
								0,
								localVariableTable,
								constantPool,
								sb );
					}
					else if ( ByteCodeConstants.CLINIT.equals( name ) )
					{
						/* static block */
						/*
						 * remove the final space, that has been appended by
						 * appendAccessFlags()
						 */
						sb.setLength( sb.length( ) - 1 );
					}
					else
					{
						BytecodeUtils.appendMethodDescriptor( name,
								false,
								( access & ACC_STATIC ) != 0,
								descriptor,
								0,
								localVariableTable,
								constantPool,
								sb );
					}
				}
				catch ( IOException e )
				{
					throw new RuntimeException( e );
				}
			}

			if ( exceptions != null && exceptions.length > 0 )
			{
				appendSpace( );
				sb.append( JavaKeywords.THROWS );
				appendSpace( );
				for ( int i = 0; i < exceptions.length; ++i )
				{
					if ( i > 0 )
					{
						appendComma( );
						appendSpace( );
					}
					sb.append( exceptions[i].replace( ByteCodeConstants.CLASS_NAME_SLASH, JavaLexicalConstants.DOT ) );
				}
			}
			if ( !hasCode( ) )
			{
				/* no instructions for interface and abstract methods */
				appendSemicolon( );
				appendNewline( );
			}
			else
			{
				appendSpace( );
				appendLeftBrace( );
				appendNewline( );
				incrementIndent( );
			}
		}

		public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
		{
			if ( mv != null )
			{
				return mv.visitAnnotation( desc, visible );
			}
			return null;
		}

		public AnnotationVisitor visitAnnotationDefault( )
		{
			if ( mv != null )
			{
				return mv.visitAnnotationDefault( );
			}
			return null;
		}

		public void visitAttribute( final Attribute attr )
		{
			if ( mv != null )
			{
				mv.visitAttribute( attr );
			}
		}

		public void visitCode( )
		{
			if ( mv != null )
			{
				mv.visitCode( );
			}
		}

		public boolean visitCode( byte[] bytes )
		{
			return visitCode( bytes, 0, bytes.length );
		}

		public boolean visitCode( byte[] bytes, int offset, int length )
		{
			IndexedInstructionParser parser = new IndexedInstructionParser( bytes, offset, length );
			instructions = parser.parse( );
			offsetInstructionMap = parser.offsetInstructionMap;

			/* load try blocks */
			exceptionTable = parser.parseExceptionTable( );

			HashSet<String> attributeNames = new HashSet<String>( );
			attributeNames.add( ByteCodeConstants.LINE_NUMBER_TABLE );
			attributeNames.add( ByteCodeConstants.LOCAL_VARIABLE_TABLE );
			attributeNames.add( ByteCodeConstants.LOCAL_VARIABLE_TYPE_TABLE );

			Map<String, Object> attrs = parser.parseAttributes( attributeNames, constantPool );

			lineNumberTable = (LineNumberTableEntry[]) attrs.get( ByteCodeConstants.LINE_NUMBER_TABLE );

			LocalVariableTableEntry[] varTableEntries = (LocalVariableTableEntry[]) attrs
					.get( ByteCodeConstants.LOCAL_VARIABLE_TABLE );
			LocalVariableTypeTableEntry[] varTypeTable = (LocalVariableTypeTableEntry[]) attrs
					.get( ByteCodeConstants.LOCAL_VARIABLE_TYPE_TABLE );
			localVariableTable = createLocalVariableTable( varTableEntries != null );
			if ( varTableEntries != null && varTableEntries.length > 0 )
			{
				for ( LocalVariableTableEntry entry : varTableEntries )
				{
					localVariableTable.addLocalVariableTableEntry( entry );
				}
			}
			if ( varTypeTable != null && varTypeTable.length > 0 )
			{
				for ( LocalVariableTypeTableEntry entry : varTypeTable )
				{
					localVariableTable.addLocalVariableTypeTableEntry( entry );
				}
			}

			renderSignature( );

			if ( exceptionTable != null && exceptionTable.length > 0 )
			{

				rootTryBlocks = new ArrayList<TryBlock>( );

				ExceptionTableEntry[] sortedExceptionTable = new ExceptionTableEntry[exceptionTable.length];
				System.arraycopy( exceptionTable, 0, sortedExceptionTable, 0, exceptionTable.length );
				Arrays.sort( sortedExceptionTable, new ExceptionTableEntryComparator( ) );

				for ( ExceptionTableEntry en : sortedExceptionTable )
				{
					TryBlock b = findEnclosingTryBlock( en );
					TryBlock newTryBlock = null;
					if ( b == null )
					{
						/* this is a new root block */
						newTryBlock = new TryBlock( en );
						rootTryBlocks.add( newTryBlock );
					}
					else
					{
						newTryBlock = b.createEnclosedTryBlock( en );
					}
					openedTryBlock = newTryBlock;
				}

				TryBlock dummyParentTryBlock = new TryBlock( null, 0, length );
				for ( TryBlock tb : rootTryBlocks )
				{
					tb.enclosingTryBlock = dummyParentTryBlock;
					tb.computeHandlersEnds( offsetInstructionMap );
				}
			}

			for ( AbstractInstruction instruction : instructions )
			{

				/* starting try blocks */
				List<Block> blocks = blockStarts.get( instruction.getOffset( ) );
				if ( blocks != null )
				{
					for ( Block block : blocks )
					{
						block.appendStart( );
					}
				}

				InstructionRenderer r = null;
				if ( instruction instanceof SimpleInstruction )
				{
					r = new InstructionRenderer( (SimpleInstruction) instruction );
				}
				else if ( instruction instanceof AbstractInstruction )
				{
					r = new InstructionRenderer( (AbstractInstruction) instruction );
				}
				else
				{
					throw new RuntimeException(
							"Could not find renderer for '" + instruction.getClass( ).getName( ) + "'" );
				}

				try
				{
					r.render( this );
				}
				catch ( IOException e )
				{
					throw new RuntimeException( e );
				}

				if ( r instanceof IInstructionLine )
				{
					instructionLines.add( (IInstructionLine) r );
				}

				/* ending try blocks */
				blocks = blockEnds.get( instruction.getOffset( ) + instruction.getLength( ) );
				if ( blocks != null )
				{
					for ( Block block : blocks )
					{
						block.appendEnd( );
					}
				}
			}

			if ( showLineNumberTable )
			{
				renderLineNumberTable( lineNumberTable );
			}

			if ( showLocalVariableTable )
			{
				renderLocalVariableTable( );
			}

			if ( showExceptionTable )
			{
				renderExceptionTable( );
			}

			return true;
		}

		public void visitEnd( )
		{

			if ( !hasCode( ) )
			{
				/*
				 * signature of an abstract or native method we must call it
				 * here as visitCode(...) will not be called for abstract
				 * methods
				 */
				renderSignature( );
			}
			else
			{
				/* end of non-abstract method */
				decrementIndent( );
				appendRightBrace( );
				appendNewline( );
			}
			/* changed to 0-based */
			lastLine = lineCount - 1;

			if ( mv != null )
			{
				mv.visitEnd( );
			}

			/* add every method, no matter if it is abstract */
			methodSections.add( this );

			methodBorderLinesList.add( firstLine );
			methodBorderLinesList.add( lastLine );

		}

		public void visitFieldInsn( final int opcode, final String owner, final String name, final String desc )
		{
			if ( mv != null )
			{
				mv.visitFieldInsn( opcode, owner, name, desc );
			}
		}

		public void visitFrame( final int type, final int nLocal, final Object[] local, final int nStack,
				final Object[] stack )
		{
			if ( mv != null )
			{
				mv.visitFrame( type, nLocal, local, nStack, stack );
			}
		}

		public void visitIincInsn( final int var, final int increment )
		{
			if ( mv != null )
			{
				mv.visitIincInsn( var, increment );
			}
		}

		public void visitInsn( final int opcode )
		{
			if ( mv != null )
			{
				mv.visitInsn( opcode );
			}
		}

		public void visitIntInsn( final int opcode, final int operand )
		{
			if ( mv != null )
			{
				mv.visitIntInsn( opcode, operand );
			}
		}

		public void visitJumpInsn( final int opcode, final Label label )
		{
			if ( mv != null )
			{
				mv.visitJumpInsn( opcode, label );
			}
		}

		public void visitLabel( final Label label )
		{
			if ( mv != null )
			{
				mv.visitLabel( label );
			}
		}

		public void visitLdcInsn( final Object cst )
		{
			if ( mv != null )
			{
				mv.visitLdcInsn( cst );
			}
		}

		public void visitLineNumber( final int line, final Label start )
		{

			if ( mv != null )
			{
				mv.visitLineNumber( line, start );
			}
		}

		public void visitLocalVariable( final String name, final String desc, final String signature, final Label start,
				final Label end, final int index )
		{
			if ( mv != null )
			{
				mv.visitLocalVariable( name, desc, signature, start, end, index );
			}
		}

		public void visitLocalVariableTable( byte[] bytes, int offset, int entryCount )
		{
			// LocalVariableTableParser p = new LocalVariableTableParser(bytes,
			// offset, entryCount);
			// LocalVariableTableEntry[] tbl = p.parse();
			// for (LocalVariableTableEntry entry : tbl) {
			// System.out.println();
			// //addLocalVariableTableEntry(entry);
			// }
		}

		// public void addLocalVariableTableEntry(LocalVariableTableEntry entry)
		// {
		// indexedLocalVariableTable.put(entry.getIndex(), entry);
		// }

		public void visitLookupSwitchInsn( final Label dflt, final int[] keys, final Label[] labels )
		{
			if ( mv != null )
			{
				mv.visitLookupSwitchInsn( dflt, keys, labels );
			}
		}

		public void visitMaxs( final int maxStack, final int maxLocals )
		{
			if ( mv != null )
			{
				mv.visitMaxs( maxStack, maxLocals );
			}
			max_stack = maxStack;
			max_locals = maxLocals;

			if ( showMaxs )
			{
				renderMaxs( );
			}
		}

		public void visitMethodInsn( final int opcode, final String owner, final String name, final String desc )
		{
			if ( mv != null )
			{
				mv.visitMethodInsn( opcode, owner, name, desc );
			}
		}

		public void visitMultiANewArrayInsn( final String desc, final int dims )
		{
			if ( mv != null )
			{
				mv.visitMultiANewArrayInsn( desc, dims );
			}
		}

		public AnnotationVisitor visitParameterAnnotation( final int parameter, final String desc,
				final boolean visible )
		{
			if ( mv != null )
			{
				return mv.visitParameterAnnotation( parameter, desc, visible );
			}
			return null;
		}

		public void visitTableSwitchInsn( final int min, final int max, final Label dflt, final Label... labels )
		{
			if ( mv != null )
			{
				mv.visitTableSwitchInsn( min, max, dflt, labels );
			}
		}

		public void visitTryCatchBlock( final Label start, final Label end, final Label handler, final String type )
		{
			if ( mv != null )
			{
				mv.visitTryCatchBlock( start, end, handler, type );
			}
		}

		public void visitTypeInsn( final int opcode, final String type )
		{
			if ( mv != null )
			{
				mv.visitTypeInsn( opcode, type );
			}
		}

		public void visitVarInsn( final int opcode, final int var )
		{
			if ( mv != null )
			{
				mv.visitVarInsn( opcode, var );
			}
		}

		public IInstructionLine findInstructionLine( int sourceCodeLine )
		{
			if ( lineNumberTable == null
					|| lineNumberTable.length == 0
					|| sourceCodeLine == ByteCodeConstants.INVALID_OFFSET

					/*
					 * if there are no instructions we will find nothing either
					 */
					|| instructionLines.size( ) == 0 )
			{
				return null;
			}

			int offset = lookUpLineNumberTableForStartPC( sourceCodeLine + 1 );
			if ( offset != ByteCodeConstants.INVALID_OFFSET )
			{

				List<IInstructionLine> lines = getInstructionLines( );
				for ( IInstructionLine line : lines )
				{
					if ( offset == line.getInstruction( ).getOffset( ) )
					{
						return line;
					}
				}

			}
			return null;
		}

	}

	protected class AnnotationRenderer extends AnnotationVisitor
	{

		/**
		 * The {@link AnnotationVisitor} to which this visitor delegates calls.
		 * May be <tt>null</tt>.
		 */
		protected AnnotationVisitor av;

		private int valueNumber = 0;

		/**
		 * Constructs a new {@link AnnotationRenderer}.
		 */
		public AnnotationRenderer( )
		{
			super( com.drgarbage.asm.Opcodes.ASM4 );
		}

		// ------------------------------------------------------------------------
		// Implementation of the AnnotationVisitor interface
		// ------------------------------------------------------------------------

		private void appendComa( final int i )
		{
			if ( i != 0 )
			{
				sb.append( ", " );
			}
		}

		public void visit( final String name, final Object value )
		{
			// sb.setLength(0);
			appendComa( valueNumber++ );

			if ( name != null )
			{
				sb.append( name ).append( '=' );
			}

			if ( value instanceof String )
			{
				visitString( (String) value );
			}
			else if ( value instanceof Type )
			{
				visitType( (Type) value );
			}
			else if ( value instanceof Byte )
			{
				visitByte( ( (Byte) value ).byteValue( ) );
			}
			else if ( value instanceof Boolean )
			{
				visitBoolean( ( (Boolean) value ).booleanValue( ) );
			}
			else if ( value instanceof Short )
			{
				visitShort( ( (Short) value ).shortValue( ) );
			}
			else if ( value instanceof Character )
			{
				visitChar( ( (Character) value ).charValue( ) );
			}
			else if ( value instanceof Integer )
			{
				visitInt( ( (Integer) value ).intValue( ) );
			}
			else if ( value instanceof Float )
			{
				visitFloat( ( (Float) value ).floatValue( ) );
			}
			else if ( value instanceof Long )
			{
				visitLong( ( (Long) value ).longValue( ) );
			}
			else if ( value instanceof Double )
			{
				visitDouble( ( (Double) value ).doubleValue( ) );
			}
			else if ( value.getClass( ).isArray( ) )
			{
				sb.append( '{' );
				if ( value instanceof byte[] )
				{
					byte[] v = (byte[]) value;
					for ( int i = 0; i < v.length; i++ )
					{
						appendComa( i );
						visitByte( v[i] );
					}
				}
				else if ( value instanceof boolean[] )
				{
					boolean[] v = (boolean[]) value;
					for ( int i = 0; i < v.length; i++ )
					{
						appendComa( i );
						visitBoolean( v[i] );
					}
				}
				else if ( value instanceof short[] )
				{
					short[] v = (short[]) value;
					for ( int i = 0; i < v.length; i++ )
					{
						appendComa( i );
						visitShort( v[i] );
					}
				}
				else if ( value instanceof char[] )
				{
					char[] v = (char[]) value;
					for ( int i = 0; i < v.length; i++ )
					{
						appendComa( i );
						visitChar( v[i] );
					}
				}
				else if ( value instanceof int[] )
				{
					int[] v = (int[]) value;
					for ( int i = 0; i < v.length; i++ )
					{
						appendComa( i );
						visitInt( v[i] );
					}
				}
				else if ( value instanceof long[] )
				{
					long[] v = (long[]) value;
					for ( int i = 0; i < v.length; i++ )
					{
						appendComa( i );
						visitLong( v[i] );
					}
				}
				else if ( value instanceof float[] )
				{
					float[] v = (float[]) value;
					for ( int i = 0; i < v.length; i++ )
					{
						appendComa( i );
						visitFloat( v[i] );
					}
				}
				else if ( value instanceof double[] )
				{
					double[] v = (double[]) value;
					for ( int i = 0; i < v.length; i++ )
					{
						appendComa( i );
						visitDouble( v[i] );
					}
				}
				sb.append( '}' );
			}

			// text.add(sb.toString());

			if ( av != null )
			{
				av.visit( name, value );
			}
		}

		// FIXME Annotations rendering
		public AnnotationVisitor visitAnnotation( final String name, final String desc )
		{
			// sb.setLength(0);
			appendComa( valueNumber++ );
			if ( name != null )
			{
				sb.append( name ).append( '=' );
			}
			sb.append( '@' );
			sb.append( desc );
			sb.append( '(' );
			// text.add(sb.toString());
			AnnotationRenderer tav = createTraceAnnotationVisitor( );
			// sb.append(tav.getText());
			sb.append( ")" );
			if ( av != null )
			{
				tav.av = av.visitAnnotation( name, desc );
			}
			return tav;
		}

		// FIXME Annotations rendering
		public AnnotationVisitor visitArray( final String name )
		{
			// sb.setLength(0);
			appendComa( valueNumber++ );
			if ( name != null )
			{
				sb.append( name ).append( '=' );
			}
			sb.append( '{' );
			// text.add(sb.toString());
			AnnotationRenderer tav = createTraceAnnotationVisitor( );
			// sb.append(tav.getText());
			sb.append( "}" );
			if ( av != null )
			{
				tav.av = av.visitArray( name );
			}
			return tav;
		}

		private void visitBoolean( final boolean value )
		{
			sb.append( value );
		}

		private void visitByte( final byte value )
		{
			sb.append( "(byte)" ).append( value );
		}

		private void visitChar( final char value )
		{
			sb.append( "(char)" ).append( (int) value );
		}

		private void visitDouble( final double value )
		{
			sb.append( value ).append( 'D' );
		}

		public void visitEnd( )
		{
			if ( av != null )
			{
				av.visitEnd( );
			}
		}

		public void visitEnum( final String name, final String desc, final String value )
		{
			// FIXME annotation rendering
			appendComa( valueNumber++ );
			if ( name != null )
			{
				sb.append( name ).append( '=' );
			}
			sb.append( desc );
			sb.append( '.' ).append( value );
			// text.add(sb.toString());

			if ( av != null )
			{
				av.visitEnum( name, desc, value );
			}
		}

		private void visitFloat( final float value )
		{
			sb.append( value ).append( 'F' );
		}

		private void visitInt( final int value )
		{
			sb.append( value );
		}

		private void visitLong( final long value )
		{
			sb.append( value ).append( 'L' );
		}

		private void visitShort( final short value )
		{
			sb.append( "(short)" ).append( value );
		}

		private void visitString( final String value )
		{
			BytecodeUtils.appendString( sb, value );
		}

		// ------------------------------------------------------------------------
		// Utility methods
		// ------------------------------------------------------------------------

		private void visitType( final Type value )
		{
			sb.append( value.getClassName( ) ).append( ".class" );
		}
	}

	protected class FieldRenderer extends FieldVisitor implements IFieldSection
	{

		/**
		 * Type signature.
		 */
		private String descriptor;
		protected int documentLine = ByteCodeConstants.INVALID_OFFSET;
		/**
		 * The {@link FieldVisitor} to which this visitor delegates calls. May
		 * be <tt>null</tt>.
		 */
		protected FieldVisitor fv;
		protected String name;

		public FieldRenderer( String name, String descriptor, int documentLine )
		{
			super( com.drgarbage.asm.Opcodes.ASM4 );
			this.name = name;
			this.descriptor = descriptor;
			this.documentLine = documentLine;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.drgarbage.classfile.render.intf.IOutlineElement#
		 * getBytecodeDocumentLine()
		 */
		public int getBytecodeDocumentLine( )
		{
			return documentLine;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jdt.core.IField#getTypeSignature()
		 */
		public String getDescriptor( )
		{
			return descriptor;
		}

		public String getName( )
		{
			return name;
		}

		public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
		{
			AnnotationVisitor av = visitAnnotationImpl( desc, visible );
			if ( fv != null )
			{
				( (AnnotationRenderer) av ).av = fv.visitAnnotation( desc, visible );
			}
			return av;
		}

		public void visitAttribute( final Attribute attr )
		{
			visitAttributeImpl( attr );

			if ( fv != null )
			{
				fv.visitAttribute( attr );
			}
		}

		public void visitEnd( )
		{
			if ( fv != null )
			{
				fv.visitEnd( );
			}
		}
	}

	public static class IndexedInstructionParser extends InstructionParser
	{

		private HashMap<Integer, AbstractInstruction> offsetInstructionMap = new HashMap<Integer, AbstractInstruction>( );

		public IndexedInstructionParser( byte[] code, int offset, int length )
		{
			super( code, offset, length );
		}

		@Override
		protected AbstractInstruction parseNextInstruction( boolean wide ) throws IOException
		{
			AbstractInstruction instruction = super.parseNextInstruction( wide );
			offsetInstructionMap.put( instruction.getOffset( ), instruction );
			return instruction;
		}

	}

	protected class InstructionRenderer implements IInstructionLine
	{

		protected boolean commentOpened = false;
		protected AbstractInstruction instruction;

		private int line = INVALID_LINE;

		private AbstractMethodRenderer methodRenderer;

		public InstructionRenderer( AbstractInstruction instruction )
		{
			this.instruction = instruction;
		}

		protected void appendComment( )
		{
			try
			{
				if ( instruction instanceof ILocalVariableIndexProvider )
				{
					if ( methodRenderer != null )
					{

						String name = methodRenderer.localVariableTable.findArgName(
								( (ILocalVariableIndexProvider) instruction ).getLocalVariableIndex( ),
								instruction.getOffset( ),
								methodRenderer.isConstructor( ),
								methodRenderer.isAbstract( ) );

						if ( name != null )
						{
							openCommentIfNeeded( );
							if ( instruction instanceof IncrementInstruction )
							{
								appendSpace( );
								append( name );
								int incr = ( (IncrementInstruction) instruction ).getIncrementConst( );
								char sign = incr >= 0 ? JavaLexicalConstants.PLUS : JavaLexicalConstants.MINUS;
								if ( Math.abs( incr ) == 1 )
								{
									append( sign );
									append( sign );
								}
								else
								{
									appendSpace( );
									append( sign );
									append( JavaLexicalConstants.EQUALS );
									appendSpace( );
									append( Math.abs( incr ) );
								}
							}
							else
							{
								appendSpace( );
								append( name );
							}
							appendSpace( );
						}
					}
				}
				if ( instruction instanceof IConstantPoolIndexProvider )
				{
					if ( methodRenderer != null && constantPool != null )
					{

						AbstractConstantPoolEntry cpInfo = constantPool[( (IConstantPoolIndexProvider) instruction )
								.getConstantPoolIndex( )];

						String const_ = null;
						if ( cpInfo instanceof ConstantFloatInfo )
						{
							const_ = String.valueOf( ( (ConstantFloatInfo) cpInfo ).getFloat( ) );
						}
						else if ( cpInfo instanceof ConstantIntegerInfo )
						{
							const_ = String.valueOf( ( (ConstantIntegerInfo) cpInfo ).getInt( ) );
						}
						else if ( cpInfo instanceof ConstantDoubleInfo )
						{
							const_ = String.valueOf( ( (ConstantDoubleInfo) cpInfo ).getDouble( ) );
						}
						else if ( cpInfo instanceof ConstantLongInfo )
						{
							const_ = String.valueOf( ( (ConstantLongInfo) cpInfo ).getLong( ) );
						}
						else if ( cpInfo instanceof ConstantStringInfo )
						{
							int i = ( (ConstantStringInfo) cpInfo ).getStringIndex( );
							StringBuffer buf = new StringBuffer( );
							BytecodeUtils.appendString( buf, ( (ConstantUtf8Info) constantPool[i] ).getString( ) );
							const_ = buf.toString( );
						}
						else if ( cpInfo instanceof ConstantFieldrefInfo )
						{
							ConstantFieldrefInfo constantFieldrefInfo = (ConstantFieldrefInfo) cpInfo;
							if ( instruction.getOpcode( ) == Opcodes.GETSTATIC
									|| instruction.getOpcode( ) == Opcodes.PUTSTATIC )
							{
								ConstantClassInfo constantClassInfo = (ConstantClassInfo) constantPool[constantFieldrefInfo
										.getClassIndex( )];
								String name = ( (ConstantUtf8Info) constantPool[constantClassInfo.getNameIndex( )] )
										.getString( );
								const_ = name.replace( ByteCodeConstants.CLASS_NAME_SLASH, JavaLexicalConstants.DOT );
							}
							else
							{
								const_ = "";
							}
							ConstantNameAndTypeInfo constantNameAndTypeInfo = (ConstantNameAndTypeInfo) constantPool[constantFieldrefInfo
									.getNameAndTypeIndex( )];
							String fieldName = ( (ConstantUtf8Info) constantPool[constantNameAndTypeInfo
									.getNameIndex( )] ).getString( );
							const_ += JavaLexicalConstants.DOT + fieldName;
						}
						else if ( cpInfo instanceof ConstantMethodrefInfo )
						{
							ConstantMethodrefInfo constantMethodrefInfo = (ConstantMethodrefInfo) cpInfo;
							ConstantNameAndTypeInfo constantNameAndTypeInfo = (ConstantNameAndTypeInfo) constantPool[constantMethodrefInfo
									.getNameAndTypeIndex( )];
							String methodName = ( (ConstantUtf8Info) constantPool[constantNameAndTypeInfo
									.getNameIndex( )] ).getString( );
							if ( ByteCodeConstants.INIT.equals( methodName ) )
							{
								ConstantClassInfo constantClassInfo = (ConstantClassInfo) constantPool[constantMethodrefInfo
										.getClassIndex( )];
								String name = ( (ConstantUtf8Info) constantPool[constantClassInfo.getNameIndex( )] )
										.getString( );
								methodName = name.replace( ByteCodeConstants.CLASS_NAME_SLASH,
										JavaLexicalConstants.DOT );
							}
							else if ( instruction.getOpcode( ) == Opcodes.INVOKESTATIC )
							{
								ConstantClassInfo constantClassInfo = (ConstantClassInfo) constantPool[constantMethodrefInfo
										.getClassIndex( )];
								String name = ( (ConstantUtf8Info) constantPool[constantClassInfo.getNameIndex( )] )
										.getString( );
								methodName = name.replace( ByteCodeConstants.CLASS_NAME_SLASH,
										JavaLexicalConstants.DOT ) + JavaLexicalConstants.DOT + methodName;
							}
							StringBuilder sb = new StringBuilder( );

							boolean isConstructor = instruction.getOpcode( ) == Opcodes.INVOKESPECIAL;
							boolean isStatic = false;

							String descriptor = ( (ConstantUtf8Info) constantPool[constantNameAndTypeInfo
									.getDescriptorIndex( )] ).getString( );
							BytecodeUtils.appendMethodDescriptor( methodName,
									isConstructor,
									isStatic,
									descriptor,
									0,
									methodRenderer.localVariableTable,
									constantPool,
									sb );
							// ClassFileDocument.appendMethodDescriptor(methodName,
							// isConstructor, isStatic,
							// constantMethodrefInfo.getNameAndTypeInfo().getDescriptor(),
							// methodRenderer.getIndexedLocalVariableTable(),
							// methodRenderer.methodInfo.getClassFile(), 0, sb);
							const_ = sb.toString( );

						}
						else if ( cpInfo instanceof ConstantInterfaceMethodrefInfo )
						{
							ConstantInterfaceMethodrefInfo constantInterfaceMethodrefInfo = (ConstantInterfaceMethodrefInfo) cpInfo;
							ConstantNameAndTypeInfo constantNameAndTypeInfo = (ConstantNameAndTypeInfo) constantPool[constantInterfaceMethodrefInfo
									.getNameAndTypeIndex( )];
							String methodName = ( (ConstantUtf8Info) constantPool[constantNameAndTypeInfo
									.getNameIndex( )] ).getString( );
							StringBuilder sb = new StringBuilder( );

							boolean isStatic = false;

							String descriptor = ( (ConstantUtf8Info) constantPool[constantNameAndTypeInfo
									.getDescriptorIndex( )] ).getString( );
							BytecodeUtils.appendMethodDescriptor( methodName,
									false,
									isStatic,
									descriptor,
									0,
									methodRenderer.localVariableTable,
									constantPool,
									sb );
							const_ = sb.toString( );

						}
						else if ( cpInfo instanceof ConstantClassInfo )
						{
							/*
							 * the constructor info should be clear from the
							 * subsequent invokespecial
							 */
							// ConstantClassInfo constantClassInfo =
							// (ConstantClassInfo) cpInfo;
							// String methodName =
							// constantClassInfo.getName().replace(ByteCodeConstants.CLASS_NAME_SLASH,
							// JavaLexicalConstants.DOT);
							// const_ = JavaKeywords.NEW +
							// JavaLexicalConstants.SPACE + methodName;
							ConstantClassInfo constantClassInfo = (ConstantClassInfo) cpInfo;
							if ( instruction.getOpcode( ) == Opcodes.ANEWARRAY )
							{

								String className = BytecodeUtils.resolveConstantPoolTypeName( constantClassInfo,
										constantPool );

								// String className =
								// ((ConstantUtf8Info)constantPool[constantClassInfo.getNameIndex()]).getString();
								className = className.replace( ByteCodeConstants.CLASS_NAME_SLASH,
										JavaLexicalConstants.DOT );
								StringBuilder sb = new StringBuilder( );
								sb.append( JavaKeywords.NEW );
								sb.append( JavaLexicalConstants.SPACE );
								sb.append( className );
								sb.append( JavaLexicalConstants.LEFT_SQUARE_BRACKET );
								sb.append( JavaLexicalConstants.RIGHT_SQUARE_BRACKET );
								const_ = sb.toString( );
							}
							else if ( instruction.getOpcode( ) == Opcodes.MULTIANEWARRAY )
							{
								String className = ( (ConstantUtf8Info) constantPool[constantClassInfo
										.getNameIndex( )] ).getString( );
								StringBuilder sb = new StringBuilder( );
								sb.append( JavaKeywords.NEW );
								sb.append( JavaLexicalConstants.SPACE );
								BytecodeUtils.appendFieldDescriptor( className, 0, sb );
								const_ = sb.toString( );
							}
							else if ( instruction.getOpcode( ) == Opcodes.CHECKCAST )
							{
								String className = ( (ConstantUtf8Info) constantPool[constantClassInfo
										.getNameIndex( )] ).getString( );
								const_ = className.replace( ByteCodeConstants.CLASS_NAME_SLASH,
										JavaLexicalConstants.DOT );
							}
							else if ( instruction.getOpcode( ) == Opcodes.INSTANCEOF )
							{
								String className = ( (ConstantUtf8Info) constantPool[constantClassInfo
										.getNameIndex( )] ).getString( );
								if ( className.charAt( 0 ) == ByteCodeConstants.ARRAY_BEGINNING_BRACKET )
								{
									StringBuilder sbb = new StringBuilder( );
									BytecodeUtils.appendArrayDescriptor( className, 0, sbb );
									className = sbb.toString( );
								}
								else
								{
									className = className.replace( ByteCodeConstants.CLASS_NAME_SLASH,
											JavaLexicalConstants.DOT );
								}
								const_ = JavaKeywords.INSTANCEOF + JavaLexicalConstants.SPACE + className;
							}
						}
						else if ( cpInfo instanceof ConstantMethodHandleInfo )
						{
							// ConstantMethodHandleInfo constantMethodHandleInfo
							// = (ConstantMethodHandleInfo) cpInfo;
							// int index =
							// constantMethodHandleInfo.getReferenceIndex();
						}
						else if ( cpInfo instanceof ConstantMethodTypeInfo )
						{
							ConstantMethodTypeInfo constantMethodTypeInfo = (ConstantMethodTypeInfo) cpInfo;
							String descriptor = ( (ConstantUtf8Info) constantPool[constantMethodTypeInfo
									.getDescriptorIndex( )] ).getString( );
							const_ = descriptor;
						}
						else if ( cpInfo instanceof ConstantInvokeDynamicInfo )
						{
							ConstantInvokeDynamicInfo constantInvokeDynamicInfo = (ConstantInvokeDynamicInfo) cpInfo;
							ConstantNameAndTypeInfo nameAndTypeInfo = (ConstantNameAndTypeInfo) constantPool[constantInvokeDynamicInfo
									.getNameAndTypeIndex( )];
							String nameAndType_name = ( (ConstantUtf8Info) constantPool[nameAndTypeInfo
									.getNameIndex( )] ).getString( );
							String nameAndType_descr = ( (ConstantUtf8Info) constantPool[nameAndTypeInfo
									.getDescriptorIndex( )] ).getString( );

							StringBuilder sb = new StringBuilder( );
							boolean isConstructor = false;
							boolean isStatic = false;
							BytecodeUtils.appendMethodDescriptor( nameAndType_name,
									isConstructor,
									isStatic,
									nameAndType_descr,
									0,
									methodRenderer.localVariableTable,
									constantPool,
									sb );
							const_ = sb.toString( );
						}
						else
						{
							throw new RuntimeException( "invalid type " + cpInfo.getClass( ) );
						}
						if ( const_ != null )
						{
							openCommentIfNeeded( );
							appendSpace( );
							append( const_ );
							appendSpace( );
						}
					}
				}
				// FIX: bug #5
				// if (instruction instanceof BranchInstruction) {
				// if (methodRenderer != null) {
				// BranchInstruction bi = (BranchInstruction) instruction;
				// openCommentIfNeeded();
				//
				// appendSpace();
				// append(bi.getOffset());
				// appendSpace();
				// append(JavaLexicalConstants.PLUS);
				// appendSpace();
				// append(bi.getBranchOffset());
				// appendSpace();
				// append(JavaLexicalConstants.EQUALS);
				// appendSpace();
				// append(bi.getBranchOffset() + bi.getOffset());
				// appendSpace();
				// }
				// }
				if ( instruction instanceof NewArrayInstruction )
				{
					if ( methodRenderer != null )
					{
						NewArrayInstruction newArrayInstruction = (NewArrayInstruction) instruction;

						openCommentIfNeeded( );
						appendSpace( );
						append( JavaKeywords.NEW );
						appendSpace( );
						append( NewArrayInstruction.resolveType( newArrayInstruction.getImmediateByte( ) ) );
						append( JavaLexicalConstants.LEFT_SQUARE_BRACKET );
						append( JavaLexicalConstants.RIGHT_SQUARE_BRACKET );
						appendSpace( );

					}
				}

				closeCommentIfNeeded( );
			}
			catch ( IOException e )
			{
				throw new RuntimeException( e );
			}
		}

		private void appendInt( int i )
		{
			appendSpace( );
			append( String.valueOf( i ) );
		}

		protected void appendOperands( )
		{
			if ( instruction instanceof MultianewarrayInstruction )
			{
				appendInt( ( (MultianewarrayInstruction) instruction ).getImmediateShort( ) );
				appendInt( ( (MultianewarrayInstruction) instruction ).getDimensions( ) );
			}
			else if ( instruction instanceof IncrementInstruction )
			{
				appendInt( ( (IncrementInstruction) instruction ).getImmediateByte( ) );
				appendInt( ( (IncrementInstruction) instruction ).getIncrementConst( ) );
			}
			else if ( instruction instanceof InvokeInterfaceInstruction )
			{
				appendInt( ( (InvokeInterfaceInstruction) instruction ).getImmediateShort( ) );
				appendInt( ( (InvokeInterfaceInstruction) instruction ).getCount( ) );
			}
			else if ( instruction instanceof ImmediateByteInstruction )
			{
				appendInt( ( (ImmediateByteInstruction) instruction ).getImmediateByte( ) );
			}
			else if ( instruction instanceof ImmediateShortInstruction )
			{
				appendInt( ( (ImmediateShortInstruction) instruction ).getImmediateShort( ) );
			}
			else if ( instruction instanceof ImmediateIntInstruction )
			{
				appendInt( ( (ImmediateIntInstruction) instruction ).getImmediateInt( ) );
			}
			else if ( instruction instanceof BranchInstruction )
			{
				BranchInstruction bi = (BranchInstruction) instruction;
				int val = bi.getBranchOffset( );
				if ( !showRelativeBranchTargetOffsets )
				{
					/* compute absolute offset */
					val += bi.getOffset( );
				}
				appendInt( val );
			}
			else if ( instruction instanceof TableSwitchInstruction )
			{
				TableSwitchInstruction tsi = (TableSwitchInstruction) instruction;
				appendInt( tsi.getDefaultOffset( ) );
				appendInt( tsi.getLow( ) );
				appendInt( tsi.getHigh( ) );

				appendSpace( );
				append( JavaLexicalConstants.LEFT_SQUARE_BRACKET );
				int[] offs = tsi.getJumpOffsets( );
				for ( int i = 0; i < offs.length; i++ )
				{
					if ( i != 0 )
					{
						appendComma( );
						appendSpace( );
					}
					int val = offs[i];
					if ( !showRelativeBranchTargetOffsets )
					{
						/* compute absolute offset */
						val += tsi.getOffset( );
					}
					append( String.valueOf( val ) );
				}
				append( JavaLexicalConstants.RIGHT_SQUARE_BRACKET );
			}
			else if ( instruction instanceof LookupSwitchInstruction )
			{
				LookupSwitchInstruction lsi = (LookupSwitchInstruction) instruction;
				appendInt( lsi.getDefaultOffset( ) );

				List<MatchOffsetEntry> ens = lsi.getMatchOffsetPairs( );
				appendInt( ens.size( ) );

				appendSpace( );
				append( JavaLexicalConstants.LEFT_SQUARE_BRACKET );
				for ( int i = 0; i < ens.size( ); i++ )
				{
					if ( i != 0 )
					{
						appendComma( );
						appendSpace( );
					}
					MatchOffsetEntry en = ens.get( i );
					append( en.getMatch( ) );
					append( " => " );

					int val = en.getOffset( );
					if ( !showRelativeBranchTargetOffsets )
					{
						/* compute absolute offset */
						val += lsi.getOffset( );
					}
					append( String.valueOf( val ) );
				}
				append( JavaLexicalConstants.RIGHT_SQUARE_BRACKET );
			}
		}

		protected void closeCommentIfNeeded( )
		{
			if ( commentOpened )
			{
				appendCommentEnd( );
				commentOpened = false;
			}
		}

		public AbstractInstruction getInstruction( )
		{
			return instruction;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.drgarbage.asm.render.intf.IInstructionLine#getLine()
		 */
		public int getLine( )
		{
			return line;
		}

		protected void openCommentIfNeeded( )
		{
			if ( !commentOpened )
			{

				appendPaddedCommentBegin( );
				commentOpened = true;
			}
		}

		public void render( AbstractMethodRenderer methodRenderer ) throws IOException
		{
			this.methodRenderer = methodRenderer;

			if ( showSourceLineNumbers && methodRenderer.lineNumberTable != null )
			{
				int currentStartPc = ByteCodeConstants.INVALID_OFFSET;
				if ( methodRenderer.currentLineNumberTableIndex >= 0
						&& methodRenderer.currentLineNumberTableIndex < methodRenderer.lineNumberTable.length )
				{
					currentStartPc = methodRenderer.lineNumberTable[methodRenderer.currentLineNumberTableIndex]
							.getStartPc( );
				}

				if ( currentStartPc == instruction.getOffset( ) )
				{
					/* yes, this is the begining of a line */

					appendCommentBegin( );
					appendSpace( );
					append( ByteCodeConstants.L_LINE );
					append( methodRenderer.lineNumberTable[methodRenderer.currentLineNumberTableIndex]
							.getLineNumber( ) );
					appendSpace( );
					appendCommentEnd( );
					appendNewline( );

					methodRenderer.currentLineNumberTableIndex++;
				}

			}

			line = AbstractClassFileDocument.this.lineCount;

			/*
			 * leading spaces to padd the offsets commented out as this padding
			 * does not look nice in combination with try and catch blocks for
			 * (int i = String.valueOf(instruction.getOffset()).length(); i <
			 * methodRenderer.getLengthsOrderMagnitude(); i++) { appendSpace();
			 * }
			 */
			append( String.valueOf( instruction.getOffset( ) ) );

			appendSpace( );
			append( ByteCodeConstants.OPCODE_MNEMONICS[instruction.getOpcode( )] );
			appendOperands( );
			appendSemicolon( );
			appendComment( );
			appendNewline( );

			this.methodRenderer = null;
		}

	}

	protected class SignatureRenderer extends SignatureVisitor
	{

		/**
		 * Stack used to keep track of class types that have arguments. Each
		 * element of this stack is a boolean encoded in one bit. The top of the
		 * stack is the lowest order bit. Pushing false = *2, pushing true =
		 * *2+1, popping = /2.
		 */
		private int argumentStack;

		/**
		 * Stack used to keep track of array class types. Each element of this
		 * stack is a boolean encoded in one bit. The top of the stack is the
		 * lowest order bit. Pushing false = *2, pushing true = *2+1, popping =
		 * /2.
		 */
		private int arrayStack;

		private final StringBuffer declaration;

		private StringBuffer exceptions;

		private boolean isInterface;

		private StringBuffer returnType;

		private boolean seenFormalParameter;

		private boolean seenInterface;

		private boolean seenInterfaceBound;

		private boolean seenParameter;

		private String separator = "";

		public SignatureRenderer( final int access )
		{
			super( com.drgarbage.asm.Opcodes.ASM4 );
			isInterface = ( access & ACC_INTERFACE ) != 0;
			this.declaration = new StringBuffer( );
		}

		private SignatureRenderer( final StringBuffer buf )
		{
			super( com.drgarbage.asm.Opcodes.ASM4 );
			this.declaration = buf;
		}

		private void endFormals( )
		{
			if ( seenFormalParameter )
			{
				declaration.append( JavaLexicalConstants.GT );
				seenFormalParameter = false;
			}
		}

		private void endType( )
		{
			if ( arrayStack % 2 == 0 )
			{
				arrayStack /= 2;
			}
			else
			{
				while ( arrayStack % 2 != 0 )
				{
					arrayStack /= 2;
					declaration.append( JavaLexicalConstants.LEFT_SQUARE_BRACKET )
							.append( JavaLexicalConstants.RIGHT_SQUARE_BRACKET );
				}
			}
		}

		public String getDeclaration( )
		{
			return declaration.toString( );
		}

		public String getExceptions( )
		{
			return exceptions == null ? null : exceptions.toString( );
		}

		public String getReturnType( )
		{
			return returnType == null ? null : returnType.toString( );
		}

		private void startType( )
		{
			arrayStack *= 2;
		}

		public SignatureVisitor visitArrayType( )
		{
			startType( );
			arrayStack |= 1;
			return this;
		}

		public void visitBaseType( final char descriptor )
		{

			switch ( descriptor )
			{
				case ByteCodeConstants.V_VOID :
					declaration.append( JavaKeywords.VOID );
					break;
				case ByteCodeConstants.B_BYTE :
					declaration.append( JavaKeywords.BYTE );
					break;
				case ByteCodeConstants.J_LONG :
					declaration.append( JavaKeywords.LONG );
					break;
				case ByteCodeConstants.Z_BOOLEAN :
					declaration.append( JavaKeywords.BOOLEAN );
					break;
				case ByteCodeConstants.I_INT :
					declaration.append( JavaKeywords.INT );
					break;
				case ByteCodeConstants.S_SHORT :
					declaration.append( JavaKeywords.SHORT );
					break;
				case ByteCodeConstants.C_CHAR :
					declaration.append( JavaKeywords.CHAR );
					break;
				case ByteCodeConstants.F_FLOAT :
					declaration.append( JavaKeywords.FLOAT );
					break;
				case ByteCodeConstants.D_DOUBLE :
					declaration.append( JavaKeywords.DOUBLE );
					break;
				default :
					throw new IllegalArgumentException(
							"Unexpected base type character '" + descriptor + "' in descriptor." );
			}
			endType( );
		}

		public SignatureVisitor visitClassBound( )
		{
			separator = _EXTENDS_;
			startType( );
			return this;
		}

		public void visitClassType( final String name )
		{
			if ( ByteCodeConstants.JAVA_LANG_OBJECT.equals( name ) )
			{
				// Map<java.lang.Object,java.util.List>
				// or
				// abstract public V get(Object key); (seen in Dictionary.class)
				// should have Object
				// but java.lang.String extends java.lang.Object is unnecessary
				boolean needObjectClass = argumentStack % 2 != 0 || seenParameter;
				if ( needObjectClass )
				{
					declaration.append( separator )
							.append( name.replace( JavaLexicalConstants.SLASH, JavaLexicalConstants.DOT ) );
				}
			}
			else
			{
				declaration.append( separator )
						.append( name.replace( JavaLexicalConstants.SLASH, JavaLexicalConstants.DOT ) );
			}
			separator = "";
			argumentStack *= 2;
		}

		public void visitEnd( )
		{
			if ( argumentStack % 2 != 0 )
			{
				declaration.append( JavaLexicalConstants.GT );
			}
			argumentStack /= 2;
			endType( );
		}

		public SignatureVisitor visitExceptionType( )
		{
			if ( exceptions == null )
			{
				exceptions = new StringBuffer( );
			}
			else
			{
				exceptions.append( COMMA_ );
			}
			// startType();
			return new SignatureRenderer( exceptions );
		}

		public void visitFormalTypeParameter( final String name )
		{
			if ( seenFormalParameter )
			{
				declaration.append( JavaLexicalConstants.COMMA ).append( JavaLexicalConstants.SPACE );
			}
			else
			{
				declaration.append( JavaLexicalConstants.LT );
				seenFormalParameter = true;
			}
			declaration.append( name );
			seenInterfaceBound = false;
		}

		public void visitInnerClassType( final String name )
		{
			if ( argumentStack % 2 != 0 )
			{
				declaration.append( JavaLexicalConstants.GT );
			}
			argumentStack /= 2;
			declaration.append( JavaLexicalConstants.DOT );
			declaration.append( separator )
					.append( name.replace( JavaLexicalConstants.SLASH, JavaLexicalConstants.DOT ) );
			separator = "";
			argumentStack *= 2;
		}

		public SignatureVisitor visitInterface( )
		{
			separator = seenInterface ? COMMA_ : isInterface ? _EXTENDS_ : _IMPLEMENTS_;
			seenInterface = true;
			startType( );
			return this;
		}

		public SignatureVisitor visitInterfaceBound( )
		{
			if ( seenInterfaceBound )
			{
				separator = COMMA_;
			}
			else
			{
				separator = _EXTENDS_;
			}
			startType( );
			return this;
		}

		public SignatureVisitor visitParameterType( )
		{
			endFormals( );
			if ( seenParameter )
			{
				declaration.append( COMMA_ );
			}
			else
			{
				seenParameter = true;
				declaration.append( JavaLexicalConstants.LEFT_PARENTHESIS );
			}
			startType( );
			return this;
		}

		public SignatureVisitor visitReturnType( )
		{
			endFormals( );
			if ( seenParameter )
			{
				seenParameter = false;
			}
			else
			{
				declaration.append( JavaLexicalConstants.LEFT_PARENTHESIS );
			}
			declaration.append( JavaLexicalConstants.RIGHT_PARENTHESIS );
			returnType = new StringBuffer( );
			return new SignatureRenderer( returnType );
		}

		public SignatureVisitor visitSuperclass( )
		{
			endFormals( );
			separator = _EXTENDS_;
			startType( );
			return this;
		}

		// -----------------------------------------------

		public void visitTypeArgument( )
		{
			if ( argumentStack % 2 == 0 )
			{
				++argumentStack;
				declaration.append( JavaLexicalConstants.LT );
			}
			else
			{
				declaration.append( JavaLexicalConstants.COMMA ).append( JavaLexicalConstants.SPACE );
			}
			declaration.append( JavaLexicalConstants.QUESTION_MARK );
		}

		public SignatureVisitor visitTypeArgument( final char tag )
		{
			if ( argumentStack % 2 == 0 )
			{
				++argumentStack;
				declaration.append( JavaLexicalConstants.LT );
			}
			else
			{
				declaration.append( JavaLexicalConstants.COMMA ).append( JavaLexicalConstants.SPACE );
			}

			if ( tag == EXTENDS )
			{
				declaration.append( JavaLexicalConstants.QUESTION_MARK ).append( _EXTENDS_ );
			}
			else if ( tag == SUPER )
			{
				declaration.append( JavaLexicalConstants.QUESTION_MARK )
						.append( JavaLexicalConstants.SPACE )
						.append( JavaKeywords.SUPER )
						.append( JavaLexicalConstants.SPACE );
			}

			startType( );
			return this;
		}

		public void visitTypeVariable( final String name )
		{
			declaration.append( name );
			endType( );
		}
	}

	protected class TextTable
	{

		private ByteCodeConstants.Align[] columnAlignments;
		private int[] columnWidths;
		private List<String[]> rows;

		public TextTable( Align[] columnAlignments )
		{
			super( );
			this.columnAlignments = columnAlignments;
		}

		public void addRow( String[] values )
		{
			if ( rows == null )
			{
				rows = new ArrayList<String[]>( 4 );
			}
			rows.add( values );
		}

		public void appendLine( )
		{
			beginRow( );
			for ( int i = 0; i < columnWidths.length; i++ )
			{
				if ( i > 0 )
				{
					sb.append( JavaLexicalConstants.PLUS ).append( JavaLexicalConstants.MINUS );
				}
				else
				{
					/* first column */
					sb.append( JavaLexicalConstants.MINUS );
				}
				for ( int j = 0; j < columnWidths[i]; j++ )
				{
					sb.append( JavaLexicalConstants.MINUS );
				}
				sb.append( JavaLexicalConstants.MINUS );
			}
			endRow( );
		}

		public void appendRow( String[] values )
		{
			beginRow( );
			for ( int i = 0; i < values.length; i++ )
			{
				if ( i > 0 )
				{
					sb.append( JavaLexicalConstants.PIPE ).append( JavaLexicalConstants.SPACE );
				}
				else
				{
					/* first column */
					sb.append( JavaLexicalConstants.SPACE );
				}
				appendValue( values[i], columnAlignments[i], columnWidths[i] );
				sb.append( JavaLexicalConstants.SPACE );
			}
			endRow( );
		}

		public void appendValue( String value, ByteCodeConstants.Align align, int width )
		{
			if ( value == null )
			{
				value = "";
			}
			int diff = width - value.length( );
			if ( diff < 0 )
			{
				diff = 0;
			}
			switch ( align )
			{
				case LEFT :
					sb.append( value );
					for ( int i = 0; i < diff; i++ )
					{
						sb.append( JavaLexicalConstants.SPACE );
					}
					break;
				case CENTER :
					int halfDiff = diff / 2;
					for ( int i = 0; i < halfDiff; i++ )
					{
						sb.append( JavaLexicalConstants.SPACE );
					}
					sb.append( value );
					if ( diff % 2 != 0 )
					{
						/* diff was not even */
						sb.append( JavaLexicalConstants.SPACE );
					}
					for ( int i = 0; i < halfDiff; i++ )
					{
						sb.append( JavaLexicalConstants.SPACE );
					}
					break;
				case RIGHT :
					for ( int i = 0; i < diff; i++ )
					{
						sb.append( JavaLexicalConstants.SPACE );
					}
					sb.append( value );
					break;
				default :
					throw new IllegalStateException( "Unexpected alignment type '" + align + "'" );
			}
		}

		public void beginRow( )
		{
			appendCommentBegin( );
			sb.append( JavaLexicalConstants.SPACE );
		}

		public int computeWidth( )
		{
			int result = 0;
			for ( int i = 0; i < columnWidths.length; i++ )
			{
				if ( i > 0 )
				{
					result += 2;
				}
				else
				{
					/* first column */
					result++;
				}
				result += columnWidths[i];
				result++;
			}
			return result;
		}

		public void endRow( )
		{
			sb.append( JavaLexicalConstants.SPACE );
			appendCommentEnd( );
			appendNewline( );
		}

		protected void recomputeColumnWidths( )
		{
			this.columnWidths = new int[columnAlignments.length];
			/* compute column widths */
			for ( int col = 0; col < columnAlignments.length; col++ )
			{
				int w = 0;
				if ( rows != null )
				{
					for ( String[] row : rows )
					{
						String val = row[col];
						if ( val != null && val.length( ) > w )
						{
							w = val.length( );
						}
					}
				}
				columnWidths[col] = w;
			}
		}

		public void render( )
		{

			if ( rows != null )
			{
				if ( this.columnWidths == null )
				{
					recomputeColumnWidths( );
				}

				int row = 0;
				appendLine( );
				appendRow( rows.get( row ) );
				appendLine( );

				for ( int i = 1; i < rows.size( ); i++ )
				{
					appendRow( rows.get( i ) );
				}

				appendLine( );
			}

		}
	}

	public static final String _EXTENDS_ = new StringBuilder( ).append( JavaLexicalConstants.SPACE )
			.append( JavaKeywords.EXTENDS )
			.append( JavaLexicalConstants.SPACE )
			.toString( );

	public static final String _IMPLEMENTS_ = new StringBuilder( ).append( JavaLexicalConstants.SPACE )
			.append( JavaKeywords.IMPLEMENTS )
			.append( JavaLexicalConstants.SPACE )
			.toString( );
	protected static final String COMMA_ = new StringBuilder( ).append( JavaLexicalConstants.COMMA )
			.append( JavaLexicalConstants.SPACE )
			.toString( );
	public static final Attribute[] DEFAULT_ATTRIBUTES = new Attribute[0];
	public static final int DEFAULT_COMMENT_OFFSET = 24;
	public static final String L_JAVA_LANG_DEPRECATED_SEMICOLON = new StringBuilder( )
			.append( ByteCodeConstants.L_REFERENCE )
			.append( ByteCodeConstants.JAVA_LANG_DEPRECATED )
			.append( JavaLexicalConstants.SEMICOLON )
			.toString( );
	protected int classSignatureDocumentLine;

	protected String classSimpleName;

	protected int commentOffset = DEFAULT_COMMENT_OFFSET;
	protected AbstractConstantPoolEntry[] constantPool;

	protected ArrayList<IFieldSection> fieldSections = new ArrayList<IFieldSection>( );

	protected MessageFormat formatCoversBytesXToY = new MessageFormat( ByteCodeConstants.COVERS_BYTES_X_TO_Y );
	protected ArrayList<String> headerLines;
	protected int indent = 0;

	protected String indentationString = "    ";
	protected int lineCount = 0;
	protected int[] methodBorderLines;
	protected ArrayList<Integer> methodBorderLinesList = new ArrayList<Integer>( );
	protected ArrayList<IMethodSection> methodSections = new ArrayList<IMethodSection>( );
	protected String name;
	protected boolean renderTryCatchFinallyBlocks = false;
	protected StringBuffer sb = new StringBuffer( );

	protected boolean showConstantPool = false;
	protected boolean showLineNumberTable = false;

	protected boolean showLocalVariableTable = false;
	protected boolean showExceptionTable = false;
	protected boolean showRelativeBranchTargetOffsets = true;
	protected boolean showSourceLineNumbers = false;
	protected boolean showMaxs = false;

	public AbstractClassFileDocument( )
	{
		super( com.drgarbage.asm.Opcodes.ASM4 );

		IPreferenceStore store = JavaDecompilerPlugin.getDefault( ).getPreferenceStore( );
		showConstantPool = store.getBoolean( JavaDecompilerPlugin.CLASS_FILE_ATTR_SHOW_CONSTANT_POOL );
		showLineNumberTable = store.getBoolean( JavaDecompilerPlugin.CLASS_FILE_ATTR_SHOW_LINE_NUMBER_TABLE );
		showSourceLineNumbers = store.getBoolean( JavaDecompilerPlugin.CLASS_FILE_ATTR_SHOW_SOURCE_LINE_NUMBERS );
		showLocalVariableTable = store.getBoolean( JavaDecompilerPlugin.CLASS_FILE_ATTR_SHOW_VARIABLE_TABLE );
		showExceptionTable = store.getBoolean( JavaDecompilerPlugin.CLASS_FILE_ATTR_SHOW_EXCEPTION_TABLE );
		showMaxs = store.getBoolean( JavaDecompilerPlugin.CLASS_FILE_ATTR_SHOW_MAXS );
		renderTryCatchFinallyBlocks = store.getBoolean( JavaDecompilerPlugin.CLASS_FILE_ATTR_RENDER_TRYCATCH_BLOCKS );

		if ( JavaDecompilerPlugin.BRANCH_TARGET_ADDRESS_ABSOLUTE
				.equals( store.getString( JavaDecompilerPlugin.BRANCH_TARGET_ADDRESS_RENDERING ) ) )
		{
			showRelativeBranchTargetOffsets = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.drgarbage.asm.render.intf.IClassFileDocument#addFieldSection(com.
	 * drgarbage.asm.render.intf.IFieldSection)
	 */
	public void addFieldSection( IFieldSection f )
	{
		fieldSections.add( f );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Appendable#append(char)
	 */
	public Appendable append( char c )
	{
		if ( c == '\n' )
		{
			appendNewline( );
		}
		else
		{
			appendIndentationIfNeeded( );
			sb.append( c );
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Appendable#append(java.lang.CharSequence)
	 */
	public Appendable append( CharSequence str )
	{
		for ( int i = 0; i < str.length( ); i++ )
		{
			append( str.charAt( i ) );
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Appendable#append(java.lang.CharSequence, int, int)
	 */
	public Appendable append( CharSequence str, int start, int end ) throws IOException
	{
		for ( int i = start; i < end; i++ )
		{
			append( str.charAt( i ) );
		}
		return null;
	}

	/**
	 * Append an indentation string if needed an the
	 * <code>String.valueOf(obj)</code>.
	 * 
	 * @param obj
	 */
	public void append( Object obj )
	{
		appendIndentationIfNeeded( );
		String s = String.valueOf( obj );
		append( s );
	}

	/**
	 * Appends a string representation of the given access modifiers to
	 * {@link #buf buf}.
	 * 
	 * @param access
	 *            some access modifiers.
	 */
	protected void appendAccess( final int access )
	{
		appendIndentationIfNeeded( );
		if ( ( access & ACC_PUBLIC ) != 0 )
		{
			append( JavaKeywords.PUBLIC );
			appendSpace( );
		}
		if ( ( access & ACC_PRIVATE ) != 0 )
		{
			append( JavaKeywords.PRIVATE );
			appendSpace( );
		}
		if ( ( access & ACC_PROTECTED ) != 0 )
		{
			append( JavaKeywords.PROTECTED );
			appendSpace( );
		}
		if ( ( access & ACC_FINAL ) != 0 )
		{
			append( JavaKeywords.FINAL );
			appendSpace( );
		}
		if ( ( access & ACC_STATIC ) != 0 )
		{
			append( JavaKeywords.STATIC );
			appendSpace( );
		}
		if ( ( access & ACC_SYNCHRONIZED ) != 0 )
		{
			append( JavaKeywords.SYNCHRONIZED );
			appendSpace( );
		}
		if ( ( access & ACC_VOLATILE ) != 0 )
		{
			append( JavaKeywords.VOLATILE );
			appendSpace( );
		}
		if ( ( access & ACC_TRANSIENT ) != 0 )
		{
			append( JavaKeywords.TRANSIENT );
			appendSpace( );
		}
		if ( ( access & ACC_ABSTRACT ) != 0 )
		{
			append( JavaKeywords.ABSTRACT );
			appendSpace( );
		}
		if ( ( access & ACC_STRICT ) != 0 )
		{
			append( JavaKeywords.STRICTFP );
			appendSpace( );
		}
		if ( ( access & ACC_ENUM ) != 0 )
		{
			append( JavaKeywords.ENUM );
			appendSpace( );
		}
	}

	/**
	 * Appends a {@link JavaLexicalConstants#AT} and <code>annotation</code>.
	 * 
	 * @param annotation
	 */
	public void appendAnnotation( String annotation )
	{
		appendIndentationIfNeeded( );
		append( JavaLexicalConstants.AT );
		append( annotation );

	}

	protected void appendClassFileFormatVersion( int version )
	{
		int major = BytecodeUtils.getMajor( version );
		int minor = BytecodeUtils.getMinor( version );
		appendClassFileFormatVersion( major, minor );
	}

	protected void appendClassFileFormatVersion( int major, int minor )
	{
		appendCommentBegin( );
		appendSpace( );
		sb.append( ByteCodeConstants.CLASS_FILE_FORMAT_VERSION );
		appendSpace( );

		if ( major == ByteCodeConstants.INVALID_OFFSET || minor == ByteCodeConstants.INVALID_OFFSET )
		{
			sb.append( ByteCodeConstants.UNKNOWN_INFORMATION );
		}
		else
		{
			sb.append( major );
			appendDot( );
			sb.append( minor );

			appendSpace( );
			appendLeftParenthesis( );
			sb.append( ByteCodeConstants.JAVA );
			appendSpace( );
			sb.append( BytecodeUtils.getLowestJavaPlatformVersion( major, minor ) );
			appendRightParenthesis( );
			appendSpace( );
			appendCommentEnd( );
			appendNewline( );
		}
	}

	/**
	 * Append an indentation string if needed and
	 * {@link JavaLexicalConstants#COLON}.
	 * 
	 */
	public void appendColon( )
	{
		appendIndentationIfNeeded( );
		sb.append( JavaLexicalConstants.COLON );
	}

	/**
	 * Append an indentation string if needed and
	 * {@link JavaLexicalConstants#COMMA}.
	 * 
	 */
	public void appendComma( )
	{
		appendIndentationIfNeeded( );
		sb.append( JavaLexicalConstants.COMMA );
	}

	/**
	 * Append an indentation string if needed and
	 * {@link JavaLexicalConstants#COMMENT_BEGIN}.
	 * 
	 */
	public void appendCommentBegin( )
	{
		appendIndentationIfNeeded( );
		sb.append( JavaLexicalConstants.COMMENT_BEGIN );
	}

	/**
	 * Append an indentation string if needed and
	 * {@link JavaLexicalConstants#COMMENT_END}.
	 * 
	 */
	public void appendCommentEnd( )
	{
		appendIndentationIfNeeded( );
		sb.append( JavaLexicalConstants.COMMENT_END );
	}

	protected void appendConstantPool( )
	{
		if ( showConstantPool )
		{
			if ( constantPool == null )
			{
				appendNewline( );
				appendCommentBegin( );
				appendSpace( );
				sb.append( ByteCodeConstants.CONSTANT_POOL_NOT_AVAILABLE );
				appendSpace( );
				appendCommentEnd( );
				appendNewline( );

			}
			else if ( constantPool.length == 0 )
			{
				appendNewline( );
				appendCommentBegin( );
				appendSpace( );
				sb.append( ByteCodeConstants.CONSTANT_POOL_EMPTY );
				appendSpace( );
				appendCommentEnd( );
				appendNewline( );
			}
			else
			{
				/* available and not empty */

				appendNewline( );

				TextTable tbl = new TextTable( new ByteCodeConstants.Align[]{
						ByteCodeConstants.Align.RIGHT, ByteCodeConstants.Align.LEFT, ByteCodeConstants.Align.LEFT
				} );

				tbl.addRow( new String[]{
						ByteCodeConstants.INDEX, ByteCodeConstants.TAG, ByteCodeConstants.INFO
				} );

				for ( int i = 0; i < constantPool.length; i++ )
				{
					AbstractConstantPoolEntry en = constantPool[i];

					if ( en != null )
					{
						tbl.addRow( new String[]{
								String.valueOf( i ), en.getTagMnemonics( ), en.getInfo( )
						} );
					}
				}

				tbl.recomputeColumnWidths( );
				tbl.beginRow( );
				tbl.appendValue( ByteCodeConstants.CONSTANT_POOL, ByteCodeConstants.Align.CENTER, tbl.computeWidth( ) );
				tbl.endRow( );
				tbl.render( );

			}
		}
	}

	protected void appendDebugInfoComment( String debugInfo )
	{
		appendCommentBegin( );
		appendSpace( );
		sb.append( ByteCodeConstants.DEBUG_INFO );
		appendSpace( );
		sb.append( debugInfo );
		appendSpace( );
		appendCommentEnd( );
		appendNewline( );
	}

	protected void appendDeprecated( int access )
	{
		if ( ( access & ACC_DEPRECATED ) != 0 )
		{
			appendAnnotation( JavaAnnotations.DEPRECATED );
			appendNewline( );
		}
	}

	/**
	 * Append an indentation string if needed and
	 * {@link JavaLexicalConstants#DOT}.
	 * 
	 */
	public void appendDot( )
	{
		appendIndentationIfNeeded( );
		sb.append( JavaLexicalConstants.DOT );
	}

	/**
	 * Append an indentation string if needed and
	 * {@link JavaLexicalConstants#GT}.
	 * 
	 */
	public void appendGt( )
	{
		appendIndentationIfNeeded( );
		sb.append( JavaLexicalConstants.GT );
	}

	protected void appendHeaderComment( String classLoadedFrom, String debugTargetName )
	{

		// Bundle bundle = BytecodeVisualizerPlugin.getDefault().getBundle();
		//
		// bundle =
		// Platform.getBundle(CoreConstants.BYTECODE_VISUALIZER_PLUGIN_ID);
		// if (bundle == null) {
		// /* this should not happen */
		// throw new RuntimeException(""+
		// CoreConstants.BYTECODE_VISUALIZER_PLUGIN_ID +" not installed.");
		// }
		//
		// String providerWww = Platform.getResourceString(bundle, "%"+
		// CoreConstants.providerWww);
		// String provider = Platform.getResourceString(bundle, "%"+
		// CoreConstants.providerNameLabel);
		// String pluginName = Platform.getResourceString(bundle, "%"+
		// CoreConstants.pluginName);

		// String msg = MessageFormat.format(ByteCodeConstants.Generated_by_x,
		// new Object[] {provider + " "+ pluginName});

		// appendHeaderLine(msg);
		// appendHeaderLine(providerWww);

		// if (BytecodeVisualizerPlugin.getDefault() != null) {
		// /* jUnit tests work only if we test for null here */
		//
		// appendHeaderLine(ByteCodeConstants.Version,
		// BytecodeVisualizerPlugin.PLUGIN_VERSION);
		//
		// }
		// if (classLoadedFrom != null) {
		// appendHeaderLine(ByteCodeConstants.Class_retrieved_from,
		// classLoadedFrom);
		// }
		// if (debugTargetName != null) {
		// appendHeaderLine(ByteCodeConstants.Debug_target, debugTargetName);
		// }
		//
		// //SimpleDateFormat sdf = new
		// SimpleDateFormat(CoreConstants.ISO_DATE_TIME_FORMAT_FULL);
		// //appendHeaderLine(ByteCodeConstants.Retrieved_on, sdf.format(new
		// Date()));
		//
		//
		// flushHeader();

	}

	protected void appendHeaderLine( String line )
	{
		if ( headerLines == null )
		{
			headerLines = new ArrayList<String>( 8 );
		}
		headerLines.add( line );
	}

	protected void appendHeaderLine( String key, String value )
	{
		appendHeaderLine( key + JavaLexicalConstants.COLON + JavaLexicalConstants.SPACE + value );
	}

	/**
	 * Appends the indentation string {@link #indent}-times.
	 */
	protected void appendIndentation( )
	{
		for ( int i = 0; i < indent; i++ )
		{
			sb.append( indentationString );
		}
	}

	/**
	 * Appends the indentation string {@link #indent}-times if we are just at
	 * the beginning of a line.
	 */
	protected void appendIndentationIfNeeded( )
	{
		if ( sb.length( ) > 0 && sb.charAt( sb.length( ) - 1 ) == '\n' )
		{
			appendIndentation( );
		}
	}

	public void appendJavaSourcePath( String byteCodePath )
	{
		appendIndentationIfNeeded( );
		sb.append( byteCodePath.replace( ByteCodeConstants.CLASS_NAME_SLASH, JavaLexicalConstants.DOT ) );
	}

	public void appendLeftBrace( )
	{
		appendIndentationIfNeeded( );
		sb.append( JavaLexicalConstants.LEFT_BRACE );
	}

	public void appendLeftParenthesis( )
	{
		sb.append( JavaLexicalConstants.LEFT_PARENTHESIS );

	}

	/**
	 * Append an indentation string if needed and
	 * {@link JavaLexicalConstants#LT}.
	 * 
	 */
	public void appendLt( )
	{
		appendIndentationIfNeeded( );
		sb.append( JavaLexicalConstants.LT );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.drgarbage.classfile.render.intf.IClassFileDocument#appendNewline()
	 */
	public void appendNewline( )
	{
		sb.append( JavaLexicalConstants.NEWLINE );
		lineCount++;
	}

	protected void appendPackage( String thisClassName )
	{
		String packageName = getPackageName( thisClassName );
		if ( packageName != null )
		{
			append( JavaKeywords.PACKAGE );
			appendSpace( );
			append( packageName );
			appendSemicolon( );
			appendNewline( );
		}
	}

	public void appendPaddedCommentBegin( )
	{
		/*
		 * count the chars fom the endOffset to the last \n or beginning of the
		 * document
		 */
		int i = sb.length( ) - 1;
		int lastNonWsIndex = i;
		for ( ; i >= 0 && sb.charAt( i ) != '\n'; i-- )
		{
			if ( !Character.isWhitespace( sb.charAt( i ) ) )
			{
				lastNonWsIndex = i;
			}
		}
		if ( sb.charAt( i ) == '\n' )
		{
			i++;
		}

		int targetLenghth = lastNonWsIndex + getCommentOffset( ) + 1;
		if ( targetLenghth <= sb.length( ) )
		{
			targetLenghth = sb.length( ) + 1;
		}

		for ( int j = sb.length( ) - 1; j < targetLenghth; j++ )
		{
			sb.append( JavaLexicalConstants.SPACE );
		}

		appendCommentBegin( );
	}

	public void appendRightBrace( )
	{
		appendIndentationIfNeeded( );
		sb.append( JavaLexicalConstants.RIGHT_BRACE );
	}

	public void appendRightParenthesis( )
	{
		sb.append( JavaLexicalConstants.RIGHT_PARENTHESIS );

	}

	public void appendSemicolon( )
	{
		appendIndentationIfNeeded( );
		sb.append( JavaLexicalConstants.SEMICOLON );
	}

	protected void appendSourcePathComment( String path )
	{
		appendNewline( );
		appendCommentBegin( );
		appendSpace( );
		sb.append( ByteCodeConstants.COMPILED_FROM );
		appendSpace( );
		sb.append( path );
		appendSpace( );
		appendCommentEnd( );
		appendNewline( );

	}

	public void appendSpace( )
	{
		appendIndentationIfNeeded( );
		sb.append( JavaLexicalConstants.SPACE );
	}

	protected AnnotationRenderer createTraceAnnotationVisitor( )
	{
		return new AnnotationRenderer( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.drgarbage.classfile.render.intf.IClassFileDocument#decrementIndent()
	 */
	public void decrementIndent( )
	{
		indent--;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals( Object obj )
	{
		return sb.equals( obj );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.drgarbage.asm.render.intf.IClassFileDocument#findField(int)
	 */
	public IFieldSection findFieldSection( int line )
	{
		for ( IFieldSection f : getFieldSections( ) )
		{
			if ( f.getBytecodeDocumentLine( ) == line )
			{
				return f;
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.drgarbage.asm.render.intf.IClassFileDocument#findFieldSection(java.
	 * lang.String)
	 */
	public IFieldSection findFieldSection( String fieldName )
	{
		for ( IFieldSection f : getFieldSections( ) )
		{
			if ( f.getName( ).equals( fieldName ) )
			{
				return f;
			}
		}

		return null;
	}

	public int findIndentationAt( int index )
	{
		int result = 0;
		int i = index;
		while ( ( sb.charAt( i ) ) != '\n' )
		{
			i--;
		}
		while ( i + indentationString.length( ) < index && matchPosition( i, indentationString ) )
		{
			result++;
			i += indentationString.length( );
		}
		return result;
	}

	public IInstructionLine findInstructionLine( int sourceCodeLine )
	{
		List<IMethodSection> methodSections = getMethodSections( );
		if ( methodSections != null )
		{
			for ( IMethodSection methodSection : methodSections )
			{
				IInstructionLine result = methodSection.findInstructionLine( sourceCodeLine );
				if ( result != null )
				{
					return result;
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.drgarbage.classfile.render.intf.IClassFileDocument#findMethod(int)
	 */
	public IMethodSection findMethodSection( int line )
	{
		for ( IMethodSection ms : methodSections )
		{
			if ( ms.getFirstLine( ) != ByteCodeConstants.INVALID_OFFSET
					&& ms.getFirstLine( ) <= line
					&& ms.getLastLine( ) != ByteCodeConstants.INVALID_OFFSET
					&& ms.getLastLine( ) >= line )
			{
				return ms;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.drgarbage.asm.render.intf.IClassFileDocument#findMethodSection(java.
	 * lang.String, java.lang.String)
	 */
	public IMethodSection findMethodSection( String methodName, String methodSignature )
	{
		for ( IMethodSection m : getMethodSections( ) )
		{
			if ( m.getDescriptor( ).equals( methodSignature ) && m.getName( ).equals( methodName ) )
			{
				return m;
			}
		}

		return null;
	}

	@SuppressWarnings("unused")
	private void flushHeader( )
	{
		if ( headerLines != null && headerLines.size( ) > 0 )
		{
			int maxLength = 0;
			for ( String line : headerLines )
			{
				if ( line.length( ) > maxLength )
				{
					maxLength = line.length( );
				}
			}

			appendCommentBegin( );
			appendSpace( );
			for ( int i = 0; i < maxLength; i++ )
			{
				sb.append( JavaLexicalConstants.ASTERISK );
			}
			appendSpace( );
			appendCommentEnd( );
			appendNewline( );

			for ( String line : headerLines )
			{
				appendCommentBegin( );
				appendSpace( );
				sb.append( line );
				for ( int i = 0; i < maxLength - line.length( ); i++ )
				{
					appendSpace( );
				}
				appendSpace( );
				appendCommentEnd( );
				appendNewline( );
			}

			appendCommentBegin( );
			appendSpace( );
			for ( int i = 0; i < maxLength; i++ )
			{
				sb.append( JavaLexicalConstants.ASTERISK );
			}
			appendSpace( );
			appendCommentEnd( );
			appendNewline( );

		}

		/* free the list */
		headerLines = null;
	}

	public String getClassName( )
	{
		return name;
	}

	public int getClassSignatureDocumentLine( )
	{
		return classSignatureDocumentLine;
	}

	public String getClassSimpleName( )
	{
		return classSimpleName;
	}

	public int getCommentOffset( )
	{
		return commentOffset;
	}

	public AbstractConstantPoolEntry[] getConstantPool( )
	{
		return constantPool;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.drgarbage.asm.render.intf.IClassFileDocument#getFieldSections()
	 */
	public List<IFieldSection> getFieldSections( )
	{
		return fieldSections;
	}

	public int getIndent( )
	{
		return indent;
	}

	public String getIndentationString( )
	{
		return indentationString;
	}

	public int getLineCount( )
	{
		return lineCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.drgarbage.classfile.render.intf.IClassFileDocument#getMethodSections(
	 * )
	 */
	public List<IMethodSection> getMethodSections( )
	{
		return methodSections;
	}

	protected String getPackageName( String thisClassName )
	{
		return JavaSourceUtils.getPackage( name );
	}

	public int hashCode( )
	{
		return sb.hashCode( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.drgarbage.classfile.render.intf.IClassFileDocument#incrementIndent()
	 */
	public void incrementIndent( )
	{
		indent++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.drgarbage.asm.render.intf.IClassFileDocument#isFieldSelected(int)
	 */
	public boolean isLineInField( int line )
	{
		for ( IFieldSection fs : fieldSections )
		{
			if ( fs.getBytecodeDocumentLine( ) == line )
			{
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.drgarbage.classfile.render.intf.IClassFileDocument#isLineInMethod(
	 * int)
	 */
	public boolean isLineInMethod( int line )
	{
		int found = Arrays.binarySearch( methodBorderLines, line );
		if ( found >= 0 )
		{
			/*
			 * we found a border line which is in a method
			 */
			return true;
		}
		else
		{
			/* the match is somewhere in between the border line numbers */
			/*
			 * the theory here is very simple: test, if the found =
			 * (-(<i>insertion point</i>) - 1) is even or odd even means between
			 * start and end of a method so return true or false otherwise
			 */
			return found % 2 == 0;
		}
	}

	/**
	 * <code>true</code> if the <code>try</code>, <code>catch</code> and
	 * <code>finally</code> blocks are rendered by this; <code>false</code>
	 * otherwise.
	 * 
	 * @return the renderTryCatchFinallyBlocks
	 */
	public boolean isRenderTryCatchFinallyBlocks( )
	{
		return renderTryCatchFinallyBlocks;
	}

	/**
	 * Returns the number of characters in the internal {@link StringBuilder}.
	 * 
	 * @return <code>true</code> or <code>false</code>
	 */

	public int length( )
	{
		return sb.length( );
	}

	protected boolean matchPosition( int position, String str )
	{
		for ( int i = 0; i < str.length( ); i++ )
		{
			if ( sb.charAt( i + position ) != str.charAt( i ) )
			{
				return false;
			}
		}
		return true;
	}

	public void setCommentOffset( int commentOffset )
	{
		this.commentOffset = commentOffset;
	}

	public void setIndent( int indent )
	{
		this.indent = indent;
	}

	public void setIndentationString( String indentationString )
	{
		this.indentationString = indentationString;
	}

	/**
	 * Sets the boolean flag telling if the the <code>try</code>,
	 * <code>catch</code> and <code>finally</code> blocks will be rendered
	 * (<code>true</code>) by this or not (<code>false</code>).
	 * 
	 * @param renderTryCatchFinallyBlocks
	 *            the renderTryCatchFinallyBlocks to set
	 */
	public void setRenderTryCatchFinallyBlocks( boolean renderTryCatchFinallyBlocks )
	{
		this.renderTryCatchFinallyBlocks = renderTryCatchFinallyBlocks;
	}

	/**
	 * Returns the textual representation of this {@link ClassFileDocument}.
	 * 
	 * @return the textual representation of this {@link ClassFileDocument}
	 */
	public String toString( )
	{
		return sb.toString( );
	}

	/**
	 * Prints a disassembled view of the given annotation.
	 * 
	 * @param desc
	 *            the class descriptor of the annotation class.
	 * @param visible
	 *            <tt>true</tt> if the annotation is visible at runtime.
	 * @return a visitor to visit the annotation values.
	 */
	protected AnnotationVisitor visitAnnotationImpl( final String desc, final boolean visible )
	{
		if ( !L_JAVA_LANG_DEPRECATED_SEMICOLON.equals( desc ) )
		{
			// TODO test the annotation
			appendNewline( );
			appendCommentBegin( );
			appendSpace( );
			if ( visible )
			{
				sb.append( ByteCodeConstants.VISIBLE );
			}
			else
			{
				sb.append( ByteCodeConstants.INVISIBLE );
			}
			appendSpace( );
			sb.append( ByteCodeConstants.ANNOTATION );
			appendSpace( );
			appendCommentEnd( );
			appendNewline( );

			append( JavaLexicalConstants.AT );
			// FIXME render the descriptor
			sb.append( desc );
			sb.append( JavaLexicalConstants.LEFT_PARENTHESIS );
			// FIXME Annotations rendering
			AnnotationRenderer tav = createTraceAnnotationVisitor( );
			sb.append( JavaLexicalConstants.RIGHT_PARENTHESIS );
			appendNewline( );
			return tav;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Prints a disassembled view of the given attribute.
	 * 
	 * @param attr
	 *            an attribute.
	 */
	public void visitAttributeImpl( final Attribute attr )
	{
		// FIXME attribute rendering
		appendNewline( );
		appendCommentBegin( );
		appendSpace( );
		sb.append( ByteCodeConstants.ATTRIBUTE );
		appendSpace( );
		appendCommentEnd( );
		appendNewline( );

		appendCommentBegin( );
		appendSpace( );
		// TODO class name rendering
		sb.append( attr.type );
		appendSpace( );
		appendCommentEnd( );
		appendNewline( );

		// if (attr instanceof Traceable) {
		// ((Traceable) attr).trace(sb, null);
		// }
	}

	public void visitConstantPool( final byte[] bytes, int offset, int entryCount )
	{
		if ( bytes == null || entryCount == 0 )
		{
			constantPool = null;
		}
		else
		{
			ConstantPoolParser cpp = new ConstantPoolParser( bytes, offset, entryCount );
			constantPool = cpp.parse( );
		}
	}

	protected FieldVisitor visitField( final int access, final String name, final String desc, final String signature
	// final Object value
	)
	{

		appendNewline( );
		appendDeprecated( access );

		int fieldLine = lineCount;

		appendAccess( access );

		if ( signature != null )
		{

			SignatureRenderer sv = new SignatureRenderer( 0 );
			SignatureReader r = new SignatureReader( signature );
			r.acceptType( sv );
			sb.append( sv.getDeclaration( ) );
			appendSpace( );
			append( name );
			appendSemicolon( );
			appendNewline( );
		}
		else
		{
			try
			{
				BytecodeUtils.appendFieldDescriptor( desc, 0, sb );
			}
			catch ( IOException e )
			{
				throw new RuntimeException( e );
			}
			appendSpace( );
			append( name );
			appendSemicolon( );
			appendNewline( );

		}

		FieldRenderer fieldRenderer = new FieldRenderer( name, desc, fieldLine );

		fieldSections.add( fieldRenderer );

		return fieldRenderer;

	}

}