package info.kgeorgiy.ja.kapelyushok.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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



    private <T> List<List<? extends T>> splitOnBlocks(int threads, List<? extends T> values) {
        List<List<? extends T>> blocks = new ArrayList<>();
        int blockSize = values.size() / threads;
        int mod = values.size() % threads;
        int left = 0;
        int right;
        for (int i = 0; i < threads; i++) {
            int realBlockSize = blockSize + (mod > 0 ? 1 : 0);
            mod--;
            right = left + realBlockSize;
            blocks.add(values.subList(left, right));
            left = right;
        }
        return blocks;
    }

    private <T, R> List<R> getResult(int threads, List<? extends T> values, Function<List<? extends T>, R> function) throws InterruptedException {

        threads = Math.min(threads, values.size());
        List<List<? extends T>> lists = splitOnBlocks(threads, values);

        if (parallelMapper == null) {
            Thread[] threadArray = new Thread[threads];
            List<R> result = new ArrayList<>(Collections.nCopies(threads, null));
            IntStream.range(0, threads).forEach( i -> {
                threadArray[i] = new Thread(() -> result.set(i, function.apply(lists.get(i))));
                threadArray[i].start();
            });
            for (int i = 0; i < threads; i++) {
                threadArray[i].join();
            }
            return result;
        }
        return parallelMapper.map(function, lists);
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
        return getResult(threads, values, l -> l.stream().map(Object::toString).collect(Collectors.joining())).stream().map(Object::toString).collect(Collectors.joining());
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
        return getResult(threads, values, l -> l.stream().filter(predicate).collect(Collectors.toList())).stream().flatMap(Collection::stream).collect(Collectors.toList());
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
        return getResult(threads, values, l -> l.stream().map(f).collect(Collectors.toList())).stream().flatMap(Collection::stream).collect(Collectors.toList());
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
        return getResult(threads, values, l -> l.stream().max(comparator).orElseThrow()).stream().max(comparator).orElseThrow();
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
        return getResult(threads, values, l -> l.stream().allMatch(predicate)).stream().allMatch(Boolean::booleanValue);
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
        return (int) getResult(threads, values, l -> l.stream().filter(predicate).count()).stream().mapToLong(Long::longValue).sum();
    }
}
