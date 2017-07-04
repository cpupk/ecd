/**
 * Copyright (c) 2008-2013, Dr. Garbage Community
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

package com.drgarbage.classfile.editors;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.ParseException;

import com.drgarbage.asm.ClassReader;
import com.drgarbage.asm_ext.IConstantPoolVisitor;
import com.drgarbage.bytecode.BytecodeUtils;
import com.drgarbage.bytecode.ConstantPoolParser;
import com.drgarbage.bytecode.constant_pool.AbstractConstantPoolEntry;
import com.drgarbage.bytecode.constant_pool.ConstantUtf8Info;

/**
 * The class file parser to generate a readable representation from the class file.
 * 
 * @author Sergej Alekseev
 * @version $Revision: 515 $ $Id: ClassFileParser.java 515 2014-02-05 07:54:26Z
 *          salekseev $
 */
public class ClassFileParser
{
	/** bytes of the class file */
	private byte[] bytes = null;

	/** global offset of the current position in the byte array */
	private int offset = -1;

	/** Array of the constant pool entries */
	private AbstractConstantPoolEntry[] constantPool;

	private String _12_BYTES = "                                     ";
	private String _14_BYTES = "                                           ";
	private String _16_BYTES = "                                                 ";

	/**
	 * Creates a class file representation.
	 * 
	 * @param bytes
	 * @return the text representation of the class file
	 * @throws ParseException
	 */
	public String parseClassFile( byte[] bytes ) throws ParseException
	{
		this.bytes = bytes;
		StringBuffer buf = new StringBuffer( );
		try
		{
			ClassReader cr = new ClassReader( bytes, 0, bytes.length, new ConstantPoolVisitor( ) );
			/*
			 * ClassFile { u4 magic; u2 minor_version; u2 major_version; u2
			 * constant_pool_count; cp_info
			 * constant_pool[constant_pool_count-1]; u2 access_flags; u2
			 * this_class; u2 super_class; u2 interfaces_count; u2
			 * interfaces[interfaces_count]; u2 fields_count; field_info
			 * fields[fields_count]; u2 methods_count; method_info
			 * methods[methods_count]; u2 attributes_count; attribute_info
			 * attributes[attributes_count]; }
			 */
			offset = 0;

			buf.append( appendBytes( offset, offset + 4 ) );
			offset = offset + 4;
			buf.append( _12_BYTES );
			buf.append( "/* u4 magic */" );
			buf.append( '\n' );

			buf.append( appendBytes( offset, offset + 2 ) );
			int minor = cr.readUnsignedShort( offset );
			offset = offset + 2;
			buf.append( _14_BYTES );
			buf.append( "/* u2 minor_version=" );
			buf.append( minor );
			buf.append( " */" );
			buf.append( '\n' );

			buf.append( appendBytes( offset, offset + 2 ) );
			int major = cr.readUnsignedShort( offset );
			offset = offset + 2;
			buf.append( _14_BYTES );
			buf.append( "/* u2 major_version=" );
			buf.append( major );
			buf.append( " */" );
			buf.append( '\n' );

			buf.append( _16_BYTES );
			buf.append( "/* java " );
			buf.append( BytecodeUtils.getLowestJavaPlatformVersion( major, minor ) );
			buf.append( " */" );
			buf.append( '\n' );
			buf.append( '\n' );

			buf.append( appendBytes( offset, offset + 2 ) );
			offset = offset + 2;
			buf.append( _14_BYTES );
			buf.append( "/* u2 constant_pool_count=" );
			buf.append( cr.getItemCount( ) );
			buf.append( " */" );
			buf.append( '\n' );

			buf.append( constantPoolToString( ) );
			buf.append( '\n' );

			buf.append( appendBytes( offset, offset + 2 ) );
			offset = offset + 2;
			buf.append( _14_BYTES );
			buf.append( "/* u2 access_flags: " );
			buf.append( cr.getAccess( ) );
			buf.append( " */" );
			buf.append( '\n' );

			buf.append( appendBytes( offset, offset + 2 ) );
			offset = offset + 2;
			buf.append( _14_BYTES );
			buf.append( "/* u2 this_class: " );
			buf.append( cr.getClassName( ) );
			buf.append( " */" );
			buf.append( '\n' );

			buf.append( appendBytes( offset, offset + 2 ) );
			offset = offset + 2;
			buf.append( _14_BYTES );
			buf.append( "/* u2 super_class: " );
			buf.append( cr.getSuperName( ) );
			buf.append( " */" );
			buf.append( '\n' );

			/* interfaces */
			buf.append( '\n' );
			buf.append( _16_BYTES );
			buf.append( "/* Interfaces: */" );
			buf.append( '\n' );

			buf.append( appendBytes( offset, offset + 2 ) );
			offset = offset + 2;
			buf.append( _14_BYTES );
			buf.append( "/* u2 interfaces_count=" );
			buf.append( cr.getInterfaces( ).length );
			buf.append( " */" );
			buf.append( '\n' );

			buf.append( appendInterfaces( cr.getInterfaces( ) ) );
			buf.append( '\n' );
			buf.append( '\n' );

			/* fields */
			buf.append( _16_BYTES );
			buf.append( "/* Fields: */" );
			buf.append( '\n' );

			buf.append( appendBytes( offset, offset + 2 ) );
			int count = cr.readUnsignedShort( offset );
			offset = offset + 2;
			buf.append( _14_BYTES );
			buf.append( "/* u2 fields_count=" );
			buf.append( count );
			buf.append( " */" );
			buf.append( '\n' );

			/*
			 * field_info { u2 access_flags; u2 name_index; u2 descriptor_index;
			 * u2 attributes_count; attribute_info attributes[attributes_count];
			 * }
			 */

			for ( int i = 0; i < count; i++ )
			{
				buf.append( _16_BYTES );
				buf.append( "/* Field[" );
				buf.append( i );
				buf.append( ']' );
				buf.append( " */" );
				buf.append( '\n' );

				buf.append( appendBytes( offset, offset + 2 ) );
				int access_flags = cr.readUnsignedShort( offset );
				offset += 2;
				buf.append( _14_BYTES );
				buf.append( "/* u2 access_flags=" );
				buf.append( access_flags );
				buf.append( " */" );
				buf.append( '\n' );

				buf.append( appendBytes( offset, offset + 2 ) );
				int name_index = cr.readUnsignedShort( offset );
				offset += 2;
				buf.append( _14_BYTES );
				buf.append( "/* u2 name_index=" );
				buf.append( name_index );
				buf.append( ", " );
				buf.append( constantPool[name_index].getInfo( ) );
				buf.append( " */" );
				buf.append( '\n' );

				buf.append( appendBytes( offset, offset + 2 ) );
				int descriptor_index = cr.readUnsignedShort( offset );
				offset += 2;
				buf.append( _14_BYTES );
				buf.append( "/* u2 descriptor_index=" );
				buf.append( descriptor_index );
				buf.append( ", " );
				buf.append( constantPool[descriptor_index].getInfo( ) );
				buf.append( " */" );
				buf.append( '\n' );

				buf.append( appendBytes( offset, offset + 2 ) );
				int attributes_count = cr.readUnsignedShort( offset );
				offset += 2;
				buf.append( _14_BYTES );
				buf.append( "/* u2 attributes_count=" );
				buf.append( attributes_count );
				buf.append( " */" );
				buf.append( '\n' );

				/*
				 * attribute_info { u2 attribute_name_index; u4
				 * attribute_length; u1 info[attribute_length]; }
				 */
				for ( int j = 0; j < attributes_count; j++ )
				{
					buf.append( appendBytes( offset, offset + 2 ) );
					int attribute_name_index = cr.readUnsignedShort( offset );
					offset += 2;
					buf.append( _14_BYTES );
					buf.append( "/* u2 attribute_name_index=" );
					buf.append( attribute_name_index );
					buf.append( ", " );
					buf.append( constantPool[attribute_name_index].getInfo( ) );
					buf.append( " */" );
					buf.append( '\n' );

					buf.append( appendBytes( offset, offset + 4 ) );
					int attribute_length = cr.readInt( offset );
					offset += 4;
					buf.append( _12_BYTES );
					buf.append( "/* u4 attribute_length=" );
					buf.append( attribute_length );
					buf.append( " */" );
					buf.append( '\n' );

					buf.append( _16_BYTES );
					buf.append( "/* Attribute bytes: */" );
					buf.append( '\n' );
					buf.append( appendBytes( offset, offset + attribute_length ) );
					offset += attribute_length;
					buf.append( '\n' );
				}
			}
			buf.append( '\n' );

			/* methods */
			buf.append( _16_BYTES );
			buf.append( "/* Methods: */" );
			buf.append( '\n' );

			buf.append( appendBytes( offset, offset + 2 ) );
			int methods_count = cr.readUnsignedShort( offset );
			offset = offset + 2;
			buf.append( _14_BYTES );
			buf.append( "/* u2 methods_count=" );
			buf.append( methods_count );
			buf.append( " */" );
			buf.append( '\n' );

			/*
			 * method_info { u2 access_flags; u2 name_index; u2
			 * descriptor_index; u2 attributes_count; attribute_info
			 * attributes[attributes_count]; }
			 */

			for ( int i = 0; i < methods_count; i++ )
			{
				buf.append( _16_BYTES );
				buf.append( "/* Method[" );
				buf.append( i );
				buf.append( ']' );
				buf.append( " */" );
				buf.append( '\n' );

				buf.append( appendBytes( offset, offset + 2 ) );
				int access_flags = cr.readUnsignedShort( offset );
				offset += 2;
				buf.append( _14_BYTES );
				buf.append( "/* u2 access_flags=" );
				buf.append( access_flags );
				buf.append( " */" );
				buf.append( '\n' );

				buf.append( appendBytes( offset, offset + 2 ) );
				int name_index = cr.readUnsignedShort( offset );
				offset += 2;
				buf.append( _14_BYTES );
				buf.append( "/* u2 name_index=" );
				buf.append( name_index );
				buf.append( ", " );
				buf.append( constantPool[name_index].getInfo( ) );
				buf.append( " */" );
				buf.append( '\n' );

				buf.append( appendBytes( offset, offset + 2 ) );
				int descriptor_index = cr.readUnsignedShort( offset );
				offset += 2;
				buf.append( _14_BYTES );
				buf.append( "/* u2 descriptor_index=" );
				buf.append( descriptor_index );
				buf.append( ", " );
				buf.append( constantPool[descriptor_index].getInfo( ) );
				buf.append( " */" );
				buf.append( '\n' );

				buf.append( appendBytes( offset, offset + 2 ) );
				int attributes_count = cr.readUnsignedShort( offset );
				offset += 2;
				buf.append( _14_BYTES );
				buf.append( "/* u2 attributes_count=" );
				buf.append( attributes_count );
				buf.append( " */" );
				buf.append( '\n' );

				/*
				 * attribute_info { u2 attribute_name_index; u4
				 * attribute_length; u1 info[attribute_length]; }
				 */
				for ( int j = 0; j < attributes_count; j++ )
				{
					buf.append( _16_BYTES );
					buf.append( "/* Attribute[" );
					buf.append( j );
					buf.append( ']' );
					buf.append( " */" );
					buf.append( '\n' );

					buf.append( appendBytes( offset, offset + 2 ) );
					int attribute_name_index = cr.readUnsignedShort( offset );
					offset += 2;
					buf.append( _14_BYTES );
					buf.append( "/* u2 attribute_name_index=" );
					buf.append( attribute_name_index );
					buf.append( ", " );
					boolean codeAttribute = false;
					if ( constantPool[attribute_name_index] instanceof ConstantUtf8Info )
					{
						String utf8String = ( (ConstantUtf8Info) constantPool[attribute_name_index] ).getString( );
						if ( utf8String.equals( "Code" ) )
						{
							codeAttribute = true;
						}
					}
					buf.append( constantPool[attribute_name_index].getInfo( ) );
					buf.append( " */" );
					buf.append( '\n' );

					buf.append( appendBytes( offset, offset + 4 ) );
					int attribute_length = cr.readInt( offset );
					offset += 4;
					buf.append( _12_BYTES );
					buf.append( "/* u4 attribute_length=" );
					buf.append( attribute_length );
					buf.append( " */" );
					buf.append( '\n' );
					buf.append( _16_BYTES );
					buf.append( "/* Attribute bytes: */" );
					buf.append( '\n' );

					if ( codeAttribute )
					{
						/*
						 * Code_attribute { u2 attribute_name_index; u4
						 * attribute_length; u2 max_stack; u2 max_locals; u4
						 * code_length; u1 code[code_length]; u2
						 * exception_table_length; { u2 start_pc; u2 end_pc; u2
						 * handler_pc; u2 catch_type; }
						 * exception_table[exception_table_length]; u2
						 * attributes_count; attribute_info
						 * attributes[attributes_count]; }
						 */
						buf.append( _16_BYTES );
						buf.append( "/* Code_attribute: */" );
						buf.append( '\n' );

						buf.append( appendBytes( offset, offset + 2 ) );
						int max_stack = cr.readUnsignedShort( offset );
						offset += 2;
						buf.append( _14_BYTES );
						buf.append( "/* u2 max_stack=" );
						buf.append( max_stack );
						buf.append( " */" );
						buf.append( '\n' );

						buf.append( appendBytes( offset, offset + 2 ) );
						int max_locals = cr.readUnsignedShort( offset );
						offset += 2;
						buf.append( _14_BYTES );
						buf.append( "/* u2 max_locals=" );
						buf.append( max_locals );
						buf.append( " */" );
						buf.append( '\n' );

						buf.append( appendBytes( offset, offset + 4 ) );
						int code_length = cr.readInt( offset );
						offset += 4;
						buf.append( _12_BYTES );
						buf.append( "/* u4 code_length=" );
						buf.append( code_length );
						buf.append( " */" );
						buf.append( '\n' );

						buf.append( _16_BYTES );
						buf.append( "/* Code instructions: */" );
						buf.append( '\n' );

						buf.append( appendBytes( offset, offset + code_length ) );
						offset += code_length;
						buf.append( '\n' );

						/* exception_table */
						buf.append( appendBytes( offset, offset + 2 ) );
						int exception_table_length = cr.readUnsignedShort( offset );
						offset += 2;
						buf.append( _14_BYTES );
						buf.append( "/* u2 exception_table_length=" );
						buf.append( exception_table_length );
						buf.append( " */" );
						buf.append( '\n' );

						for ( int e = 0; e < exception_table_length; e++ )
						{
							/*
							 * u2 start_pc; u2 end_pc; u2 handler_pc; u2
							 * catch_type;
							 */
							buf.append( appendBytes( offset, offset + 4 ) );
							int start_pc = cr.readUnsignedShort( offset );
							offset += 2;
							int end_pc = cr.readUnsignedShort( offset );
							offset += 2;
							buf.append( _12_BYTES );
							buf.append( "/* u2 start_pc=" );
							buf.append( start_pc );
							buf.append( " u2 end_pc=" );
							buf.append( end_pc );
							buf.append( " */" );
							buf.append( '\n' );

							buf.append( appendBytes( offset, offset + 4 ) );
							int handler_pc = cr.readUnsignedShort( offset );
							offset += 2;
							int catch_type = cr.readUnsignedShort( offset );
							offset += 2;
							buf.append( _12_BYTES );
							buf.append( "/* u2 handler_pc=" );
							buf.append( handler_pc );
							buf.append( " u2 catch_type=" );
							buf.append( catch_type );
							buf.append( " */" );
							buf.append( '\n' );
						}

						buf.append( appendBytes( offset, offset + 2 ) );
						int code_attributes_count = cr.readUnsignedShort( offset );
						offset += 2;
						buf.append( _14_BYTES );
						buf.append( "/* u2 attributes_count=" );
						buf.append( code_attributes_count );
						buf.append( " */" );
						buf.append( '\n' );

						/*
						 * attribute_info { u2 attribute_name_index; u4
						 * attribute_length; u1 info[attribute_length]; }
						 */
						for ( int cj = 0; cj < code_attributes_count; cj++ )
						{
							buf.append( appendBytes( offset, offset + 2 ) );
							int code_attribute_name_index = cr.readUnsignedShort( offset );
							offset += 2;
							buf.append( _14_BYTES );
							buf.append( "/* u2 attribute_name_index=" );
							buf.append( code_attribute_name_index );
							buf.append( ", " );
							buf.append( constantPool[code_attribute_name_index].getInfo( ) );
							buf.append( " */" );
							buf.append( '\n' );

							buf.append( appendBytes( offset, offset + 4 ) );
							int code_attribute_length = cr.readInt( offset );
							offset += 4;
							buf.append( _12_BYTES );
							buf.append( "/* u4 attribute_length=" );
							buf.append( code_attribute_length );
							buf.append( " */" );
							buf.append( '\n' );

							buf.append( _16_BYTES );
							buf.append( "/* Attribute bytes: */" );
							buf.append( '\n' );
							buf.append( appendBytes( offset, offset + code_attribute_length ) );
							offset += code_attribute_length;
							buf.append( '\n' );
						}
						codeAttribute = false;
					}
					else
					{
						buf.append( appendBytes( offset, offset + attribute_length ) );
						offset += attribute_length;
					}
					buf.append( '\n' );
				}
			}
			buf.append( '\n' );

			/* class attrinutes */
			buf.append( appendBytes( offset, offset + 2 ) );
			int class_attributes_count = cr.readUnsignedShort( offset );
			offset += 2;
			buf.append( _14_BYTES );
			buf.append( "/* u2 attributes_count=" );
			buf.append( class_attributes_count );
			buf.append( " */" );
			buf.append( '\n' );

			/*
			 * attribute_info { u2 attribute_name_index; u4 attribute_length; u1
			 * info[attribute_length]; }
			 */
			for ( int j = 0; j < class_attributes_count; j++ )
			{
				buf.append( appendBytes( offset, offset + 2 ) );
				int attribute_name_index = cr.readUnsignedShort( offset );
				offset += 2;
				buf.append( _14_BYTES );
				buf.append( "/* u2 attribute_name_index=" );
				buf.append( attribute_name_index );
				buf.append( ", " );
				buf.append( constantPool[attribute_name_index].getInfo( ) );
				buf.append( " */" );
				buf.append( '\n' );

				buf.append( appendBytes( offset, offset + 4 ) );
				int attribute_length = cr.readInt( offset );
				offset += 4;
				buf.append( _12_BYTES );
				buf.append( "/* u4 attribute_length=" );
				buf.append( attribute_length );
				buf.append( " */" );
				buf.append( '\n' );

				buf.append( _16_BYTES );
				buf.append( "/* Attribute bytes: */" );
				buf.append( '\n' );
				buf.append( appendBytes( offset, offset + attribute_length ) );
				offset += attribute_length;
				buf.append( '\n' );
			}

			buf.append( "/* end of the class file */" );

			/* rest - should always be empty */
			buf.append( appendBytes( offset, bytes.length ) );
		}
		catch ( ArrayIndexOutOfBoundsException e )
		{
			buf.append( '\n' );
			buf.append( e );
			buf.append( '\n' );
			StackTraceElement[] stackTraceElement = e.getStackTrace( );
			for ( StackTraceElement ste : stackTraceElement )
			{
				buf.append( ste );
				buf.append( '\n' );
			}
			buf.append( '\n' );
		}

		return buf.toString( );
	}

