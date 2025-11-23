package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.llm.model.dto.DrugInfoDto;
import cn.zhangchuangla.medicine.llm.model.response.AssistantChatResponse;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/23
 */
public interface AssistantService {


    DrugInfoDto parseDrugInfoByImage(List<String> imageUrls);

    Flux<AssistantChatResponse> chat(String message);
}
