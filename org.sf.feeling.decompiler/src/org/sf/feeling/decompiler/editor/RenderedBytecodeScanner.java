/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.ui.text.AbstractJavaScanner;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.sf.feeling.decompiler.JavaDecompilerPlugin;

import com.drgarbage.bytecode.ByteCodeConstants;
import com.drgarbage.javasrc.JavaKeywords;
import com.drgarbage.javasrc.JavaLexicalConstants;

/**
 * A Java code scanner.
 */
public final class RenderedBytecodeScanner extends AbstractJavaScanner implements JavaLexicalConstants, JavaKeywords {

	private static class WhitespaceDetector implements IWhitespaceDetector {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.text.rules.IWhitespaceDetector#isWhitespace(char)
		 */
		public boolean isWhitespace(char c) {
			return Character.isWhitespace(c);
		}

	}

	private static class WordDetector implements IWordDetector {

		/*
		 * @see IWordDetector#isWordStart
		 */
		public boolean isWordStart(char c) {
			return Character.isJavaIdentifierStart(c);
		}

		/*
		 * @see IWordDetector#isWordPart
		 */
		public boolean isWordPart(char c) {
			return Character.isJavaIdentifierPart(c);
		}
	}

	private static class SimpleRule extends WordRule {

		private IToken token;
		private StringBuffer fBuffer = new StringBuffer();

		public SimpleRule(IWordDetector detector, IToken token) {
			super(detector);
			this.token = token;
		}

		@Override
		public IToken evaluate(ICharacterScanner scanner) {
			int c = scanner.read();
			if (fDetector.isWordStart((char) c)) {
				if (fColumn == UNDEFINED || (fColumn == scanner.getColumn() - 1)) {

					fBuffer.setLength(0);
					do {
						fBuffer.append((char) c);
						c = scanner.read();
					} while (c != ICharacterScanner.EOF && fDetector.isWordPart((char) c));
					scanner.unread();

					return token;

				}
			}

			scanner.unread();
			return Token.UNDEFINED;
		}

	}

	private static class NumberWordDetector implements IWordDetector {

		/*
		 * @see IWordDetector#isWordStart
		 */
		public boolean isWordStart(char c) {
			return Character.isDigit(c);
		}

		/*
		 * @see IWordDetector#isWordPart
		 */
		public boolean isWordPart(char c) {
			return Character.isDigit(c) || c == JavaLexicalConstants.DOT;
		}
	}

	/**
	 * Rule to detect java operators.
	 *
	 * @since 3.0
	 */
	private class OperatorRule implements IRule {

		/** Java operators */
		private final char[] JAVA_OPERATORS = { SEMICOLON, LEFT_PARENTHESIS, RIGHT_PARENTHESIS, LEFT_BRACE, RIGHT_BRACE,
				DOT, EQUALS, SLASH, BACKSLASH, PLUS, MINUS, ASTERISK, LEFT_SQUARE_BRACKET, RIGHT_SQUARE_BRACKET, LT, GT,
				COLON, QUESTION_MARK, EXCLAMATION_MARK, COMMA, PIPE, AMPERSAND, CARET, PERCENT, TILDE };
		/** Token to return for this rule */
		private final IToken fToken;

		/**
		 * Creates a new operator rule.
		 *
		 * @param token Token to use for this rule
		 */
		public OperatorRule(IToken token) {
			fToken = token;
		}

		/**
		 * Is this character an operator character?
		 *
		 * @param character Character to determine whether it is an operator character
		 * @return <code>true</code> iff the character is an operator,
		 *         <code>false</code> otherwise.
		 */
		public boolean isOperator(char character) {
			for (int index = 0; index < JAVA_OPERATORS.length; index++) {
				if (JAVA_OPERATORS[index] == character)
					return true;
			}
			return false;
		}

		/*
		 * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.
		 * rules.ICharacterScanner)
		 */
		public IToken evaluate(ICharacterScanner scanner) {

			int character = scanner.read();
			if (isOperator((char) character)) {
				do {
					character = scanner.read();
				} while (isOperator((char) character));
				scanner.unread();
				return fToken;
			} else {
				scanner.unread();
				return Token.UNDEFINED;
			}
		}
	}

	private static final String[] RENDERED_BYTECODE_KEYWORDS = { ABSTRACT, CATCH, CLASS, CONST, DEFAULT, EXTENDS, FINAL,
			FINALLY, IMPLEMENTS, IMPORT, INTERFACE, NATIVE, PACKAGE, PRIVATE, PROTECTED, PUBLIC, STATIC, SUPER,
			SYNCHRONIZED, THIS, THROWS, TRANSIENT, TRY, VOLATILE, ENUM, VOID, BOOLEAN, CHAR, BYTE, SHORT, STRICTFP, INT,
			LONG, FLOAT, DOUBLE, FALSE, NULL, TRUE };

	private static String[] tokenProperties = { IJavaColorConstants.JAVA_KEYWORD, IJavaColorConstants.JAVA_STRING,
			IJavaColorConstants.JAVA_DEFAULT, IJavaColorConstants.JAVA_OPERATOR,
			JavaDecompilerPlugin.BYTECODE_MNEMONIC };

	/**
	 * Creates a Java code scanner
	 *
	 * @param manager the color manager
	 * @param store   the preference store
	 */
	public RenderedBytecodeScanner(IColorManager manager, IPreferenceStore store) {
		super(manager, store);
		initialize();
	}

	/*
	 * @see AbstractJavaScanner#getTokenProperties()
	 */
	protected String[] getTokenProperties() {
		return tokenProperties;
	}

	/*
	 * @see AbstractJavaScanner#createRules()
	 */
	protected List createRules() {

		List<IRule> rules = new ArrayList<IRule>();

		/* character constants */
		Token token = getToken(IJavaColorConstants.JAVA_STRING);
		rules.add(new SingleLineRule("'", "'", token, '\\')); //$NON-NLS-1$ //$NON-NLS-2$

		/* whitespace */
		rules.add(new WhitespaceRule(new WhitespaceDetector()));

		/* operators and brackets */
		token = getToken(IJavaColorConstants.JAVA_OPERATOR);
		rules.add(new OperatorRule(token));

		/* keywords */
		WordRule wordRule = new WordRule(new WordDetector());
		token = getToken(IJavaColorConstants.JAVA_KEYWORD);
		for (int i = 0; i < RENDERED_BYTECODE_KEYWORDS.length; i++) {
			wordRule.addWord(RENDERED_BYTECODE_KEYWORDS[i], token);
		}

		token = getToken(JavaDecompilerPlugin.BYTECODE_MNEMONIC);
		for (int i = 0; i < ByteCodeConstants.OPCODE_MNEMONICS.length; i++) {
			String word = ByteCodeConstants.OPCODE_MNEMONICS[i];
			if (word != null) {
				wordRule.addWord(word, token);
			}
		}

		rules.add(wordRule);

		/* identifiers */
		token = getToken(IJavaColorConstants.JAVA_DEFAULT);
		rules.add(new SimpleRule(new WordDetector(), token));

		/* numbers */
		token = getToken(IJavaColorConstants.JAVA_STRING);
		rules.add(new SimpleRule(new NumberWordDetector(), token));

		setDefaultReturnToken(getToken(IJavaColorConstants.JAVA_DEFAULT));
		return rules;
	}

}
