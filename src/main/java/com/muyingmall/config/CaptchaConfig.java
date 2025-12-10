package com.muyingmall.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 图形验证码配置类
 * 使用Kaptcha生成验证码图片
 */
@Configuration
public class CaptchaConfig {

    @Bean
    public DefaultKaptcha captchaProducer() {
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        
        // 验证码图片宽度
        properties.setProperty("kaptcha.image.width", "150");
        // 验证码图片高度
        properties.setProperty("kaptcha.image.height", "50");
        // 验证码字符长度
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 验证码字符集
        properties.setProperty("kaptcha.textproducer.char.string", "ABCDEFGHJKLMNPQRSTUVWXYZ23456789");
        // 字体大小
        properties.setProperty("kaptcha.textproducer.font.size", "38");
        // 字体颜色
        properties.setProperty("kaptcha.textproducer.font.color", "black");
        // 字体
        properties.setProperty("kaptcha.textproducer.font.names", "Arial,Courier");
        // 干扰线颜色
        properties.setProperty("kaptcha.noise.color", "gray");
        // 背景渐变色
        properties.setProperty("kaptcha.background.clear.from", "white");
        properties.setProperty("kaptcha.background.clear.to", "white");
        // 边框
        properties.setProperty("kaptcha.border", "no");
        
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
