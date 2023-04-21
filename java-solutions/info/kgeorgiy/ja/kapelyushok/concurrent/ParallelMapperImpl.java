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

    private static class ResultList<R> {
        private final List<R> result;
        private int cnt;

        public ResultList(int n) {
            result = new ArrayList<>(Collections.nCopies(n, null));
            cnt = 0;
        }

        public synchronized void set(int i, R value) {
            result.set(i, value);
            cnt++;
            if (cnt == result.size()) {
                notify();
            }
        }

        public synchronized List<R> getResult() throws InterruptedException {
            while (cnt != result.size()) {
                wait();
            }
            return result;
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
        Runnable runnable = () -> {
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
            }
        };
        threadsList = Stream.generate(() -> new Thread(runnable)).limit(threads).collect(Collectors.toList());
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
        ResultList<R> resultList = new ResultList<>(args.size());
        IntStream.range(0, args.size()).forEach(i -> {
            synchronized (tasks) {
                tasks.add(() -> {
                    resultList.set(i, f.apply(args.get(i)));
                });
                tasks.notify();
            }
        });
        return resultList.getResult();
    }

    /** Stops all threads. All unfinished mappings are left in undefined state. */
    @Override
    public void close() {
        threadsList.forEach(Thread::interrupt);
        // :NOTE: здесь была завязка на Thread::isInterrupted и был бесконечный цикл
        while (threadsList.stream().anyMatch(Thread::isAlive)) {
            threadsList.forEach(thread -> {
                try {
                    thread.join();
                } catch (InterruptedException ignored) {
                }
            });
        }
    }
}
