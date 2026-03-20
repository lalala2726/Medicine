package cn.zhangchuangla.medicine.admin.common.storage.service.impl;

import cn.zhangchuangla.medicine.admin.common.storage.config.FileUploadProperties;
import cn.zhangchuangla.medicine.admin.common.storage.config.MinioConfig;
import cn.zhangchuangla.medicine.admin.common.storage.service.MinioStorageService;
import cn.zhangchuangla.medicine.model.vo.FileUploadVo;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 文件上传服务对象路径测试。
 */
class FileUploadServiceImplTests {

    /**
     * 默认文件桶名称。
     */
    private static final String BUCKET_NAME = "medicine";

    /**
     * 默认 CDN 地址。
     */
    private static final String CDN_FILE_URL = "https://medicine-cdn.zhangchuangla.cn/resources/2026/03/test-file.pdf";

    @Test
    void uploadBuildsObjectNameWithNormalizedUploadPath() {
        MinioStorageService minioStorageService = mock(MinioStorageService.class);
        FileUploadProperties fileUploadProperties = new FileUploadProperties();
        MinioConfig minioConfig = buildMinioConfig("/resources/");
        FileUploadServiceImpl fileUploadService = new FileUploadServiceImpl(minioStorageService, fileUploadProperties, minioConfig);
        MockMultipartFile multipartFile = buildPdfFile();
        when(minioStorageService.uploadFile(eq(BUCKET_NAME), any(String.class), eq(multipartFile))).thenReturn(CDN_FILE_URL);

        FileUploadVo uploadVo = fileUploadService.upload(multipartFile);

        ArgumentCaptor<String> objectNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(minioStorageService).uploadFile(eq(BUCKET_NAME), objectNameCaptor.capture(), eq(multipartFile));
        String actualObjectName = objectNameCaptor.getValue();
        assertObjectNameMatches(actualObjectName);
        assertEquals(CDN_FILE_URL, uploadVo.getFileUrl());
    }

    @Test
    void uploadDefaultsUploadPathToResourcesWhenBlank() {
        MinioStorageService minioStorageService = mock(MinioStorageService.class);
        FileUploadProperties fileUploadProperties = new FileUploadProperties();
        MinioConfig minioConfig = buildMinioConfig("   ");
        FileUploadServiceImpl fileUploadService = new FileUploadServiceImpl(minioStorageService, fileUploadProperties, minioConfig);
        MockMultipartFile multipartFile = buildPdfFile();
        when(minioStorageService.uploadFile(eq(BUCKET_NAME), any(String.class), eq(multipartFile))).thenReturn(CDN_FILE_URL);

        fileUploadService.upload(multipartFile);

        ArgumentCaptor<String> objectNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(minioStorageService).uploadFile(eq(BUCKET_NAME), objectNameCaptor.capture(), eq(multipartFile));
        String actualObjectName = objectNameCaptor.getValue();
        assertObjectNameMatches(actualObjectName);
        assertTrue(actualObjectName.startsWith("resources/"));
    }

    /**
     * 功能描述：构建最小 MinIO 配置对象，供上传路径规则测试复用。
     * <p>
     * 参数说明：
     *
     * @param uploadPath String 待测试的上传路径前缀。
     *                   返回值：{@link MinioConfig}，已填充桶名与上传路径的配置对象。
     *                   异常说明：无。
     */
    private MinioConfig buildMinioConfig(String uploadPath) {
        MinioConfig minioConfig = new MinioConfig();
        minioConfig.setBucketName(BUCKET_NAME);
        minioConfig.setUploadPath(uploadPath);
        return minioConfig;
    }

    /**
     * 功能描述：构建测试用 PDF 文件，确保通过默认白名单校验。
     * <p>
     * 参数说明：无。
     * 返回值：{@link MockMultipartFile}，测试用上传文件对象。
     * 异常说明：无。
     */
    private MockMultipartFile buildPdfFile() {
        return new MockMultipartFile("file", "manual.pdf", "application/pdf", "test".getBytes());
    }

    /**
     * 功能描述：断言上传对象路径符合 resources/yyyy/MM/uuid.ext 的结构约束。
     * <p>
     * 参数说明：
     *
     * @param actualObjectName String 实际生成的对象路径。
     *                         返回值：无。
     *                         异常说明：断言失败时由测试框架抛出异常。
     */
    private void assertObjectNameMatches(String actualObjectName) {
        String expectedPrefix = "resources/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM")) + "/";
        assertTrue(actualObjectName.startsWith(expectedPrefix));
        assertTrue(actualObjectName.endsWith(".pdf"));
    }
}
