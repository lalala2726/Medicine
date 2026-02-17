package cn.zhangchuangla.medicine.agent.mapper;

import cn.zhangchuangla.medicine.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 Mapper 接口。
 * <p>
 * 提供用户数据的数据访问操作，包括用户角色和权限的查询。
 *
 * @author Chuang
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 查询用户角色编码列表。
     *
     * @param userId 用户 ID
     * @return 角色编码列表
     */
    List<String> listRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 查询用户权限编码列表。
     *
     * @param userId 用户 ID
     * @return 权限编码列表
     */
    List<String> listPermissionCodesByUserId(@Param("userId") Long userId);

    /**
     * 查询全部启用的权限编码列表。
     *
     * @return 权限编码列表
     */
    List<String> listAllEnabledPermissionCodes();
}
