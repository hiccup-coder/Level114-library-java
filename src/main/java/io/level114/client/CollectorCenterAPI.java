/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  java.net.http.HttpClient
 *  java.net.http.HttpRequest
 *  java.net.http.HttpRequest$BodyPublishers
 *  java.net.http.HttpRequest$Builder
 *  java.net.http.HttpResponse
 *  java.net.http.HttpResponse$BodyHandlers
 */
package io.level114.client;

import io.level114.domain.Report;
import io.level114.domain.ReportCreateResponse;
import io.level114.domain.ReportNonce;
import io.level114.domain.Server;
import io.level114.util.JsonUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

public class CollectorCenterAPI {
    private final HttpClient httpClient;
    private final String url;
    private final String serverApiKey;
    private final Logger logger;
    private final String userAgent;

    public CollectorCenterAPI(String url, String serverApiKey, Logger logger, String userAgent) {
        this.url = CollectorCenterAPI.trimTrailingSlash(url);
        this.serverApiKey = serverApiKey == null ? "" : serverApiKey.trim();
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();
        this.logger = logger;
        this.userAgent = userAgent == null || userAgent.isBlank() ? "Level114-MinerMonitor" : userAgent;
    }

    public HttpResponse<String> get(String path, Map<String, String> query) throws IOException, InterruptedException {
        String target = this.buildUrl(path, query);
        int attempts = 0;
        IOException lastIo = null;
        InterruptedException lastInterrupted = null;
        while (++attempts <= 3) {
            try {
                HttpRequest.Builder b = this.baseRequest(target).GET();
                HttpResponse resp = this.httpClient.send(b.build(), HttpResponse.BodyHandlers.ofString((Charset)StandardCharsets.UTF_8));
                if (CollectorCenterAPI.shouldRetry(resp.statusCode()) && attempts < 3) {
                    CollectorCenterAPI.sleepBackoff(attempts);
                    continue;
                }
                return resp;
            } catch (IOException e) {
                lastIo = e;
                if (attempts >= 3) {
                    throw e;
                }
                CollectorCenterAPI.sleepBackoff(attempts);
            } catch (InterruptedException e) {
                lastInterrupted = e;
                if (attempts >= 3) {
                    throw e;
                }
                CollectorCenterAPI.sleepBackoff(attempts);
            }
        }
        if (lastIo != null) {
            throw lastIo;
        }
        if (lastInterrupted != null) {
            throw lastInterrupted;
        }
        throw new IOException("GET retry loop terminated unexpectedly");
    }

    public HttpResponse<String> post(String path, Object body) throws IOException, InterruptedException {
        String target = this.buildUrl(path, null);
        String json = JsonUtils.toJson(body);
        int attempts = 0;
        IOException lastIo = null;
        InterruptedException lastInterrupted = null;
        while (++attempts <= 3) {
            try {
                HttpRequest.Builder b = this.baseRequest(target).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString((String)json));
                HttpResponse resp = this.httpClient.send(b.build(), HttpResponse.BodyHandlers.ofString((Charset)StandardCharsets.UTF_8));
                if (CollectorCenterAPI.shouldRetry(resp.statusCode()) && attempts < 3) {
                    CollectorCenterAPI.sleepBackoff(attempts);
                    continue;
                }
                return resp;
            } catch (IOException e) {
                lastIo = e;
                if (attempts >= 3) {
                    throw e;
                }
                CollectorCenterAPI.sleepBackoff(attempts);
            } catch (InterruptedException e) {
                lastInterrupted = e;
                if (attempts >= 3) {
                    throw e;
                }
                CollectorCenterAPI.sleepBackoff(attempts);
            }
        }
        if (lastIo != null) {
            throw lastIo;
        }
        if (lastInterrupted != null) {
            throw lastInterrupted;
        }
        throw new IOException("POST retry loop terminated unexpectedly");
    }

    public Optional<Server> getServer(String serverId) {
        try {
            HttpResponse<String> response = this.get("/servers/" + serverId, null);
            if (response.statusCode() != 200) {
                this.logger.severe("Failed to get server: body=" + (String)response.body());
                return Optional.empty();
            }
            return Optional.of(JsonUtils.fromJson((String)response.body(), Server.class));
        } catch (Exception e) {
            this.logger.severe("Failed to get server: " + e.getMessage());
            return Optional.empty();
        }
    }

    public ReportNonce getReportNonce() {
        try {
            HttpResponse<String> response = this.get("/reports/nonce", null);
            if (response.statusCode() != 200) {
                this.logger.severe("Failed to get report nonce: body=" + (String)response.body());
                return null;
            }
            return JsonUtils.fromJson((String)response.body(), ReportNonce.class);
        } catch (Exception e) {
            this.logger.severe("Failed to get report nonce: " + e.getMessage());
            return null;
        }
    }

    public String sendReport(Report report) {
        try {
            HttpResponse<String> response = this.post("/reports/create", report);
            if (response.statusCode() != 200) {
                this.logger.severe("Failed to send report: body=" + (String)response.body());
                return null;
            }
            ReportCreateResponse dto = JsonUtils.fromJson((String)response.body(), ReportCreateResponse.class);
            return dto.getSignature();
        } catch (Exception e) {
            this.logger.severe("Failed to send report: " + e.getMessage());
            return null;
        }
    }

    private HttpRequest.Builder baseRequest(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder((URI)URI.create(url)).timeout(Duration.ofSeconds(15L)).header("Accept", "application/json").header("Authorization", "Bearer " + this.serverApiKey).header("User-Agent", this.userAgent);
        return builder;
    }

    private String buildUrl(String path, Map<String, String> query) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.url);
        if (path != null && !path.isBlank()) {
            if (!path.startsWith("/")) {
                sb.append('/');
            }
            sb.append(path);
        }
        if (query != null && !query.isEmpty()) {
            boolean first = true;
            for (Map.Entry<String, String> e : query.entrySet()) {
                sb.append(first ? (char)'?' : '&');
                first = false;
                sb.append(CollectorCenterAPI.urlEncode(e.getKey())).append('=').append(CollectorCenterAPI.urlEncode(e.getValue()));
            }
        }
        return sb.toString();
    }

    private static String trimTrailingSlash(String s) {
        if (s == null) {
            return "";
        }
        String out = s.trim();
        while (out.endsWith("/")) {
            out = out.substring(0, out.length() - 1);
        }
        return out;
    }

    private static String urlEncode(String s) {
        try {
            return URLEncoder.encode((String)(s == null ? "" : s), (Charset)StandardCharsets.UTF_8);
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
            long jitter = ThreadLocalRandom.current().nextLong(0L, 250L);
            Thread.sleep(Math.min(delay + jitter, 5000L));
        } catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }
}

