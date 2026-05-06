# DDD 领域驱动设计 + 经典四层架构 + AI 能力独立分层

## 项目结构

com.knowpivot.server
│
├─ interfaces # API层（对前端）
│ ├─ api
│ └─ vo
│
├─ application # 用例编排层
│ ├─ service
│ └─ dto
│
├─ domain # 业务核心
│ ├─ knowledge
│ ├─ document
│ ├─ conversation
│
├─ ai # ⭐ AI接入层（重点变化）
│ ├─ gateway # AI网关（统一入口）
│ ├─ client # 调Python
│ ├─ model # AI请求/响应模型
│ └─ strategy # 多AI策略（预留）
│
├─ infrastructure # 技术实现
│ ├─ persistence
│ ├─ storage
│ └─ cache
│
└─ common
└─ KnowPivotServerApplication.java

## 调用关系

前端请求
$\longrightarrow$
interfaces/Controller （接收请求）
$\longrightarrow$
application/Service （编排流程）
$\longrightarrow$
domain/Service （真正业务逻辑）
$\longrightarrow$
infrastructure/Mapper （操作数据库）

## 数据结构存放

- dto 放 application 层，vo 放 interfaces 层，model（业务模型） 放 domain层， entity 放 infrastructure层

## 「OrderAppService」VS「OrderDomainService」示例

```
AppService：只管「先后顺序、组合调用、事务、参数组装、对象转换」
不写任何业务判断、不写计算公式

DomainService：只管「业务规则、判断、计算、核心校验」
不管流程、不管数据库、不管前端入参
```

### 业务例子

下单逻辑：

1. 根据商品 id 查商品
2. 判断库存是否充足
3. 计算订单总价
4. 生成订单
5. 扣减库存

```java title="OrderAppService.java"
// 应用层：application/service
@Service
@Transactional
public class OrderAppService {

    // 注入多个领域服务，只负责调用
    private final GoodsDomainService goodsDomainService;
    private final OrderDomainService orderDomainService;

    // 下单【整体流程】
    public void placeOrder(Long goodsId, Integer buyCount) {
        // 步骤1：调用领域服务，获取商品（纯粹调用）
        Goods goods = goodsDomainService.getGoods(goodsId);

        // 步骤2：调用领域层的业务规则：校验库存（只调用，不自己写判断）
        goodsDomainService.checkStock(goods, buyCount);

        // 步骤3：调用领域服务，生成订单（只调用）
        Order order = orderDomainService.createOrder(goods, buyCount);

        // 步骤4：调用领域服务，扣减库存（只调用）
        goodsDomainService.deductStock(goodsId, buyCount);

        // 步骤5：保存订单（调用基础设施仓储）
        orderRepository.save(order);
    }
}
```

```java
// 领域层：domain/service
@Service
public class GoodsDomainService {

    private final GoodsRepository goodsRepository;

    // 业务：查询商品 + 【业务校验】商品是否存在
    public Goods getGoods(Long goodsId) {
        GoodsPO po = goodsRepository.selectById(goodsId);
        // 业务规则：商品不存在抛异常
        if (po == null) {
            throw new RuntimeException("商品不存在");
        }
        // PO转领域对象
        return GoodsConverter.toDomain(po);
    }

    // 【核心业务规则】库存校验
    public void checkStock(Goods goods, Integer buyCount) {
        // 纯业务判断 → 写在Domain
        if (goods.getStock() < buyCount) {
            throw new RuntimeException("库存不足，无法下单");
        }
        if (buyCount <= 0) {
            throw new RuntimeException("购买数量非法");
        }
    }

    // 业务：扣减库存
    public void deductStock(Long goodsId, Integer buyCount) {
        goodsRepository.updateStock(goodsId, buyCount);
    }
}
```

```java
// 领域层：domain/service
@Service
public class OrderDomainService {

    // 【核心业务逻辑】计算价格、构建订单
    public Order createOrder(Goods goods, Integer buyCount) {
        Order order = new Order();
        order.setGoodsId(goods.getId());
        order.setBuyCount(buyCount);

        // 【业务计算逻辑】金额计算 → 写在Domain
        BigDecimal totalPrice = goods.getPrice()
                .multiply(new BigDecimal(buyCount));
        order.setTotalPrice(totalPrice);

        return order;
    }
}
```
