package debugger.testprogs;
public class MultipleTypes {

    String s;

    public static void main(String args[]) {
        MultipleTypes multi = new MultipleTypes();

        TripleCycle a = new TripleCycle();
        TripleCycle b = new TripleCycle();
        TripleCycle c = new TripleCycle();

        a.next = b;
        b.next = c;
        c.next = a;

        SimpleReference ref = new SimpleReference(5);

        func(multi, a, ref);
    }

    public static void func(MultipleTypes m, TripleCycle t, SimpleReference s) {
        System.out.println("break here");
    }

}
