package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.dto.TransactionQueryDTO;
import com.muyingmall.entity.AccountTransaction;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 账户交易记录Mapper接口
 */
@Mapper
public interface AccountTransactionMapper extends BaseMapper<AccountTransaction> {

        /**
         * 查询交易记录列表
         *
         * @param queryDTO 查询条件
         * @return 交易记录列表
         */
        @Select({
                        "<script>",
                        "SELECT",
                        "  t.transaction_id as id, t.user_id, t.account_id, t.type, t.amount, t.balance, t.status,",
                        "  t.payment_method, t.transaction_no, t.related_id as relatedId, t.create_time, t.update_time, t.description, t.remark,",
                        "  u.user_id as u_id, u.username as u_username, u.nickname as u_nickname, u.email as u_email, u.phone as u_phone",
                        "FROM",
                        "  account_transaction t",
                        "LEFT JOIN",
                        "  user u ON t.user_id = u.user_id",
                        "<where>",
                        "  <if test='userId != null'>",
                        "    AND t.user_id = #{userId}",
                        "  </if>",
                        "  <if test='type != null'>",
                        "    AND t.type = #{type}",
                        "  </if>",
                        "  <if test='status != null'>",
                        "    AND t.status = #{status}",
                        "  </if>",
                        "  <if test='paymentMethod != null and paymentMethod != \"\"'>",
                        "    AND t.payment_method = #{paymentMethod}",
                        "  </if>",
                        "  <if test='transactionNo != null and transactionNo != \"\"'>",
                        "    AND t.transaction_no = #{transactionNo}",
                        "  </if>",
                        "  <if test='startTime != null'>",
                        "    AND t.create_time &gt;= #{startTime}",
                        "  </if>",
                        "  <if test='endTime != null'>",
                        "    AND t.create_time &lt;= #{endTime}",
                        "  </if>",
                        "  <if test='keyword != null and keyword != \"\"'>",
                        "    AND (u.username LIKE CONCAT('%', #{keyword}, '%')",
                        "    OR u.nickname LIKE CONCAT('%', #{keyword}, '%')",
                        "    OR u.email LIKE CONCAT('%', #{keyword}, '%')",
                        "    OR u.phone LIKE CONCAT('%', #{keyword}, '%'))",
                        "  </if>",
                        "</where>",
                        "ORDER BY t.create_time DESC",
                        "</script>"
        })
        @Results({
                        @Result(id = true, column = "id", property = "id"),
                        @Result(column = "user_id", property = "userId"),
                        @Result(column = "account_id", property = "accountId"),
                        @Result(column = "type", property = "type"),
                        @Result(column = "amount", property = "amount"),
                        @Result(column = "balance", property = "balance"),
                        @Result(column = "status", property = "status"),
                        @Result(column = "payment_method", property = "paymentMethod"),
                        @Result(column = "transaction_no", property = "transactionNo"),
                        @Result(column = "relatedId", property = "relatedId"),
                        @Result(column = "create_time", property = "createTime"),
                        @Result(column = "update_time", property = "updateTime"),
                        @Result(column = "description", property = "description"),
                        @Result(column = "remark", property = "remark"),
                        @Result(property = "user", column = "u_id", javaType = com.muyingmall.entity.User.class, one = @One(select = "com.muyingmall.mapper.UserMapper.selectById"))
        })
        List<AccountTransaction> selectTransactionList(TransactionQueryDTO queryDTO);

        /**
         * 根据ID查询交易记录
         * 注意：计算 beforeBalance 和 afterBalance
         *
         * @param id 交易记录ID
         * @return 交易记录
         */
        @Select("<script>" +
                        "SELECT transaction_id as id, user_id, account_id, type, amount, balance, status, payment_method, " +
                        "transaction_no, related_id as relatedId, create_time, update_time, description, remark, " +
                        "balance as afterBalance, " +
                        "CASE " +
                        "  WHEN type IN (1, 3, 4) AND amount &gt; 0 THEN balance - amount " +
                        "  WHEN type IN (1, 3, 4) AND amount &lt; 0 THEN balance - amount " +
                        "  WHEN type = 2 THEN balance + amount " +
                        "  ELSE balance " +
                        "END as beforeBalance " +
                        "FROM account_transaction WHERE transaction_id = #{id}" +
                        "</script>")
        @Results({
                        @Result(id = true, column = "id", property = "id"),
                        @Result(column = "user_id", property = "userId"),
                        @Result(column = "account_id", property = "accountId"),
                        @Result(column = "type", property = "type"),
                        @Result(column = "amount", property = "amount"),
                        @Result(column = "balance", property = "balance"),
                        @Result(column = "status", property = "status"),
                        @Result(column = "payment_method", property = "paymentMethod"),
                        @Result(column = "transaction_no", property = "transactionNo"),
                        @Result(column = "relatedId", property = "relatedId"),
                        @Result(column = "create_time", property = "createTime"),
                        @Result(column = "update_time", property = "updateTime"),
                        @Result(column = "description", property = "description"),
                        @Result(column = "remark", property = "remark"),
                        @Result(column = "beforeBalance", property = "beforeBalance"),
                        @Result(column = "afterBalance", property = "afterBalance")
        })
        AccountTransaction selectById(@Param("id") Integer id);

