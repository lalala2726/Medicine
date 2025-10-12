package cn.zhangchuangla.medicine.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "cn.zhangchuangla.medicine")
@MapperScan("cn.zhangchuangla.medicine.mapper")
@EnableTransactionManagement
public class MedicineAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicineAdminApplication.class, args);
        String hint = """
                     _       _           _         ____  _             _                 ____                               __       _\s
                    / \\   __| |_ __ ___ (_)_ __   / ___|| |_ __ _ _ __| |_ _   _ _ __   / ___| _   _  ___ ___ ___  ___ ___ / _|_   _| |
                   / _ \\ / _` | '_ ` _ \\| | '_ \\  \\___ \\| __/ _` | '__| __| | | | '_ \\  \\___ \\| | | |/ __/ __/ _ \\/ __/ __| |_| | | | |
                  / ___ \\ (_| | | | | | | | | | |  ___) | || (_| | |  | |_| |_| | |_) |  ___) | |_| | (_| (_|  __/\\__ \\__ \\  _| |_| | |
                 /_/   \\_\\__,_|_| |_| |_|_|_| |_| |____/ \\__\\__,_|_|   \\__|\\__,_| .__/  |____/ \\__,_|\\___\\___\\___||___/___/_|  \\__,_|_|
                                                                                |_|                                                   \s
                """;
        System.out.println(hint);
    }

}
