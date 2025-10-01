package io.level114.client;

import io.level114.domain.Server;
import io.level114.domain.Report;
import io.level114.domain.ReportNonce;
import io.level114.util.JsonUtils;
import io.level114.domain.ReportCreateResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.concurrent.ThreadLocalRandom;

public class CollectorCenterAPI {
    private final HttpClient httpClient;
    private final String url;
    private final String serverApiKey;
    private final Logger logger;
    private final String userAgent;

    public CollectorCenterAPI(String url, String serverApiKey, Logger logger, String userAgent) {
        this.url = trimTrailingSlash(url);
        this.serverApiKey = serverApiKey == null ? "" : serverApiKey.trim();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.logger = logger;
        this.userAgent = (userAgent == null || userAgent.isBlank()) ? "Level114-MinerMonitor" : userAgent;
    }

    public HttpResponse<String> get(String path, Map<String, String> query) throws IOException, InterruptedException {
        String target = buildUrl(path, query);
        int attempts = 0;
        IOException lastIo = null;
        InterruptedException lastInterrupted = null;
        while (++attempts <= 3) {
            try {
                HttpRequest.Builder b = baseRequest(target).GET();
                HttpResponse<String> resp = httpClient.send(b.build(),
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (shouldRetry(resp.statusCode()) && attempts < 3) {
                    sleepBackoff(attempts);
                    continue;
                }
                return resp;
            } catch (IOException e) {
                lastIo = e;
                if (attempts >= 3)
                    throw e;
                sleepBackoff(attempts);
            } catch (InterruptedException e) {
                lastInterrupted = e;
                if (attempts >= 3)
                    throw e;
                sleepBackoff(attempts);
            }
        }
        if (lastIo != null)
            throw lastIo;
        if (lastInterrupted != null)
            throw lastInterrupted;
        throw new IOException("GET retry loop terminated unexpectedly");
    }

    public HttpResponse<String> post(String path, Object body) throws IOException, InterruptedException {
        String target = buildUrl(path, null);
        String json = JsonUtils.toJson(body);
        int attempts = 0;
        IOException lastIo = null;
        InterruptedException lastInterrupted = null;
        while (++attempts <= 3) {
            try {
                HttpRequest.Builder b = baseRequest(target)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json));
                HttpResponse<String> resp = httpClient.send(b.build(),
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (shouldRetry(resp.statusCode()) && attempts < 3) {
                    sleepBackoff(attempts);
                    continue;
                }
                return resp;
            } catch (IOException e) {
                lastIo = e;
                if (attempts >= 3)
                    throw e;
                sleepBackoff(attempts);
            } catch (InterruptedException e) {
                lastInterrupted = e;
                if (attempts >= 3)
                    throw e;
                sleepBackoff(attempts);
            }
        }
        if (lastIo != null)
            throw lastIo;
        if (lastInterrupted != null)
            throw lastInterrupted;
        throw new IOException("POST retry loop terminated unexpectedly");
    }

    public Optional<Server> getServer(String serverId) {
        try {
            HttpResponse<String> response = get("/servers/" + serverId, null);
            if (response.statusCode() != 200) {
                this.logger.severe("Failed to get server: body=" + response.body());
                return Optional.empty();
            }
            return Optional.of(JsonUtils.fromJson(response.body(), Server.class));
        } catch (Exception e) {
            this.logger.severe("Failed to get server: " + e.getMessage());
            return Optional.empty();
        }
    }

    public ReportNonce getReportNonce() {
        try {
            HttpResponse<String> response = get("/reports/nonce", null);
            if (response.statusCode() != 200) {
                this.logger.severe("Failed to get report nonce: body=" + response.body());
                return null;
            }
            return JsonUtils.fromJson(response.body(), ReportNonce.class);
        } catch (Exception e) {
            this.logger.severe("Failed to get report nonce: " + e.getMessage());
            return null;
        }
    }

    public String sendReport(Report report) {
        try {
            HttpResponse<String> response = post("/reports/create", report);
            if (response.statusCode() != 200) {
                this.logger
                        .severe("Failed to send report: body=" + response.body());
                return null;
            }
            ReportCreateResponse dto = JsonUtils.fromJson(response.body(), ReportCreateResponse.class);
            return dto.getSignature();
        } catch (Exception e) {
            this.logger.severe("Failed to send report: " + e.getMessage());
            return null;
        }
    }

    private HttpRequest.Builder baseRequest(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + serverApiKey)
                .header("User-Agent", userAgent);
        return builder;
    }

    private String buildUrl(String path, Map<String, String> query) {
        StringBuilder sb = new StringBuilder();
        sb.append(url);
        if (path != null && !path.isBlank()) {
            if (!path.startsWith("/"))
                sb.append('/');
            sb.append(path);
        }
        if (query != null && !query.isEmpty()) {
            boolean first = true;
            for (Map.Entry<String, String> e : query.entrySet()) {
                sb.append(first ? '?' : '&');
                first = false;
                sb.append(urlEncode(e.getKey())).append('=').append(urlEncode(e.getValue()));
            }
        }
        return sb.toString();
    }

    private static String trimTrailingSlash(String s) {
        if (s == null)
            return "";
        String out = s.trim();
        while (out.endsWith("/"))
            out = out.substring(0, out.length() - 1);
        return out;
    }

    private static String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private static boolean shouldRetry(int status) {
        return status == 429 || status >= 500;
    }

    private static void sleepBackoff(int attempt) {
        try {
            long base = 500L;
            long delay = base * (1L << Math.max(0, attempt - 1));
            long jitter = ThreadLocalRandom.current().nextLong(0, 250);
            Thread.sleep(Math.min(delay + jitter, 5000L));
        } catch (InterruptedException ignored) {
        }
    }
}
