# 医药售卖系统 (Medicine Sales System)

基于 **Spring Boot 3.x + MyBatis-Plus + React 18 + Ant Design Pro** 构建的智能医药电商系统，提供药品管理、库存追踪、订单交易、购物车、支付与
AI 智能客服等功能。
系统包含 **Admin 后台管理端** 与 **Client 用户端** 两个模块，实现完整的医药电商闭环。

---

## 核心功能

* **药品管理**：支持药品分类、规格、图片、价格策略
* **库存批次**：按批次号、有效期、仓库位置进行库存追踪
* **订单系统**：支持下单、支付、发货、退货、退款
* **购物车功能**：用户可添加、修改、删除商品
* **供应商管理**：记录药品采购来源及入库信息
* **库存流水**：所有出入库操作均可追溯
* **智能客服**：集成大语言模型，实现用户问答与业务辅助

---

## ⚙️ 技术架构

| 层级 | 技术栈                                                  |
|----|------------------------------------------------------|
| 后端 | Spring Boot 3.x · MyBatis-Plus · Redis · JWT · MySQL |
| 前端 | React 18 · Ant Design Pro · TailwindCSS              |
| 通讯 | RESTful API · JSON                                   |
| 其他 | Lombok · Validation · Docker · Swagger OpenAPI       |

---

## 模块说明

```
medicine-admin     # 管理后台（商品、库存、订单、用户管理）
medicine-client    # 客户端（商城端，商品展示、下单、支付）
medicine-common    # 通用模块（实体类、枚举、工具）
medicine-ai        # 智能客服模块（LLM 集成）
```

---

## 数据库设计

核心表结构包括：

* `medicine_category` —— 药品分类
* `medicine` —— 药品基础信息
* `medicine_stock` —— 库存批次管理
* `medicine_price` —— 不同价格策略
* `inventory_log` —— 库存变动流水
* `supplier` —— 供应商信息
* `order` / `order_item` —— 订单与明细

---

## 快速启动

### 后端

```bash
cd medicine-admin
mvn clean install
mvn spring-boot:run
```

### 前端

```bash
cd medicine-client
pnpm install
pnpm dev
```

默认访问：

* 管理端：[http://localhost:8080](http://localhost:8080)
* 客户端：[http://localhost:3000](http://localhost:3000)

---

## 环境依赖

* JDK 21+
* MySQL 8+
* Node.js 18+
* Redis 6+
* Maven 3.9+

---

## 权限与安全

* JWT 鉴权机制
* Spring Security 6
* 数据分级权限控制
* 审计与日志追踪

---

## 智能模块

系统可集成 LLM（如 OpenAI、DeepSeek、Ollama 等）
用于客服问答、商品推荐、常见问题自动回复等。

---
