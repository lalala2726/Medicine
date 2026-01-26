package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.admin.model.request.NotifyMessageListRequest;
import cn.zhangchuangla.medicine.model.entity.NotifyMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

public interface NotifyMessageMapper extends BaseMapper<NotifyMessage> {

    /**
     * 管理端通知消息列表查询。
     */
    Page<NotifyMessage> selectNotifyMessagePage(@Param("page") Page<NotifyMessage> page,
                                                @Param("request") NotifyMessageListRequest request);
}
