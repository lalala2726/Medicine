package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.MallOrderMapper;
import cn.zhangchuangla.medicine.client.model.request.OrderCreateRequest;
import cn.zhangchuangla.medicine.client.model.vo.OrderCreateVo;
import cn.zhangchuangla.medicine.client.service.MallOrderService;
import cn.zhangchuangla.medicine.client.service.MallProductService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseResultCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.security.utils.SecurityUtils;
import cn.zhangchuangla.medicine.model.entity.MallOrder;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

/**
 * @author Chuang
 */
@Service
public class MallOrderServiceImpl extends ServiceImpl<MallOrderMapper, MallOrder> implements MallOrderService {

    private static final int ORDER_STATUS_WAIT_PAY = 0;
    private static final int PAY_TYPE_ALIPAY = 1;
    private static final int FLAG_FALSE = 0;
    private static final int ORDER_TIMEOUT_MINUTES = 30;

    private final MallProductService mallProductService;

    public MallOrderServiceImpl(MallProductService mallProductService) {
        this.mallProductService = mallProductService;
    }

    /**
     * 创建商城订单的核心流程：校验库存 → 扣减库存 → 构建订单 → 返回支付信息。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVo createOrder(OrderCreateRequest request) {
        // 1. 查询商品详情并校验上架状态
        MallProduct product = mallProductService.getMallProductById(request.getProductId());
        BigDecimal totalAmount = validateProductAndCalculateAmount(request, product);

        // 4. 扣减库存，内部包含乐观锁控制
        mallProductService.deductStock(request.getProductId(), request.getQuantity());

        // 5. 生成业务订单号并补充订单基础信息
        String orderNo = generateOrderNo();
        Date now = new Date();

        MallOrder order = MallOrder.builder()
                .orderNo(orderNo)
                .userId(resolveCurrentUserId())
                .totalAmount(totalAmount)
                .payAmount(BigDecimal.ZERO)
                .freightAmount(BigDecimal.ZERO)
                .payType(PAY_TYPE_ALIPAY)
                .orderStatus(ORDER_STATUS_WAIT_PAY)
                .deliveryType(product.getDeliveryType())
                .receiverDetail(request.getAddress())
                .note(request.getRemark())
                .refundFlag(FLAG_FALSE)
                .afterSaleFlag(FLAG_FALSE)
                .createTime(now)
                .updateTime(now)
                .build();

        if (!save(order)) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "创建订单失败，请稍后再试");
        }

        Date expireTime = Date.from(LocalDateTime.now()
                .plusMinutes(ORDER_TIMEOUT_MINUTES)
                .atZone(ZoneId.systemDefault())
                .toInstant());

        // 6. 返回下单结果，供前端确认订单信息
        return OrderCreateVo.builder()
                .orderNo(orderNo)
                .outTradeNo(orderNo)
                .totalAmount(totalAmount)
                .payType("ALIPAY")
                .status("WAIT_PAY")
                .createTime(now)
                .expireTime(expireTime)
                .productSummary(buildProductSummary(product, request.getQuantity()))
                .redirectUrl("/pay/confirm?orderNo=" + orderNo)
                .build();
    }

    /**
     * 校验商品状态与库存，并计算订单总金额。
     */
    private BigDecimal validateProductAndCalculateAmount(OrderCreateRequest request, MallProduct product) {
        if (!Objects.equals(product.getStatus(), 1)) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "商品未上架或已下架");
        }
        // 2. 校验库存是否满足下单数量
        Integer stock = product.getStock();
        if (stock == null || stock < request.getQuantity()) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR,
                    String.format("商品库存不足，当前库存：%d", stock == null ? 0 : stock));
        }

        // 3. 计算订单应付金额（示例中不包含运费、优惠）
        BigDecimal price = product.getPrice();
        if (price == null) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "商品价格未配置");
        }
        return price.multiply(BigDecimal.valueOf(request.getQuantity()));
    }

    /**
     * 查询订单的支付关键信息，确保在支付前再做一次状态校验。
     */
    @Override
    public OrderCreateVo getOrderPayInfo(String orderNo) {
        // 使用订单号查询待支付订单，避免重复支付
        MallOrder order = lambdaQuery()
                .eq(MallOrder::getOrderNo, orderNo)
                .one();
        if (order == null) {
            throw new ServiceException(ResponseResultCode.RESULT_IS_NULL, "订单不存在");
        }
        if (!Objects.equals(order.getOrderStatus(), ORDER_STATUS_WAIT_PAY)) {
            throw new ServiceException(ResponseResultCode.OPERATION_ERROR, "订单状态异常，无法发起支付");
        }

        // 以 VO 格式返回支付关键信息，供前端拼装支付请求或确认页面
        return OrderCreateVo.builder()
                .orderNo(order.getOrderNo())
                .outTradeNo(order.getOrderNo())
                .totalAmount(order.getTotalAmount())
                .payType("ALIPAY")
                .status("WAIT_PAY")
                .createTime(order.getCreateTime())
                .productSummary("商城订单-" + order.getOrderNo())
                .redirectUrl("/pay/confirm?orderNo=" + order.getOrderNo())
                .build();
    }

    /**
     * 组装商品摘要，方便前端展示订单信息。
     */
    private String buildProductSummary(MallProduct product, int quantity) {
        String unit = product.getUnit();
        if (unit != null && !unit.isBlank()) {
            return product.getName() + " " + quantity + unit;
        }
        return product.getName() + " x" + quantity;
    }

    /**
     * 获取当前登录用户 ID；若未登录则返回 null（允许匿名下单场景）。
     */
    private Long resolveCurrentUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 生成业务唯一的订单编号。
     */
    private String generateOrderNo() {
        String prefix = "O";
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = String.format("%06d", (int) (Math.random() * 1000000));
        return prefix + datePart + randomPart;
    }
}

