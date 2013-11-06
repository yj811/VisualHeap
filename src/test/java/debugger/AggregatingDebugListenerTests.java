package debugger;

import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.visualheap.debugger.AggregatingDebugListener;
import org.visualheap.debugger.DebugListener;

import com.sun.jdi.StackFrame;
import com.sun.jdi.ObjectReference;

public class AggregatingDebugListenerTests {
	
	@Rule public JUnitRuleMockery context = new JUnitRuleMockery();
	

	AggregatingDebugListener aggSingle;
	AggregatingDebugListener aggThree;
	DebugListener listenerOne;
	DebugListener listenerTwo;
	DebugListener listenerThree;
	StackFrame sf;
	

	@Before
	public void setUp() {
		aggSingle = new AggregatingDebugListener();
		aggThree = new AggregatingDebugListener();
		listenerOne = context.mock(DebugListener.class, "one");
		listenerTwo = context.mock(DebugListener.class, "two");
		listenerThree = context.mock(DebugListener.class, "three");
		sf = context.mock(StackFrame.class, "stackFrame");
		aggSingle.addListener(listenerOne);
		aggThree.addListener(listenerOne);
		aggThree.addListener(listenerTwo);
		aggThree.addListener(listenerThree);
	}
	
	
	@Test
	public void singleListenerRecievesBreakpoint() {
		
		context.checking(new Expectations() {{
			oneOf(listenerOne).onBreakpoint(sf);
		}});
		
		aggSingle.onBreakpoint(sf);
		
	}
	
	@Test
	public void singleListenerRecievesStep() {
		
		context.checking(new Expectations() {{
			oneOf(listenerOne).onStep(sf);
		}});
		
		aggSingle.onStep(sf);
		
	}
	
	@Test
	public void singleListenerRecievesVMStart() {
		
		context.checking(new Expectations() {{
			oneOf(listenerOne).vmStart();
		}});
		
		aggSingle.vmStart();
		
	}
	
	@Test
	public void singleListenerRecievesVMDeath() {
		
		context.checking(new Expectations() {{
			oneOf(listenerOne).vmDeath();
		}});
		
		aggSingle.vmDeath();
		
	}
	
	@Test
	public void tripleListenerRecievesBreakpoint() {
		
		context.checking(new Expectations() {{
			oneOf(listenerOne).onBreakpoint(sf);
			oneOf(listenerTwo).onBreakpoint(sf);
			oneOf(listenerThree).onBreakpoint(sf);
		}});
		
		aggThree.onBreakpoint(sf);
		
	}
	
	@Test
	public void tripleListenerRecievesStep() {
		
		context.checking(new Expectations() {{
			oneOf(listenerOne).onStep(sf);
			oneOf(listenerTwo).onStep(sf);
			oneOf(listenerThree).onStep(sf);
		}});
		
		aggThree.onStep(sf);
		
	}
	
	@Test
	public void tripleListenerRecievesVMStart() {
		
		context.checking(new Expectations() {{
			oneOf(listenerOne).vmStart();
			oneOf(listenerTwo).vmStart();
			oneOf(listenerThree).vmStart();
		}});
		
		aggThree.vmStart();
		
	}
	
	@Test
	public void tripleListenerRecievesVMDeath() {
		
		context.checking(new Expectations() {{
			oneOf(listenerOne).vmDeath();
			oneOf(listenerTwo).vmDeath();
			oneOf(listenerThree).vmDeath();
		}});
		
		aggThree.vmDeath();
		
	}

}
