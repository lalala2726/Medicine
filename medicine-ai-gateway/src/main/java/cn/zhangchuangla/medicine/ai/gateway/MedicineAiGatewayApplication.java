package cn.zhangchuangla.medicine.ai.gateway;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI Gateway 启动类
 *
 * @author Chuang
 * <p>
 * created on 2026/2/12
 */
@SpringBootApplication(scanBasePackages = {"cn.zhangchuangla.medicine"})
@MapperScan("cn.zhangchuangla.medicine.ai.gateway.mapper")
public class MedicineAiGatewayApplication {

    public static void main(String[] args) {
        String hint = """
                    _    ___    ____       _                                 _             _           _ _\s
                   / \\  |_ _|  / ___| __ _| |_ _____      ____ _ _   _   ___| |_ __ _ _ __| |_ ___  __| | |
                  / _ \\  | |  | |  _ / _` | __/ _ \\ \\ /\\ / / _` | | | | / __| __/ _` | '__| __/ _ \\/ _` | |
                 / ___ \\ | |  | |_| | (_| | ||  __/\\ V  V / (_| | |_| | \\__ \\ || (_| | |  | ||  __/ (_| |_|
                /_/   \\_\\___|  \\____|\\__,_|\\__\\___| \\_/\\_/ \\__,_|\\__, | |___/\\__\\__,_|_|   \\__\\___|\\__,_(_)
                                                                 |___/                                    \s
                """;
        SpringApplication.run(MedicineAiGatewayApplication.class, args);
        System.out.println(hint);
    }

}
