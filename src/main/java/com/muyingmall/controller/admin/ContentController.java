package com.muyingmall.controller.admin;

import com.muyingmall.common.CacheConstants;
import com.muyingmall.common.result.Result;
import com.muyingmall.util.CacheProtectionUtil;
import com.muyingmall.util.RedisUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * 内容管理控制器
 * 提供内容管理相关的API端点
 */
@RestController
@RequestMapping("/admin/content")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "内容管理", description = "提供内容管理相关的API")
public class ContentController {

    private final RedisUtil redisUtil;
    private final CacheProtectionUtil cacheProtectionUtil;

    /**
     * 获取内容列表
     */
    @GetMapping("")
    @Operation(summary = "获取内容列表")
    public Result<Map<String, Object>> getContentList(
            @RequestParam(value = "page", defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") @Parameter(description = "每页条数") Integer pageSize,
            @RequestParam(value = "type", required = false) @Parameter(description = "内容类型") String type,
            @RequestParam(value = "status", required = false) @Parameter(description = "内容状态") String status,
            @RequestParam(value = "keyword", required = false) @Parameter(description = "关键词") String keyword) {
        try {
            log.info("获取内容列表，页码：{}，每页条数：{}，类型：{}，状态：{}，关键词：{}", page, pageSize, type, status, keyword);

            // 构建缓存键
            StringBuilder cacheKey = new StringBuilder(CacheConstants.CONTENT_LIST_KEY);
            cacheKey.append("page_").append(page)
                    .append("_size_").append(pageSize);

            if (type != null && !type.isEmpty()) {
                cacheKey.append("_type_").append(type);
            }

            if (status != null && !status.isEmpty()) {
                cacheKey.append("_status_").append(status);
            }

            if (keyword != null && !keyword.isEmpty()) {
                cacheKey.append("_keyword_").append(keyword);
            }

            // 使用缓存保护工具获取数据
            Callable<Map<String, Object>> dbFallback = () -> {
                // 模拟从数据库获取数据
                Map<String, Object> result = new HashMap<>();
                List<Map<String, Object>> contentList = new ArrayList<>();

                // 模拟数据
                for (int i = 0; i < pageSize; i++) {
                    Map<String, Object> content = new HashMap<>();
                    content.put("id", "content-" + (i + 1));
                    content.put("title", "示例内容 " + (i + 1));
                    content.put("type", i % 2 == 0 ? "article" : "page");
                    content.put("status", i % 3 == 0 ? "draft" : (i % 3 == 1 ? "published" : "scheduled"));
                    content.put("content", "这是示例内容 " + (i + 1) + " 的正文内容...");
                    content.put("author", "admin");
                    content.put("createdAt", new Date().toString());
                    content.put("updatedAt", new Date().toString());

                    List<String> tags = new ArrayList<>();
                    tags.add("示例");
                    tags.add("测试");
                    content.put("tags", tags);

                    List<String> categories = new ArrayList<>();
                    categories.add("未分类");
                    content.put("categories", categories);

                    contentList.add(content);
                }

                result.put("list", contentList);
                result.put("total", 100);
                result.put("page", page);
                result.put("pageSize", pageSize);

                return result;
            };

            // 使用缓存保护查询，缓存时间30分钟
            Map<String, Object> result = cacheProtectionUtil.queryWithProtection(
                    cacheKey.toString(),
                    CacheConstants.MEDIUM_EXPIRE_TIME,
                    dbFallback);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取内容列表失败", e);
            return Result.error("获取内容列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取内容详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取内容详情")
    public Result<Map<String, Object>> getContentDetail(@PathVariable("id") String id) {
        try {
            log.info("获取内容详情，ID：{}", id);

            // 构建缓存键
            String cacheKey = CacheConstants.CONTENT_DETAIL_KEY + id;

            // 使用互斥锁保护的缓存查询
            String lockKey = cacheKey + ":lock";

            Callable<Map<String, Object>> dbFallback = () -> {
                // 模拟从数据库获取数据
                Map<String, Object> content = new HashMap<>();
                content.put("id", id);
                content.put("title", "示例内容 " + id);
                content.put("type", "article");
                content.put("status", "published");
                content.put("content", "这是示例内容 " + id + " 的正文内容，包含更多详细信息...");
                content.put("excerpt", "这是示例内容 " + id + " 的摘要...");
                content.put("author", "admin");
                content.put("createdAt", new Date().toString());
                content.put("updatedAt", new Date().toString());
                content.put("publishedAt", new Date().toString());

                content.put("seoTitle", "SEO标题 " + id);
                content.put("seoDescription", "SEO描述 " + id);
                content.put("seoKeywords", "关键词1,关键词2,关键词3");
                content.put("coverImage", "https://picsum.photos/800/400");

                List<String> tags = new ArrayList<>();
                tags.add("示例");
                tags.add("测试");
                content.put("tags", tags);

                List<String> categories = new ArrayList<>();
                categories.add("未分类");
                content.put("categories", categories);

                return content;
            };

            // 使用带互斥锁的缓存查询，防止缓存击穿
            Map<String, Object> content = cacheProtectionUtil.queryWithMutex(
                    cacheKey,
                    lockKey,
                    CacheConstants.MEDIUM_EXPIRE_TIME,
                    dbFallback);

            return Result.success(content);
        } catch (Exception e) {
            log.error("获取内容详情失败", e);
            return Result.error("获取内容详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建内容
     */
    @PostMapping("")
    @Operation(summary = "创建内容")
    public Result<Map<String, Object>> createContent(@RequestBody Map<String, Object> content) {
        try {
            log.info("创建内容：{}", content);

            Map<String, Object> createdContent = new HashMap<>(content);
            createdContent.put("id", "content-" + UUID.randomUUID().toString().substring(0, 8));
            createdContent.put("createdAt", new Date().toString());
            createdContent.put("updatedAt", new Date().toString());

            if ("published".equals(content.get("status"))) {
                createdContent.put("publishedAt", new Date().toString());
            } else if ("scheduled".equals(content.get("status"))) {
                createdContent.put("scheduledAt", content.get("scheduledAt"));
            }

            // 清除相关缓存
            clearContentListCache();

            return Result.success(createdContent);
        } catch (Exception e) {
            log.error("创建内容失败", e);
            return Result.error("创建内容失败: " + e.getMessage());
        }
    }

    /**
     * 更新内容
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新内容")
    public Result<Map<String, Object>> updateContent(
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> content) {
        try {
            log.info("更新内容，ID：{}，内容：{}", id, content);

            Map<String, Object> updatedContent = new HashMap<>(content);
            updatedContent.put("id", id);
            updatedContent.put("updatedAt", new Date().toString());

            if ("published".equals(content.get("status")) && content.get("publishedAt") == null) {
                updatedContent.put("publishedAt", new Date().toString());
            }

            // 清除相关缓存
            clearContentCache(id);
            clearContentListCache();

            return Result.success(updatedContent);
        } catch (Exception e) {
            log.error("更新内容失败", e);
            return Result.error("更新内容失败: " + e.getMessage());
        }
    }

    /**
     * 删除内容
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除内容")
    public Result<Boolean> deleteContent(@PathVariable("id") String id) {
        try {
            log.info("删除内容，ID：{}", id);

            // 清除相关缓存
            clearContentCache(id);
            clearContentListCache();

            return Result.success(true);
        } catch (Exception e) {
            log.error("删除内容失败", e);
            return Result.error("删除内容失败: " + e.getMessage());
        }
    }

    /**
     * 获取媒体资源列表
     */
    @GetMapping("/media")
    @Operation(summary = "获取媒体资源列表")
    public Result<Map<String, Object>> getMediaList(
            @RequestParam(value = "page", defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "20") @Parameter(description = "每页条数") Integer pageSize,
            @RequestParam(value = "type", required = false) @Parameter(description = "媒体类型") String type) {
        try {
            log.info("获取媒体资源列表，页码：{}，每页条数：{}，类型：{}", page, pageSize, type);

            // 构建缓存键
            StringBuilder cacheKey = new StringBuilder(CacheConstants.CONTENT_KEY_PREFIX);
            cacheKey.append("media:")
                    .append("page_").append(page)
                    .append("_size_").append(pageSize);

            if (type != null && !type.isEmpty()) {
                cacheKey.append("_type_").append(type);
            }

            // 使用缓存保护工具获取数据
            Callable<Map<String, Object>> dbFallback = () -> {
                // 模拟从数据库获取数据
                Map<String, Object> result = new HashMap<>();
                List<Map<String, Object>> mediaList = new ArrayList<>();

                // 模拟数据
                for (int i = 0; i < pageSize; i++) {
                    Map<String, Object> media = new HashMap<>();
                    media.put("id", "media-" + (i + 1));
                    media.put("name", "image-" + (i + 1) + ".jpg");
                    media.put("url", "https://picsum.photos/id/" + ((i + 1) * 10) + "/200/200");
                    media.put("type", "image");
                    media.put("mimeType", "image/jpeg");
                    media.put("size", 1024 * (i + 1));

                    Map<String, Object> dimensions = new HashMap<>();
                    dimensions.put("width", 200);
                    dimensions.put("height", 200);
                    media.put("dimensions", dimensions);

                    media.put("createdAt", new Date().toString());
                    media.put("updatedAt", new Date().toString());

                    List<String> tags = new ArrayList<>();
                    tags.add("示例");
                    media.put("tags", tags);

                    media.put("folder", "默认");

                    mediaList.add(media);
                }

                result.put("list", mediaList);
                result.put("total", 100);
                result.put("page", page);
                result.put("pageSize", pageSize);

                return result;
            };

            // 使用缓存保护查询，缓存时间30分钟
            Map<String, Object> result = cacheProtectionUtil.queryWithProtection(
                    cacheKey.toString(),
                    CacheConstants.MEDIUM_EXPIRE_TIME,
                    dbFallback);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取媒体资源列表失败", e);
            return Result.error("获取媒体资源列表失败: " + e.getMessage());
        }
    }

    /**
     * 上传媒体资源
     */
    @PostMapping("/media/upload")
    @Operation(summary = "上传媒体资源")
    public Result<Map<String, Object>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "tags", required = false) String tags) {
        try {
            log.info("上传媒体资源，文件名：{}，文件夹：{}，标签：{}", file.getOriginalFilename(), folder, tags);

            Map<String, Object> media = new HashMap<>();
            media.put("id", "media-" + UUID.randomUUID().toString().substring(0, 8));
            media.put("name", file.getOriginalFilename());
            media.put("url", "https://picsum.photos/200/200"); // 模拟URL
            media.put("type", "image");
            media.put("mimeType", file.getContentType());
            media.put("size", file.getSize());

            Map<String, Object> dimensions = new HashMap<>();
            dimensions.put("width", 200);
            dimensions.put("height", 200);
            media.put("dimensions", dimensions);

            media.put("createdAt", new Date().toString());
            media.put("updatedAt", new Date().toString());

            List<String> tagList = new ArrayList<>();
            if (tags != null && !tags.isEmpty()) {
                tagList.addAll(Arrays.asList(tags.split(",")));
            }
            media.put("tags", tagList);

            media.put("folder", folder != null ? folder : "默认");

            // 清除媒体列表缓存
            clearMediaListCache();

            return Result.success(media);
        } catch (Exception e) {
            log.error("上传媒体资源失败", e);
            return Result.error("上传媒体资源失败: " + e.getMessage());
        }
    }

    /**
     * 获取模板列表
     */
    @GetMapping("/templates")
    @Operation(summary = "获取模板列表")
    public Result<List<Map<String, Object>>> getTemplateList(
            @RequestParam(value = "type", required = false) @Parameter(description = "模板类型") String type) {
        try {
            log.info("获取模板列表，类型：{}", type);

            // 构建缓存键
            String cacheKey = CacheConstants.CONTENT_KEY_PREFIX + "templates";
            if (type != null && !type.isEmpty()) {
                cacheKey += ":" + type;
            }

            // 使用缓存保护工具获取数据
            Callable<List<Map<String, Object>>> dbFallback = () -> {
                // 模拟从数据库获取数据
                List<Map<String, Object>> templates = new ArrayList<>();

                // 模拟数据
                Map<String, Object> template1 = new HashMap<>();
                template1.put("id", "tpl-1");
                template1.put("name", "默认邮件模板");
                template1.put("type", "email");
                template1.put("content", "<div>{{content}}</div>");
                template1.put("createdAt", new Date().toString());
                template1.put("updatedAt", new Date().toString());
                template1.put("description", "默认邮件模板");
                template1.put("variables", Arrays.asList("content", "user_name"));
                template1.put("isDefault", true);
                templates.add(template1);

                Map<String, Object> template2 = new HashMap<>();
                template2.put("id", "tpl-2");
                template2.put("name", "默认短信模板");
                template2.put("type", "sms");
                template2.put("content", "{{content}}");
                template2.put("createdAt", new Date().toString());
                template2.put("updatedAt", new Date().toString());
                template2.put("description", "默认短信模板");
                template2.put("variables", Collections.singletonList("content"));
                template2.put("isDefault", true);
                templates.add(template2);

                Map<String, Object> template3 = new HashMap<>();
                template3.put("id", "tpl-3");
                template3.put("name", "默认页面模板");
                template3.put("type", "page");
                template3.put("content", "<div class=\"page\">{{content}}</div>");
                template3.put("createdAt", new Date().toString());
                template3.put("updatedAt", new Date().toString());
                template3.put("description", "默认页面模板");
                template3.put("variables", Arrays.asList("content", "title"));
                template3.put("isDefault", true);
                templates.add(template3);

                if (type != null && !type.isEmpty()) {
                    // 如果指定了类型，则过滤
                    templates = templates.stream()
                            .filter(t -> type.equals(t.get("type")))
                            .collect(java.util.stream.Collectors.toList());
                }

                return templates;
            };

            // 使用缓存保护查询，缓存时间较长（12小时），因为模板不常变化
            List<Map<String, Object>> templates = cacheProtectionUtil.queryWithProtection(
                    cacheKey,
                    CacheConstants.LONG_EXPIRE_TIME / 2, // 12小时
                    dbFallback);

            return Result.success(templates);
        } catch (Exception e) {
            log.error("获取模板列表失败", e);
            return Result.error("获取模板列表失败: " + e.getMessage());
        }
    }

    /**
     * SEO分析
     */
    @PostMapping("/seo/analyze")
    @Operation(summary = "SEO分析")
    public Result<Map<String, Object>> analyzeSeo(@RequestBody Map<String, Object> data) {
        try {
            log.info("SEO分析，数据：{}", data);

            Map<String, Object> content = (Map<String, Object>) data.get("content");
            String keyword = (String) data.get("keyword");

            // SEO分析不缓存，因为每次分析可能都不同
            Map<String, Object> result = new HashMap<>();
            result.put("score", 75);

            // 标题分析
            Map<String, Object> title = new HashMap<>();
            title.put("score", 80);
            title.put("length", ((String) content.get("title")).length());
            title.put("containsKeyword", ((String) content.get("title")).contains(keyword));
            title.put("suggestions", Collections.singletonList("尝试在标题中包含关键词"));
            result.put("title", title);

            // 描述分析
            Map<String, Object> description = new HashMap<>();
            description.put("score", 70);
            description.put("length",
                    content.get("seoDescription") != null ? ((String) content.get("seoDescription")).length() : 0);
            description.put("containsKeyword", content.get("seoDescription") != null
                    && ((String) content.get("seoDescription")).contains(keyword));
            description.put("suggestions", Collections.singletonList("描述长度应在50-160字符之间"));
            result.put("description", description);

            // 内容分析
            Map<String, Object> contentAnalysis = new HashMap<>();
            contentAnalysis.put("score", 65);
            contentAnalysis.put("wordCount",
                    content.get("content") != null ? ((String) content.get("content")).length() : 0);
            contentAnalysis.put("keywordDensity", 1.5);
            contentAnalysis.put("readability", 70);
            contentAnalysis.put("suggestions", Arrays.asList("增加内容长度", "适当增加关键词密度"));
            result.put("content", contentAnalysis);

            // 标题结构分析
            Map<String, Object> headings = new HashMap<>();
            headings.put("score", 60);
            headings.put("h1Count", 1);
            headings.put("h2Count", 3);
            headings.put("h3Count", 5);
            headings.put("containsKeyword", true);
            headings.put("suggestions", Collections.singletonList("确保H1标签包含关键词"));
            result.put("headings", headings);

            // 链接分析
            Map<String, Object> links = new HashMap<>();
            links.put("score", 50);
            links.put("internalCount", 2);
            links.put("externalCount", 1);
            links.put("suggestions", Collections.singletonList("增加内部链接数量"));
            result.put("links", links);

            // 图片分析
            Map<String, Object> images = new HashMap<>();
            images.put("score", 90);
            images.put("count", 5);
            images.put("withAlt", 5);
            images.put("suggestions", Collections.emptyList());
            result.put("images", images);

            return Result.success(result);
        } catch (Exception e) {
            log.error("SEO分析失败", e);
            return Result.error("SEO分析失败: " + e.getMessage());
        }
    }

    /**
     * 获取关键词建议
     */
    @GetMapping("/seo/keyword-suggestions")
    @Operation(summary = "获取关键词建议")
    public Result<List<String>> getKeywordSuggestions(
            @RequestParam("keyword") @Parameter(description = "关键词") String keyword) {
        try {
            log.info("获取关键词建议，关键词：{}", keyword);

            // 构建缓存键
            String cacheKey = CacheConstants.CONTENT_KEY_PREFIX + "seo:keyword:" + keyword;

            // 使用缓存保护工具获取数据
            Callable<List<String>> dbFallback = () -> {
                // 模拟从数据库获取数据
                List<String> suggestions = new ArrayList<>();
                suggestions.add(keyword + " 优惠");
                suggestions.add(keyword + " 推荐");
                suggestions.add(keyword + " 价格");
                suggestions.add(keyword + " 品牌");
                suggestions.add(keyword + " 评测");

                return suggestions;
            };

            // 使用缓存保护查询，缓存时间1天，因为关键词建议变化不频繁
            List<String> suggestions = cacheProtectionUtil.queryWithProtection(
                    cacheKey,
                    CacheConstants.LONG_EXPIRE_TIME,
                    dbFallback);

            return Result.success(suggestions);
        } catch (Exception e) {
            log.error("获取关键词建议失败", e);
            return Result.error("获取关键词建议失败: " + e.getMessage());
        }
    }

    /**
     * 清除内容缓存
     * 管理员用于手动刷新缓存数据
     */
    @PostMapping("/cache/clear")
    @Operation(summary = "清除内容缓存")
    public Result<Boolean> clearCache(
            @RequestParam(value = "type", required = false) @Parameter(description = "缓存类型") String type) {
        try {
            String pattern;
            if (type != null && !type.isEmpty()) {
                pattern = CacheConstants.CONTENT_KEY_PREFIX + type + "*";
            } else {
                pattern = CacheConstants.CONTENT_KEY_PREFIX + "*";
            }

            // 使用scan命令清除匹配的缓存
            long count = redisUtil.deleteByScan(pattern);

            log.info("清除内容缓存成功，共清除{}个键", count);
            return Result.success(true);
        } catch (Exception e) {
            log.error("清除内容缓存失败", e);
            return Result.error("清除内容缓存失败: " + e.getMessage());
        }
    }

    // ============================== 私有辅助方法 ==============================

    /**
     * 清除特定内容的缓存
     */
    private void clearContentCache(String contentId) {
        String cacheKey = CacheConstants.CONTENT_DETAIL_KEY + contentId;
        redisUtil.del(cacheKey);
        log.debug("清除内容详情缓存：{}", cacheKey);
    }

    /**
     * 清除内容列表缓存
     */
    private void clearContentListCache() {
        // 清除所有内容列表缓存
        Set<String> keys = redisUtil.keys(CacheConstants.CONTENT_LIST_KEY + "*");
        if (keys != null && !keys.isEmpty()) {
            redisUtil.del(keys.toArray(new String[0]));
            log.debug("清除内容列表缓存，共{}个键", keys.size());
        }
    }

    /**
     * 清除媒体列表缓存
     */
    private void clearMediaListCache() {
        // 清除所有媒体列表缓存
        Set<String> keys = redisUtil.keys(CacheConstants.CONTENT_KEY_PREFIX + "media:*");
        if (keys != null && !keys.isEmpty()) {
            redisUtil.del(keys.toArray(new String[0]));
            log.debug("清除媒体列表缓存，共{}个键", keys.size());
        }
    }
}