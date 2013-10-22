package debugger.testprogs;

public class CyclicReference {

	private CyclicReference other;
	
	CyclicReference() {
		other = new CyclicReference(this);
	}
	
	CyclicReference(CyclicReference other) {
		this.other = other;
	}

	public static void main(String[] args) {
		CyclicReference cyclic = new CyclicReference();
		
		System.out.println(cyclic.hashCode());
	}

}
