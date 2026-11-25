package com.wesker.order.controller.admin;

import com.wesker.order.constant.MessageConstant;
import com.wesker.order.result.Result;
import com.wesker.order.utils.AliOssUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
public class CommonController {

    private static final Logger log = LoggerFactory.getLogger(CommonController.class);
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private final AliOssUtils aliOssUtils;

    public CommonController(AliOssUtils aliOssUtils) {
        this.aliOssUtils = aliOssUtils;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || file.getSize() > MAX_IMAGE_SIZE) {
            return Result.error(MessageConstant.INVALID_IMAGE_SIZE);
        }
        try {
            byte[] bytes = file.getBytes();
            String extension = imageExtension(bytes);
            if (extension == null) {
                return Result.error(MessageConstant.INVALID_IMAGE_FORMAT);
            }
            String objectName = "dishes/" + UUID.randomUUID() + extension;
            return Result.success(aliOssUtils.upload(bytes, objectName));
        } catch (IOException exception) {
            return Result.error(MessageConstant.IMAGE_READ_FAILED);
        } catch (IllegalStateException exception) {
            log.warn("OSS 图片上传失败，异常类型 {}", exception.getCause() == null
                    ? exception.getClass().getSimpleName() : exception.getCause().getClass().getSimpleName());
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }

    private String imageExtension(byte[] bytes) throws IOException {
        try (var image = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(image);
            if (!readers.hasNext()) {
                return null;
            }
            var reader = readers.next();
            try {
                reader.setInput(image, true, true);
                String extension = switch (reader.getFormatName().toLowerCase(Locale.ROOT)) {
                    case "png" -> ".png";
                    case "jpeg" -> ".jpg";
                    default -> null;
                };
                return extension != null && reader.getWidth(0) > 0 && reader.getHeight(0) > 0 ? extension : null;
            } finally {
                reader.dispose();
            }
        }
    }
}
