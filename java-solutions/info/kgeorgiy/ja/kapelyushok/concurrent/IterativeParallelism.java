package info.kgeorgiy.ja.kapelyushok.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Class implementing {@link ListIP}.
 */
public class IterativeParallelism implements ListIP {
    private final ParallelMapper parallelMapper;

    /**
     * Constructor with given {@link ParallelMapper}.
     * @param parallelMapper given {@link ParallelMapper}.
     */
    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    /**
     * Constructor without arguments.
     */
    public IterativeParallelism() {
        parallelMapper = null;
    }


    private <T> List<Stream<? extends T>> splitOnBlocks(int threads, List<? extends T> values) {
        List<Stream<? extends T>> blocks = new ArrayList<>();
        int blockSize = values.size() / threads;
        int mod = values.size() % threads;
        int left = 0;
        int right;
        for (int i = 0; i < threads; i++) {
            int realBlockSize = blockSize + (mod > 0 ? 1 : 0);
            mod--;
            right = left + realBlockSize;
            blocks.add(values.subList(left, right).stream());
            left = right;
        }
        return blocks;
    }

    // :NOTE: уже есть map, нужно добавить reduce
    private <T, R> R getResult(int threads, List<? extends T> values, Function<Stream<? extends T>, R> mapper, Function<Stream<R>, R> reducer) throws InterruptedException {

        threads = Math.min(threads, values.size());
        List<Stream<? extends T>> blocks = splitOnBlocks(threads, values);

        if (parallelMapper == null) {
            List<Thread> threadList = new ArrayList<>();
            List<R> result = new ArrayList<>(Collections.nCopies(threads, null));
            IntStream.range(0, threads).forEach( i -> {
                Thread thread = new Thread(() -> result.set(i, mapper.apply(blocks.get(i))));
                thread.start();
                threadList.add(thread);
            });
            InterruptedException interruptedException = null;
            for (Thread thread : threadList) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    // :NOTE: ошибки не надо игнорировать
                    // надо завершить все треды
                    if (interruptedException == null) {
                        interruptedException = e;
                    } else {
                        interruptedException.addSuppressed(e);
                    }
                }
            }
            if (interruptedException != null) {
                throw interruptedException;
            }
            return reducer.apply(result.stream());
        }
        return reducer.apply(parallelMapper.map(mapper, blocks).stream());
    }

    /**
     * Join values to string.
     *
     * @param threads number of concurrent threads.
     * @param values  values to join.
     * @return list of joined results of {@link #toString()} call on each value.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return getResult(threads, values,
                s -> s.map(Object::toString).collect(Collectors.joining()),
                s -> s.map(Object::toString).collect(Collectors.joining()));
    }

    /**
     * Filters values by predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to filter.
     * @param predicate filter predicate.
     * @return list of values satisfying given predicate. Order of values is preserved.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return getResult(threads, values,
                s -> s.filter(predicate).collect(Collectors.toList()),
                s -> s.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    /**
     * Maps values.
     *
     * @param threads number of concurrent threads.
     * @param values  values to map.
     * @param f       mapper function.
     * @return list of values mapped by given function.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return getResult(threads, values,
                s -> s.map(f).collect(Collectors.toList()),
                s -> s.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    /**
     * Returns maximum value.
     *
     * @param threads    number of concurrent threads.
     * @param values     values to get maximum of.
     * @param comparator value comparator.
     * @return maximum of given values
     * @throws InterruptedException   if executing thread was interrupted.
     * @throws NoSuchElementException if no values are given.
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return getResult(threads, values,
                s -> s.max(comparator).orElseThrow(),
                s -> s.max(comparator).orElseThrow());
    }

    /**
     * Returns minimum value.
     *
     * @param threads    number of concurrent threads.
     * @param values     values to get minimum of.
     * @param comparator value comparator.
     * @return minimum of given values
     * @throws InterruptedException   if executing thread was interrupted.
     * @throws NoSuchElementException if no values are given.
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    /**
     * Returns whether all values satisfy predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return whether all values satisfy predicate or {@code true}, if no values are given.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return getResult(threads, values,
                s -> s.allMatch(predicate),
                s -> s.allMatch(Boolean::booleanValue));
    }

    /**
     * Returns whether any of values satisfies predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return whether any value satisfies predicate or {@code false}, if no values are given.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    /**
     * Returns number of values satisfying predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @return number of values satisfying predicate.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> int count(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return getResult(threads, values,
                s -> (int) s.filter(predicate).count(),
                s -> s.mapToInt(Integer::intValue).sum());
    }
}
