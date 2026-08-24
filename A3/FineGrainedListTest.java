package ca.mcgill.ecse420.a3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FineGrainedListTest {

    public static void main(String[] args) throws InterruptedException {
        FineGrainedList<Integer> list = new FineGrainedList<>();
        list.add(3);
        list.add(7);
        list.add(15);
        assert list.contains(3) : "should have 3";
        assert list.contains(7) : "should have 7";
        assert !list.contains(99) : "99 was never added";
        System.out.println("basic test passed");

        list.remove(7);
        assert !list.contains(7) : "7 was removed";
        assert list.contains(3) : "3 should still be there";
        System.out.println("remove test passed");

        FineGrainedList<Integer> concList = new FineGrainedList<>();
        ExecutorService pool = Executors.newFixedThreadPool(4);

        for (int t = 0; t < 4; t++) {
            final int id = t;
            pool.submit(() -> {
                for (int i = id * 10; i < id * 10 + 10; i++) {
                    concList.add(i);
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        for (int i = 0; i < 40; i++) {
            assert concList.contains(i) : "missing " + i;
        }
        System.out.println("concurrent test passed");
        System.out.println("all tests passed");
    }
}
