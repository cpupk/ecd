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

import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
  *  A {@link DataInputStream} implementing {@link CountedDataInput}.
  *
  */
public class CountedDataInputStream extends DataInputStream
                                 implements CountedDataInput
{

    /**
	  * <code>InputStream</code> which counts the number of bytes read.
	  *
	  *  @version $Revision: 187 $
	  *  $Id: CountedDataInputStream.java 187 2013-06-03 05:47:37Z salekseev $
	  */
	static class CountedInputStream extends FilterInputStream {
	
	    private int byteCount = 0;
	
	    /**
	        Constructor.
	        @param in the input stream.
	     */
	    public CountedInputStream(InputStream in) {
	        super(in);
	    }
	    
	    public int read() throws IOException {
	        int b = in.read();
	        byteCount++;
	        return b;
	    }
	
	    public int read(byte[] b) throws IOException {
	        return read(b, 0, b.length);
	    }
	
	    public int read(byte[] b, int offset, int len) throws IOException {
	        int readCount = in.read(b, 0, b.length);
	        byteCount += readCount;
	        return readCount;
	        
	    }
	    
	    public long skip(long n) throws IOException {
	        long skipCount = in.skip(n);
	        byteCount += (int)skipCount;
	        return skipCount;
	    }
	
	    // Marking invalidates byteCount
	    public boolean markSupported() {
	        return false;
	    }
	
	    /**
	        Get the number of bytes read.
	        @return the number of bytes
	     */
	    public int getByteCount() {
	        return byteCount;
	    }
	}

	/**
        A constructor.
        @param in an {@link InputStream} to read from.
     */
    public CountedDataInputStream(InputStream in) {
        super(new CountedInputStream(in));
    }
    
    /* (non-Javadoc)
     * @see com.drgarbage.bytecode.InstructionParser.CountedInput#getByteCount()
     */
    public int getByteCount() {
        return ((CountedInputStream)in).getByteCount();
    }
    
}