# plugin-equipment

Halo 2.0 的装备管理插件, 支持在 Console 进行管理以及为主题端提供 `/equipments` 页面路由。

插件由 [图库管理](https://www.halo.run/store/apps/app-BmQJW) 插件 改编而来

## 使用方式
1. 安装，插件安装和更新方式可参考：<https://docs.halo.run/user-guide/plugins>
2. 安装完成之后，访问 Console 左侧的**装备**菜单项，即可进行管理。
3. 内置模板，无需主题支持，但也可以通过主题自定义模板。
4. 前台访问地址为 `/equipments`，主题提供模板（equipments.html）。

## 评论适配

主题开发者可以参考 [自定义标签](https://docs.halo.run/developer-guide/theme/template-tag/#halocomment)，来为装备页面接入评论功能。

### 参数值
group：plugin.halo.run

kind: Plugin

name: ${pluginName}

### 示例

```html
<div th:if="${haloCommentEnabled}">
    <halo:comment group="equipment.kunkunyu.com"
      kind="EquipmentPage" th:name="${equipmentName}"
    />
</div>
```

## 使用文档

https://docs.kunkunyu.com/docs/equipment

## 交流群
* 添加企业微信 （备注进群）
  <img width="360" src="https://api.minio.yyds.pink/kunkunyu/files/2025/02/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20250212142105-pbceif.jpg" />

* QQ群
  <img width="360" src="https://api.minio.yyds.pink/kunkunyu/files/2025/05/qq-708998089-iqowsh.webp" />

## 开发环境

```bash
git clone git@github.com:chengzhongxue/plugin-equipment.git

# 或者当你 fork 之后

git clone git@github.com:{your_github_id}/plugin-equipment.git
```

```bash
cd path/to/plugin-equipment
```

```bash
# macOS / Linux
./gradlew pnpmInstall

# Windows
./gradlew.bat pnpmInstall
```

```bash
# macOS / Linux
./gradlew build

# Windows
./gradlew.bat build
```

修改 Halo 配置文件：

```yaml
halo:
  plugin:
    runtime-mode: development
    classes-directories:
      - "build/classes"
      - "build/resources"
    lib-directories:
      - "libs"
    fixedPluginPath:
      - "/path/to/plugin-equipment"
```
