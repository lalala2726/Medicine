package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.common.security.base.BaseController;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识库管理控制器
 * <p>
 * 提供知识库的增删改查功能，包括列表查询、详情查询、添加、更新和删除操作
 *
 * @author Chuang
 * <p>
 * created on 2025/12/4
 */
@RestController
@RequestMapping("/knowledge_base")
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin') or hasRole('super_admin')")
@Tag(name = "知识库管理", description = "知识库 CRUD 接口")
public class KnowledgeBaseController extends BaseController {

}
