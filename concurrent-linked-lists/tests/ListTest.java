package tests;

import linkedLists.SimplifiedList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

abstract class ListTest {
    public static final int TRIALS = 5000;
    abstract <E> SimplifiedList<E> createList();

    @Test
    void sequentialTest(){
        SimplifiedList<String> list = createList();
        list.add("one");
        list.add("three");
        list.add("five");
        list.add("seven");

        list.insert(3, "six");
        list.insert(1, "two");
        list.insert(3, "four");


        assertEquals(7, list.count());
        assertEquals("one",   list.get(0));
        assertTrue(list.contains("one"));
        assertEquals("two",   list.get(1));
        assertTrue(list.contains("two"));
        assertEquals("three", list.get(2));
        assertTrue(list.contains("three"));
        assertEquals("four",  list.get(3));
        assertTrue(list.contains("four"));
        assertEquals("five",  list.get(4));
        assertTrue(list.contains("five"));
        assertEquals("six",   list.get(5));
        assertTrue(list.contains("six"));
        assertEquals("seven", list.get(6));
        assertTrue(list.contains("seven"));


        list.remove("one");
        assertEquals(6, list.count());
        assertFalse(list.contains("one"));

        list.remove("two");
        assertEquals(5, list.count());
        assertFalse(list.contains("two"));

        list.remove("four");
        assertEquals(4, list.count());
        assertTrue(list.contains("three"));
        assertFalse(list.contains("four"));

        list.remove("three");
        assertEquals(3, list.count());
        assertFalse(list.contains("three"));
        assertTrue(list.contains("five"));

        list.remove("five");
        assertEquals(2, list.count());
        assertFalse(list.contains("five"));

        list.remove("six");
        assertEquals(1, list.count());
        assertFalse(list.contains("six"));

        list.remove("seven");
        assertEquals(0, list.count());
        assertFalse(list.contains("seven"));
    }

    @Test
    void testInsertRead() throws InterruptedException {
        for (int i = 0; i < TRIALS; i++) {
            concurrentInsertRead(true);
            concurrentInsertRead(false);
        }
    }

    void concurrentInsertRead(boolean readerFirst) throws InterruptedException {
        SimplifiedList<String> list = createList();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        Thread reader = new Thread(()->{
            assertEquals("a", list.get(0));
            assertEquals("b", list.get(1));
            assertEquals("c", list.get(2));
            assertEquals("d", list.get(3));
        });
        Thread inserter = new Thread(()->{
           list.add("e");
           list.add("f");
           list.add("g");
           list.add("h");
        });
        if(readerFirst) {
            reader.start();
            inserter.start();
        } else {
            inserter.start();
            reader.start();
        }
        reader.join();
        inserter.join();
        assertEquals("e", list.get(4));
        assertEquals("f", list.get(5));
        assertEquals("g", list.get(6));
        assertEquals("h", list.get(7));
    }

    @Test
    void testRemovals() throws InterruptedException {
        for (int i = 0; i < TRIALS; i++) {
            concurrentRemoves(true);
            concurrentRemoves(false);
            concurrentRemovesHead(true);
            concurrentRemovesHead(false);
        }
    }

    @Test
    void testRemoveInsert() throws InterruptedException{
        for (int i = 0; i < TRIALS; i++) {
            concurrentRemoveInsert(true, true);
            concurrentRemoveInsert(true, false);
            concurrentRemoveInsert(false, true);
            concurrentRemoveInsert(false, false);
        }
    }

    void concurrentRemoveInsert(boolean removeFirst, boolean headFirst) throws InterruptedException{
        System.out.println();
        SimplifiedList<String> list = createList();
        list.add("head");
        list.add("tail");
        list.add("trash");
        Thread headRemover = new Thread(()->{
            System.out.println("HEAD REMOVER - Started");
            list.remove("head");
            System.out.println("HEAD REMOVER - Ended");
        });
        headRemover.setName("Head Remover");
        Thread tailRemover = new Thread(()->{
            System.out.println("TAIL REMOVER - Started");
            list.remove("tail");
            System.out.println("TAIL REMOVER - Ended");
        });
        tailRemover.setName("Tail Remover");
        Thread firstRemover, secondRemover;
        if(headFirst){
            firstRemover = headRemover;
            secondRemover = tailRemover;
        } else {
            firstRemover = tailRemover;
            secondRemover = headRemover;
        }
        Thread inserter = new Thread(()->{
            System.out.println("INSERTER     - Started");
            list.insert(1, "new");
            System.out.println("INSERTER     - Ended");
        });
        inserter.setName("Inserter");
        if(removeFirst){
            firstRemover.start();
            secondRemover.start();
            inserter.start();
        } else{
            inserter.start();
            firstRemover.start();
            secondRemover.start();
        }
        firstRemover.join();
        secondRemover.join();
        inserter.join();
        assertEquals(2, list.count());
        list.remove("trash");
        assertEquals(1, list.count());
        assertEquals("new", list.get(0));
    }

    @Test
    void testMultipleInsert() throws InterruptedException{
        for (int i = 0; i < TRIALS; i++) {
            concurrentInsertMultiple();
        }
    }
    void concurrentInsertMultiple() throws InterruptedException{
        final int NUM = 3;
        SimplifiedList<String> list = createList();
        list.add("head");
        list.add("tail");
        Thread[] inserters = new Thread[NUM];
        for (int i = 0; i < NUM; i++) {
            final int finalI = i;
            inserters[i] = new Thread(()->list.insert(1, "new_" + finalI));
            inserters[i].setName("Inserter " + i);
        }
        for (int i = 0; i < NUM; i++) {
            inserters[i].start();
        }
        for (int i = 0; i < NUM; i++) {
            inserters[i].join();
        }
        assertEquals(NUM + 2, list.count());
        for (int i = 0; i < NUM; i++) {
            assertTrue(list.contains("new_" + i), i + " not found");
        }
    }

    void concurrentRemoves(boolean redFirst) throws InterruptedException {
        SimplifiedList<String> list = createList();
        list.add("yellow");
        list.add("red");
        list.add("blue");
        list.add("yellow");
        Thread redRemover = new Thread(()->list.remove("red"));
        Thread blueRemover = new Thread(()->list.remove("blue"));
        if(redFirst){
            redRemover.start();
            blueRemover.start();
        } else{
            blueRemover.start();
            redRemover.start();
        }
        redRemover.join();
        blueRemover.join();
        assertEquals(2, list.count());
        assertEquals("yellow", list.get(0));
        assertEquals("yellow", list.get(1));
    }

    void concurrentRemovesHead(boolean redFirst) throws InterruptedException {
        System.out.println();
        SimplifiedList<String> list = createList();
        list.add("red");
        list.add("blue");
        list.add("yellow");
        assertEquals(3, list.count());
        Thread blueRemover = new Thread(()->{
            System.out.println("BLUE REMOVER - Started");
            list.remove("blue");
            System.out.println("BLUE REMOVER - Ended");
        });
        blueRemover.setName("Blue Remover");
        Thread redRemover = new Thread(()->{
            System.out.println("RED REMOVER  - Started");
            list.remove("red");
            System.out.println("RED REMOVER  - Ended");
        });
        redRemover.setName("Red Remover");
        if(redFirst){
            redRemover.start();
            blueRemover.start();
        } else{
            blueRemover.start();
            redRemover.start();
        }
        redRemover.join();
        blueRemover.join();
        assertEquals(1, list.count());
        assertEquals("yellow", list.get(0));
    }
}