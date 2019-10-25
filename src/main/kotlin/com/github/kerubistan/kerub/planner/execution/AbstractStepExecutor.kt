package com.github.kerubistan.kerub.planner.execution

import com.github.kerubistan.kerub.planner.StepExecutor
import com.github.kerubistan.kerub.planner.steps.AbstractOperationalStep

/**
 * Generic framework class to better organize what a step executor have to do.
 */
abstract class AbstractStepExecutor<T : AbstractOperationalStep, U> : StepExecutor<T> {

	/**
	 * Check if the pre-conditions of performing the action are met, throw an exception if not.
	 * Default implementation does nothing, since most commands can be executed without pre-check.
	 */
	open fun prepare(step: T) {
		// most step may not need any check before trying to execute the operation
		// therefore the default implementation is doing nothing
	}

	/**
	 * Perform the step.
	 */
	abstract fun perform(step: T): U

	/**
	 * Verify the execution of the step.
	 * By default does nothing, since most junix commands should fail on error, specific tools can use this if not.
	 */
	open fun verify(step: T) {
		// optionally the step executors can verify if the execution is successful, but this is most of the time
		// not needed, therefore the default implementation is not doing anything
	}

	/**
	 * Update the database with the new fact
	 */
	abstract fun update(step: T, updates: U)

	final override fun execute(step: T) {
		prepare(step)
		val updates = perform(step)
		verify(step)
		update(step, updates)
	}
}