	/**
	 * Creates a string as a representation of the constant pool bytes.
	 * 
	 * @return the string represented the constant pool bytes
	 * @throws ParseException
	 */
	private String constantPoolToString( ) throws ParseException
	{
		StringBuffer buf = new StringBuffer( );
		for ( int i = 0; i < constantPool.length; i++ )
		{
			AbstractConstantPoolEntry en = constantPool[i];
			boolean appendConstantPoolinfo = true;
			if ( en != null )
			{
				try
				{
					ByteArrayOutputStream os = new ByteArrayOutputStream( );
					DataOutputStream dos = new DataOutputStream( os );
					en.write( dos );

					byte byteArray[] = os.toByteArray( );
					int j = 1;
					for ( ; j <= byteArray.length; j++ )
					{
						buf.append( String.format( "%02X", byteArray[( j - 1 )] ) );
						if ( ( j % 16 ) == 0 )
						{
							if ( appendConstantPoolinfo )
							{
								buf.append( ' ' );
								appendConstantPoolEntryInfo( buf, en, i );
								appendConstantPoolinfo = false;
							}
							buf.append( '\n' );
						}
						else
						{
							buf.append( ' ' );
						}
					}
					int rest = j % 16;
					if ( rest != 0 )
					{
						for ( j = 0; j < ( 16 - rest + 1 ) * 3; j++ )
						{
							buf.append( ' ' );
						}
					}
					else
					{
						buf.append( "   " );
					}

					offset = offset + byteArray.length;

					if ( appendConstantPoolinfo )
					{
						appendConstantPoolEntryInfo( buf, en, i );
						appendConstantPoolinfo = false;
					}
					buf.append( '\n' );

				}
				catch ( IOException e )
				{
					throw new ParseException( "Constant pool entry could not be parsed.", i );
				}
			}
		}

		return buf.toString( );
	}

