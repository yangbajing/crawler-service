# Crawler Service

爬虫服务

- Akka Stream & Http 1.0
- Cassandra 2.1
- Json4s 3.3

## Install

### 安装Cassandra

[http://www.yangbajing.me/2015/10/22/canssandra%E5%BC%80%E5%A7%8B/](http://www.yangbajing.me/2015/10/22/canssandra%E5%BC%80%E5%A7%8B/)

### 配置

1. `util/src/main/resources/reference.conf`: 默认配置
2. `app/src/main/resources/application.conf`: 产品配置

具体使用说明请参考：[https://github.com/typesafehub/config](https://github.com/typesafehub/config)`

### 编译

```
./sbt app/assembly
```

### 运行

```
java -jar app/target/scala-2.11/crawler-app.jar
```

