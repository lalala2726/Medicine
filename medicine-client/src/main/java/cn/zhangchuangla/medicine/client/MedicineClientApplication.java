package cn.zhangchuangla.medicine.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "cn.zhangchuangla.medicine")
public class MedicineClientApplication {

    public static void main(String[] args) {
        String hint = """
                   ____ _ _            _     ____  _             _                 ____                               __       _\s
                  / ___| (_) ___ _ __ | |_  / ___|| |_ __ _ _ __| |_ _   _ _ __   / ___| _   _  ___ ___ ___  ___ ___ / _|_   _| |
                 | |   | | |/ _ \\ '_ \\| __| \\___ \\| __/ _` | '__| __| | | | '_ \\  \\___ \\| | | |/ __/ __/ _ \\/ __/ __| |_| | | | |
                 | |___| | |  __/ | | | |_   ___) | || (_| | |  | |_| |_| | |_) |  ___) | |_| | (_| (_|  __/\\__ \\__ \\  _| |_| | |
                  \\____|_|_|\\___|_| |_|\\__| |____/ \\__\\__,_|_|   \\__|\\__,_| .__/  |____/ \\__,_|\\___\\___\\___||___/___/_|  \\__,_|_|
                                                                          |_|                                                   \s
                """;
        SpringApplication.run(MedicineClientApplication.class, args);
        System.out.println(hint);
    }
}
