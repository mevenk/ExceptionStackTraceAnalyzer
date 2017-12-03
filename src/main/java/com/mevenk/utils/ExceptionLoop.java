package com.mevenk.utils;

import static com.mevenk.utils.ExceptionStackTraceAnalyzer.iterations;
import static com.mevenk.utils.ExceptionStackTraceAnalyzer.stacktraceNotStoppedClasses;
import static com.mevenk.utils.ExceptionStackTraceAnalyzer.stacktraceStoppedClasses;

import java.util.Date;
import java.util.concurrent.TimeUnit;

class ExceptionLoop extends Thread {

	private Class<? extends Throwable> throwableToBeThrown;
	private String throwableSimpleName;
	private String throwableQualifiedName;
	private Date dateStart;

	public ExceptionLoop(Class<? extends Throwable> throwableToBeThrown) {
		this.throwableToBeThrown = throwableToBeThrown;
		throwableSimpleName = "[" + throwableToBeThrown.getSimpleName() + "]";
		throwableQualifiedName = "[" + throwableToBeThrown.getName() + "]";
		super.setName(throwableSimpleName);
	}

	/**
	 * @return the throwableToBeThrown
	 */
	public Class<? extends Throwable> getThrowableToBeThrown() {
		return throwableToBeThrown;
	}

	/**
	 * @return the throwableSimpleName
	 */
	public String getThrowableSimpleName() {
		return throwableSimpleName;
	}

	/**
	 * @return the throwableQualifiedName
	 */
	public String getThrowableQualifiedName() {
		return throwableQualifiedName;
	}

	/**
	 * @return the dateStart
	 */
	public Date getDateStart() {
		return dateStart;
	}

	@Override
	public void run() {
		System.out.println(this.getName() + " Started!!");
		dateStart = new Date();
		for (int currentIteration = 1; currentIteration <= iterations; currentIteration++) {
			try {
				throw throwableToBeThrown.newInstance();
			} catch (Throwable throwable) {
				if (throwable.getStackTrace().length == 0) {
					stacktraceStopped(currentIteration);
					break;
				}
			} finally {
				if (currentIteration == iterations) {
					stacktraceNotStopped();
				}
			}

		}

	}

	private void stacktraceStopped(int iteration) {
		System.out.println(throwableQualifiedName + " Stacktrace stopped @ iteration: " + iteration);
		stacktraceStoppedClasses.put(throwableToBeThrown, iteration);
	}

	private void stacktraceNotStopped() {
		System.out.println(throwableQualifiedName + " Stacktrace NOT stopped even after " + iterations + " iterations "
				+ ExceptionStackTraceAnalyzer.LINE_SEPARATOR + " Time taken: "
				+ TimeUnit.MILLISECONDS.toSeconds((new Date().getTime() - dateStart.getTime())) + " s");
		stacktraceNotStoppedClasses.add(throwableToBeThrown);
	}
}