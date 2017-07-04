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

import java.util.List;

/**
 * Represenation of a try block.
 * 
 * @author Peter Palaga
 * @version $Revision: 187 $
 * $Id: ITryBlock.java 187 2013-06-03 05:47:37Z salekseev $
 */
public interface ITryBlock {
	/**
	 * Returns 1-based number of line on which this try block ends.
	 * 
	 * @return 1-based line number.
	 */
	public int getEndLine();
	
	/**
	 * {@link List} of {@link Integer}s representing 1-based line numbers 
	 * on which the associated exception handlers startOffset.
	 * 
	 * @return {@link List} of 1-based line numbers
	 */
	public List<Integer> getExceptionHandlerLines();
	
	/**
	 * Returns 1-based number of line on which this try block starts.
	 * 
	 * @return 1-based line number.
	 */
	public int getStartLine();
}
