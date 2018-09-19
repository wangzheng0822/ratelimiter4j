# Ratelimiter4j 
### Features
RateLimiter是一个高度容错，低延迟，高性能的限流开发库/框架，提供了对HTTP接口的访问限流功能。其特点：
* 提供了各种灵活的配置方式，同时支持零配置纯编程使用模式
* 支持yaml, json等多种限流规则配置格式
* 支持本地文件配置限流规则或者zookeeper集中配置方式
* 使用SPI插件式开发模式，支持自定义限流规则格式，限流算法等
* 提供了基于内存的单机限流算法和基于Redis的分布式限流算法
* 高度容错，限流框架的任何异常都不会影响业务接口
* 低延迟，限流框架较小影响业务接口响应时间
* 支持不同粒度的接口限流，支持接口中包含restful url template variables
* 灵活的集成方式，方便集成spring开发框架

### Prerequisite
RateLimit4J需要Java 8及以上版本

### Building
```shell
$ git clone https://github.com/wangzheng0822/ratelimiter4j
$ cd ratelimiter4j/
$ gradle build
```

### Quickstart

基于内存的限流，如果不需要特殊配置，使用起来非常简单，如下几行代码即可：
```java
UrlRateLimiter ratelimiter = new MemoryUrlRateLimiter();
try {
  ratelimiter.limit("app-1", "http://www.eudemon.com/v1/user/12345");
  System.out.println("passed");
} catch (OverloadException e) {
  // business logic
} catch (InvalidUrlException e) {
  // business logic
} catch (InternalErrorException e) {
  // business logic
}
```
限流规则配置，放置在classpath下面：ratelimiter-rule.yaml
```yaml
configs:
- appId: app-1
  limits:
  - api: /v1/user
    limit: 100
  - api: /v1/order
    limit: 50
- appId: app-2
  limits:
  - api: /v1/user
    limit: 50
  - api: /v1/order
    limit: 50
```

基于Redis的分布式限流，如果不需要特殊设置，只需要简单配置一下Redis地址，如下：
```java
System.setProperty("ratelimiter.redis.address", "127.0.0.1:6379");
UrlRateLimiter ratelimiter = new DistributedUrlRateLimiter();
try {
  ratelimiter.limit("app-1", "http://www.eudemon.com/v1/user/12345");
  System.out.println("passed");
} catch (OverloadException e) {
  // business logic
} catch (InvalidUrlException e) {
  // business logic
} catch (InternalErrorException e) {
  // business logic
}
```
限流规则配置，放置在classpath下面：ratelimiter-rule.yaml，同上面的相同

### Documentation
作者还写了一篇科普文章，发表于Infoq的架构公众号(聊聊架构)：[微服务接口限流的设计与思考](https://mp.weixin.qq.com/s?__biz=MzIwMzg1ODcwMw==&mid=2247488188&idx=1&sn=9e77a94b271909fecba136baab66a722&chksm=96c9a4dca1be2dcae2b1780cefaf22a6b6db4ecbe46357cf9e66dd3635db31bd3fea29414c36#rd)

更加详细配置及其使用方法及其examples，请参看[User Guide开发手册](https://github.com/wangzheng0822/ratelimiter/wiki/1.-User-Guide%E5%BC%80%E5%8F%91%E6%89%8B%E5%86%8C "User Guide开发手册")
 
使用前请先确认性能是否满足应用场景，请务必阅读[Benchmark性能测试报告](https://github.com/wangzheng0822/ratelimiter/wiki/2.-Benchmark%E6%80%A7%E8%83%BD%E6%B5%8B%E8%AF%95%E6%8A%A5%E5%91%8A)

必要情况下请参照测试文档和benchmark code自行测试[RateLimiterBenchmarkTest.java](https://github.com/wangzheng0822/ratelimiter/blob/master/src/test/java/com/eudemon/ratelimiter/benchmark/RateLimiterBenchmarkTest.java)


### Roadmap
<table>
  <tr>
    <td>功能</td>
    <td>优先级</td>
    <td>进度</td>
  </tr>
  <tr>
    <td>分布式限流算法支持Redis cluster & sharding</td>
    <td>p1</td>
    <td>未开发</td>
  </tr>
  <tr>
    <td>支持limit接口超时参数</td>
    <td>p1</td>
    <td>未开发</td>
  </tr>
  <tr>
    <td>限流规则支持指定time unit</td>
    <td>p1</td>
    <td>in progress</td>
  </tr>
  <tr>
    <td>支持不区分app ID的限流模式</td>
    <td>p1</td>
    <td>in progress</td>
  </tr>
  <tr>
    <td>支持线程并发限制</td>
    <td>p1</td>
    <td>未开发</td>
  </tr>
  <tr>
    <td>限流规则支持GET/POST等http schema</td>
    <td>p1</td>
    <td>in progress</td>
  </tr>
  <tr>
    <td>分布式限流算法性能优化</td>
    <td>p1</td>
    <td>未开发</td>
  </tr>
  <tr>
    <td>支持黑白名单和鉴权功能</td>
    <td>p1</td>
    <td>未开发</td>
  </tr>
  <tr>
    <td>支持更多的限流算法</td>
    <td>p1</td>
    <td>未开发</td>
  </tr>
  <tr>
    <td>支持更加通用的限流：db,业务,dubbo等</td>
    <td>p1</td>
    <td>未开发</td>
  </tr>
  <tr>
    <td>支持可以任意指定rule存放的位置file</td>
    <td>p1</td>
    <td>in progress</td>
  </tr>
  <tr>
    <td>添加更加完善的监控</td>
    <td>p1</td>
    <td>未开发</td>
  </tr>
</table>