	private void appendConstantPoolEntryInfo( StringBuffer buf, AbstractConstantPoolEntry en, int i )
	{
		buf.append( " /* [" );
		buf.append( String.valueOf( i ) );
		buf.append( "] " );
		buf.append( en.getTagMnemonics( ) );
		buf.append( ' ' );
		buf.append( en.getInfo( ) );
		buf.append( ' ' );
		buf.append( "*/" );
	}

	/**
	 * Creates a string representation of interface bytes.
	 * 
	 * @param interafaces
	 * @return string
	 */
	private String appendInterfaces( String[] interafaces )
	{
		StringBuffer buf = new StringBuffer( );
		for ( String i : interafaces )
		{
			buf.append( appendBytes( offset, offset + 2 ) );
			offset = offset + 2;

			buf.append( _14_BYTES );
			buf.append( "/* " );
			buf.append( i );
			buf.append( " */ " );
		}

		return buf.toString( );
	}

	/**
	 * Creates a string representation of bytes.
	 * 
	 * @param start
	 *            offset
	 * @param end
	 *            offset
	 * @return string
	 */
	private String appendBytes( int start, int end )
	{
		StringBuffer buf = new StringBuffer( );
		for ( int i = start, c = 1; i < end; i++, c++ )
		{
			String s = String.format( "%02X", bytes[i] );
			buf.append( s );
			if ( ( c % 16 ) == 0 )
			{
				buf.append( '\n' );
			}
			else
			{
				buf.append( ' ' );
			}
		}

		return buf.toString( );
	}

