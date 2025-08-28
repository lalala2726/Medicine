package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.mapper.UserMapper;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author zhangchuang
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

}




