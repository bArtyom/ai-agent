# Agent 调用流程 - 简洁版

## 快速流程

```
1. 测试代码调用 loveApp.doChat("你好", chatId)

2. LoveApp.doChat() 创建请求:
   chatClient.prompt()
   .user(message)
   .call()

3. Advisor 链式执行:
   ① MessageChatMemoryAdvisor
      └─ 从 ChatMemory 加载历史消息
      └─ chain.nextAroundCall()
   
   ② AuthCheckAdvisor
      └─ before() 检查权限和违禁词
      └─ chain.nextAroundCall()
   
   ③ LLM (DashScope)
      └─ 接收 [历史消息 + 当前消息]
      └─ 生成响应

4. 响应层层返回
   ① AuthCheckAdvisor 返回 response
   ② MessageChatMemoryAdvisor 保存消息到 ChatMemory
   ③ 返回给用户
```

---

## 每步详细说明

### 1. Spring 注入 (初始化)

```java
@Component
public class LoveApp {
    private final ChatClient chatClient;
    
    // Spring 自动调用: ChatModel 注入
    public LoveApp(ChatModel dashscopeChatModel) {
        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }
}

// Spring 自动创建 LoveApp Bean，可注入使用
@Resource
private LoveApp loveApp;
```

### 2. 调用 doChat 方法

```java
public String doChat(String message, String chatId) {
    // ① 创建 PromptBuilder
    ChatResponse response = chatClient.prompt()
            
            // ② 设置用户消息
            .user(message)
            
            // ③ 配置 Advisor 参数
            .advisors(spec -> spec.param(
                CHAT_MEMORY_CONVERSATION_ID_KEY, chatId
            ))
            
            // ④ 执行同步调用（触发 Advisor 链）
            .call()
            
            .chatResponse();
    
    // ⑤ 提取文本
    String content = response.getResult().getOutput().getText();
    return content;
}
```

### 3. Advisor 链执行

#### Advisor 1: MessageChatMemoryAdvisor

```
作用: 管理对话历史

执行:
① aroundCall() 被触发
② 从 ChatMemory 加载历史:
   SELECT * FROM memory WHERE chatId="xxx"
③ chain.nextAroundCall() 
   → 调用下一个 Advisor
④ 得到响应后
   保存到 ChatMemory:
   INSERT INTO memory VALUES(...)
⑤ 返回响应
```

#### Advisor 2: AuthCheckAdvisor (你写的)

```
作用: 权限检查 + 违禁词检查

执行:
① before() 
   ├─ getUserId(context) → "user123"
   ├─ checkPermission("user123")
   │  └─ 检查是否被封禁
   ├─ checkBannedWords(messages)
   │  └─ 检查违禁词
   └─ logAudit() → 记录日志

② 检查通过 ✓
   chain.nextAroundCall()
   → 没有下一个 Advisor
   → 调用 LLM

③ 返回响应
```

### 4. LLM 调用

```
DashScope API 收到:
├─ System Prompt
│  "扮演恋爱心理专家..."
├─ History Messages (从 ChatMemory 加载)
│  ├─ User: "你好，我是程序员鱼皮"
│  ├─ Assistant: "很高兴认识你..."
│  └─ ...
└─ Current Message
   User: "我想让另一半更爱我"

LLM 生成响应:
"我理解你的想法..."

返回 ChatResponse
```

### 5. 响应返回

```
AuthCheckAdvisor.aroundCall() 返回
    ↑
MessageChatMemoryAdvisor 保存消息
    ↑
返回给用户: "我理解你的想法..."
```

---

## 对象关系图

```
Spring Boot 启动
    ↓
创建 ChatModel (DashScope 实现)
    ↓
创建 LoveApp:
    ├─ 创建 ChatMemory (内存存储)
    ├─ 创建 ChatClient
    │  ├─ 注册 MessageChatMemoryAdvisor
    │  └─ (AuthCheckAdvisor 由框架自动检测)
    └─ 保存为 Bean
    ↓
LoveApp 注入到测试类
    ↓
调用 doChat()
```

---

## 三轮对话示例

```
【第一轮】
Request:  "你好，我是程序员鱼皮"
History:  无
Response: "很高兴认识你！..."
Memory:   保存第一轮

【第二轮】
Request:  "我想让另一半更爱我"
History:  [第一轮的对话]
Response: "我理解你..."
Memory:   保存第二轮

【第三轮】
Request:  "我的另一半叫什么来着？"
History:  [第一轮, 第二轮的对话]
Response: "你说是编程导航。..."  ← 从历史中回忆！
Memory:   保存第三轮
```

---

## 关键流程点

| 顺序 | 发生的事 | 类/方法 |
|------|---------|---------|
| 1 | Spring 创建 ChatModel | `@Component` 自动注入 |
| 2 | Spring 创建 LoveApp | `LoveApp.constructor()` |
| 3 | 测试代码获得 LoveApp | `@Resource private LoveApp` |
| 4 | 调用方法 | `loveApp.doChat(msg, chatId)` |
| 5 | 创建请求 | `ChatClient.prompt()` |
| 6 | 加载历史 | `MessageChatMemoryAdvisor.aroundCall()` |
| 7 | 权限检查 | `AuthCheckAdvisor.before()` |
| 8 | 调用 LLM | `ChatModel.call()` |
| 9 | 保存消息 | `ChatMemory.add()` |
| 10 | 返回响应 | `response.getText()` |

