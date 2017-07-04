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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <code>OutputStream</code> which counts the number of bytes written.
 *
 */
public class CountedOutputStream extends FilterOutputStream {

    private int byteCount = 0;

    /**
     * Constructor.
     *
     * @param out the output stream.
     */
    public CountedOutputStream(OutputStream out) {
        super(out);
    }

    public void write(int b) throws IOException {
        out.write(b);
        byteCount++;
    }

    /**
     * Get the number of bytes written.
     *
     * @return the number of bytes
     */
    public int getByteCount() {
        return byteCount;
    }
}