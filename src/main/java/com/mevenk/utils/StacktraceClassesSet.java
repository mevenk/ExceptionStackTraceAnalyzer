/**
 * 
 */
package com.mevenk.utils;

import static com.mevenk.utils.ExceptionStackTraceAnalyzer.LINE_SEPARATOR;
import static com.mevenk.utils.ExceptionStackTraceAnalyzer.TAB_SPACE;

import java.util.HashSet;
import java.util.Iterator;

/**
 * @author Venkatesh
 *
 */
public class StacktraceClassesSet extends HashSet<Class<? extends Throwable>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4095675829486190803L;

	@Override
	public String toString() {
		if (this.isEmpty()) {
			return super.toString();
		}
		StringBuilder stringBuilderStacktraceClassesSet = new StringBuilder();
		stringBuilderStacktraceClassesSet.append("{" + LINE_SEPARATOR);

		Iterator<Class<? extends Throwable>> iterator = iterator();

		while (iterator.hasNext()) {
			stringBuilderStacktraceClassesSet.append(TAB_SPACE);
			stringBuilderStacktraceClassesSet.append(iterator.next().getName());
			stringBuilderStacktraceClassesSet.append(LINE_SEPARATOR);
		}
		stringBuilderStacktraceClassesSet.append(TAB_SPACE + "}");
		return stringBuilderStacktraceClassesSet.toString();
	}

}
