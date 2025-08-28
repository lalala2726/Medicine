package cn.zhangchuangla.medicine.service.impl;

import cn.zhangchuangla.medicine.common.exception.ParamException;
import cn.zhangchuangla.medicine.common.exception.ServiceException;
import cn.zhangchuangla.medicine.common.utils.Assert;
import cn.zhangchuangla.medicine.constants.RolesConstant;
import cn.zhangchuangla.medicine.model.entity.User;
import cn.zhangchuangla.medicine.security.entity.AuthTokenVo;
import cn.zhangchuangla.medicine.security.token.TokenService;
import cn.zhangchuangla.medicine.service.AuthService;
import cn.zhangchuangla.medicine.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public Long register(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new ParamException("用户名或密码不能为空");
        }
        User exists = userService.lambdaQuery().eq(User::getUsername, username).one();
        if (exists != null) {
            throw new ServiceException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username.trim());
        user.setRoles(Set.of(RolesConstant.USER).toString());
        user.setPassword(passwordEncoder.encode(password.trim()));
        userService.save(user);
        return user.getId();
    }

    @Override
    public AuthTokenVo login(String username, String password) {
        Assert.hasText(username, "用户名不能为空");
        Assert.hasText(password, "密码不能为空");
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username.trim(),
                password.trim());
        Authentication authentication = authenticationManager.authenticate(token);
        // 生成会话令牌
        var session = tokenService.createToken(authentication);
        return AuthTokenVo.builder()
                .accessToken(session.getAccessToken())
                .refreshToken(session.getRefreshToken())
                .build();
    }

    @Override
    public AuthTokenVo refresh(String refreshToken) {
        Assert.hasText(refreshToken, "刷新令牌不能为空");
        return tokenService.refreshToken(refreshToken);
    }
}
