package debugger;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.visualheap.debugger.NullListener;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;

public class CountingDebugListener extends NullListener {

	private CountDownLatch latch;
	private int count;

	public CountingDebugListener() {
		this.latch = new CountDownLatch(1);
		count = 0;
	}
	
	@Override
	public void onBreakpoint(StackFrame sf) {
		System.out.println("breakpoint reached");
		count = getObjectReferencesFromStackFrame(sf).size();
		latch.countDown();
	}
	
	public int getResult() throws InterruptedException {
		latch.await();
		return count;
	}

	public void reset() {
		latch = new CountDownLatch(1);
	}

	@Override
	public void onStep(StackFrame sf) {
		System.out.println("step performed");
		count = getObjectReferencesFromStackFrame(sf).size();
		latch.countDown();
	}

}
