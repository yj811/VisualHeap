package debugger;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

import org.junit.Test;
import org.visualheap.debugger.DebugListener;

import com.sun.jdi.ObjectReference;


public class CountingDebugListener implements DebugListener {

	private CountDownLatch latch;
	private int count;

	public CountingDebugListener() {
		this.latch = new CountDownLatch(1);
		count = 0;
	}
	
	@Override
	public void onBreakpoint(List<ObjectReference> fromStackFrame) {
		System.out.println("breakpoint reached");
		count = fromStackFrame.size();
		latch.countDown();
	}
	
	public int getResult() throws InterruptedException {
		latch.await();
		return count;
	}

	public void reset() {
		latch = new CountDownLatch(1);
	}

}
