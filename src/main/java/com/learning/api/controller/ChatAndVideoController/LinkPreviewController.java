package com.learning.api.controller.ChatAndVideoController;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.api.dto.ChatRoom.LinkPreviewDto;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/api/linkPreview")
public class LinkPreviewController {

    private static final int TIMEOUT_MS = 4_000;
    private static final int MAX_BYTES  = 65_536; // 64 KB

    private static final Pattern OG_TITLE = Pattern.compile(
            "<meta[^>]+property=[\"']og:title[\"'][^>]+content=[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern OG_DESC = Pattern.compile(
            "<meta[^>]+property=[\"']og:description[\"'][^>]+content=[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern OG_IMAGE = Pattern.compile(
            "<meta[^>]+property=[\"']og:image[\"'][^>]+content=[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern TITLE_TAG = Pattern.compile(
            "<title[^>]*>([^<]{1,200})</title>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    @GetMapping
    public ResponseEntity<LinkPreviewDto> preview(@RequestParam String url) {
        if (url == null || !url.matches("^https?://.*")) {
            return ResponseEntity.badRequest().build();
        }
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            if (host == null || isPrivateHost(host)) {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        try {
            String html = fetchHead(url);
            return ResponseEntity.ok(parse(html, url));
        } catch (Exception e) {
            log.debug("linkPreview fetch failed for {}: {}", url, e.getMessage());
            return ResponseEntity.ok(new LinkPreviewDto(null, null, null, url));
        }
    }

    private String fetchHead(String urlStr) throws Exception {
        URL u = URI.create(urlStr).toURL();
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; ChatPreviewBot/1.0)");
        conn.setRequestProperty("Accept", "text/html");
        conn.setInstanceFollowRedirects(true);

        int status = conn.getResponseCode();
        if (status < 200 || status >= 400) {
            throw new Exception("HTTP " + status);
        }

        byte[] buf = new byte[MAX_BYTES];
        int read = 0;
        try (var is = conn.getInputStream()) {
            int n;
            while (read < MAX_BYTES && (n = is.read(buf, read, MAX_BYTES - read)) != -1) {
                read += n;
            }
        }
        conn.disconnect();
        return new String(buf, 0, read, StandardCharsets.UTF_8);
    }

    private LinkPreviewDto parse(String html, String sourceUrl) {
        String title       = extractGroup(OG_TITLE, html);
        String description = extractGroup(OG_DESC,  html);
        String imageUrl    = extractGroup(OG_IMAGE,  html);

        if (title == null) title = extractGroup(TITLE_TAG, html);
        if (title != null) title = title.trim();

        if (imageUrl != null && !imageUrl.startsWith("http")) {
            try {
                imageUrl = URI.create(sourceUrl).resolve(imageUrl).toString();
            } catch (Exception ignored) {}
        }

        return new LinkPreviewDto(title, description, imageUrl, sourceUrl);
    }

    private String extractGroup(Pattern p, String input) {
        Matcher m = p.matcher(input);
        return m.find() ? m.group(1) : null;
    }

    private boolean isPrivateHost(String host) {
        String h = host.toLowerCase();
        return h.equals("localhost")
            || h.startsWith("127.")
            || h.startsWith("10.")
            || h.startsWith("192.168.")
            || h.startsWith("172.16.")
            || h.endsWith(".local");
    }
}
