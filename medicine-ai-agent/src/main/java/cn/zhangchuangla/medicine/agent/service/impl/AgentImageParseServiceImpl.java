package cn.zhangchuangla.medicine.agent.service.impl;

import cn.zhangchuangla.medicine.agent.config.AgentProperties;
import cn.zhangchuangla.medicine.agent.service.AgentImageParseService;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.core.utils.JSONUtils;
import cn.zhangchuangla.medicine.common.http.RequestClient;
import cn.zhangchuangla.medicine.common.http.model.BaseResponse;
import cn.zhangchuangla.medicine.common.http.model.ClientRequest;
import cn.zhangchuangla.medicine.common.http.model.HttpResult;
import cn.zhangchuangla.medicine.model.dto.MedicineInfoDto;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2026/2/1
 */
@Service
public class AgentImageParseServiceImpl implements AgentImageParseService {

    public static final String IMAGE_PARSE = "/api/image/parse/drug";
    private final AgentProperties agentProperties;

    public AgentImageParseServiceImpl(AgentProperties agentProperties) {
        this.agentProperties = agentProperties;
    }

    @Override
    public MedicineInfoDto medicineInfoParse(List<String> imageUrls) {
        if (CollectionUtils.isEmpty(imageUrls)) {
            throw new IllegalArgumentException("图片地址不能为空");
        }
        if (imageUrls.size() > 10) {
            throw new IllegalArgumentException("最大支持10张图片");
        }
        HashMap<String, List<String>> body = new HashMap<>();
        body.put("image_urls", imageUrls);
        ClientRequest clientRequest = ClientRequest.builder()
                .url(agentProperties.getUrl() + IMAGE_PARSE)
                .body(JSONUtils.toJson(body))
                .build();

        HttpResult<String> result = RequestClient.post(clientRequest);

        if (!result.isSuccessful()) {
            throw new ServiceException(String.format("解析失败！稍后再试～ 错误信息：%s", result.getData()));
        }
        BaseResponse<MedicineInfoDto> medicineInfoDtoBaseResponse = BaseResponse.fromJson(result.getData(), MedicineInfoDto.class);
        if (medicineInfoDtoBaseResponse != null && medicineInfoDtoBaseResponse.getCode() == 200) {
            return medicineInfoDtoBaseResponse.getData();
        }
        throw new ServiceException("解析失败！稍后再试～");
    }
}
