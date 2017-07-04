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
 * A <code>listener</code> that will be fired on each update of the {@link IClassFileDocument}
 * by which it is registered.
 *
 * @author Sergej Alekseev and Peter Palaga 
 * @version $Revision: 187 $
 * $Id: IDocumentUpdateListener.java 187 2013-06-03 05:47:37Z salekseev $
 */
public interface IDocumentUpdateListener {

	/**
	 * fired on each update of the {@link IClassFileDocument}
     * by which this {@link IDocumentUpdateListener} is registered.
	 */
	public void documentUpdated(IClassFileDocument classFileDocument);
	
}
