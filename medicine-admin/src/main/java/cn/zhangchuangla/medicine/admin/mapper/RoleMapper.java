package cn.zhangchuangla.medicine.admin.mapper;

import cn.zhangchuangla.medicine.admin.model.request.RoleListRequest;
import cn.zhangchuangla.medicine.model.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

public interface RoleMapper extends BaseMapper<Role> {

    Page<Role> roleList(Page<Role> page, @Param("query") RoleListRequest query);
}
