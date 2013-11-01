package debugger;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.visualheap.debugger.NullListener;

import com.sun.jdi.ObjectReference;


public class LatchingDebugListener extends NullListener {


	private CountDownLatch latch;
	private List<ObjectReference> fromStackFrame;

	public LatchingDebugListener() {
		this.latch = new CountDownLatch(1);
	}
	
	@Override
	public void onBreakpoint(List<ObjectReference> fromStackFrame) {	
		this.fromStackFrame = fromStackFrame;
		latch.countDown();
	}
	
	public List<ObjectReference> getResult() throws InterruptedException {
		latch.await();
		return fromStackFrame;
	}

	@Override
	public void onStep(List<ObjectReference> fromStackFrame) {
		// TODO Auto-generated method stub
		
	}


}
