package cn.zhangchuangla.medicine.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "cn.zhangchuangla.medicine")
public class MedicineClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicineClientApplication.class, args);
    }
}
