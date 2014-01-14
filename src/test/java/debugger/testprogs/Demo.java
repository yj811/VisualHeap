package debugger.testprogs;

public class Demo {

    public static void main(String args[]) {
        func();
    }

    public static void func() {
        BinaryTree tree = new BinaryTree(3);
        CyclicReference cyclic = new CyclicReference();
        NullReference nullRef = new NullReference();

        System.out.println("Examples created");

        foo(tree, cyclic, nullRef);
    }

    public static void foo(BinaryTree tree, CyclicReference cyclic, NullReference nullRef) {

    }
}