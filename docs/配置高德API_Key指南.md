# 配置高德API Key指南

## 问题说明
当前天气功能无法正常使用，因为高德API Key未配置或无效。

## 解决方案

### 方法1：通过环境变量配置（推荐）

#### Windows系统：
1. 打开系统环境变量设置
2. 新建系统环境变量：
   - 变量名：`AMAP_API_KEY`
   - 变量值：你的高德API Key（例如：`a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6`）
3. 重启IDE或命令行窗口
4. 重启后端服务

#### Linux/Mac系统：
```bash
# 临时设置（当前终端会话有效）
export AMAP_API_KEY=你的高德API_Key

# 永久设置（添加到 ~/.bashrc 或 ~/.zshrc）
echo 'export AMAP_API_KEY=你的高德API_Key' >> ~/.bashrc
source ~/.bashrc
```

### 方法2：直接在配置文件中配置

编辑 `travel-server/src/main/resources/application-dev.yml` 文件：

```yaml
third-party:
  amap:
    api:
      url: https://restapi.amap.com
      key: 你的高德API_Key  # 替换这里的值
```

⚠️ **注意**：如果使用Git，建议不要将真实的API Key提交到代码仓库，应使用环境变量。

### 方法3：获取高德API Key

1. 访问高德开放平台：https://lbs.amap.com/
2. 注册/登录账号
3. 进入"控制台" -> "应用管理" -> "我的应用"
4. 创建新应用或选择已有应用
5. 添加"Web服务"类型的Key
6. 复制生成的Key

### 验证配置

配置完成后，重启后端服务，查看控制台日志：
- ✅ 如果看到"高德天气API调用成功"，说明配置正确
- ❌ 如果仍然看到"高德API Key无效"，请检查：
  1. Key是否完整复制（不要有多余空格）
  2. 环境变量是否正确设置
  3. 是否重启了后端服务

## 重要提示

### API Key类型说明

高德开放平台提供多种类型的Key：
- **Web端(JS API)**：用于前端地图显示（已配置）
- **Web服务**：用于后端API调用（天气、地理编码等）← **天气功能需要这个**

您需要创建两个Key：
1. Web端(JS API) Key：用于前端地图组件（已有：`4c1487f8f9b1c39bb406fdf78c214c76`）
2. **Web服务 Key**：用于后端天气API（**需要新建**）

创建Web服务Key时：
- ✅ 服务平台选择"Web服务"
- ✅ 确保"可使用服务"中包含"天气查询API"
- ⚠️ 如果只创建了JS API Key，天气功能无法使用

创建完成后，将新的Web服务Key更新到 `application-dev.yml` 的 `third-party.amap.api.key` 配置中。

