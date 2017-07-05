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

package com.drgarbage.utils;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

/**
 * Utils for control flow factory actions.
 *
 * @author Sergej Alekseev
 * @version $Revision: 187 $
 * $Id: ActionUtils.java 187 2013-06-03 05:47:37Z salekseev $
 */
public class ActionUtils {
	
	/**
	 * Resolves a type name in the context of the declaring type.
	 * 
	 * @param refTypeSig the type name in signature notation (for example 'QVector') this can also be an array type, but dimensions will be ignored.
	 * @param declaringType the context for resolving (type where the reference was made in)
	 * @return returns the fully qualified type name or build-in-type name. if a unresolved type couldn't be resolved null is returned
	 */
	public static String getResolvedTypeName(String refTypeSig, IType declaringType) throws JavaModelException, IllegalArgumentException {
		int arrayCount= Signature.getArrayCount(refTypeSig);
		
		/*
		 * use the last element for resolving the type
		 * For example: QString [QString or [[QString
		 * The last element is always 'Q'
		 */
		char type= refTypeSig.charAt(arrayCount);
		if (type == Signature.C_UNRESOLVED) {
			String name= ""; //$NON-NLS-1$
			int bracket= refTypeSig.indexOf(Signature.C_GENERIC_START, arrayCount + 1);
			if (bracket > 0)
				name= refTypeSig.substring(arrayCount + 1, bracket);
			else {
				int semi= refTypeSig.indexOf(Signature.C_SEMICOLON, arrayCount + 1);
				if (semi == -1) {
					throw new IllegalArgumentException();
				}
				name= refTypeSig.substring(arrayCount + 1, semi);
			}
			String[][] resolvedNames= declaringType.resolveType(name);
			if (resolvedNames != null && resolvedNames.length > 0) {
				StringBuffer res=new StringBuffer();
				for(int i=0; i <arrayCount; i++){
					res.append('[');
				}
				res.append(concatenateName(resolvedNames[0][0], resolvedNames[0][1]));
				return res.toString();
			}
			throw new IllegalArgumentException();
		} else {
			return refTypeSig;
		}
	}
	
	/**
	 * Concatenates two names. Uses a slash '/' for separation.
	 * Terminates the string with a simicolor ';'. 
	 * Both strings can be empty or <code>null</code>.
	 */
	public static String concatenateName(String name1, String name2) {
		StringBuffer buf= new StringBuffer("L");
		if (name1 != null && name1.length() > 0) {
			buf.append(name1.replace('.', '/'));
		}
		if (name2 != null && name2.length() > 0) {
			if (buf.length() > 0) {
				buf.append('/');
			}
			buf.append(name2);
		}	
		
		buf.append(";");
		return buf.toString();
	}
}
