package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.llm.model.dto.DrugInfoDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/23
 */
public interface AdminAssistantService {


    /**
     * 解析图片中的药品信息
     *
     * @param imageUrls 图片地址
     * @return 药品信息
     */
    DrugInfoDto parseDrugInfoByImage(List<String> imageUrls);

    /**
     * 聊天
     *
     * @param message 消息
     * @return sse
     */
    SseEmitter chat(String message);
}
