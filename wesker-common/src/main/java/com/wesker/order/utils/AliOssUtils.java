package com.wesker.order.utils;

import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.OSSClientBuilder;
import com.aliyun.sdk.service.oss2.PresignOptions;
import com.aliyun.sdk.service.oss2.credentials.StaticCredentialsProvider;
import com.aliyun.sdk.service.oss2.models.GetObjectRequest;
import com.aliyun.sdk.service.oss2.models.PutObjectRequest;
import com.aliyun.sdk.service.oss2.transport.BinaryData;
import com.wesker.order.properties.AliOssProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URLConnection;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class AliOssUtils {

    private static final String THUMBNAIL_PROCESS = "image/resize,m_fill,w_128,h_128/format,webp/quality,Q_80";
    private static final Duration READ_URL_VALIDITY = Duration.ofMinutes(5);
    private static final Duration IMAGE_URL_REUSE_DURATION = Duration.ofMinutes(4);
    private static final int MAX_CACHED_IMAGE_URLS = 512;

    private final AliOssProperties properties;
    private final Map<String, CachedImageUrls> cachedImageUrls = new LinkedHashMap<>(16, 0.75f, true);

    public AliOssUtils(AliOssProperties properties) {
        this.properties = properties;
    }

    // 上传文件到 OSS，返回对象键，例如 dishes/coffee.jpg。
    public String upload(byte[] bytes, String objectName) {
        if (bytes == null || bytes.length == 0 || !StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("文件内容和 OSS 对象键不能为空");
        }

        OSSClientBuilder builder = clientBuilder();
        try (OSSClient client = builder.build()) {
            client.putObject(PutObjectRequest.newBuilder()
                    .bucket(properties.bucketName())
                    .key(objectName)
                    .contentType(URLConnection.guessContentTypeFromName(objectName))
                    .body(BinaryData.fromBytes(bytes))
                    .build());
        } catch (Exception exception) {
            throw new IllegalStateException("上传文件到 OSS 失败", exception);
        }
        return objectName;
    }

    // 从已有的对象键生成有效期为 5 分钟的读取链接。
    public String generateReadUrl(String objectName) {
        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("OSS 对象键不能为空");
        }
        try (OSSClient client = clientBuilder().build()) {
            return presignReadUrl(client, objectName, null);
        } catch (Exception exception) {
            throw new IllegalStateException("生成 OSS 临时访问链接失败", exception);
        }
    }

    // 在签名过期前复用地址，使浏览器能命中同一张缩略图的缓存。
    public synchronized Map<String, ImageUrls> generateImageUrls(Collection<String> objectNames) {
        if (objectNames.isEmpty()) {
            return Map.of();
        }
        Instant now = Instant.now();
        Map<String, ImageUrls> urls = new HashMap<>();
        List<String> unsignedNames = new ArrayList<>();
        for (String objectName : objectNames) {
            CachedImageUrls cached = cachedImageUrls.get(objectName);
            if (cached != null && now.isBefore(cached.reuseUntil())) {
                urls.put(objectName, cached.urls());
            } else {
                unsignedNames.add(objectName);
            }
        }
        if (unsignedNames.isEmpty()) {
            return urls;
        }

        Instant reuseUntil = now.plus(IMAGE_URL_REUSE_DURATION);
        try (OSSClient client = clientBuilder().build()) {
            for (String objectName : unsignedNames) {
                String original = presignReadUrl(client, objectName, null);
                String thumbnail = presignReadUrl(client, objectName, reuseUntil);
                ImageUrls image = new ImageUrls(original, thumbnail);
                urls.put(objectName, image);
                cachedImageUrls.put(objectName, new CachedImageUrls(image, reuseUntil));
                if (cachedImageUrls.size() > MAX_CACHED_IMAGE_URLS) {
                    cachedImageUrls.remove(cachedImageUrls.keySet().iterator().next());
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException("生成 OSS 临时访问链接失败", exception);
        }
        return urls;
    }

    private String presignReadUrl(OSSClient client, String objectName, Instant thumbnailCacheUntil) {
        var request = GetObjectRequest.newBuilder()
                .bucket(properties.bucketName())
                .key(objectName);
        if (thumbnailCacheUntil != null) {
            request.process(THUMBNAIL_PROCESS)
                    .responseCacheControl("private")
                    .responseExpires(DateTimeFormatter.RFC_1123_DATE_TIME.format(thumbnailCacheUntil.atOffset(ZoneOffset.UTC)));
        }
        // SDK 会在设置有效期时计算截止时间，每次签名都要重新生成配置。
        PresignOptions options = PresignOptions.newBuilder()
                .expiration(READ_URL_VALIDITY)
                .build();
        return client.presign(request.build(), options).url();
    }

    public record ImageUrls(String original, String thumbnail) {
    }

    private record CachedImageUrls(ImageUrls urls, Instant reuseUntil) {
    }

    private OSSClientBuilder clientBuilder() {
        if (!StringUtils.hasText(properties.bucketName()) || !StringUtils.hasText(properties.region())
                || !StringUtils.hasText(properties.accessKeyId()) || !StringUtils.hasText(properties.accessKeySecret())) {
            throw new IllegalStateException("请先填写 OSS 的 Bucket、地域和 AccessKey 配置");
        }
        return OSSClient.newBuilder()
                .region(properties.region())
                .credentialsProvider(new StaticCredentialsProvider(properties.accessKeyId(), properties.accessKeySecret()));
    }
}