        /**
         * 插入交易记录
         *
         * @param transaction 交易记录
         * @return 影响行数
         */
        @Insert("INSERT INTO account_transaction (user_id, account_id, type, amount, balance, status, " +
                        "payment_method, transaction_no, related_id, create_time, update_time, description, remark) " +
                        "VALUES (#{userId}, #{accountId}, #{type}, #{amount}, #{balance}, #{status}, " +
                        "#{paymentMethod}, #{transactionNo}, #{relatedId}, #{createTime}, #{updateTime}, #{description}, #{remark})")
        @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "transaction_id")
        int insert(AccountTransaction transaction);

        /**
         * 安全插入交易记录，确保关键字段不为空
         *
         * @param transaction 交易记录
         */
        default void secureInsert(AccountTransaction transaction) {
                // 打印完整对象信息，辅助调试
                System.out.println("准备插入交易记录，完整对象：" + transaction);

                // 检查所有关键字段
                if (transaction.getAccountId() == null || transaction.getAccountId() <= 0) {
                        System.out.println("交易记录的关键对象属性：userId=" + transaction.getUserId() +
                                        ", type=" + transaction.getType() +
                                        ", amount=" + transaction.getAmount());
                        throw new IllegalArgumentException(
                                        "交易记录的账户ID(accountId)不能为空或无效值，当前值：" + transaction.getAccountId());
                }
                if (transaction.getUserId() == null || transaction.getUserId() <= 0) {
                        throw new IllegalArgumentException("交易记录的用户ID(userId)不能为空或无效值，当前值：" + transaction.getUserId());
                }

                // 记录成功通过验证的日志
                System.out.println("交易记录验证通过，准备插入: userId=" + transaction.getUserId() +
                                ", accountId=" + transaction.getAccountId() +
                                ", type=" + transaction.getType() +
                                ", amount=" + transaction.getAmount() +
                                ", balance=" + transaction.getBalance());

                // 调用原始insert方法
                insert(transaction);

                // 记录成功插入的日志
                System.out.println("交易记录插入成功，ID：" + transaction.getId());
        }

        /**
         * 更新交易记录状态
         *
         * @param transaction 交易记录
         * @return 影响行数
         */
        @Update("UPDATE account_transaction SET status = #{status}, update_time = #{updateTime} WHERE transaction_id = #{id}")
        int updateStatus(AccountTransaction transaction);

        /**
         * 根据用户ID查询交易记录列表
         * 注意：计算 beforeBalance 和 afterBalance
         *
         * @param userId 用户ID
         * @return 交易记录列表
         */
        @Select("<script>" +
                        "SELECT transaction_id as id, user_id, account_id, type, amount, balance, status, payment_method, " +
                        "transaction_no, related_id as relatedId, create_time, update_time, description, remark, " +
                        "balance as afterBalance, " +
                        "CASE " +
                        "  WHEN type IN (1, 3, 4) AND amount &gt; 0 THEN balance - amount " +
                        "  WHEN type IN (1, 3, 4) AND amount &lt; 0 THEN balance - amount " +
                        "  WHEN type = 2 THEN balance + amount " +
                        "  ELSE balance " +
                        "END as beforeBalance " +
                        "FROM account_transaction WHERE user_id = #{userId} ORDER BY create_time DESC" +
                        "</script>")
        @Results({
                        @Result(id = true, column = "id", property = "id"),
                        @Result(column = "user_id", property = "userId"),
                        @Result(column = "account_id", property = "accountId"),
                        @Result(column = "type", property = "type"),
                        @Result(column = "amount", property = "amount"),
                        @Result(column = "balance", property = "balance"),
                        @Result(column = "status", property = "status"),
                        @Result(column = "payment_method", property = "paymentMethod"),
                        @Result(column = "transaction_no", property = "transactionNo"),
                        @Result(column = "relatedId", property = "relatedId"),
                        @Result(column = "create_time", property = "createTime"),
                        @Result(column = "update_time", property = "updateTime"),
                        @Result(column = "description", property = "description"),
                        @Result(column = "remark", property = "remark"),
                        @Result(column = "beforeBalance", property = "beforeBalance"),
                        @Result(column = "afterBalance", property = "afterBalance")
        })
        List<AccountTransaction> selectByUserId(@Param("userId") Integer userId);

        /**
         * 分页查询交易记录列表
         * 注意：计算 beforeBalance 和 afterBalance
         * - afterBalance = balance（交易后余额）
         * - beforeBalance = balance - amount（充值/退款类型：1,3）或 balance + amount（消费类型：2）
         *
         * @param page     分页参数
         * @param queryDTO 查询条件
         * @return 分页结果
         */
        @Select({
                        "<script>",
                        "SELECT",
                        "  t.transaction_id as id, t.user_id, t.account_id, t.type, t.amount, t.balance, t.status,",
                        "  t.payment_method, t.transaction_no, t.related_id as relatedId, t.description, t.remark,",
                        "  t.create_time, t.update_time,",
                        "  t.balance as afterBalance,",
                        "  CASE",
                        "    WHEN t.type IN (1, 3, 4) AND t.amount &gt; 0 THEN t.balance - t.amount",
                        "    WHEN t.type IN (1, 3, 4) AND t.amount &lt; 0 THEN t.balance - t.amount",
                        "    WHEN t.type = 2 THEN t.balance + t.amount",
                        "    ELSE t.balance",
                        "  END as beforeBalance",
                        "FROM",
                        "  account_transaction t",
                        "<where>",
                        "  <if test='query.userId != null'>",
                        "    AND t.user_id = #{query.userId}",
                        "  </if>",
                        "  <if test='query.type != null'>",
                        "    AND t.type = #{query.type}",
                        "  </if>",
                        "  <if test='query.status != null'>",
                        "    AND t.status = #{query.status}",
                        "  </if>",
                        "  <if test='query.paymentMethod != null and query.paymentMethod != \"\"'>",
                        "    AND t.payment_method = #{query.paymentMethod}",
                        "  </if>",
                        "  <if test='query.transactionNo != null and query.transactionNo != \"\"'>",
                        "    AND t.transaction_no = #{query.transactionNo}",
                        "  </if>",
                        "  <if test='query.startTime != null'>",
                        "    AND t.create_time &gt;= #{query.startTime}",
                        "  </if>",
                        "  <if test='query.endTime != null'>",
                        "    AND t.create_time &lt;= #{query.endTime}",
                        "  </if>",
                        "</where>",
                        "ORDER BY t.create_time DESC",
                        "</script>"
        })
        IPage<AccountTransaction> getTransactionPage(Page<AccountTransaction> page,
                        @Param("query") TransactionQueryDTO queryDTO);

        /**
         * 根据用户ID和交易类型统计交易金额
         *
         * @param userId 用户ID
         * @param type   交易类型
         * @return 交易金额总和
         */
        @Select("SELECT COALESCE(SUM(amount), 0) FROM account_transaction WHERE user_id = #{userId} AND type = #{type} AND status = 1")
        BigDecimal sumAmountByUserIdAndType(@Param("userId") Integer userId, @Param("type") Integer type);

        /**
         * 查询用户的交易记录统计信息（用于调试）
         *
         * @param userId 用户ID
         * @return 统计信息
         */
        @Select("SELECT type, status, COUNT(*) as count, SUM(amount) as total_amount FROM account_transaction WHERE user_id = #{userId} GROUP BY type, status")
        List<Map<String, Object>> getTransactionStatsByUserId(@Param("userId") Integer userId);

        /**
         * 根据交易类型和状态统计所有用户的交易金额总和
         * 用于全局统计（如：总充值、总消费）
         *
         * @param type   交易类型（1-充值，2-消费，3-退款，4-调整）
         * @param status 交易状态（1-成功）
         * @return 交易金额总和
         */
        @Select("SELECT COALESCE(SUM(ABS(amount)), 0) FROM account_transaction WHERE type = #{type} AND status = #{status}")
        BigDecimal sumByTypeAndStatus(@Param("type") Integer type, @Param("status") Integer status);
}