package debugger.testprogs;

/**
 * Created with IntelliJ IDEA.
 * User: Anna
 * Date: 13/01/14
 * Time: 12:31
 * To change this template use File | Settings | File Templates.
 */
public class Demo {

    public static void main(String args[]) {

        TreeReference tree = new TreeReference(3);
        CyclicReference cyclic = new CyclicReference();
        NullReference nullRef = new NullReference();

        func(tree, cyclic, nullRef);
    }

    public static void func(TreeReference tree, CyclicReference cyclic, NullReference nullRefs) {

        System.out.println("break here");
    }
}