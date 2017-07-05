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

package com.drgarbage.javalang;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * The collection of java utility methods.
 * 
 * @author Sergej Alekseev
 * @version $Revision: 426 $
 * $Id: JavaLangUtils.java 426 2013-11-29 14:47:54Z salekseev $
 */
public class JavaLangUtils {
	
	public static File findFileResource(String[] classPath, String packageName, String className) throws IOException {
		if(packageName == null){
			packageName= "";
		}
	    String relativePath = packageName.replace('.', File.separatorChar) + (packageName.length() == 0 ? "" : File.separator) + className + ".class";
	    for (int i = 0; i <  classPath.length; i++) {
	        File currentClassPathEntry = new File(classPath[i]);
	        if (!currentClassPathEntry.exists()) {
	            continue;
	        }
	        if (currentClassPathEntry.isDirectory()) {
	            File file = new File(currentClassPathEntry, relativePath);
	            if (file.exists()) {
	            	return file;
	            }
	        } else if (currentClassPathEntry.isFile()) {
	        	/* not supported, just ignore */
	        }
	    }
	    return null;
	}
	
	public static InputStream findResource(String[] classPath, String packageName, String className) throws IOException {
		if(packageName == null){
			packageName= "";
		}
	    String relativePath = packageName.replace('.', File.separatorChar) + (packageName.length() == 0 ? "" : File.separator) + className + ".class";
	    String jarRelativePath = relativePath.replace(File.separatorChar, '/');
	    for (int i = 0; i <  classPath.length; i++) {
	        File currentClassPathEntry = new File(classPath[i]);
	        if (!currentClassPathEntry.exists()) {
	            continue;
	        }
	        if (currentClassPathEntry.isDirectory()) {
	            File file = new File(currentClassPathEntry, relativePath);
	            if (file.exists()) {
	                return new FileInputStream(file);
	            }
	        } else if (currentClassPathEntry.isFile()) {
	        	JarFile jarFile = null;
            	jarFile = new JarFile(currentClassPathEntry);
                JarEntry jarEntry = jarFile.getJarEntry(jarRelativePath);
                if (jarEntry != null) {
                    return jarFile.getInputStream(jarEntry);
                }
                else {
	            	if (jarFile != null) {
		                jarFile.close();
	            	}
                }
	        }
	    }
	    return null;
	}
	
	public static String[] computeRuntimeClassPath(IJavaProject jp) throws CoreException{
		if(jp == null){
			return null;
		}

		String[] classpath = JavaRuntime.computeDefaultRuntimeClassPath(jp);
		return classpath;
	}

}
