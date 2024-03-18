package org.sf.feeling.decompiler;

public class JavaDecompilerConstants {

	public static final String EDITOR_ID = "org.sf.feeling.decompiler.ClassFileEditor"; //$NON-NLS-1$
	public static final String PLUGIN_ID = "org.sf.feeling.decompiler"; //$NON-NLS-1$
	public static final String TEMP_DIR = "org.sf.feeling.decompiler.tempd"; //$NON-NLS-1$

	public static final String REUSE_BUFFER = "org.sf.feeling.decompiler.reusebuff"; //$NON-NLS-1$
	public static final String IGNORE_EXISTING = "org.sf.feeling.decompiler.alwaysuse"; //$NON-NLS-1$
	public static final String USE_ECLIPSE_FORMATTER = "org.sf.feeling.decompiler.use_eclipse_formatter"; //$NON-NLS-1$
	public static final String USE_ECLIPSE_SORTER = "org.sf.feeling.decompiler.use_eclipse_sorter"; //$NON-NLS-1$
	public static final String DECOMPILER_TYPE = "org.sf.feeling.decompiler.type"; //$NON-NLS-1$
	public static final String PREF_DISPLAY_LINE_NUMBERS = "jd.ide.eclipse.prefs.DisplayLineNumbers"; //$NON-NLS-1$
	public static final String PREF_DISPLAY_METADATA = "jd.ide.eclipse.prefs.DisplayMetadata"; //$NON-NLS-1$
	public static final String ALIGN = "jd.ide.eclipse.prefs.RealignLineNumbers"; //$NON-NLS-1$
	public static final String DEFAULT_EDITOR = "org.sf.feeling.decompiler.default_editor"; //$NON-NLS-1$ ;
	public static final String EXPORT_ENCODING = "org.sf.feeling.decompiler.export.encoding"; //$NON-NLS-1$ ;
	@Deprecated
	public static final String ATTACH_SOURCE = "org.sf.feeling.decompiler.attach_source"; //$NON-NLS-1$ ;
	public static final String bytecodeMnemonicPreferencesPrefix = "BYTECODEMNEMONIC_"; //$NON-NLS-1$
	public static final String BYTECODE_MNEMONIC = bytecodeMnemonicPreferencesPrefix + "bytecodeMnemonic"; //$NON-NLS-1$
	public static final String BYTECODE_MNEMONIC_BOLD = bytecodeMnemonicPreferencesPrefix + "bytecodeMnemonic_bold"; //$NON-NLS-1$
	public static final String BYTECODE_MNEMONIC_ITALIC = bytecodeMnemonicPreferencesPrefix + "bytecodeMnemonic_italic"; //$NON-NLS-1$
	public static final String BYTECODE_MNEMONIC_STRIKETHROUGH = bytecodeMnemonicPreferencesPrefix
	+ "bytecodeMnemonic_strikethrough"; //$NON-NLS-1$
	public static final String BYTECODE_MNEMONIC_UNDERLINE = bytecodeMnemonicPreferencesPrefix
	+ "bytecodeMnemonic_underline"; //$NON-NLS-1$
	public static final String classFileAttributePreferencesPrefix = "CLASS_FILE_ATTR_"; //$NON-NLS-1$
	public static final String CLASS_FILE_ATTR_SHOW_CONSTANT_POOL = classFileAttributePreferencesPrefix
	+ "show_constantPool"; //$NON-NLS-1$
	public static final String CLASS_FILE_ATTR_SHOW_LINE_NUMBER_TABLE = classFileAttributePreferencesPrefix
	+ "show_lineNumberTable"; //$NON-NLS-1$
	public static final String CLASS_FILE_ATTR_SHOW_VARIABLE_TABLE = classFileAttributePreferencesPrefix
	+ "show_localVariableTable"; //$NON-NLS-1$
	public static final String CLASS_FILE_ATTR_SHOW_EXCEPTION_TABLE = classFileAttributePreferencesPrefix
	+ "show_exceptionTable"; //$NON-NLS-1$
	public static final String CLASS_FILE_ATTR_SHOW_MAXS = classFileAttributePreferencesPrefix + "show_maxs"; //$NON-NLS-1$
	public static final String CLASS_FILE_ATTR_RENDER_TRYCATCH_BLOCKS = classFileAttributePreferencesPrefix
	+ "render_tryCatchBlocks"; //$NON-NLS-1$
	public static final String CLASS_FILE_ATTR_SHOW_SOURCE_LINE_NUMBERS = classFileAttributePreferencesPrefix
	+ "render_sourceLineNumbers"; //$NON-NLS-1$
	public static final String BRANCH_TARGET_ADDRESS_RENDERING = "BRANCH_TARGET_ADDRESS_RENDERING"; //$NON-NLS-1$
	public static final String BRANCH_TARGET_ADDRESS_ABSOLUTE = BRANCH_TARGET_ADDRESS_RENDERING + "_ABSOLUTE"; //$NON-NLS-1$
	public static final String BRANCH_TARGET_ADDRESS_RELATIVE = BRANCH_TARGET_ADDRESS_RENDERING + "_RELATIVE"; //$NON-NLS-1$
	public static final int SOURCE_MODE = 0;
	public static final int BYTE_CODE_MODE = 1;
	public static final int DISASSEMBLER_MODE = 2;

}
