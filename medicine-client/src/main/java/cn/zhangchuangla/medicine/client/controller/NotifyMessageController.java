package cn.zhangchuangla.medicine.client.controller;

import cn.zhangchuangla.medicine.client.model.request.NotifyMessageListRequest;
import cn.zhangchuangla.medicine.client.model.vo.NotifyMessageDetailVo;
import cn.zhangchuangla.medicine.client.model.vo.NotifyMessageListVo;
import cn.zhangchuangla.medicine.client.service.NotifyMessageService;
import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.core.base.TableDataResult;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 客户端通知消息接口。
 */
@RestController
@RequestMapping("/notify/message")
@RequiredArgsConstructor
@Tag(name = "通知消息", description = "客户端通知消息查询接口")
public class NotifyMessageController extends BaseController {

    private final NotifyMessageService notifyMessageService;

    /**
     * 查询当前用户通知列表。
     *
     * @param request 查询条件
     * @return 通知列表分页结果
     */
    @GetMapping("/list")
    @Operation(summary = "通知消息列表")
    public AjaxResult<TableDataResult> listNotifyMessages(NotifyMessageListRequest request) {
        Page<NotifyMessageListVo> page = notifyMessageService.listUserMessages(request);
        return getTableData(page);
    }

    /**
     * 获取通知详情，并自动标记已读。
     *
     * @param id 通知ID
     * @return 通知详情
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "通知消息详情")
    public AjaxResult<NotifyMessageDetailVo> getNotifyMessage(@PathVariable("id") Long id) {
        NotifyMessageDetailVo detailVo = notifyMessageService.getMessageDetail(id);
        return success(detailVo);
    }

    /**
     * 删除当前用户可见的通知消息。
     *
     * @param id 通知ID
     * @return 删除结果
     */
    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "删除通知消息")
    public AjaxResult<Void> deleteNotifyMessage(@PathVariable Long id) {
        boolean result = notifyMessageService.deleteMessage(id);
        return toAjax(result);
    }
}
