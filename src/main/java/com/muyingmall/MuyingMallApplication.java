package com.muyingmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
public class MuyingMallApplication {

	public static void main(String[] args) {
		SpringApplication.run(MuyingMallApplication.class, args);
		System.out.println("\n" +
				"(♥◠‿◠)ﾉﾞ  母婴商城启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
				"\n" +
				"    ███╗   ███╗    ██╗   ██╗    \n" +
				"    ████╗ ████║    ╚██╗ ██╔╝    \n" +
				"    ██╔████╔██║     ╚████╔╝     \n" +
				"    ██║╚██╔╝██║      ╚██╔╝      \n" +
				"    ██║ ╚═╝ ██║       ██║       \n" +
				"    ╚═╝     ╚═╝       ╚═╝       \n" +
				"    ♡ Muying Mall Started ♡     \n");
	}

}
