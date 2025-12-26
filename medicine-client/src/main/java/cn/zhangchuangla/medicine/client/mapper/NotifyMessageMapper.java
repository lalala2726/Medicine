package cn.zhangchuangla.medicine.client.mapper;

import cn.zhangchuangla.medicine.client.model.request.NotifyMessageListRequest;
import cn.zhangchuangla.medicine.model.entity.NotifyMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

public interface NotifyMessageMapper extends BaseMapper<NotifyMessage> {

    /**
     * 分页查询用户通知消息列表。
     */
    Page<NotifyMessage> selectUserMessagePage(@Param("page") Page<NotifyMessage> page,
                                              @Param("request") NotifyMessageListRequest request,
                                              @Param("userId") Long userId);
}
