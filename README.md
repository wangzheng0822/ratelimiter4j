
RateLimiter是一个限流框架，提供了对HTTP接口的访问限流功能。其特点：
* 提供了各种灵活的配置方式，同时支持零配置纯编程使用模式
* 支持yaml, json等多种限流规则配置格式
* 支持本地文件配置限流规则或者zookeeper集中配置方式
* 使用SPI插件式开发模式，方便使用者二次开发
* 提供了基于内存的单机限流和基于Redis的分布式限流功能
* 高度容错，异常并不会影响业务接口，参看user guide
* 低延迟，不影响业务接口响应时间，参看benchmark
* 支持不同粒度的接口限流，支持接口中包含restful url template variables

# Get started:
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
System.setProperty("ratelimiter.redis.address", "127.0.0.1");
System.setProperty("ratelimiter.redis.port", "6379");
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

另外，更加详细配置及其使用方法，请参看user guide:xxx
  
另外，使用前请先确认性能是否满足应用场景，所以请务必阅读benchmark性能测试报告，必要情况下请参照测试文档和benchmark code自行测试：benchmarkxxx

联系作者：提交issue或wangyifei0822@qq.com或weixin:wangzheng0822
