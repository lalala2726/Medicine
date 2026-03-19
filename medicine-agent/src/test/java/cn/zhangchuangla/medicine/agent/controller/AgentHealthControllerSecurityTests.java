package cn.zhangchuangla.medicine.agent.controller;

import cn.zhangchuangla.medicine.agent.advice.AgentResponseDescriptionAdvice;
import cn.zhangchuangla.medicine.agent.model.vo.health.RpcHealthVo;
import cn.zhangchuangla.medicine.agent.service.RpcHealthService;
import cn.zhangchuangla.medicine.agent.support.AgentVoDescriptionResolver;
import cn.zhangchuangla.medicine.common.security.config.SecurityConfig;
import cn.zhangchuangla.medicine.common.security.config.SecurityProperties;
import cn.zhangchuangla.medicine.common.security.filter.TokenAuthenticationFilter;
import cn.zhangchuangla.medicine.common.security.handel.AccessDeniedHandlerImpl;
import cn.zhangchuangla.medicine.common.security.handel.AuthenticationEntryPointImpl;
import cn.zhangchuangla.medicine.common.security.token.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AgentHealthController.class)
@Import({
        SecurityConfig.class,
        SecurityProperties.class,
        TokenAuthenticationFilter.class,
        AuthenticationEntryPointImpl.class,
        AccessDeniedHandlerImpl.class,
        AgentResponseDescriptionAdvice.class,
        AgentVoDescriptionResolver.class
})
class AgentHealthControllerSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RpcHealthService rpcHealthService;

    @MockBean
    private TokenService tokenService;

    @Test
    void rpcHealth_ShouldAllowAnonymousAccess_AndReturnStructuredResponse() throws Exception {
        when(rpcHealthService.checkRpcHealth()).thenReturn(createHealth());

        mockMvc.perform(get("/agent/health/rpc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.overallStatus").value("DEGRADED"))
                .andExpect(jsonPath("$.data.admin.reachable").value(false))
                .andExpect(jsonPath("$.data.admin.status").value("DOWN"))
                .andExpect(jsonPath("$.data.admin.reason").value("UNAVAILABLE"))
                .andExpect(jsonPath("$.data.client.reachable").value(true))
                .andExpect(jsonPath("$.data.client.status").value("UP"))
                .andExpect(jsonPath("$.data.client.reason").value("OK"));
    }

    private RpcHealthVo createHealth() {
        RpcHealthVo health = new RpcHealthVo();
        health.setOverallStatus("DEGRADED");
        health.setAdmin(createDependency(false, "DOWN", 21L, "UNAVAILABLE"));
        health.setClient(createDependency(true, "UP", 7L, "OK"));
        return health;
    }

    private RpcHealthVo.DependencyHealthVo createDependency(boolean reachable, String status, long latencyMs, String reason) {
        RpcHealthVo.DependencyHealthVo dependency = new RpcHealthVo.DependencyHealthVo();
        dependency.setReachable(reachable);
        dependency.setStatus(status);
        dependency.setLatencyMs(latencyMs);
        dependency.setReason(reason);
        return dependency;
    }
}
