package cn.zhangchuangla.medicine.controller;

import cn.zhangchuangla.medicine.common.base.AjaxResult;
import cn.zhangchuangla.medicine.common.base.BaseController;
import cn.zhangchuangla.medicine.common.base.TableDataResult;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.request.user.UserAddRequest;
import cn.zhangchuangla.medicine.model.request.user.UserListQueryRequest;
import cn.zhangchuangla.medicine.model.request.user.UserUpdateRequest;
import cn.zhangchuangla.medicine.model.vo.user.UserListVo;
import cn.zhangchuangla.medicine.model.vo.user.UserVo;
import cn.zhangchuangla.medicine.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Chuang
 * <p>
 * created on 2025/9/3 09:09
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户接口", description = "提供用户的增删改查")
public class UserController extends BaseController {

    private final UserService userService;

    /**
     * 获取用户列表
     *
     * @param request 用户列表查询参数
     * @return 用户列表视图对象集合
     */
    @GetMapping("/list")
    @Operation(summary = "用户列表")
    public AjaxResult<TableDataResult> listUser(UserListQueryRequest request) {
        Page<User> userPage = userService.listUser(request);
        List<UserListVo> userListVos = copyListProperties(userPage, UserListVo.class);
        return getTableData(userPage, userListVos);
    }


    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "用户详情")
    public AjaxResult<UserVo> getUserById(@PathVariable("id") Long id) {
        User user = userService.getUserById(id);
        UserVo userVO = copyProperties(user, UserVo.class);
        return success(userVO);
    }

    /**
     * 添加用户
     *
     * @param request 添加用户参数
     * @return 添加结果
     */
    @PostMapping
    @Operation(summary = "添加用户")
    public AjaxResult<Void> addUser(@RequestBody UserAddRequest request) {
        boolean result = userService.addUser(request);
        return toAjax(result);
    }

    /**
     * 修改用户
     *
     * @param request 修改用户参数
     * @return 修改结果
     */
    @PutMapping
    @Operation(summary = "修改用户")
    public AjaxResult<Void> updateUser(@RequestBody UserUpdateRequest request) {
        boolean result = userService.updateUser(request);
        return toAjax(result);
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除用户")
    public AjaxResult<Void> deleteUser(@PathVariable("ids") List<Long> userId) {
        boolean result = userService.deleteUser(userId);
        return toAjax(result);
    }

}
