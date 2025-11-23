package cn.zhangchuangla.medicine.common.core.utils;

import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * 图片相关工具类。
 */
public final class ImageUtils {

    private static final int ONE_MB = 1024 * 1024;

    private ImageUtils() {
    }

    /**
     * 确保图片不超过 1MB，超出则有损压缩为 JPEG。
     *
     * @param data          原始图片二进制
     * @param originalMimeType 原始 mime 类型，用于在无需压缩时保持返回值一致
     * @return 压缩后的图片数据和对应 mimeType
     */
    public static EncodedImage ensureUnder1MB(byte[] data, String originalMimeType) {
        if (data == null || data.length == 0) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "图片内容不能为空");
        }

        if (data.length <= ONE_MB) {
            return new EncodedImage(data, originalMimeType);
        }

        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
            if (image == null) {
                throw new ServiceException(ResponseCode.PARAM_ERROR, "图片格式无法解析");
            }

            float quality = 0.9f;
            byte[] compressed = data;
            while (quality >= 0.1f) {
                compressed = compressJpeg(image, quality);
                if (compressed.length <= ONE_MB) {
                    break;
                }
                quality -= 0.1f;
            }
            return new EncodedImage(compressed, "image/jpeg");
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException(ResponseCode.OPERATION_ERROR, "图片压缩失败，请稍后重试");
        }
    }

    private static byte[] compressJpeg(BufferedImage image, float quality) throws Exception {
        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam writeParam = jpgWriter.getDefaultWriteParam();
        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        writeParam.setCompressionQuality(quality);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             MemoryCacheImageOutputStream output = new MemoryCacheImageOutputStream(baos)) {
            jpgWriter.setOutput(output);
            jpgWriter.write(null, new IIOImage(image, null, null), writeParam);
            output.flush();
            return baos.toByteArray();
        } finally {
            jpgWriter.dispose();
        }
    }

    public record EncodedImage(byte[] data, String mimeType) {
    }
}
