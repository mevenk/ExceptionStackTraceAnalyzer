/**
 * 
 */
package com.mevenk.utils;

import static com.mevenk.utils.ExceptionStackTraceAnalyzer.SystemExitCause.EXCEPTION;
import static com.mevenk.utils.ExceptionStackTraceAnalyzer.SystemExitCause.MAX_ATTEMPTS_REACHED;
import static com.mevenk.utils.ExceptionStackTraceAnalyzer.SystemExitCause.NO_EXCEPTIONS_FOUND;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jutils.jhardware.HardwareInfo;
import org.jutils.jhardware.model.ProcessorInfo;
import org.reflections.Reflections;

/**
 * @author Venkatesh
 *
 */
public class ExceptionStackTraceAnalyzer {

	enum SystemExitCause {
		EXCEPTION(1), MAX_ATTEMPTS_REACHED(2), NO_EXCEPTIONS_FOUND(3);

		private Pattern pattern = Pattern.compile("([A-Z]{1})([A-Z]*)(_?)");

		private int returnStatusCode;

		SystemExitCause(int returnStatusCode) {
			this.returnStatusCode = returnStatusCode;
		}

		public int returnStatusCode() {
			return returnStatusCode;
		}

		public String getAsFormattedText() {
			boolean matchFound = false;
			String formattedText = this.toString();
			StringBuilder stringBuilderFormattedText = new StringBuilder();
			Matcher matcher = pattern.matcher(formattedText);
			while (matcher.find()) {
				matchFound = true;
				stringBuilderFormattedText.append(matcher.group(1) + matcher.group(2).toLowerCase() + " ");
			}
			if (matchFound) {
				formattedText = stringBuilderFormattedText.toString().replaceAll("\\s$", "");
			}
			return formattedText;
		}

	}

	static final String LINE_SEPARATOR = System.lineSeparator();
	static long iterationsMaxValue = Integer.MAX_VALUE;

	static final String TAB_SPACE = "	";
	static final String JAVA = "java";

	static final String EXCEPTION_NAME_SUFFIX = "Exception";

	static final int ITERATIONS_INPUT_PROMPT_MAX_ATTEMPTS = 3;
	static int iterationsInputPromptCounter = 1;

	static Map<Class<? extends Throwable>, Integer> stacktraceStoppedClasses = Collections
			.synchronizedMap(new StacktraceClassesMap());
	static Set<Class<? extends Throwable>> stacktraceNotStoppedClasses = Collections
			.synchronizedSet(new StacktraceClassesSet());

	static long iterationsInput = 0L;
	static boolean allThrowableClasses = true;

	static Scanner scanner = new Scanner(System.in);

