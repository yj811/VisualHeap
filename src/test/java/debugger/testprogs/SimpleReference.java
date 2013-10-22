package debugger.testprogs;

public class SimpleReference {
	
	private SimpleReference ref;
	
	public SimpleReference(int i) {
		ref = (i > 0) ? new SimpleReference(i--) : null;
	}
	
	public static void main(String[] args) {
		
		SimpleReference ref = new SimpleReference(10);
		
		System.out.println(ref.hashCode());
		
		
	}

}
