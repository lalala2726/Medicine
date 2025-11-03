package cn.zhangchuangla.medicine.admin.controller;

import cn.zhangchuangla.medicine.common.core.base.AjaxResult;
import cn.zhangchuangla.medicine.common.security.annotation.Anonymous;
import cn.zhangchuangla.medicine.common.security.base.BaseController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Chuang
 * <p>
 * created on 2025/11/4 01:19
 */
@RestController
@RequestMapping("/")
public class IndexController extends BaseController {


    @GetMapping
    @Anonymous
    public AjaxResult<Void> index() {
        return success("欢迎访问医药商城系统！");
    }
}
