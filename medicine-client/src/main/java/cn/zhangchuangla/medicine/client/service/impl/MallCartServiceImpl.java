package cn.zhangchuangla.medicine.client.service.impl;

import cn.zhangchuangla.medicine.client.mapper.MallCartMapper;
import cn.zhangchuangla.medicine.client.service.MallCartService;
import cn.zhangchuangla.medicine.client.service.MallProductImageService;
import cn.zhangchuangla.medicine.client.service.MallProductService;
import cn.zhangchuangla.medicine.common.core.enums.ResponseCode;
import cn.zhangchuangla.medicine.common.core.exception.ServiceException;
import cn.zhangchuangla.medicine.common.security.base.BaseService;
import cn.zhangchuangla.medicine.model.entity.MallCart;
import cn.zhangchuangla.medicine.model.entity.MallProduct;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author Chuang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MallCartServiceImpl extends ServiceImpl<MallCartMapper, MallCart>
        implements MallCartService, BaseService {

    private final MallProductService mallProductService;
    private final MallProductImageService mallProductImageService;

    /**
     * 添加商品到购物车（带数量参数）
     *
     * @param productId 商品ID
     * @param quantity  添加数量
     * @return 添加结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)  // 事务保护：确保整个方法要么全部成功，要么全部失败回滚
    public boolean addProduct(Long productId, Integer quantity) {
        // 参数验证
        if (productId == null || productId <= 0) {
            throw new ServiceException(ResponseCode.PARAM_ERROR, "商品ID无效");
        }
        if (quantity == null || quantity <= 0) {
            log.warn("用户{}尝试添加无效数量{}的商品{}", getUserId(), quantity, productId);
            throw new ServiceException(ResponseCode.PARAM_ERROR, "添加数量必须大于0");
        }

        Long currentUserId = getUserId();
        log.info("用户{}开始添加商品{}到购物车，数量：{}", currentUserId, productId, quantity);

        try {
            // 验证商品信息
            MallProduct mallProduct = validateProduct(productId);

            // 检查并预留库存
            checkAndReserveInventory(productId, quantity, mallProduct.getName());

            // 执行购物车操作
            boolean result = addToCartOperation(currentUserId, productId, quantity, mallProduct);

            if (result) {
                log.info("用户{}成功添加商品{}到购物车，数量：{}", currentUserId, productId, quantity);
            } else {
                log.error("用户{}添加商品{}到购物车失败", currentUserId, productId);
            }

            return result;
        } catch (Exception e) {
            log.error("用户{}添加商品{}到购物车时发生异常：{}", currentUserId, productId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 验证商品信息
     */
    private MallProduct validateProduct(Long productId) {
        MallProduct mallProduct = mallProductService.getById(productId);
        if (mallProduct == null) {
            log.warn("商品{}不存在", productId);
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "商品不存在");
        }

        if (mallProduct.getStatus() != 1) { // 假设1表示上架状态
            log.warn("商品{}已下架", productId);
            throw new ServiceException(ResponseCode.RESULT_IS_NULL, "商品已下架");
        }

        return mallProduct;
    }

    /**
     * 检查并预留库存（解决数据一致性问题）
     * <p>
     * 并发问题场景：
     * 假设商品A库存为1，用户1和用户2同时购买
     * 如果没有原子操作：
     * 1. 用户1查询库存=1 → 用户2查询库存=1
     * 2. 用户1购买成功，库存变为0
     * 3. 用户2也能购买成功，库存变为-1（超卖！）
     * <p>
     * 解决方案：使用原子操作，确保检查库存和扣减库存是一个不可分割的操作
     */
    private void checkAndReserveInventory(Long productId, Integer quantity, String productName) {
        // 使用原子性操作检查和扣减库存
        // 这个SQL等价于：UPDATE mall_product SET stock = stock - ? WHERE id = ? AND stock >= ?
        // 只有当库存足够时才会更新，且更新和检查在数据库层面是原子的
        boolean stockUpdated = mallProductService.lambdaUpdate()
                .eq(MallProduct::getId, productId)
                .ge(MallProduct::getStock, quantity) // 条件：库存必须大于等于需求数量
                .setSql("stock = stock - " + quantity)  // 原子操作：库存减去指定数量
                .update();

        if (!stockUpdated) {
            // 查询当前库存信息用于日志
            MallProduct currentProduct = mallProductService.getById(productId);
            Integer currentStock = currentProduct != null ? currentProduct.getStock() : 0;

            log.warn("商品{}库存不足，当前库存：{}，需求：{}", productName, currentStock, quantity);
            throw new ServiceException(ResponseCode.RESULT_IS_NULL,
                    String.format("商品库存不足，当前库存：%d", currentStock));
        }

        log.info("成功预留商品{}库存，数量：{}", productName, quantity);
    }

    /**
     * 执行购物车操作（解决并发安全问题）
     * <p>
     * 悲观锁使用场景说明：
     * 假设用户A连续快速点击"添加到购物车"按钮两次
     * 如果没有悲观锁：
     * 1. 第一次请求：查询购物车为空 → 准备创建新记录
     * 2. 第二次请求：查询购物车为空（因为第一次还没提交）→ 也准备创建新记录
     * 3. 结果：创建了2条相同的购物车记录（重复数据）
     * <p>
     * 悲观锁解决方案：
     * - FOR UPDATE：锁定查询到的记录，直到事务提交
     * - 其他事务必须等待锁释放才能修改这些记录
     * - 确保同一用户对同一商品的购物车操作是串行的
     */
    private boolean addToCartOperation(Long userId, Long productId, Integer quantity, MallProduct mallProduct) {
        // 获取商品封面图片
        String productCoverImage = getProductCoverImage(productId);

        // 使用悲观锁查询现有购物车项
        LambdaQueryWrapper<MallCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MallCart::getUserId, userId)
                .eq(MallCart::getProductId, productId)
                .last("FOR UPDATE"); // 悲观锁：锁定查询到的记录，防止其他事务修改

        MallCart existingCart = getOne(queryWrapper);

        if (existingCart == null) {
            // 创建新的购物车项
            return createNewCartItem(userId, productId, quantity, mallProduct, productCoverImage);
        } else {
            // 更新现有购物车项数量
            return updateExistingCartItem(existingCart, quantity, mallProduct);
        }
    }

    /**
     * 创建新的购物车项
     */
    private boolean createNewCartItem(Long userId, Long productId, Integer quantity,
                                      MallProduct mallProduct, String productCoverImage) {
        MallCart newCart = MallCart.builder()
                .userId(userId)
                .productId(productId)
                .productName(mallProduct.getName())
                .productImage(productCoverImage)
                .cartNum(quantity)
                .createTime(new Date())
                .updateTime(new Date())
                .build();

        boolean result = save(newCart);
        if (result) {
            log.info("为用户{}创建新的购物车项，商品：{}，数量：{}", userId, mallProduct.getName(), quantity);
        }
        return result;
    }

    /**
     * 更新现有购物车项
     * <p>
     * 原子操作说明：
     * 传统更新方式的问题：
     * 1. 先查询当前数量：cart_num = 5
     * 2. 计算新数量：new_num = 5 + 2 = 7
     * 3. 更新数据库：UPDATE SET cart_num = 7
     * <p>
     * 并发问题：
     * - 两个请求同时查询到cart_num = 5
     * - 两个请求都计算出new_num = 7
     * - 最终数量是7，而不是正确的9（丢失了一次更新）
     * <p>
     * 原子操作解决方案：
     * - 直接在数据库层面进行加法操作：cart_num = cart_num + 2
     * - 数据库保证这个操作的原子性
     * - 不会丢失任何一次更新
     */
    private boolean updateExistingCartItem(MallCart existingCart, Integer additionalQuantity,
                                           MallProduct mallProduct) {
        // 原子性更新数量，避免并发更新导致的数据丢失
        boolean updateResult = lambdaUpdate()
                .eq(MallCart::getId, existingCart.getId())
                .setSql("cart_num = cart_num + " + additionalQuantity)  // 原子操作：直接在数据库层面加法
                .setEntity(MallCart.builder().updateTime(new Date()).build())
                .update();

        if (updateResult) {
            log.info("更新用户{}的购物车项，商品：{}，增加数量：{}",
                    existingCart.getUserId(), mallProduct.getName(), additionalQuantity);
        }
        return updateResult;
    }

    /**
     * 获取商品封面图片（完善异常处理）
     */
    private String getProductCoverImage(Long productId) {
        try {
            String productCoverImage = mallProductImageService.getProductCoverImage(productId);
            if (StringUtils.isBlank(productCoverImage)) {
                log.warn("商品{}没有封面图片，使用默认图片", productId);
                productCoverImage = "/images/default-product.jpg"; // 设置默认图片
            }
            return productCoverImage;
        } catch (Exception e) {
            log.warn("获取商品{}封面图片失败：{}，使用默认图片", productId, e.getMessage());
            return "/images/default-product.jpg";
        }
    }
}




