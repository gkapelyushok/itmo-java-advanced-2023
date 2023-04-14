package info.kgeorgiy.ja.kapelyushok.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Class implementing {@link ParallelMapper}.
 */
public class ParallelMapperImpl implements ParallelMapper {

    private final Queue<Runnable> tasks = new LinkedList<>();
    private final List<Thread> threadsList;

    private static class Counter {
        private int cnt = 0;

        private int getCnt() {
            return cnt;
        }

        private void increment() {
            cnt++;
        }
    }

    /**
     * Thread-count constructor.
     * Creates a ParallelMapperImpl instance operating with {@code threads}
     * threads of type {@link Thread}.
     *
     * @param threads count of threads
     */
    public ParallelMapperImpl(int threads) {
        threadsList = Stream.generate(() ->
                new Thread(() -> {
                    try {
                        Runnable task;
                        while (!Thread.interrupted()) {
                            synchronized (tasks) {
                                while (tasks.isEmpty()) {
                                    tasks.wait();
                                }
                                task = tasks.poll();
                            }
                            task.run();
                        }
                    } catch (InterruptedException ignored) {

                    } finally {
                        Thread.currentThread().interrupt();
                    }
                }
                )
        ).limit(threads).collect(Collectors.toList());
        threadsList.forEach(Thread::start);
    }

    /**
     * Maps function {@code f} over specified {@code args}.
     * Mapping for each element performed in parallel.
     *
     * @throws InterruptedException if calling thread was interrupted
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        List<R> result = new ArrayList<>(Collections.nCopies(args.size(), null));
        final Counter counter = new Counter();
        IntStream.range(0, args.size()).forEach(i -> {
            synchronized (tasks) {
                tasks.add(() -> {
                    result.set(i, f.apply(args.get(i)));
                    synchronized (counter) {
                        counter.increment();
                        if (counter.getCnt() == result.size()) {
                            counter.notify();
                        }
                    }
                });
                tasks.notify();
            }
        });
        synchronized (counter) {
            while (counter.getCnt() != result.size()) {
                counter.wait();
            }
        }
        return result;
    }

    /** Stops all threads. All unfinished mappings are left in undefined state. */
    @Override
    public void close() {
        synchronized (threadsList) {
            threadsList.forEach(Thread::interrupt);
            threadsList.forEach(thread -> {
                try {
                    thread.join();
                } catch (InterruptedException ignored) {

                }
            });
        }
    }
}
