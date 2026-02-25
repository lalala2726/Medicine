package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.MallAfterSaleTimelineMapper;
import cn.zhangchuangla.medicine.client.service.MallAfterSaleTimelineService;
import cn.zhangchuangla.medicine.model.entity.MallAfterSaleTimeline;
import cn.zhangchuangla.medicine.model.enums.OperatorTypeEnum;
import cn.zhangchuangla.medicine.model.enums.OrderEventTypeEnum;
import cn.zhangchuangla.medicine.model.vo.AfterSaleTimelineVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 售后时间线Service实现
 *
 * @author Chuang
 * created 2025/11/08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MallAfterSaleTimelineServiceImpl extends ServiceImpl<MallAfterSaleTimelineMapper, MallAfterSaleTimeline>
        implements MallAfterSaleTimelineService {

    @Override
    public void addTimeline(Long afterSaleId, String eventType, String eventStatus,
                            String operatorType, Long operatorId, String description) {
        MallAfterSaleTimeline timeline = MallAfterSaleTimeline.builder()
                .afterSaleId(afterSaleId)
                .eventType(eventType)
                .eventStatus(eventStatus)
                .operatorType(operatorType)
                .operatorId(operatorId)
                .description(description)
                .createTime(new Date())
                .build();
        save(timeline);
    }

    @Override
    public List<AfterSaleTimelineVo> getTimelineList(Long afterSaleId) {
        List<MallAfterSaleTimeline> timelineList = lambdaQuery()
                .eq(MallAfterSaleTimeline::getAfterSaleId, afterSaleId)
                .orderByDesc(MallAfterSaleTimeline::getCreateTime)
                .list();

        return timelineList.stream().map(timeline -> {
            OrderEventTypeEnum eventTypeEnum = OrderEventTypeEnum.fromCode(timeline.getEventType());
            OperatorTypeEnum operatorTypeEnum = OperatorTypeEnum.fromCode(timeline.getOperatorType());

            return AfterSaleTimelineVo.builder()
                    .id(timeline.getId())
                    .eventType(timeline.getEventType())
                    .eventTypeName(eventTypeEnum != null ? eventTypeEnum.getName() : "未知")
                    .eventStatus(timeline.getEventStatus())
                    .operatorType(timeline.getOperatorType())
                    .operatorTypeName(operatorTypeEnum != null ? operatorTypeEnum.getName() : "未知")
                    .description(timeline.getDescription())
                    .createTime(timeline.getCreateTime())
                    .build();
        }).collect(Collectors.toList());
    }
}

