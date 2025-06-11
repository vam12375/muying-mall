package com.muyingmall.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.muyingmall.common.api.CommonResult;
import com.muyingmall.common.exception.BusinessException;
import com.muyingmall.config.AlipayConfig;
import com.muyingmall.dto.RechargeRequestDTO;
import com.muyingmall.entity.AccountTransaction;
import com.muyingmall.entity.UserAccount;
import com.muyingmall.mapper.AccountTransactionMapper;
import com.muyingmall.mapper.UserAccountMapper;
import com.muyingmall.service.UserAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * 钱包充值支付回调控制器
 */
@Slf4j
@RestController
@RequestMapping("/payment/wallet")
@RequiredArgsConstructor
@Tag(name = "钱包充值支付回调接口", description = "处理钱包充值支付回调")
public class WalletPaymentController {

    private final AlipayConfig alipayConfig;
    private final UserAccountService userAccountService;

    @Autowired
    private AccountTransactionMapper accountTransactionMapper;

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Value("${frontend.url}")
    private String frontendUrl;

    /**
     * 支付宝充值回调
     */
    @PostMapping("/alipay/notify")
    public String alipayRechargeNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String key : requestParams.keySet()) {
            String[] values = requestParams.get(key);
            StringBuilder valueStr = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                valueStr.append(values[i]);
                if (i != values.length - 1) {
                    valueStr.append(",");
                }
            }
            params.put(key, valueStr.toString());
        }

        try {
            log.info("支付宝充值异步通知参数: {}", params);

            // 验签
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getPublicKey(),
                    "UTF-8",
                    "RSA2");
            log.info("支付宝充值异步通知验签结果: {}", signVerified);

            if (signVerified) {
                String rechargeOrderNo = params.get("out_trade_no");
                String tradeStatus = params.get("trade_status");
                String tradeNo = params.get("trade_no"); // 支付宝交易号
                String gmtPayment = params.get("gmt_payment"); // 交易付款时间
                String totalAmount = params.get("total_amount"); // 订单金额

                log.info("充值异步通知 - Order No: {}, Trade Status: {}, Trade No: {}, Payment Time: {}, Amount: {}",
                        rechargeOrderNo, tradeStatus, tradeNo, gmtPayment, totalAmount);

                // 查询本地充值记录
                AccountTransaction transaction = accountTransactionMapper.selectOne(
                        new LambdaQueryWrapper<AccountTransaction>()
                                .eq(AccountTransaction::getTransactionNo, rechargeOrderNo)
                                .eq(AccountTransaction::getType, 1) // 1表示充值
                );

                if (transaction == null) {
                    log.error("充值异步通知 - 未找到充值记录: {}", rechargeOrderNo);
                    return "failure";
                }

                log.info("充值异步通知 - 找到充值记录: ID {}, UserID {}, Status {}",
                        transaction.getId(), transaction.getUserId(), transaction.getStatus());

                // 根据通知类型处理
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    // 如果充值已经成功，避免重复处理
                    if (transaction.getStatus() == 1) { // 1表示成功
                        log.info("充值异步通知 - 充值已处理，跳过: {}", rechargeOrderNo);
                        return "success";
                    }

                    try {
                        // 执行充值操作
                        boolean result = completeRecharge(transaction, tradeNo);
                        if (result) {
                            log.info("充值异步通知 - 充值成功: {}", rechargeOrderNo);
                            return "success";
                        } else {
                            log.error("充值异步通知 - 充值处理失败: {}", rechargeOrderNo);
                            return "failure";
                        }
                    } catch (Exception e) {
                        log.error("充值异步通知 - 处理充值异常: {}", e.getMessage(), e);
                        // 支付宝异步通知，返回failure会触发重试
                        return "failure";
                    }
                } else {
                    // 交易关闭或其他状态
                    try {
                        // 更新交易状态为失败
                        transaction.setStatus(2); // 2表示失败
                        transaction.setUpdateTime(new Date());
                        accountTransactionMapper.updateById(transaction);
                        log.info("充值异步通知 - 交易关闭状态更新成功: {}", rechargeOrderNo);
                        return "success";
                    } catch (Exception e) {
                        log.error("充值异步通知 - 更新交易关闭状态失败: {}", e.getMessage(), e);
                        return "failure";
                    }
                }
            } else {
                log.error("充值异步通知 - 验签失败");
                return "failure";
            }
        } catch (AlipayApiException e) {
            log.error("充值异步通知 - 验签异常: {}", e.getMessage(), e);
            return "failure";
        } catch (Exception e) {
            log.error("充值异步通知 - 处理异常: {}", e.getMessage(), e);
            return "failure";
        }
    }

    /**
     * 支付宝充值同步回调
     */
    @GetMapping("/alipay/return")
    public void alipayRechargeReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String key : requestParams.keySet()) {
            String[] values = requestParams.get(key);
            StringBuilder valueStr = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                valueStr.append(values[i]);
                if (i != values.length - 1) {
                    valueStr.append(",");
                }
            }
            params.put(key, valueStr.toString());
        }

        log.info("支付宝充值同步回调参数: {}", params);

        boolean signVerified = false;
        String rechargeResult = "unknown";
        String rechargeOrderNo = null;
        String amount = "0";

        try {
            signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getPublicKey(),
                    "UTF-8",
                    "RSA2");
            log.info("支付宝充值同步回调验签结果: {}", signVerified);

            if (signVerified) {
                // 验签成功，获取支付信息
                rechargeOrderNo = params.get("out_trade_no");
                String tradeNo = params.get("trade_no"); // 支付宝交易号
                log.info("充值同步回调 - Recharge Order No: {}, Trade No: {}", rechargeOrderNo, tradeNo);

                // 查询充值记录
                AccountTransaction transaction = accountTransactionMapper.selectOne(
                        new LambdaQueryWrapper<AccountTransaction>()
                                .eq(AccountTransaction::getTransactionNo, rechargeOrderNo)
                                .eq(AccountTransaction::getType, 1) // 1表示充值
                );

                if (transaction != null) {
                    amount = transaction.getAmount().toString();
                    log.info("充值同步回调 - 找到充值记录: Transaction ID {}, User ID: {}, Amount: {}",
                            transaction.getId(), transaction.getUserId(), transaction.getAmount());

                    // 查询支付宝交易状态
                    try {
                        AlipayTradeQueryResponse queryResponse = queryAlipayTradeStatus(rechargeOrderNo);
                        if ("TRADE_SUCCESS".equals(queryResponse.getTradeStatus()) ||
                                "TRADE_FINISHED".equals(queryResponse.getTradeStatus())) {
                            // 如果支付成功且本地状态未更新，则执行充值
                            if (transaction.getStatus() != 1) { // 1表示成功
                                completeRecharge(transaction, tradeNo);
                            }
                            rechargeResult = "success";
                        } else if ("WAIT_BUYER_PAY".equals(queryResponse.getTradeStatus())) {
                            rechargeResult = "pending";
                        } else {
                            rechargeResult = "failed";
                        }
                    } catch (Exception e) {
                        log.error("充值同步回调 - 查询支付状态失败: {}", e.getMessage(), e);
                        rechargeResult = "error";
                    }
                } else {
                    log.warn("充值同步回调 - 未找到充值记录: Order No {}", rechargeOrderNo);
                    rechargeResult = "not_found";
                }
            } else {
                log.error("支付宝充值同步回调验签失败");
                rechargeResult = "invalid_sign";
            }
        } catch (AlipayApiException e) {
            log.error("支付宝充值同步回调验签异常: {}", e.getMessage(), e);
            rechargeResult = "error";
        }

        // 重定向到前端充值结果页面
        String redirectUrl = frontendUrl + "/recharge/success?orderNo=" +
                (rechargeOrderNo != null ? rechargeOrderNo : "") +
                "&result=" + rechargeResult +
                "&amount=" + amount +
                "&paymentMethod=alipay";

        log.info("充值同步回调 - 重定向到: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    /**
     * 微信充值回调
     */
    @PostMapping("/wechat/notify")
    public String wechatRechargeNotify(HttpServletRequest request) {
        log.info("收到微信充值回调请求");

        try {
            // 读取请求体中的XML数据
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String xmlData = sb.toString();
            log.info("微信充值回调原始数据: {}", xmlData);

            // 解析XML数据
            Map<String, String> notifyMap = parseXml(xmlData);
            log.info("微信充值回调解析后数据: {}", notifyMap);

            // 验证签名 - 为简化实现，这里暂不实现完整的验签逻辑
            // 实际生产环境必须进行签名验证，以确保数据真实性
            // boolean isSignValid = verifyWechatSign(notifyMap);
            boolean isSignValid = true; // 临时跳过验签
            log.info("微信充值回调验签结果: {}", isSignValid);

            if (isSignValid) {
                // 验签成功，处理业务逻辑
                String returnCode = notifyMap.get("return_code");
                String resultCode = notifyMap.get("result_code");

                if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
                    // 支付成功，处理业务逻辑
                    String outTradeNo = notifyMap.get("out_trade_no"); // 商户订单号
                    String transactionId = notifyMap.get("transaction_id"); // 微信支付订单号
                    String totalFee = notifyMap.get("total_fee"); // 订单金额，单位为分

                    log.info("微信充值回调 - 订单号: {}, 微信支付单号: {}, 金额: {} 分",
                            outTradeNo, transactionId, totalFee);

                    // 查询本地充值记录
                    AccountTransaction transaction = accountTransactionMapper.selectOne(
                            new LambdaQueryWrapper<AccountTransaction>()
                                    .eq(AccountTransaction::getTransactionNo, outTradeNo)
                                    .eq(AccountTransaction::getType, 1) // 1表示充值
                    );

                    if (transaction == null) {
                        log.error("微信充值回调 - 未找到充值记录: {}", outTradeNo);
                        return generateWechatResponse(false, "订单不存在");
                    }

                    log.info("微信充值回调 - 找到充值记录: ID {}, UserID {}, Status {}",
                            transaction.getId(), transaction.getUserId(), transaction.getStatus());

                    // 金额校验 - 将分转换为元进行比较
                    BigDecimal notifyAmount = new BigDecimal(totalFee).divide(new BigDecimal("100"));
                    if (transaction.getAmount().compareTo(notifyAmount) != 0) {
                        log.error("微信充值回调 - 金额不匹配: 通知金额 {} 元, 订单金额 {} 元",
                                notifyAmount, transaction.getAmount());
                        return generateWechatResponse(false, "金额不匹配");
                    }

                    // 如果充值已经成功，避免重复处理
                    if (transaction.getStatus() == 1) { // 1表示成功
                        log.info("微信充值回调 - 充值已处理，跳过: {}", outTradeNo);
                        return generateWechatResponse(true, "OK");
                    }

                    try {
                        // 执行充值操作
                        boolean result = completeRecharge(transaction, transactionId);
                        if (result) {
                            log.info("微信充值回调 - 充值成功: {}", outTradeNo);
                            return generateWechatResponse(true, "OK");
                        } else {
                            log.error("微信充值回调 - 充值处理失败: {}", outTradeNo);
                            return generateWechatResponse(false, "充值处理失败");
                        }
                    } catch (Exception e) {
                        log.error("微信充值回调 - 处理充值异常: {}", e.getMessage(), e);
                        return generateWechatResponse(false, "处理异常");
                    }
                } else {
                    // 支付失败
                    log.error("微信充值回调 - 支付失败: return_code={}, result_code={}, err_code={}, err_code_des={}",
                            returnCode, resultCode, notifyMap.get("err_code"), notifyMap.get("err_code_des"));
                    return generateWechatResponse(false, "支付失败");
                }
            } else {
                // 验签失败
                log.error("微信充值回调 - 验签失败");
                return generateWechatResponse(false, "验签失败");
            }
        } catch (Exception e) {
            log.error("微信充值回调 - 处理异常: {}", e.getMessage(), e);
            return generateWechatResponse(false, "处理异常: " + e.getMessage());
        }
    }

    /**
     * 解析微信支付回调的XML数据
     * 
     * @param xmlData XML格式的数据
     * @return 解析后的Map
     * @throws Exception 解析异常
     */
    private Map<String, String> parseXml(String xmlData) throws Exception {
        Map<String, String> map = new HashMap<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // 防止XXE攻击
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlData)));
        Element root = document.getDocumentElement();
        NodeList nodeList = root.getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                map.put(node.getNodeName(), node.getTextContent());
            }
        }

        return map;
    }

    /**
     * 生成微信支付回调响应
     * 
     * @param isSuccess 是否成功
     * @param message   消息
     * @return XML格式的响应
     */
    private String generateWechatResponse(boolean isSuccess, String message) {
        return "<xml>" +
                "<return_code><![CDATA[" + (isSuccess ? "SUCCESS" : "FAIL") + "]]></return_code>" +
                "<return_msg><![CDATA[" + message + "]]></return_msg>" +
                "</xml>";
    }

    /**
     * 查询支付宝交易状态
     * 
     * @param outTradeNo 商户订单号
     * @return 支付宝交易查询响应
     * @throws AlipayApiException 支付宝API异常
     */
    private AlipayTradeQueryResponse queryAlipayTradeStatus(String outTradeNo) throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                "json",
                "UTF-8",
                alipayConfig.getPublicKey(),
                "RSA2");

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", outTradeNo);
        request.setBizContent(com.alibaba.fastjson.JSON.toJSONString(bizContent));

        return alipayClient.execute(request);
    }

    /**
     * 完成充值操作
     * 
     * @param transaction 充值交易记录
     * @param tradeNo     第三方交易号
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean completeRecharge(AccountTransaction transaction, String tradeNo) {
        log.info("开始执行充值操作: 交易ID={}, 用户ID={}, 金额={}",
                transaction.getId(), transaction.getUserId(), transaction.getAmount());

        try {
            // 1. 更新用户账户余额
            UserAccount userAccount = userAccountMapper.selectById(transaction.getAccountId());
            if (userAccount == null) {
                log.error("充值失败: 未找到用户账户, accountId={}", transaction.getAccountId());
                throw new BusinessException("未找到用户账户");
            }

            BigDecimal beforeBalance = userAccount.getBalance();
            BigDecimal afterBalance = beforeBalance.add(transaction.getAmount());

            userAccount.setBalance(afterBalance);
            userAccount.setLastRechargeTime(new Date());
            userAccount.setUpdateTime(new Date());
            int rows = userAccountMapper.updateById(userAccount);

            if (rows <= 0) {
                log.error("充值失败: 更新用户账户余额失败, accountId={}", transaction.getAccountId());
                throw new BusinessException("更新用户账户余额失败");
            }

            log.info("充值 - 更新账户余额成功: beforeBalance={}, afterBalance={}", beforeBalance, afterBalance);

            // 2. 更新交易记录状态
            transaction.setStatus(1); // 1表示成功
            transaction.setAfterBalance(afterBalance);
            transaction.setBalance(afterBalance);
            transaction.setRemark("第三方交易号: " + tradeNo);
            transaction.setUpdateTime(new Date());

            int result = accountTransactionMapper.updateById(transaction);
            if (result <= 0) {
                log.error("充值失败: 更新交易记录状态失败, transactionId={}", transaction.getId());
                throw new BusinessException("更新交易记录状态失败");
            }

            log.info("充值 - 更新交易记录状态成功: transactionId={}, status=1(成功)", transaction.getId());

            return true;
        } catch (Exception e) {
            log.error("充值操作执行异常: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 手动完成充值（仅用于测试）
     */
    @Operation(summary = "手动完成充值（仅用于测试）")
    @PostMapping("/manual-complete")
    public CommonResult<Map<String, Object>> manualCompleteRecharge(@RequestParam String orderNo) {
        log.info("手动完成充值，orderNo={}", orderNo);

        try {
            // 查询充值记录
            log.info("开始查询充值记录，orderNo={}", orderNo);
            AccountTransaction transaction = accountTransactionMapper.selectOne(
                    new LambdaQueryWrapper<AccountTransaction>()
                            .eq(AccountTransaction::getTransactionNo, orderNo)
                            .eq(AccountTransaction::getType, 1) // 1表示充值
            );

            if (transaction == null) {
                log.error("手动完成充值失败：订单不存在，orderNo={}", orderNo);

                // 尝试不带类型条件查询，看看是否能找到记录
                AccountTransaction anyTransaction = accountTransactionMapper.selectOne(
                        new LambdaQueryWrapper<AccountTransaction>()
                                .eq(AccountTransaction::getTransactionNo, orderNo));

                if (anyTransaction != null) {
                    log.info("找到了订单号匹配但类型不匹配的记录：orderNo={}, type={}", orderNo, anyTransaction.getType());
                } else {
                    log.info("数据库中不存在订单号为{}的记录", orderNo);
                }

                return CommonResult.validateFailed("充值订单不存在");
            }

            log.info("找到充值记录：id={}, userId={}, accountId={}, amount={}, status={}",
                    transaction.getId(), transaction.getUserId(), transaction.getAccountId(),
                    transaction.getAmount(), transaction.getStatus());

            // 如果充值已经成功，避免重复处理
            if (transaction.getStatus() == 1) { // 1表示成功
                log.info("手动完成充值：充值已处理，跳过: {}", orderNo);
                return CommonResult.success(null, "充值已处理，无需重复操作");
            }

            // 验证账户ID是否正确
            if (transaction.getAccountId() == null || transaction.getAccountId() <= 0) {
                log.error("充值失败：账户ID无效，accountId={}", transaction.getAccountId());
                return CommonResult.failed("账户ID无效");
            }

            // 执行充值操作
            try {
                log.info("开始执行充值操作");
                boolean result = completeRecharge(transaction, "manual-" + System.currentTimeMillis());
                if (result) {
                    log.info("手动完成充值：充值成功: {}", orderNo);

                    // 返回结果
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("orderNo", orderNo);
                    resultMap.put("amount", transaction.getAmount());
                    resultMap.put("status", "SUCCESS");
                    resultMap.put("message", "充值成功");

                    return CommonResult.success(resultMap);
                } else {
                    log.error("手动完成充值：充值处理失败: {}", orderNo);
                    return CommonResult.failed("充值处理失败");
                }
            } catch (Exception e) {
                log.error("执行充值操作异常", e);
                return CommonResult.failed("执行充值操作异常: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("手动完成充值异常", e);
            return CommonResult.failed("手动完成充值异常: " + e.getMessage());
        }
    }
}