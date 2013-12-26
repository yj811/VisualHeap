package debugger.testprogs;
public class MultipleTypes {

    MultipleTypes m = this;

    public static void main(String args[]) {
        MultipleTypes multi = new MultipleTypes();
        TripleCycle triple = new TripleCycle();
        SimpleReference ref = new SimpleReference(5);

        func(multi, triple, ref);
    }

    public static void func(MultipleTypes m, TripleCycle t, SimpleReference s) {
        System.out.println("break here");
    }

}
