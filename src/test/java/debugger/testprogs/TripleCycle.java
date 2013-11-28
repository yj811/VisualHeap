package debugger.testprogs;

public class TripleCycle {
	
	public TripleCycle next;
	
	public static void main(String args[]) {
		TripleCycle a = new TripleCycle();
		TripleCycle b = new TripleCycle();
		TripleCycle c = new TripleCycle();
		
		a.next = b;
		b.next = c;
		c.next = a;
		
		func(a);
	}
	
	public static void func(TripleCycle entry) {
		System.out.println("break here");
	}

}
