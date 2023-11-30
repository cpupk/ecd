/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class DecompilerOutputUtil {

	public static final String NO_LINE_NUMBER = "// Warning: No line numbers available in class file"; //$NON-NLS-1$

	/**
	 * Input string
	 */
	private final String input;

	/**
	 * Input split into lines
	 */
	private final List<InputLine> inputLines = new ArrayList<>();

	/**
	 * Parsed input
	 */
	private CompilationUnit unit;

	/**
	 * Output lines
	 */
	private final List<JavaSrcLine> javaSrcLines = new ArrayList<>();

	public final static String line_separator = System.getProperty("line.separator", //$NON-NLS-1$
			"\r\n"); //$NON-NLS-1$

	private String decompilerType;

	private class InputLine {

		String line;
		int outputLineNum = -1;
		int calculatedNumLineJavaSrc = -1;

		@Override
		public String toString() {
			return line;
		}
	}

	private class JavaSrcLine {

		List<Integer> inputLines = new ArrayList<>();

		@Override
		public String toString() {
			return inputLines.toString();
		}
	}

	public DecompilerOutputUtil(String decompilerType, String input) {
		this.input = input + line_separator;
		this.decompilerType = decompilerType;
	}

	public String realign() {
		// Handle special cases
		if (input == null) {
			return null;
		}
		if (input.isEmpty()) {
			return input;
		}

		// Compute the string offset of every source line
		fillOutputList();

		// Parse source code into AST
		javaSrcLines.add(null);
		ASTParser parser = ASTParser.newParser(DecompilerOutputUtil.getMaxJSLLevel()); // AST.JLS3
		CompilerOptions option = new CompilerOptions();
		Map<String, String> options = option.getMap();
		options.put(CompilerOptions.OPTION_Compliance, DecompilerOutputUtil.getMaxDecompileLevel()); // $NON-NLS-1$
		options.put(CompilerOptions.OPTION_Source, DecompilerOutputUtil.getMaxDecompileLevel()); // $NON-NLS-1$
		parser.setCompilerOptions(options);

		parser.setSource(input.toCharArray());

		unit = (CompilationUnit) parser.createAST(null);

		// Iterate over types (ignoring enums and annotations)
		List types = unit.types();
		for (int i = 0; i < types.size(); i++) {
			if (types.get(i) instanceof AbstractTypeDeclaration) {
				// Recursively process the elements within this type
				processElements((AbstractTypeDeclaration) types.get(i));
			}
		}

		// Iterate over types (ignorning enums and annotations)
		int firstTypeLine = Integer.MAX_VALUE;
		int lastTypeLine = Integer.MIN_VALUE;
		for (int i = 0; i < types.size(); i++) {
			if (!(types.get(i) instanceof AbstractTypeDeclaration)) {
				continue;
			}
			AbstractTypeDeclaration type = (AbstractTypeDeclaration) types.get(i);

			// Recursively process the types within this type
			processTypes(type);

			// Update firstTypeLine/lastTypeLine
			int numLine = unit.getLineNumber(type.getStartPosition());
			if (numLine < firstTypeLine) {
				firstTypeLine = numLine;
			}
			numLine = unit.getLineNumber(type.getStartPosition() + type.getLength() - 1);
			if (numLine > lastTypeLine) {
				lastTypeLine = numLine;
			}
		}

		// Special case - no source items to handle so just return our input
		if (javaSrcLines.size() == 1) {
			String warning = "\r\n" + NO_LINE_NUMBER + "\r\n"; //$NON-NLS-1$ //$NON-NLS-2$
			return warning + input; // $NON-NLS-1$
		}

		// Add all the source lines above the first type
		if (firstTypeLine != Integer.MAX_VALUE) {
			addBelow(firstTypeLine - 1, 0, 0);
		}

		// Add all the source lines below the last type
		if (lastTypeLine != Integer.MIN_VALUE) {
			addBelow(inputLines.size() - 2, lastTypeLine, javaSrcLines.size() - 1);
		}

		// Create aligned source
		return toString();
	}

	public static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}

	public static String replace(String text, String searchString, String replacement) {
		if (isEmpty(text) || isEmpty(searchString) || replacement == null) {
			return text;
		}
		int start = 0;
		int end = text.indexOf(searchString, start);
		if (end == -1) {
			return text;
		}
		int replLength = searchString.length();
		int increase = replacement.length() - replLength;
		increase = (increase < 0 ? 0 : increase);
		increase *= 16;
		StringBuffer buf = new StringBuffer(text.length() + increase);
		while (end != -1) {
			buf.append(text.substring(start, end)).append(replacement);
			start = end + replLength;
			end = text.indexOf(searchString, 2);
		}
		buf.append(text.substring(start));
		return buf.toString();
	}

	@Override
	public String toString() {
		String line;
		int numLine;
		StringBuffer realignOutput = new StringBuffer();

		int lineNumberWidth = String.valueOf(javaSrcLines.size()).length();

		boolean generateEmptyString = true;
		int leftTrimSpace = 0;

		Pattern pattern = Pattern.compile("/\\*\\s+\\*/", //$NON-NLS-1$
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(input);
		if (matcher.find()) {
			generateEmptyString = false;

			pattern = Pattern.compile("([ ]+)import", //$NON-NLS-1$
					Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(input);
			if (matcher.find()) {
				leftTrimSpace = matcher.group().replace("import", "") //$NON-NLS-1$ //$NON-NLS-2$
						.length();
			}
		} else {
			pattern = Pattern.compile("([ ]+)import", //$NON-NLS-1$
					Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(input);
			if (matcher.find()) {
				leftTrimSpace = matcher.group().replace("import", "") //$NON-NLS-1$ //$NON-NLS-2$
						.length();
				generateEmptyString = true;
			}
		}

		int lastBracketIndex = input.lastIndexOf('}');
		if (lastBracketIndex != -1) {
			int trimSpace = getLeftPosition(input, input.lastIndexOf('}', lastBracketIndex - 1))
					- getLeftPosition(input, lastBracketIndex);
			if (trimSpace > 4) {
				leftTrimSpace += trimSpace - 4;
			}
		}

		for (int i = 1; i < javaSrcLines.size(); i++) {
			JavaSrcLine javaSrcLine = initJavaSrcListItem(i);

			if (javaSrcLine.inputLines.size() > 0) {
				int outputLineNumber = getOutputLineNumber(javaSrcLine);

				if (outputLineNumber != -1) {
					List<Integer> beforeLines = getBeforeLines(javaSrcLine);
					if (beforeLines != null && !beforeLines.isEmpty()) {

						int index = realignOutput.lastIndexOf(line_separator);
						if (index == realignOutput.length() - line_separator.length()) {
							realignOutput.replace(index, index + line_separator.length(), ""); //$NON-NLS-1$

							for (int j = 0; j < beforeLines.size(); j++) {
								numLine = beforeLines.get(j);
								line = inputLines.get(numLine).line;
								line = removeJavaLineNumber(line.replace("\r\n", "\n") //$NON-NLS-1$ //$NON-NLS-2$
										.replace("\n", ""), //$NON-NLS-1$ //$NON-NLS-2$
										j == 0 && generateEmptyString, leftTrimSpace);
								realignOutput.append(line);
							}

							realignOutput.append(line_separator);

							javaSrcLine.inputLines.removeAll(beforeLines);
						}
					}
				}

				if (i > 0) {
					realignOutput.append("/* " //$NON-NLS-1$
							+ getLineNumber(outputLineNumber, lineNumberWidth) + " */ "); //$NON-NLS-1$
				}

				for (int j = 0; j < javaSrcLine.inputLines.size(); j++) {
					numLine = javaSrcLine.inputLines.get(j);
					line = inputLines.get(numLine).line;
					line = removeJavaLineNumber(line.replace("\r\n", "\n").replace("\n", ""), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
							j == 0 && generateEmptyString, leftTrimSpace);
					realignOutput.append(line);
				}
			} else if (i > 1) {
				realignOutput.append("/* " //$NON-NLS-1$
						+ getLineNumber(-1, lineNumberWidth) + " */ "); //$NON-NLS-1$
			}
			realignOutput.append(line_separator);
		}
		return realignOutput.toString();
	}

	private List<Integer> getBeforeLines(JavaSrcLine javaSrcLine) {
		List<Integer> lineNumbers = new ArrayList<>();
		for (int num : javaSrcLine.inputLines) {
			InputLine line = inputLines.get(num);
			if (line != null && line.outputLineNum != -1) {
				break;
			} else {
				lineNumbers.add(num);
			}
		}
		return lineNumbers;
	}

	private int getOutputLineNumber(JavaSrcLine javaSrcLine) {
		for (int numLine : javaSrcLine.inputLines) {
			InputLine inputLine = inputLines.get(numLine);
			if (inputLine != null && inputLine.outputLineNum != -1) {
				return inputLine.outputLineNum;
			}
		}
		return -1;
	}

	private int getLeftPosition(String string, int index) {
		if (string == null || string.length() < index) {
			return -1;
		}

		for (int j = index - 1; j >= 0; j--) {
			if (j < 0) {
				break;
			}
			if (string.charAt(j) == '\n') {
				return index - j;
			}
		}
		return -1;
	}

	private String getLineNumber(int i, int lineNumberWidth) {
		String number = String.valueOf(i);
		int width = number.length();
		if (i == -1) {
			width = 0;
			number = ""; //$NON-NLS-1$
		}
		if (width < lineNumberWidth) {
			for (int j = 0; j < lineNumberWidth - width; j++) {
				number = " " + number; //$NON-NLS-1$
			}
		}

		return number;
	}

	private String generageEmptyString(int length) {
		char[] chs = new char[length];
		for (int i = 0; i < chs.length; i++) {
			chs[i] = ' ';
		}
		return new String(chs);
	}

	private void fillOutputList() {
		int lineStart = 0;
		int lineEnd = 0;
		inputLines.add(null);
		while (lineStart < input.length()) {
			// Compute line end
			lineEnd = input.indexOf('\n', lineEnd);
			if (lineEnd == -1) {
				lineEnd = input.length();
			} else {
				lineEnd++;
			}

			// Build OutputLine object
			InputLine outputLine = new InputLine();
			outputLine.line = input.substring(lineStart, lineEnd);
			inputLines.add(outputLine);

			// Next line start is current line end
			lineStart = lineEnd;
		}
	}

	public static int parseJavaLineNumber(String decompilerType, String line) {
		String regex = "/\\*\\s*\\d+\\s*\\*/"; //$NON-NLS-1$
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line.trim());
		if (matcher.find()) {
			return Integer.parseInt(matcher.group().replaceAll("[^0-9]", "")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return -1;
	}

	public static int parseJavaLineNumber(String line) {
		String regex = "/\\*\\s*\\d+\\s*\\*/"; //$NON-NLS-1$
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line.trim());
		if (matcher.find()) {
			return Integer.parseInt(matcher.group().replaceAll("[^0-9]", "")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			regex = "//\\s+\\d+"; //$NON-NLS-1$
		}
		pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(line.trim());
		if (matcher.find()) {
			return Integer.parseInt(matcher.group().replaceAll("[^0-9]", "")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return -1;
	}

	private String removeJavaLineNumber(String line, boolean generageEmptyString, int leftTrimSpace) {
		String regex = "/\\*\\s*\\d+\\s*\\*/"; //$NON-NLS-1$
//		if (DecompilerType.FernFlower.equals(decompilerType)) {
//			regex = "//\\s+\\d+(\\s*\\d*)*"; //$NON-NLS-1$
//		}
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(line.trim());

		if (matcher.find()) {
			line = line.replace(matcher.group(), ""); //$NON-NLS-1$
			if (generageEmptyString) {
				line = generageEmptyString(matcher.group().length()) + line;
			}
		}
		regex = "/\\*\\s+\\*/"; //$NON-NLS-1$
		pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(line);

		if (matcher.find()) {
			line = line.replace(matcher.group(), ""); //$NON-NLS-1$
			if (generageEmptyString) {
				line = generageEmptyString(matcher.group().length()) + line;
			}
		}
		if (leftTrimSpace > 0 && line.startsWith(generageEmptyString(leftTrimSpace))) {
			line = line.substring(leftTrimSpace);
		}
		return line;
	}

	/**
	 * Make sure {@link #javaSrcLines} is at least outputLineNum entries long
	 * (adding nulls if necessary).
	 *
	 * @param outputLineNum
	 * @return the {@link JavaSrcLine} at index outputLineNum (creating one if one
	 *         doesn't already exist).
	 */
	private JavaSrcLine initJavaSrcListItem(int outputLineNum) {

		// Fill in nulls for any previous output line nums which we haven't
		// visited yet
		if (javaSrcLines.size() <= outputLineNum) {
			for (int a = javaSrcLines.size(); a <= outputLineNum; a++)
				javaSrcLines.add(null);
		}

		// Create an output entry at the outputLineNum index
		JavaSrcLine javaSrcLine = javaSrcLines.get(outputLineNum);
		if (javaSrcLine == null) {
			javaSrcLine = new JavaSrcLine();
			javaSrcLines.set(outputLineNum, javaSrcLine);
		}
		return javaSrcLine;
	}

	private void addAbove(int inputBeginLineNo, int inputLineNo, int outputLineNo) {
		if (outputLineNo == 1) {
			return;
		}

		int offset = 1;
		/*
		 * Example:
		 * 
		 * 19: / / public static boolean isTranslucencySupported(Translucency
		 * paramTranslucency) 20: / / { 21: / 105 / switch
		 * (1.$SwitchMap$com$sun$awt$AWTUtilities$Translucency[paramTranslucency
		 * .ordinal()]) 22: / / { 23: / / case 1: 24: / 107 / return
		 * isWindowShapingSupported();
		 * 
		 * # addAbove(19, 21, 105) javaSrcLines[105] = [21] is already set when this
		 * method is called. This method creates the following entries in javaSrcLines:
		 * javaSrcLines[103] = [19] javaSrcLines[104] = [20]
		 * 
		 * # addAbove(19, 24, 107) javaSrcLines[107] = [24] is already set when this
		 * method is called. This method creates the following entries in javaSrcLines:
		 * javaSrcLines[106] = [23]
		 * 
		 * javaSrcLines[105] already has an entry so we add input line 22 to
		 * javaSrcLines[106]: javaSrcLines[105] = [22,23]
		 * 
		 * The result is the following folding of the code:
		 * 
		 * 103: / / public static boolean isTranslucencySupported(Translucency
		 * paramTranslucency) 104: / / { 105: / 105 / switch
		 * (1.$SwitchMap$com$sun$awt$AWTUtilities$Translucency[paramTranslucency
		 * .ordinal()]) 106: / / {/ / case 1: 107: / 107 / return
		 * isWindowShapingSupported();
		 */

		// Iterate backwards through the input lines towards inputBeginLineNo
		while (inputBeginLineNo <= (inputLineNo - offset)) {

			int offsetInputLine = inputLineNo - offset;
			InputLine inputLine = inputLines.get(offsetInputLine);

			if (inputLine.outputLineNum == -1) {
				// Got an InputLine without a corresponding Java source line
				JavaSrcLine javaSrcLine = null;
				int offsetOutputLine = outputLineNo - offset;

				if (offsetOutputLine > 0) {
					javaSrcLine = initJavaSrcListItem(offsetOutputLine);
				}

				if (offsetOutputLine == 1 || javaSrcLine.inputLines.size() > 0) {
					// We have reached the start of the file OR the current
					// javaSrcLine has some output lines

					int offsetOutputLineNext = offsetOutputLine + 1;

					// Get the JavaSrcLine for the output line after the current
					// one
					JavaSrcLine javaSrcLineNext = initJavaSrcListItem(offsetOutputLineNext);

					// Iterate backwards through the input lines towards
					// inputBeginLineNo from the output offset
					for (int innerOffset = offset; (inputLineNo - innerOffset) >= inputBeginLineNo; innerOffset++) {

						int innerOffsetInputLine = inputLineNo - innerOffset;
						inputLine = inputLines.get(innerOffsetInputLine);
						if (inputLine.outputLineNum == -1) {
							// Found an input line without a source line number
							// - add it to javaSrcLineNext
							javaSrcLineNext.inputLines.add(0, innerOffsetInputLine);
							inputLine.calculatedNumLineJavaSrc = offsetOutputLineNext;
						} else {
							// Got an InputLine with a corresponding Java source
							// line
							// we must already have handled this line and the
							// ones above
							// it. Time to bail out!
							break;
						}
					}
					// Run out of lines to process - bail out
					break;
				}

				// Add the offsetInputLine to the current javaSrcLine
				javaSrcLine.inputLines.add(offsetInputLine);
				inputLine.calculatedNumLineJavaSrc = offsetOutputLine;
			} else {
				// Got an InputLine with a corresponding Java source line
				// we must already have handled this line and the ones above
				// it. Time to bail out!
				break;
			}
			offset++;
		}
	}

	private void addBelow(int inputEndLineNo, int inputLineNo, int outputLineNo) {

		int offset = 1;

		// Iterate forwards through the input lines towards inputEndLineNo
		while ((inputLineNo + offset) < inputEndLineNo) {

			int offsetInputLine = inputLineNo + offset;
			InputLine outputLine = inputLines.get(offsetInputLine);

			if (outputLine.outputLineNum == -1) {
				// Got an InputLine without a corresponding Java source line
				int offsetOutputLine = outputLineNo + offset;
				JavaSrcLine javaSrcLine = initJavaSrcListItem(offsetOutputLine);

				if (javaSrcLine.inputLines.size() > 0) {
					// The current javaSrcLine has some output lines
					int offsetOutputLinePrev = offsetOutputLine - 1;

					// Get the JavaSrcLine for the output line after the current
					// one
					JavaSrcLine javaSrcLinePrev = initJavaSrcListItem(offsetOutputLinePrev);

					// Iterate forwards through the input lines towards
					// inputEndLineNo from the output offset
					for (int innerOffset = offset; (inputLineNo + innerOffset) <= inputEndLineNo; innerOffset++) {

						int innerOffsetInputLine = inputLineNo + innerOffset;
						outputLine = inputLines.get(innerOffsetInputLine);
						if (outputLine.outputLineNum == -1) {
							// Found an input line without a source line number
							// - add it to javaSrcLineNext
							javaSrcLinePrev.inputLines.add(innerOffsetInputLine);
							outputLine.calculatedNumLineJavaSrc = offsetOutputLinePrev;
						} else {
							// Got an InputLine with a corresponding Java source
							// line
							// we must already have handled this line and the
							// ones above
							// it. Time to bail out!
							break;
						}
					}
					// Run out of lines to process - bail out
					break;
				}
				javaSrcLine.inputLines.add(offsetInputLine);
				outputLine.calculatedNumLineJavaSrc = offsetOutputLine;
			} else {
				// Got an InputLine with a corresponding Java source line
				// we must already have handled this line and the ones above
				// it. Time to bail out!
				break;
			}
			offset++;
		}

		if (inputLineNo + offset == inputEndLineNo) {
			int offsetOutputLine = outputLineNo + offset;

			JavaSrcLine javaSrcLine = initJavaSrcListItem(offsetOutputLine);
			javaSrcLine.inputLines.add(inputEndLineNo);

			InputLine outputLine = inputLines.get(inputEndLineNo);
			outputLine.calculatedNumLineJavaSrc = offsetOutputLine;
		}
	}

	private void processTypes(AbstractTypeDeclaration rootType) {
		List<?> declarations = rootType.bodyDeclarations();
		for (Object declaration : declarations) {
			if (declaration instanceof AbstractTypeDeclaration) {
				AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) declaration;
				processTypes(typeDeclaration);
			}
		}

		int beginTypeLine = Integer.MAX_VALUE;
		int endTypeLine = Integer.MIN_VALUE;
		int firstMethodLine = Integer.MAX_VALUE;
		int lastMethodLine = Integer.MIN_VALUE;

		int beginTypeInputLineNo = unit.getLineNumber(rootType.getStartPosition());
		int endTypeInputLineNo = unit.getLineNumber(rootType.getStartPosition() + rootType.getLength() - 1);

		// Iterate forward through the input line numbers of the type
		for (int inputLineNo = beginTypeInputLineNo; inputLineNo <= endTypeInputLineNo; inputLineNo++) {

			// Get the output line number
			InputLine inputLine = inputLines.get(inputLineNo);
			int numLineJavaSrc = inputLine.outputLineNum;
			if (numLineJavaSrc == -1) {
				numLineJavaSrc = inputLine.calculatedNumLineJavaSrc;
			}

			if (numLineJavaSrc != -1) {
				// Update the type begin/end output line numbers
				if (beginTypeLine > numLineJavaSrc) {
					beginTypeLine = numLineJavaSrc;
				}
				if (endTypeLine < numLineJavaSrc) {
					endTypeLine = numLineJavaSrc;
				}

				// Update the type first/last method input line numbers
				if (firstMethodLine > inputLineNo) {
					firstMethodLine = inputLineNo;
				}
				if (lastMethodLine < inputLineNo) {
					lastMethodLine = inputLineNo;
				}
			}
		}

		// Process the lines above and below this type
		if (beginTypeLine != Integer.MAX_VALUE) {
			addAbove(beginTypeInputLineNo, firstMethodLine, beginTypeLine);
			addBelow(endTypeInputLineNo, lastMethodLine, endTypeLine);
		}
	}

	private void processMembers(AbstractTypeDeclaration rootType) {

		// Iterate over the declarations in this type
		List<Object> bodyDeclarations = new ArrayList<Object>();
		if (rootType instanceof EnumDeclaration) {
			EnumDeclaration enumDeclaration = (EnumDeclaration) rootType;
			List<?> enumDeclarations = enumDeclaration.enumConstants();

			// Iterate over the enum constant declarations
			int lastInputLineNo = -1;
			for (Object enumDeclObj : enumDeclarations) {
				if (enumDeclObj instanceof EnumConstantDeclaration) {
					ASTNode element = (ASTNode) enumDeclObj;
					int p = element.getStartPosition();
					int inputBeginLine = unit.getLineNumber(p);

					// If this declaration is on a new line add it to the
					// bodyDeclarations
					if (inputBeginLine != lastInputLineNo) {
						bodyDeclarations.add(enumDeclObj);
					}
					lastInputLineNo = inputBeginLine;
				}
			}
		}
		bodyDeclarations.addAll(rootType.bodyDeclarations());

		for (Object bodyDeclaration : bodyDeclarations) {
			if ((bodyDeclaration instanceof MethodDeclaration) || (bodyDeclaration instanceof Initializer)
					|| (bodyDeclaration instanceof FieldDeclaration)
					|| (bodyDeclaration instanceof EnumConstantDeclaration)) {
				ASTNode element = (ASTNode) bodyDeclaration;
				int p = element.getStartPosition();
				int inputBeginLine = unit.getLineNumber(p);
				int inputEndLine = unit.getLineNumber(p + element.getLength() - 1);
				processMember(inputBeginLine, inputEndLine);
			}
		}
	}

	private void processMember(int inputBeginLine, int inputEndLine) {
		int lastOutputLine = -1;
		int lastInputLine = -1;
		int maxLine = -1;
		// Iterate over the lines in this member
		for (int inputNumLine = inputBeginLine; inputNumLine <= inputEndLine; inputNumLine++) {

			// Parse the commented line number if available
			InputLine inputLine = inputLines.get(inputNumLine);
			inputLine.outputLineNum = parseJavaLineNumber(decompilerType, inputLine.line);

			if (inputLine.outputLineNum > 1) {

				// We have a commented line number!
				lastOutputLine = inputLine.outputLineNum;
				lastInputLine = inputNumLine;

				// Add the input line to the output JavaSrcLine
				JavaSrcLine javaSrcLine = initJavaSrcListItem(inputLine.outputLineNum);
				javaSrcLine.inputLines.add(inputNumLine);
				addAbove(inputBeginLine, inputNumLine, inputLine.outputLineNum);

				if (lastOutputLine > maxLine) {
					maxLine = lastOutputLine;
				}
			}
		}

		if (lastInputLine != -1 && lastInputLine < inputEndLine) {
			addBelow(inputEndLine, lastInputLine, maxLine);
		}
	}

	private void processElements(AbstractTypeDeclaration rootType) {
		if ((rootType instanceof TypeDeclaration) || (rootType instanceof EnumDeclaration)) {
			processMembers(rootType);
		}

		// Recurse into inner types and process their methods
		List<?> bodyDeclarations = rootType.bodyDeclarations();
		for (Object bodyDeclaration : bodyDeclarations) {
			if (bodyDeclaration instanceof AbstractTypeDeclaration) {
				processElements((AbstractTypeDeclaration) bodyDeclaration);
			}
		}
	}

	private static String level = null;

	/**
	 * @return the maximum Java version supported by the current Eclipse JDT version
	 */
	public static String getMaxDecompileLevel() {
		if (level != null) {
			return level;
		}

		// filter oot all versions that are not a simple integers e.g. "9" "10" ...
		Pattern p = Pattern.compile("^\\d+$");
		List<String> allVersions = new LinkedList<>(JavaCore.getAllVersions());
		Iterator<String> it = allVersions.iterator();
		while (it.hasNext()) {
			String v = it.next();
			if (!p.matcher(v).matches()) {
				it.remove();
			}
		}
		if (allVersions.isEmpty()) {
			level = "1.8"; //$NON-NLS-1$
			return level;
		}

		List<Integer> allVersionsInt = new ArrayList<>();
		for (String v : allVersions) {
			allVersionsInt.add(Integer.parseInt(v));
		}
		level = Integer.toString(Collections.max(allVersionsInt));
		return level;
	}

	private static int jslLevel = -1;

	public static int getMaxJSLLevel() {
		if (jslLevel != -1) {
			return jslLevel;
		}
		Pattern p = Pattern.compile("^JLS\\d+$");
		int maxFieldValue = 8; // Java 8 is supported by all Eclipse versions ECD targets
		for (Field f : AST.class.getFields()) {
			if (f.getType() != int.class || !p.matcher(f.getName()).matches()) {
				continue;
			}
			try {
				int value = f.getInt(AST.class);
				if (value > maxFieldValue) {
					maxFieldValue = value;
				}
			} catch (Exception e) {

			}
		}
		jslLevel = maxFieldValue;
		return jslLevel;
	}
}
