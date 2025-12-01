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


    DrugInfoDto parseDrugInfoByImage(List<String> imageUrls);

    SseEmitter chat(String message);
}
