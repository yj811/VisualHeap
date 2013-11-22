package debugger.testprogs;

public class TreeReference {
	
	TreeReference one;
	TreeReference two;
	
	TreeReference(int depth) {
		if(depth > 0) {
			one = new TreeReference(depth - 1);
			two = new TreeReference(depth - 1);
		}
	}
	
	public static void main(String[] args) {
		
		TreeReference first = new TreeReference(10);
		
		System.out.println("woah");
		
		
	}

}

