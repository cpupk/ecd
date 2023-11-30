/*******************************************************************************
 * Copyright (c) 2017 Chen Chao and other ECD project contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.sf.feeling.decompiler.source.attach.finder;

import java.util.List;

public abstract interface SourceCodeFinder {

	public abstract void find(String paramString, List<SourceFileResult> resultList);

	public abstract void cancel();

	public abstract String getDownloadUrl();
}
