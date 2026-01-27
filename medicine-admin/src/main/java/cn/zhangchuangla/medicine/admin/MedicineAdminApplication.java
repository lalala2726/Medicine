package cn.zhangchuangla.medicine.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"cn.zhangchuangla.medicine", "cn.zhangchuangla.agent"})
@MapperScan("cn.zhangchuangla.medicine.admin.mapper")
@EnableTransactionManagement
@EnableAsync
public class MedicineAdminApplication {


    public static void main(String[] args) {
        SpringApplication.run(MedicineAdminApplication.class, args);
        String hint = """
                  ____  _             _     ____                               __       _\s
                 / ___|| |_ __ _ _ __| |_  / ___| _   _  ___ ___ ___  ___ ___ / _|_   _| |
                 \\___ \\| __/ _` | '__| __| \\___ \\| | | |/ __/ __/ _ \\/ __/ __| |_| | | | |
                  ___) | || (_| | |  | |_   ___) | |_| | (_| (_|  __/\\__ \\__ \\  _| |_| | |
                 |____/ \\__\\__,_|_|   \\__| |____/ \\__,_|\\___\\___\\___||___/___/_|  \\__,_|_|
                
                """;
        System.out.println(hint);
    }

}
