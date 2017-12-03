/**
 * 
 */
package com.mevenk.utils;

import static com.mevenk.utils.ExceptionStackTraceAnalyzer.LINE_SEPARATOR;
import static com.mevenk.utils.ExceptionStackTraceAnalyzer.TAB_SPACE;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Venkatesh
 *
 */
public class StacktraceClassesMap extends HashMap<Class<? extends Throwable>, Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4709866798781640556L;

	@Override
	public String toString() {
		if (this.isEmpty()) {
			return super.toString();
		}
		StringBuilder stringBuilderStacktraceClassesMap = new StringBuilder();
		stringBuilderStacktraceClassesMap.append("{" + LINE_SEPARATOR);
		for (Map.Entry<Class<? extends Throwable>, Integer> currentEntry : this.entrySet()) {
			stringBuilderStacktraceClassesMap.append(currentEntry.getKey().getName());
			stringBuilderStacktraceClassesMap.append(LINE_SEPARATOR);
			stringBuilderStacktraceClassesMap.append(TAB_SPACE);
			stringBuilderStacktraceClassesMap.append(TAB_SPACE);
			stringBuilderStacktraceClassesMap.append(currentEntry.getValue() + " Iterations");
			stringBuilderStacktraceClassesMap.append(LINE_SEPARATOR);
		}
		stringBuilderStacktraceClassesMap.append(TAB_SPACE + "}");
		return stringBuilderStacktraceClassesMap.toString();
	}

}