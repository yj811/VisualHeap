package org.visualheap.debugger;

import com.sun.jdi.request.StepRequest;

/**
 * Wraps StepRequest's STEP_INTO, STEP_OVER and STEP_OUT constants.
 * @author oliver
 *
 */
public enum StepDepth {
	
	STEP_INTO(StepRequest.STEP_INTO),
	STEP_OVER(StepRequest.STEP_OVER),
	STEP_OUT(StepRequest.STEP_OUT);

	private int stepRequestDepth;
	
	StepDepth(int stepRequestDepth) {
		this.stepRequestDepth = stepRequestDepth;
	}
	
	int toStepRequestDepth() {
		return stepRequestDepth;
	}
	
}
