package cn.zhangchuangla.medicine.mapper;

import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.model.request.user.UserListQueryRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Chuang
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    /**
     * 用户列表
     *
     * @param userPage 分页对象
     * @param request  用户列表查询请求参数
     * @return 用户列表
     */
    Page<User> listUser(Page<User> userPage, @Param("request") UserListQueryRequest request);
}




