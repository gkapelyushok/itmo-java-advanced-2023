package info.kgeorgiy.ja.kapelyushok.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloaderService;
    private final ExecutorService extractorService;
    private final int perHost;
    private final Map<String, Semaphore> hostsSemaphores;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        downloaderService = Executors.newFixedThreadPool(downloaders);
        extractorService = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
        hostsSemaphores = new ConcurrentHashMap<>();
    }

    /**
     * Downloads website up to specified depth.
     *
     * @param url   start <a href="http://tools.ietf.org/html/rfc3986">URL</a>.
     * @param depth download depth.
     * @return download result.
     */
    @Override
    public Result download(String url, int depth) {
        Set<String> downloaded = ConcurrentHashMap.newKeySet();
        Map<String, IOException> errors = new ConcurrentHashMap<>();
        Set<String> queue = ConcurrentHashMap.newKeySet();
        queue.add(url);
        download(queue, depth, downloaded, errors, new Phaser(1));
        downloaded.removeAll(errors.keySet());
        return new Result(new ArrayList<>(downloaded), errors);
    }

    private void download(Set<String> urls, int depth, Set<String> downloaded, Map<String, IOException> errors, Phaser phaser) {
        if (depth < 1) {
            return;
        }
        Set<String> newUrls = ConcurrentHashMap.newKeySet();
        for (String url : urls) {
            if (!downloaded.add(url)) {
                continue;
            }
            try {
                String host = URLUtils.getHost(url);
                hostsSemaphores.putIfAbsent(host, new Semaphore(perHost));
                hostsSemaphores.get(host).acquireUninterruptibly();
                Runnable doDownload = () -> {
                    try {
                        Document document = downloader.download(url);
                        Runnable doExtract = () -> {
                            try {
                                newUrls.addAll(document.extractLinks());
                            } catch (IOException e) {
                                errors.put(url, e);
                            } finally {
                                phaser.arriveAndDeregister();
                            }
                        };
                        phaser.register();
                        extractorService.submit(doExtract);
                    } catch (IOException e) {
                        errors.put(url, e);
                    } finally {
                        phaser.arriveAndDeregister();
                        hostsSemaphores.get(host).release();
                    }
                };
                phaser.register();
                downloaderService.submit(doDownload);
            } catch (MalformedURLException e) {
                errors.put(url, e);
            }
        }
        phaser.arriveAndAwaitAdvance();
        download(newUrls, depth - 1, downloaded, errors, phaser);
    }

    /**
     * Closes this web-crawler, relinquishing any allocated resources.
     */
    @Override
    public void close() {
        downloaderService.close();
        extractorService.close();
    }

    public static void main(String[] args) throws IOException {
        if (args == null || args.length == 0 || args.length > 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Arguments should be WebCrawler url [depth [downloads [extractors [perHost]]]]");
        }
        int[] arguments = new int[args.length];
        for (int i = 1; i < args.length; i++) {
            try {
                arguments[i] = Integer.parseInt(args[i]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Arguments should be integer");
            }
        }
        String url = args[0];
        int depth = args.length > 1 ? arguments[1] : 1;
        int downloads = args.length > 2 ? arguments[2] : 1;
        int extractors = args.length > 3 ? arguments[3] : 1;
        int perHost = args.length > 4 ? arguments[4] : 1;
        new WebCrawler(new CachingDownloader(1000), downloads, extractors, perHost).download(url, depth);
    }
}
