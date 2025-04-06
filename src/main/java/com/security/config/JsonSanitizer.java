package com.security.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.text.StringEscapeUtils;
import org.owasp.encoder.Encode;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class JsonSanitizer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Dangerous elements and patterns
    private static final Pattern SCRIPT_LIKE_TAGS = Pattern.compile("(?i)<(script|iframe|svg|math|object|embed)[^>]*>.*?</\\1>", Pattern.DOTALL);
    private static final Pattern SELF_CLOSING_TAGS = Pattern.compile("(?i)<(script|iframe|svg|math|object|embed)[^>]*?/?>", Pattern.DOTALL);
    private static final Pattern CSS_JS_PATTERN = Pattern.compile("(?i)url\\s*\\((.*?)\\)");
    private static final Pattern EVENT_ATTR_PATTERN = Pattern.compile("(?i)^on\\w+\\s*=.*");
    private static final Pattern BASE64_INJECTION_PATTERN = Pattern.compile("(?i)data:(text|application)/(html|javascript);base64,");
    private static final Pattern ENCODED_JS_PATTERN = Pattern.compile("(?i)(javascript|vbscript)(&#x3A;|%3A|:)");

    public static String sanitizeJson(String rawJson) throws Exception {
        JsonNode root = objectMapper.readTree(rawJson);
        sanitizeNode(root);
        return objectMapper.writeValueAsString(root);
    }private static void sanitizeNode(JsonNode node) {
        if (node == null || node.isNull()) return;

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();

                if (value.isTextual()) {
                    objectNode.put(key, sanitizeText(value.asText(), key)); // ✅ String
                } else if (value.isArray()) {
                    sanitizeNode(value); // ✅ Array
                } else if (value.isObject()) {
                    sanitizeNode(value); // ✅ Object
                }
            }

        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                JsonNode element = node.get(i);

                if (element.isTextual()) {
                    ((com.fasterxml.jackson.databind.node.ArrayNode) node)
                            .set(i, objectMapper.getNodeFactory().textNode(sanitizeText(element.asText(), "arrayElement"))); // ✅ List item (string)
                } else if (element.isObject() || element.isArray()) {
                    sanitizeNode(element); // ✅ Nested Object/Array
                }
            }

        } else if (node.isTextual()) {
            // This is only for raw string root JSON, like: "hello"
            ((ObjectNode) node).put("value", sanitizeText(node.asText(), "rootString")); // ✅ Root string
        }
    }

    private static String sanitizeText(String value, String keyName) {
        if (value == null || value.trim().isEmpty()) return "";

        String clean = value;

        // Decode HTML entities first
        clean = StringEscapeUtils.unescapeHtml4(clean);

        // Strip script-like content using regex
        clean = SCRIPT_LIKE_TAGS.matcher(clean).replaceAll("");
        clean = SELF_CLOSING_TAGS.matcher(clean).replaceAll("");

        String lowerClean = clean.toLowerCase().trim();

        // Block known dangerous URI schemes
        if (lowerClean.startsWith("javascript:") ||
                lowerClean.startsWith("vbscript:") ||
                lowerClean.startsWith("data:text/html") ||
                lowerClean.startsWith("data:application") ||
                lowerClean.startsWith("data:image/svg+xml")) {
            return "";
        }

        // Remove base64 and encoded JS patterns
        if (BASE64_INJECTION_PATTERN.matcher(lowerClean).find() ||
                ENCODED_JS_PATTERN.matcher(lowerClean).find()) {
            return "";
        }

        // Remove inline event handler strings
        if (EVENT_ATTR_PATTERN.matcher(clean).matches()) {
            return "";
        }

        // Clean style attribute values
        if (keyName.toLowerCase().contains("style")) {
            clean = CSS_JS_PATTERN.matcher(clean).replaceAll("url(#)");
        }

        // Sanitize using Jsoup (Safelist.none() blocks everything except text)
        clean = Jsoup.clean(clean, Safelist.none());

        // Final encoding for safe rendering
        return Encode.forHtmlContent(clean);
    }
}
