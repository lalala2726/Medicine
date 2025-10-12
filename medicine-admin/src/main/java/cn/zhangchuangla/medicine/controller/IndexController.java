package cn.zhangchuangla.medicine.controller;

import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.annotation.Anonymous;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Chuang
 * <p>
 * created on 2025/8/28 15:52
 */
@RequestMapping("/")
@RestController
@Anonymous
@Tag(name = "首页", description = "首页")
public class IndexController {


    @GetMapping
    public AjaxResult<Void> index() {
        return AjaxResult.success();
    }

}
