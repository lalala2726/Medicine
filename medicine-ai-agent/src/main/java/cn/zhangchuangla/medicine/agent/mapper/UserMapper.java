package cn.zhangchuangla.medicine.agent.mapper;

import cn.zhangchuangla.medicine.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 查询用户角色编码。
     */
    List<String> listRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 查询用户权限编码。
     */
    List<String> listPermissionCodesByUserId(@Param("userId") Long userId);

    /**
     * 查询全部启用权限编码。
     */
    List<String> listAllEnabledPermissionCodes();
}
