# Maven多模块拆分实操指南

> 目标：把现有 `travel-server` 拆成“用户平台（user-app）”和“管理平台（admin-app）”两个模块，同时仍然通过一个 Spring Boot 项目打成可运行的 Jar。本指南演示如何从零搭建父子模块、迁移代码、打包验证。所有示例基于 `com.zhly` 包名，请根据实际项目调整。

---

## 1. 准备工作

1. 在 Git 新建分支：`git checkout -b feature/split-modules`。
2. 备份当前 `pom.xml` 和关键配置。
3. 确保可以用 `mvn clean package` 正常打包，方便拆分后对比。

---

## 2. 改造父 `pom.xml`

把根目录 `pom.xml` 改为父模块：

```xml
<project ...>
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.zhly</groupId>
  <artifactId>travel-server</artifactId>
  <version>1.0.0</version>
  <packaging>pom</packaging>

  <modules>
    <module>travel-common</module>
    <module>user-app</module>
    <module>admin-app</module>
    <module>travel-boot</module>
  </modules>

  <properties>
    <java.version>17</java.version>
    <spring-boot.version>3.2.5</spring-boot.version>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>${spring-boot.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <build>
    <pluginManagement>
      <plugins>
        <plugin>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-maven-plugin</artifactId>
          <version>${spring-boot.version}</version>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>
</project>
```

> 如果暂时不需要 `travel-common`、`travel-boot`，可从 `<modules>` 中移除对应项。

---

## 3. 创建子模块骨架

在根目录新增四个文件夹：`travel-common`、`user-app`、`admin-app`、`travel-boot`。每个目录都包含一个 `pom.xml`。

### 3.1 `travel-common/pom.xml`

```xml
<project ...>
  <parent>
    <groupId>com.zhly</groupId>
    <artifactId>travel-server</artifactId>
    <version>1.0.0</version>
  </parent>
  <artifactId>travel-common</artifactId>
  <packaging>jar</packaging>

  <dependencies>
    <!-- 公共依赖示例 -->
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>com.baomidou</groupId>
      <artifactId>mybatis-plus-boot-starter</artifactId>
      <version>3.5.5</version>
    </dependency>
  </dependencies>
</project>
```

在 `travel-common/src/main/java` 放共享的实体、DTO、工具类、配置基类等。

### 3.2 `user-app/pom.xml`

```xml
<project ...>
  <parent>
    <groupId>com.zhly</groupId>
    <artifactId>travel-server</artifactId>
    <version>1.0.0</version>
  </parent>
  <artifactId>user-app</artifactId>
  <packaging>jar</packaging>

  <dependencies>
    <dependency>
      <groupId>com.zhly</groupId>
      <artifactId>travel-common</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- 其它用户端特有依赖 -->
  </dependencies>
</project>
```

目录结构示例：

```
user-app/
 ├─src/main/java/com/zhly/user/...
 └─src/main/resources/（用户侧 mapper、配置）
```

### 3.3 `admin-app/pom.xml`

```xml
<project ...>
  <parent>
    <groupId>com.zhly</groupId>
    <artifactId>travel-server</artifactId>
    <version>1.0.0</version>
  </parent>
  <artifactId>admin-app</artifactId>
  <packaging>jar</packaging>

  <dependencies>
    <dependency>
      <groupId>com.zhly</groupId>
      <artifactId>travel-common</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- 管理端特有依赖 -->
  </dependencies>
</project>
```

### 3.4 `travel-boot/pom.xml`

```xml
<project ...>
  <parent>
    <groupId>com.zhly</groupId>
    <artifactId>travel-server</artifactId>
    <version>1.0.0</version>
  </parent>
  <artifactId>travel-boot</artifactId>
  <packaging>jar</packaging>

  <dependencies>
    <dependency>
      <groupId>com.zhly</groupId>
      <artifactId>user-app</artifactId>
    </dependency>
    <dependency>
      <groupId>com.zhly</groupId>
      <artifactId>admin-app</artifactId>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <executions>
          <execution>
            <goals><goal>repackage</goal></goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

在 `travel-boot/src/main/java` 新建启动类：

```java
@SpringBootApplication(scanBasePackages = "com.zhly")
@MapperScan("com.zhly.**.mapper")
public class TravelApplication {
    public static void main(String[] args) {
        SpringApplication.run(TravelApplication.class, args);
    }
}
```

---

## 4. 迁移代码与资源

1. **公共代码**（实体、DTO、utils、配置常量） → `travel-common`.
2. **用户平台**相关 Controller/Service/Mapper → `user-app`.
3. **管理平台**相关内容 → `admin-app`.
4. `application.yml` 方案：
   - `travel-boot/src/main/resources/application.yml` 作为总配置；
   - 将原用户/管理端特有配置拆成 `application-user.yml`、`application-admin.yml` 并使用 `spring.profiles` 或 `spring.config.import` 导入。
5. Mapper XML、静态资源按模块放置，记得更新 MyBatis `mapper-locations`。

迁移时建议每次处理一种功能，保证 `mvn -pl <module> clean test` 通过后再继续。

---

## 5. 构建与验证

1. **刷新 Maven**：在 IDE 中重新导入父 pom，确认四个模块都被识别。
2. **逐个模块编译**：
   ```bash
   mvn clean install -pl travel-common
   mvn clean install -pl user-app
   mvn clean install -pl admin-app
   ```
3. **聚合打包**：
   ```bash
   mvn clean package -pl travel-boot -am
   ```
   生成的可运行 Jar 位于 `travel-boot/target/travel-boot-1.0.0.jar`。
4. **运行验证**：
   ```bash
   java -jar travel-boot/target/travel-boot-1.0.0.jar
   ```
   检查用户端、管理端接口是否正常访问。

---

## 6. 常见问题与解决

| 问题 | 处理建议 |
| --- | --- |
| Bean / Mapper 扫描不到 | 检查 `@SpringBootApplication(scanBasePackages)`、`@MapperScan` 是否覆盖 `com.zhly` 全包。 |
| 配置无法读取 | 确认配置文件已移动，`spring.config.import` 或 `@PropertySource` 指向正确路径。 |
| 依赖冲突 | 在父 pom 的 `<dependencyManagement>` 统一声明版本，子模块不再重复指定。 |
| 打包后无法运行 | 确认 `spring-boot-maven-plugin` 只在 `travel-boot` 中执行 `repackage`。 |
| 子模块互相调用 | 抽取公共逻辑到 `travel-common`，避免环状依赖。 |

---

## 7. 建议的执行顺序

1. 修改父 pom 并创建四个子模块（空壳）。
2. 将公共代码迁移到 `travel-common`，保证 `mvn clean install -pl travel-common` 通过。
3. 迁移用户端代码至 `user-app`，验证 `mvn -pl user-app install`。
4. 迁移管理端代码至 `admin-app`，同样验证。
5. 在 `travel-boot` 组装依赖、添加启动类，执行 `mvn -pl travel-boot package`。
6. 完成后在新分支进行联调，确认无误再合并。

---

如在执行过程中遇到无法解决的报错，可以记录错误日志、相关模块的 `pom` 以及代码路径，随时反馈给我协助排查。祝顺利完成多模块重构！ 

