package cn.zhangchuangla.medicine.admin.service;

import cn.zhangchuangla.medicine.admin.model.request.FreezeOrUnUserWalletRequest;
import cn.zhangchuangla.medicine.admin.model.request.WalletChangeRequest;
import cn.zhangchuangla.medicine.admin.model.vo.UserConsumeInfo;
import cn.zhangchuangla.medicine.admin.model.vo.UserDetailVo;
import cn.zhangchuangla.medicine.admin.model.vo.UserWalletFlowInfoVo;
import cn.zhangchuangla.medicine.admin.model.vo.UserWalletVo;
import cn.zhangchuangla.medicine.common.core.base.PageRequest;
import cn.zhangchuangla.medicine.common.core.base.PageResult;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.request.UserAddRequest;
import cn.zhangchuangla.medicine.model.request.UserListQueryRequest;
import cn.zhangchuangla.medicine.model.request.UserUpdateRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Set;

/**
 * @author Chuang
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户ID获取用户
     *
     * @param userId 用户ID
     * @return 用户
     */
    UserDetailVo getUserDetailById(Long userId);

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户
     */
    User getUserByUsername(String username);

    /**
     * 根据用户ID获取用户角色集合
     *
     * @param userId 用户ID
     * @return 角色集合
     */
    Set<String> getUserRolesByUserId(Long userId);

    /**
     * 根据用户名获取用户角色集合
     *
     * @param username 用户名
     * @return 角色集合
     */
    Set<String> getUserRolesByUserName(String username);

    /**
     * 获取用户列表
     *
     * @return 用户列表
     */
    Page<User> listUser(UserListQueryRequest request);

    /**
     * 添加用户
     *
     * @param request 用户添加请求对象
     * @return 是否添加成功
     */
    boolean addUser(UserAddRequest request);

    /**
     * 修改用户
     *
     * @param request 用户修改请求对象
     * @return 是否修改成功
     */
    boolean updateUser(UserUpdateRequest request);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteUser(List<Long> userId);

    /**
     * 获取用户钱包流水
     *
     * @return 用户钱包流水
     */
    PageResult<UserWalletFlowInfoVo> getUserWalletFlow(Long userId, PageRequest request);


    /**
     * 获取用户消费信息
     *
     * @return 用户消费信息
     */
    PageResult<UserConsumeInfo> getConsumeInfo(Long userId, PageRequest request);

    /**
     * 开通用户钱包
     *
     * @param userId 用户ID
     * @return 是否开通成功
     */
    boolean openUserWallet(Long userId);

    /**
     * 关闭用户钱包
     *
     * @param request 请求参数
     * @return 是否关闭成功
     */
    boolean freezeUserWallet(FreezeOrUnUserWalletRequest request);


    /**
     * 解冻用户钱包
     *
     * @param request 请求参数
     * @return 是否解冻成功
     */
    boolean unfreezeUserWallet(FreezeOrUnUserWalletRequest request);

    /**
     * 充值用户钱包
     *
     * @param request 请求参数
     * @return 是否充值成功
     */
    boolean walletAmountChange(WalletChangeRequest request);

    /**
     * 根据用户ID获取用户
     *
     * @param userId 用户ID
     * @return 用户
     */
    User getUserById(Long userId);

    /**
     * 获取用户钱包
     *
     * @param userId 用户ID
     * @return 用户钱包
     */
    UserWalletVo getUserWallet(Long userId);
}
