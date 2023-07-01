package tests.utils;
import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.function.ThrowingSupplier;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

public class ExtraAssertions {
    public static void assertXOR(boolean a, boolean b) {
        assertTrue("Boolean a has value " + a +
                        " and boolean b has the value " + b +
                        " which doesn't satisfy the XOR",
                a != b);
    }

    public static void assertAllTrue(BitSet bitSet, int count){
        BitSet clone = (BitSet)bitSet.clone();
        clone.flip(0, count);
        int trueCount = bitSet.cardinality();
        int falseCount = count-trueCount;
        if(falseCount > 0){
            fail("Bit set still has " + falseCount +  " false bits out of " + count + " at {" +
                    String.join(
                            ",",
                            ()-> clone.stream().limit(count).mapToObj(x->(CharSequence)Integer.toString(x)).iterator()
                    ) + "}"
            );
        }
    }

    public static void assertNotThrows(ThrowingRunnable runnable){
        Throwable thrown = null;
        try{
            runnable.run();
        } catch (Throwable e){
            thrown = e;
            throw new RuntimeException(e);
        } finally {
            if(thrown != null){
                fail("Operation threw: " + thrown.toString());
            }
        }
    }

    public static <E> E assertNotThrows(ThrowingSupplier<E> runnable){
        Throwable thrown = null;
        try{
            return runnable.get();
        } catch (Throwable e){
            thrown = e;
            throw new RuntimeException(e);
        } finally {
            if(thrown != null){
                fail("Operation threw: " + thrown.toString());
            }
        }
    }
}