	/**
	 * Simple constant pool visitor.
	 */
	class ConstantPoolVisitor implements IConstantPoolVisitor
	{

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.drgarbage.asm_ext.IConstantPoolVisitor#visitConstantPool(byte[],
		 * int, int)
		 */
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
	}

	/**
	 * Converts the content of the string to a byte array. The multiline
	 * comments are ignored.
	 * 
	 * @param s
	 *            string
	 * @return byte array
	 * @throws ParseException
	 */
	public static byte[] getClasFileBytesFromString( String s ) throws ParseException
	{
		char charArray[] = s.toCharArray( );
		char charArray2[] = new char[charArray.length + 1];
		int j = 0;
		for ( int i = 0; i < charArray.length; i++ )
		{
			if ( charArray[i] == '/' )
			{
				if ( charArray[i + 1] == '/' )
				{ /* line comment */
					i++;
					i++;
					for ( ; i < charArray.length; i++ )
					{ /* loop until the end of the line */
						if ( charArray[i] == '\n' )
						{
							break;
						}
					}
				}
				else if ( charArray[i + 1] == '*' )
				{ /* line comment */
					i++;
					i++;
					for ( ; i < charArray.length; i++ )
					{ /* loop until the end of the comment */
						if ( charArray[i] == '*' )
						{
							if ( charArray[i + 1] == '/' )
							{
								i++;
								break;
							}
						}
					}
				}
				else
				{ /* unexpected character */
					throw new ParseException( "Unexpected character.", i );
				}
			}

			if ( ( charArray[i] >= '0' && charArray[i] <= '9' ) /*
																 * only
																 * numerical
																 * characters
																 */
					|| ( charArray[i] >= 'A' && charArray[i] <= 'F' ) /*
																		 * A, B,
																		 * C, D,
																		 * E, F
																		 */
					|| ( charArray[i] >= 'a'
							&& charArray[i] <= 'f' ) /* a, b, c , d, f */
			)
			{
				charArray2[j] = charArray[i];
				j++;
			}
		}

		/* the number of characters has to be always an even number */
		if ( j % 2 != 0 )
		{
			throw new ParseException( "The number of byte characters is not an even number.", j );
		}

		charArray2[j++] = '\0'; /* set the end of the string */

		/* copy the character array to the byte array */
		byte bytes[] = new byte[( j - 1 ) / 2];
		int b = 0;
		for ( int i = 0; i < charArray2.length; i++ )
		{
			if ( charArray2[i] == '\0' )
			{
				break;
			}

			char c1 = convertCharToDec( charArray2[i] );
			char c2 = convertCharToDec( charArray2[++i] );

			byte bb = (byte) ( ( c1 << 4 ) + c2 );
			bytes[b] = bb;
			b++;
		}

		return bytes;
	}

	/**
	 * Convert a character into byte.
	 * 
	 * @param c
	 *            is a character one of 0 ... 9, A ... F
	 * @return decimal value
	 */
	public static char convertCharToDec( char c )
	{

		switch ( c )
		{
			case '0' :
				return 0;
			case '1' :
				return 1;
			case '2' :
				return 2;
			case '3' :
				return 3;
			case '4' :
				return 4;
			case '5' :
				return 5;
			case '6' :
				return 6;
			case '7' :
				return 7;
			case '8' :
				return 8;
			case '9' :
				return 9;
			case 'A' :
			case 'a' :
				return 10;
			case 'B' :
			case 'b' :
				return 11;
			case 'C' :
			case 'c' :
				return 12;
			case 'D' :
			case 'd' :
				return 13;
			case 'E' :
			case 'e' :
				return 14;
			case 'F' :
			case 'f' :
				return 15;
		}

		return 0;
	}

}