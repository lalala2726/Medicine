package cn.zhangchuangla.medicine.agent.service.impl;

import cn.zhangchuangla.medicine.agent.constanst.URLConstant;
import cn.zhangchuangla.medicine.agent.service.KnowledgeBaseService;
import cn.zhangchuangla.medicine.agent.util.RequestClient;
import cn.zhangchuangla.medicine.common.core.exception.ParamException;
import cn.zhangchuangla.medicine.common.core.utils.Assert;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author Chuang
 * <p>
 * created on 2026/1/31
 */
@Service
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    @Override
    public void createKnowledgeBase(String knowledgeBaseName, String knowledgeBaseDescription, Integer embedding_dim) {
        Assert.notEmpty(knowledgeBaseName, "知识库名称不能为空");
        Assert.notEmpty(knowledgeBaseDescription, "知识库描述不能为空");

        // 检查知识库名称是否只包含英文或数字
        if (!knowledgeBaseName.matches("^[a-zA-Z0-9]+$")) {
            throw new ParamException("知识库名称只能包含英文字符或数字");
        }

        if (!(embedding_dim > 128 && embedding_dim < 2048 && embedding_dim % 2 == 0)) {
            throw new ParamException("嵌入维度必须在128到2048之间且为偶数");
        }

        String url = URLConstant.KNOWLEDGE_BASE_BASE_URL;

        HashMap<String, Object> body = new HashMap<>();
        body.put("knowledge_name", knowledgeBaseName);
        body.put("description", knowledgeBaseDescription);
        body.put("embedding_dim", embedding_dim);
        // 请求接口
        RequestClient.post(url, body);
    }

    @Override
    public void deleteKnowledgeBase(String knowledgeBaseName) {
        Assert.notEmpty(knowledgeBaseName, "知识库名称不能为空");
        // 检查知识库名称是否只包含英文或数字
        if (!knowledgeBaseName.matches("^[a-zA-Z0-9]+$")) {
            throw new ParamException("知识库名称只能包含英文字符或数字");
        }

        String url = URLConstant.KNOWLEDGE_BASE_BASE_URL;

        HashMap<String, Object> body = new HashMap<>();
        body.put("knowledge_name", knowledgeBaseName);

        RequestClient.delete(url, body);

    }
}
