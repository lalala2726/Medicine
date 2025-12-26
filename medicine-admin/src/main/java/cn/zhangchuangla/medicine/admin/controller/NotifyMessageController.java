package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.admin.model.request.NotifyMessageListRequest;
import cn.zhangchuangla.medicine.admin.model.request.NotifyMessageSendRequest;
import cn.zhangchuangla.medicine.admin.model.request.NotifyMessageUpdateRequest;
import cn.zhangchuangla.medicine.admin.model.vo.NotifyMessageDetailVo;
import cn.zhangchuangla.medicine.admin.model.vo.NotifyMessageListVo;
import cn.zhangchuangla.medicine.admin.service.NotifyMessageService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.security.annotation.IsAdmin;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import cn.zhangchuangla.medicine.model.entity.NotifyMessage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知消息管理接口。
 */
@RestController
@RequestMapping("/notify/message")
@RequiredArgsConstructor
@IsAdmin
@Tag(name = "通知消息管理", description = "管理端通知消息维护接口")
public class NotifyMessageController extends BaseController {

    private final NotifyMessageService notifyMessageService;

    /**
     * 管理端分页查询通知列表。
     *
     * @param request 查询条件
     * @return 通知列表分页结果
     */
    @GetMapping("/list")
    @Operation(summary = "通知消息列表")
    public AjaxResult<TableDataResult> listNotifyMessages(NotifyMessageListRequest request) {
        Page<NotifyMessage> page = notifyMessageService.listNotifyMessages(request);
        List<NotifyMessageListVo> rows = copyListProperties(page, NotifyMessageListVo.class);
        return getTableData(page, rows);
    }

    /**
     * 管理端查看通知详情。
     *
     * @param id 通知ID
     * @return 通知详情
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "通知消息详情")
    public AjaxResult<NotifyMessageDetailVo> getNotifyMessageDetail(@PathVariable Long id) {
        NotifyMessageDetailVo detailVo = notifyMessageService.getNotifyMessageDetail(id);
        return success(detailVo);
    }

    /**
     * 管理端发送通知。
     *
     * @param request 发送参数
     * @return 发送结果
     */
    @PostMapping
    @Operation(summary = "发送通知消息")
    public AjaxResult<Void> sendNotifyMessage(@Validated @RequestBody NotifyMessageSendRequest request) {
        boolean result = notifyMessageService.sendAdminMessage(request);
        return toAjax(result);
    }

    /**
     * 管理端编辑通知。
     *
     * @param request 编辑参数
     * @return 编辑结果
     */
    @PutMapping
    @Operation(summary = "编辑通知消息")
    public AjaxResult<Void> updateNotifyMessage(@Validated @RequestBody NotifyMessageUpdateRequest request) {
        boolean result = notifyMessageService.updateAdminMessage(request);
        return toAjax(result);
    }

    /**
     * 管理端删除通知
     *
     * @param ids 通知ID列表
     * @return 删除结果
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除通知消息(仅管理员发送)")
    public AjaxResult<Void> deleteNotifyMessages(@PathVariable List<Long> ids) {
        boolean result = notifyMessageService.deleteAdminMessages(ids);
        return toAjax(result);
    }
}
