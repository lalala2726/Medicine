package cn.zhangchuangla.medicine.client;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "cn.zhangchuangla.medicine")
@MapperScan("cn.zhangchuangla.medicine.client.mapper")
@EnableAsync
@EnableDubbo
public class MedicineClientApplication {

    public static void main(String[] args) {
        String hint = """
                   ____ _ _            _     ____                               __       _   ____  _             _              \s
                  / ___| (_) ___ _ __ | |_  / ___| _   _  ___ ___ ___  ___ ___ / _|_   _| | / ___|| |_ __ _ _ __| |_ _   _ _ __ \s
                 | |   | | |/ _ \\ '_ \\| __| \\___ \\| | | |/ __/ __/ _ \\/ __/ __| |_| | | | | \\___ \\| __/ _` | '__| __| | | | '_ \\\s
                 | |___| | |  __/ | | | |_   ___) | |_| | (_| (_|  __/\\__ \\__ \\  _| |_| | |  ___) | || (_| | |  | |_| |_| | |_) |
                  \\____|_|_|\\___|_| |_|\\__| |____/ \\__,_|\\___\\___\\___||___/___/_|  \\__,_|_| |____/ \\__\\__,_|_|   \\__|\\__,_| .__/\s
                                                                                                                          |_|   \s
                """;
        SpringApplication.run(MedicineClientApplication.class, args);
        System.out.println(hint);
    }
}
