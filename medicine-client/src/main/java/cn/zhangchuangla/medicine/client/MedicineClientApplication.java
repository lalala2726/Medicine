package cn.zhangchuangla.medicine.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "cn.zhangchuangla.medicine")
public class MedicineClientApplication {

    public static void main(String[] args) {
        String hint = """
                  ____  _             _     ____                               __       _\s
                 / ___|| |_ __ _ _ __| |_  / ___| _   _  ___ ___ ___  ___ ___ / _|_   _| |
                 \\___ \\| __/ _` | '__| __| \\___ \\| | | |/ __/ __/ _ \\/ __/ __| |_| | | | |
                  ___) | || (_| | |  | |_   ___) | |_| | (_| (_|  __/\\__ \\__ \\  _| |_| | |
                 |____/ \\__\\__,_|_|   \\__| |____/ \\__,_|\\___\\___\\___||___/___/_|  \\__,_|_|
                
                """;
        SpringApplication.run(MedicineClientApplication.class, args);
        System.out.println(hint);
    }
}
