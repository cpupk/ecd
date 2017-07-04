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

/**
 * Interface for the outline synchronization.
 * @author Sergej Alekseev
 * @version $Revision: 187 $
 * $Id: IOutlineElement.java 187 2013-06-03 05:47:37Z salekseev $
 */
public interface IOutlineElement {

	/**
	 * Returns the line number of the visualizer 
	 * instruction in the bytecode editor. 
	 * @return line
	 */
	public int getBytecodeDocumentLine();
	
	/**
	 * Returns the source code line number if available
	 * or -1 if not.
	 * @return line
	 */
	public int getSourceCodeDocumentLine();
}
