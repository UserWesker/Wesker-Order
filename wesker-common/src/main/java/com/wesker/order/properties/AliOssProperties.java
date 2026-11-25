package com.wesker.order.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wesker.oss")
public record AliOssProperties(
        String bucketName,
        String region,
        String accessKeyId,
        String accessKeySecret
) {
    @Override
    public String toString() {
        return "AliOssProperties[bucketName=" + bucketName + ", region=" + region + "]";
    }
}
