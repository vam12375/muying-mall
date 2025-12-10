package com.muyingmall.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyingmall.entity.UserAccount;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户账户Mapper接口
 */
@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccount> {

        /**
         * 查询用户账户列表
         *
         * @param keyword 搜索关键字(用户名/昵称/邮箱/手机)
         * @param status  账户状态: 0-冻结, 1-正常
         * @return 用户账户列表
         */
        @Select({
                        "<script>",
                        "SELECT",
                        "  ua.id, ua.user_id, ua.balance, ua.status, ua.create_time, ua.update_time,",
                        "  u.id as u_id, u.username as u_username, u.nickname as u_nickname, u.email as u_email, u.phone as u_phone",
                        "FROM",
                        "  user_account ua",
                        "LEFT JOIN",
                        "  user u ON ua.user_id = u.id",
                        "<where>",
                        "  <if test='keyword != null and keyword != \"\"'>",
                        "    AND (u.username LIKE CONCAT('%', #{keyword}, '%')",
                        "    OR u.nickname LIKE CONCAT('%', #{keyword}, '%')",
                        "    OR u.email LIKE CONCAT('%', #{keyword}, '%')",
                        "    OR u.phone LIKE CONCAT('%', #{keyword}, '%'))",
                        "  </if>",
                        "  <if test='status != null'>",
                        "    AND ua.status = #{status}",
                        "  </if>",
                        "</where>",
                        "ORDER BY ua.create_time DESC",
                        "</script>"
        })
        @Results({
                        @Result(id = true, column = "id", property = "id"),
                        @Result(column = "user_id", property = "userId"),
                        @Result(column = "balance", property = "balance"),
                        @Result(column = "status", property = "status"),
                        @Result(column = "create_time", property = "createTime"),
                        @Result(column = "update_time", property = "updateTime"),
                        @Result(property = "user", column = "u_id", javaType = com.muyingmall.entity.User.class, one = @One(select = "com.muyingmall.mapper.UserMapper.selectById"))
        })
        List<UserAccount> selectUserAccountList(@Param("keyword") String keyword, @Param("status") Integer status);

        /**
         * 根据用户ID查询账户信息
         *
         * @param userId 用户ID
         * @return 账户信息
         */
        @Select("SELECT id, user_id, balance, status, create_time, update_time FROM user_account WHERE user_id = #{userId}")
        @Results({
                        @Result(id = true, column = "id", property = "id"),
                        @Result(column = "user_id", property = "userId"),
                        @Result(column = "balance", property = "balance"),
                        @Result(column = "status", property = "status"),
                        @Result(column = "create_time", property = "createTime"),
                        @Result(column = "update_time", property = "updateTime")
        })
        UserAccount selectByUserId(@Param("userId") Integer userId);

        /**
         * 插入用户账户
         *
         * @param userAccount 用户账户
         * @return 影响行数
         */
        @Insert("INSERT INTO user_account (user_id, balance, status, create_time, update_time) " +
                        "VALUES (#{userId}, #{balance}, #{status}, #{createTime}, #{updateTime})")
        @Options(useGeneratedKeys = true, keyProperty = "id")
        int insert(UserAccount userAccount);

        /**
         * 更新账户余额
         *
         * @param userAccount 用户账户
         * @return 影响行数
         */
        @Update("UPDATE user_account SET balance = #{balance}, update_time = #{updateTime} WHERE user_id = #{userId}")
        int updateBalance(UserAccount userAccount);

        /**
         * 更新账户状态
         *
         * @param userAccount 用户账户
         * @return 影响行数
         */
        @Update("UPDATE user_account SET status = #{status}, update_time = #{updateTime} WHERE user_id = #{userId}")
        int updateStatus(UserAccount userAccount);

        /**
         * 分页查询用户账户列表
         *
         * @param page    分页参数
         * @param keyword 搜索关键字(用户名/昵称/邮箱/手机)
         * @return 分页结果
         */
        @Select({
                        "<script>",
                        "SELECT",
                        "  ua.*, u.username, u.nickname, u.email, u.phone",
                        "FROM",
                        "  user_account ua",
                        "LEFT JOIN",
                        "  user u ON ua.user_id = u.id",
                        "<where>",
                        "  <if test='keyword != null and keyword != \"\"'>",
                        "    AND (u.username LIKE CONCAT('%', #{keyword}, '%')",
                        "    OR u.nickname LIKE CONCAT('%', #{keyword}, '%')",
                        "    OR u.email LIKE CONCAT('%', #{keyword}, '%')",
                        "    OR u.phone LIKE CONCAT('%', #{keyword}, '%'))",
                        "  </if>",
                        "</where>",
                        "ORDER BY ua.create_time DESC",
                        "</script>"
        })
        IPage<UserAccount> getUserAccountPage(Page<UserAccount> page, @Param("keyword") String keyword);

        /**
         * 根据用户ID获取账户信息
         *
         * @param userId 用户ID
         * @return 用户账户信息
         */
        @Select("SELECT account_id as id, user_id, balance, status, create_time, update_time FROM user_account WHERE user_id = #{userId}")
        UserAccount getUserAccountByUserId(@Param("userId") Integer userId);
        
        /**
         * 统计所有用户账户余额总和
         *
         * @return 总余额
         */
        @Select("SELECT COALESCE(SUM(balance), 0) FROM user_account")
        java.math.BigDecimal sumAllBalance();
}