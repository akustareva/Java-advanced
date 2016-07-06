package ru.ifmo.ctddev.kustareva.webcrawler;

import info.kgeorgiy.java.advanced.crawler.*;
import javafx.util.Pair;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {

    private final Downloader downloader;
    private final int perHost;
    private final ExecutorService downloadPool, extractPool;
    final int max = Integer.MAX_VALUE / 2;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.perHost = perHost;
        this.downloadPool = Executors.newFixedThreadPool(downloaders);
        this.extractPool = Executors.newFixedThreadPool(extractors);
    }

    @Override
    public Result download(String s, int i) {
        List<String> result = Collections.synchronizedList(new ArrayList<>());
        ConcurrentHashMap<String, Boolean> was = new ConcurrentHashMap<>();
        Map<String, IOException> errors = new HashMap<>();
        Semaphore checker = new Semaphore(max);
        ConcurrentHashMap<String, Semaphore> semaphoresPerHost = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, ConcurrentLinkedDeque<Pair<String, Integer>>> queue = new ConcurrentHashMap<>();
        try {
            String host = URLUtils.getHost(s);
            ConcurrentLinkedDeque<Pair<String, Integer>> init = new ConcurrentLinkedDeque<>();
            init.addLast(new Pair<>(s, i));
            queue.put(host, init);
            semaphoresPerHost.put(host, new Semaphore(perHost));
            checker.acquire();
            downloadPool.submit(
                    () -> this.downloadProcess(host, result, errors, queue, semaphoresPerHost, checker, was));
        }  catch (IOException e) {
            errors.put(s, e);
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        try {
            checker.acquire(max);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Result(result, errors);
    }

    private void downloadProcess(String host,
                                 List<String> result,
                                 Map<String, IOException> errors,
                                 ConcurrentHashMap<String, ConcurrentLinkedDeque<Pair<String, Integer>>> queue,
                                 ConcurrentHashMap<String, Semaphore> semaphoresPerHost,
                                 Semaphore checker,
                                 ConcurrentHashMap<String, Boolean> was) {
        try{
            if (semaphoresPerHost.get(host).tryAcquire()) {
                if (!queue.get(host).isEmpty()) {
                    Pair<String, Integer> pair = queue.get(host).removeFirst();
                    String url = pair.getKey();
                    int depth = pair.getValue();
                    if (!was.containsKey(url) && url != null) {
                        was.put(url, true);
                        try {
                            Document doc = downloader.download(url);
                            checker.acquire();
                            extractPool.submit(
                                    () -> extractProcess(doc, depth, result, errors, queue, semaphoresPerHost, checker, was));
                        } catch (IOException e) {
                            errors.put(url, e);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            semaphoresPerHost.get(host).release();
                            if (!errors.containsKey(url)) { //url != null &&
                                result.add(url);
                            }
                            if (!queue.get(host).isEmpty()) {
                                try {
                                    checker.acquire();
                                    downloadPool.submit(
                                            () -> downloadProcess(host, result, errors, queue, semaphoresPerHost, checker, was));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } else {
                    semaphoresPerHost.get(host).release();
                }
            }
        } finally {
            checker.release();
        }
    }

    private void extractProcess(Document doc,
                                int depth,
                                List<String> result,
                                Map<String, IOException> errors,
                                ConcurrentHashMap<String, ConcurrentLinkedDeque<Pair<String, Integer>>> queue,
                                ConcurrentHashMap<String, Semaphore> semaphoresPerHost,
                                Semaphore checker,
                                ConcurrentHashMap<String, Boolean> was) {
        try {
            if (depth > 1) {
                List<String> docLinks = doc.extractLinks();
                docLinks.stream().distinct().forEach(url -> {
                    try {
                        String host = URLUtils.getHost(url);
                        queue.putIfAbsent(host, new ConcurrentLinkedDeque<>());
                        queue.get(host).addLast(new Pair<>(url, depth - 1));
                        semaphoresPerHost.putIfAbsent(host, new Semaphore(perHost));
                        checker.acquire();
                        downloadPool.submit(
                                () -> this.downloadProcess(host, result, errors, queue, semaphoresPerHost, checker, was));
                    } catch (MalformedURLException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            checker.release();
        }
    }

    @Override
    public void close() {
        downloadPool.shutdown();
        extractPool.shutdown();
        if (!downloadPool.isShutdown()) {
            try {
                downloadPool.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                downloadPool.shutdownNow();
            }
        }
        if (!extractPool.isShutdown()) {
            try {
                extractPool.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                extractPool.shutdownNow();
            }
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length < 1 || args.length > 4) {
            System.out.println("You should use: WebCrawler url [downloads [extractors [perHost]]]");
        }
        int downloaders = 10, extractors = 10, perHost = 10;
        if (args.length > 1) {
            downloaders = Integer.parseInt(args[1]);
        }
        if (args.length > 2) {
            extractors = Integer.parseInt(args[2]);
        }
        if (args.length == 4) {
            perHost = Integer.parseInt(args[3]);
        }
        try (WebCrawler webCrawler = new WebCrawler(new CachingDownloader(), downloaders, extractors, perHost)) {
            Result res = webCrawler.download(args[0], 2);
            System.out.println(res.getDownloaded());
            System.out.println(res.getErrors());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
