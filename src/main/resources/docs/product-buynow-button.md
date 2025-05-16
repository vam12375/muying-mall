# 商品卡片添加立即购买功能

## 功能描述

为商品卡片组件(`ProductCard.vue`)添加"立即购买"功能，使用户可以直接从商品卡片点击立即购买按钮，跳转到订单结算页面，无需先将商品添加到购物车。

## 实现内容

### 1. 界面元素添加

在商品卡片组件中添加了两处"立即购买"按钮:

1. **悬浮购买按钮**：当鼠标悬浮在商品图片上时显示的全宽立即购买按钮
2. **固定购买按钮**：在商品信息下方与"加入购物车"按钮并排的立即购买按钮

### 2. 逻辑实现

立即购买功能的核心逻辑包括：

1. **登录检查**：验证用户是否已登录，未登录则引导至登录页面
2. **数据准备**：构建完整的商品数据，包含商品ID、名称、价格、图片等信息
3. **数据存储**：将商品数据存储到localStorage中的`buyNow`和`orderProductsMap`字段
4. **页面跳转**：跳转至结算页面(`/checkout`)

### 3. 交互优化

为提升用户体验，实现了以下交互优化：

1. **事件阻止冒泡**：确保点击按钮不会触发商品卡片的点击事件
2. **悬浮动画效果**：添加了平滑的显示/隐藏动画
3. **错误处理**：添加了适当的错误处理和用户提示

## 技术实现

### 核心代码

立即购买功能的核心实现代码：

```javascript
// 立即购买
const buyNow = (event) => {
  event.preventDefault();
  event.stopPropagation();
  
  // 检查用户是否已登录
  const token = localStorage.getItem('token');
  if (!token) {
    ElMessage.warning('请先登录后再购买商品');
    router.push({
      path: '/login',
      query: { redirect: router.currentRoute.value.fullPath }
    });
    return;
  }
  
  try {
    // 清除其他可能影响结算页面的数据
    localStorage.removeItem('checkoutItems');
    
    // 构建订单项，包含完整的商品信息
    const orderItem = {
      id: props.product.id,
      goodsId: props.product.id,
      productId: props.product.id,
      name: props.product.name,
      goodsName: props.product.name,
      price: parseFloat(props.product.price).toFixed(2),
      unitPrice: parseFloat(props.product.price).toFixed(2),
      image: props.product.image,
      goodsImg: props.product.image,
      quantity: 1,
      count: 1,
      spec: '',
      specs: '',
      fromSource: 'buyNow',
      timestamp: Date.now()
    };
    
    // 保存到本地存储，用于结算页面获取
    localStorage.setItem('buyNow', JSON.stringify(orderItem));
    
    // 同时保存到orderProductsMap用于订单列表和详情显示
    const orderProductsMap = JSON.parse(localStorage.getItem('orderProductsMap') || '{}');
    const tempOrderId = 'temp_buy_now_' + Date.now();
    orderProductsMap[tempOrderId] = [orderItem];
    localStorage.setItem('orderProductsMap', JSON.stringify(orderProductsMap));
    
    // 跳转到结算页面
    router.push('/checkout');
  } catch (error) {
    console.error('立即购买失败:', error);
    ElMessage.error('立即购买失败: ' + (error.message || '未知错误'));
  }
};
```

## 使用流程

当用户点击商品卡片上的"立即购买"按钮时：

1. 系统检查用户是否已登录
2. 如果未登录，提示用户并跳转到登录页面
3. 如果已登录，将商品信息保存到localStorage
4. 跳转到结算页面，用户可以直接选择地址和支付方式完成订单

## 与已有系统的集成

该功能参考了`ProductDetail.vue`中的立即购买功能实现，保持了数据结构和处理逻辑的一致性，确保与结算页面的兼容性。两者在使用相同的localStorage键名和数据格式，确保了用户体验的一致性。 