	static long iterations;

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) {

		try {

			System.out.println("System Information");

			ProcessorInfo processorInfo = HardwareInfo.getProcessorInfo();
			Set<Entry<String, String>> fullInfos = processorInfo.getFullInfo().entrySet();

			String valueFullInfoEntrySet = null;
			for (final Entry<String, String> fullInfo : fullInfos) {
				valueFullInfoEntrySet = fullInfo.getValue();
				if (isEmpty(valueFullInfoEntrySet) || isBlank(valueFullInfoEntrySet)) {
					continue;
				}
				System.out.println(fullInfo.getKey() + ": " + valueFullInfoEntrySet);
			}

			System.out.println();

			MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
			System.out.println("Heap Memory Usage: " + LINE_SEPARATOR + TAB_SPACE + memoryMXBean.getHeapMemoryUsage());

			final Runtime runtime = Runtime.getRuntime();

			// Get current size of heap in bytes
			long heapSize = runtime.totalMemory();

			// Get maximum size of heap in bytes. The heap cannot grow beyond this size.//
			// Any attempt will result in an OutOfMemoryException.
			long heapMaxSize = runtime.maxMemory();

			// Get amount of free memory within the heap in bytes. This size will increase
			// // after garbage collection and decrease as new objects are created.
			long heapFreeSize = runtime.freeMemory();

			System.out.println("Heap Size: " + heapSize + LINE_SEPARATOR + TAB_SPACE + "Max:" + heapMaxSize
					+ LINE_SEPARATOR + TAB_SPACE + "Free: " + heapFreeSize + LINE_SEPARATOR);

			System.out.println("No of Iterations? ( Max: " + iterationsMaxValue + ")");

			promptForIterationsInput();

			ExceptionStackTraceAnalyzer.iterations = iterationsInput;

			Reflections.log = null;
			Reflections reflections = new Reflections(".*");
			Set<Class<? extends Throwable>> throwableClasses = null;

			if (allThrowableClasses) {
				throwableClasses = reflections.getSubTypesOf(Throwable.class);
			}

			if (isEmpty(throwableClasses)) {
				systemExit(NO_EXCEPTIONS_FOUND, " for ");
			}

			Thread[] exceptions = new ExceptionLoop[throwableClasses.size()];

			System.out.println(LINE_SEPARATOR + exceptions.length + " Exceptions for each of " + iterationsInput
					+ " iterations!!" + LINE_SEPARATOR);

			Thread currentExceptionThread = null;
			int threadCounter = -1;
			for (Class currentThrowableClass : throwableClasses) {
				currentExceptionThread = new ExceptionLoop(currentThrowableClass);
				exceptions[++threadCounter] = currentExceptionThread;
				currentExceptionThread.start();
			}

			for (Thread currentExceptionThreadToJoin : exceptions) {
				try {
					currentExceptionThreadToJoin.join();
				} catch (InterruptedException interruptedException) {
					System.err.println(currentExceptionThreadToJoin.getName() + " Interrupted!!");
					Thread.currentThread().interrupt();
				}
			}

			if (!isEmpty(stacktraceNotStoppedClasses)) {
				System.out.println(LINE_SEPARATOR + LINE_SEPARATOR + "Stacktrace Not Stopped classes: "
						+ stacktraceNotStoppedClasses + LINE_SEPARATOR);
			}

			if (!stacktraceStoppedClasses.isEmpty()) {
				System.out.println(LINE_SEPARATOR + LINE_SEPARATOR + "Stacktrace Stopped classes: "
						+ stacktraceStoppedClasses + LINE_SEPARATOR);
			}
			if (stacktraceNotStoppedClasses.size() == throwableClasses.size()) {
				System.out.println("No Exception Stacktrace Supressed !!");
			}

		} catch (Exception exception) {
			exception.printStackTrace();
			systemExit(EXCEPTION);

		}
	}

	/**
	 * 
	 */
	private static void promptForIterationsInput() {
		try {
			iterationsInput = scanner.nextLong();
			if (iterationsInput > iterationsMaxValue) {
				if (++iterationsInputPromptCounter > ITERATIONS_INPUT_PROMPT_MAX_ATTEMPTS) {
					systemExit(MAX_ATTEMPTS_REACHED);
				}
				System.err.println("Do not cross " + iterationsMaxValue);
				promptForIterationsInput();
			}
		} catch (Exception exception) {
			systemExit(EXCEPTION, "Proper INPUT Required!!");
		} finally {
			scanner.close();
		}
	}

	static void printError(String errorMessage) {
		System.err.println(errorMessage);
	}

	static void systemExit(SystemExitCause systemExitCause) {
		systemExit(systemExitCause, null);
	}

	static void systemExit(SystemExitCause systemExitCause, String exitMessage) {
		if (exitMessage == null) {
			System.err.println(LINE_SEPARATOR + systemExitCause.getAsFormattedText());
		} else {
			System.err.println(
					LINE_SEPARATOR + systemExitCause.getAsFormattedText() + LINE_SEPARATOR + TAB_SPACE + exitMessage);
		}

		System.exit(systemExitCause.returnStatusCode);
	}

}
