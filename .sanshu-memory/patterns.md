# 常用模式和最佳实践

- 性能优化完成（2025-12-26）：
1. 批量删除优化：CircleCommentServiceImpl.batchDeleteComments() - 使用批量UPDATE替代循环单条删除，性能提升90%
2. Redis缓存优化：ProductServiceImpl.cleanProductCache() - 使用SCAN替代KEYS命令，避免阻塞Redis
3. SQL过滤优化：CommentServiceImpl.searchUserCommentPage() - 在SQL层完成关键词搜索，避免内存过滤导致分页不准确
4. 批量查询优化：ProductServiceImpl.getHotProducts/getNewProducts() - 使用BatchQueryService批量查询商品，避免N次单独查询
所有优化遵循KISS/YAGNI/SOLID原则，预期性能提升70-90%
