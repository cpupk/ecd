package org.sf.feeling.decompiler.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.JavaElement;

public class EclipseCompatibilityHelper {

	public static IBinaryType binaryTypeGetElementInfo(BinaryType binaryType) {
		// In Eclipse 2024-03 the method return type of getElementInfo() has been
		// changed from Object to IBinaryType. This can cause a NoSuchMethodError.
		// depending on the used Eclipse version.
		// Therefore we call his method using reflection to be safe.
		try {
			Method getElemInfoMethod = binaryType.getClass().getMethod("getElementInfo");
			return (IBinaryType) getElemInfoMethod.invoke(binaryType);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public static Object javaElementGetElementInfo(JavaElement classFile) {
		// In Eclipse 2024-03 the method return type of getElementInfo() has been
		// changed from Object to IElementInfo. This can cause a NoSuchMethodError
		// depending on the used Eclipse version.
		// Therefore we call his method using reflection to be safe.
		try {
			Method getElemInfoMethod = classFile.getClass().getMethod("getElementInfo");
			return getElemInfoMethod.invoke(classFile);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}
