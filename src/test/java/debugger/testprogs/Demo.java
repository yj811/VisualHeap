package debugger.testprogs;

public class Demo {

    public static void main(String args[]) {
        func();
    }

    public static void func() {
        BinaryTree tree = new BinaryTree(3);
        CyclicReference cyclic = new CyclicReference();
        NullReference nullRef = new NullReference();
        SimpleReference[] arr = new SimpleReference[10];

        arr[0] = new SimpleReference(3);
        arr[1] = new SimpleReference(3);

        System.out.println("Examples created");

        foo(tree, cyclic, nullRef, arr);
    }

    public static void foo(BinaryTree tree, CyclicReference cyclic, NullReference nullRef, SimpleReference[] arr) {

    }
}