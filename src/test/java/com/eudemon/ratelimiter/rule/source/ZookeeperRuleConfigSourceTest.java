package com.eudemon.ratelimiter.rule.source;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.framework.imps.GetDataBuilderImpl;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.eudemon.ratelimiter.exception.ConfigurationResolveException;
import com.eudemon.ratelimiter.rule.parser.YamlRuleConfigParser;

@Test
public class ZookeeperRuleConfigSourceTest {

  public void testLoad() throws Exception {
    CuratorFramework client = Mockito.mock(CuratorFramework.class);
    doNothing().when(client).start();
    when(client.blockUntilConnected(anyInt(), any())).thenReturn(true);

    String ruleStr = "configs:\n" +
        "- appId: app-1\n" +
        "  limits:\n" +
        "  - unit: 50\n" +
        "    limit: 100\n" +
        "    api: v1/user\n" +
        "  - unit: 20\n" +
        "    limit: 50\n" +
        "    api: v1/order\n" +
        "- appId: app-2\n" +
        "  limits:\n" +
        "  - limit: 50\n" +
        "    api: v1/user\n" +
        "  - unit: 30\n" +
        "    limit: 50\n" +
        "    api: v1/order\n";
    GetDataBuilder dataBuilder = new GetDataBuilderImpl(null, null, null, null, false) {
      @Override
      public byte[] forPath(String path) throws Exception {
        return ruleStr.getBytes("UTF-8");
      }
    };
    when(client.getData()).thenReturn(dataBuilder);

    RuleConfigSource source =
        new ZookeeperRuleConfigSource(client, new YamlRuleConfigParser());
    UniformRuleConfigMapping mapping = source.load();
    assertNotNull(mapping);
    assertEquals(mapping.getConfigs().size(), 2);
    assertEquals(mapping.getConfigs().get(0).getAppId(), "app-1");
  }

  @Test(expectedExceptions = {ConfigurationResolveException.class})
  public void testLoad_withValidData() throws Exception {
    CuratorFramework client = Mockito.mock(CuratorFramework.class);
    doNothing().when(client).start();
    when(client.blockUntilConnected(anyInt(), any())).thenReturn(true);

    String ruleStr = "invalid string";
    GetDataBuilder dataBuilder = new GetDataBuilderImpl(null, null, null, null, false) {
      @Override
      public byte[] forPath(String path) throws Exception {
        return ruleStr.getBytes("UTF-8");
      }
    };
    when(client.getData()).thenReturn(dataBuilder);

    RuleConfigSource source = new ZookeeperRuleConfigSource(client, new YamlRuleConfigParser());
    source.load();
  }

  public void testLoad_withEmptyData() throws Exception {
    CuratorFramework client = Mockito.mock(CuratorFramework.class);
    doNothing().when(client).start();
    when(client.blockUntilConnected(anyInt(), any())).thenReturn(true);

    String ruleStr = "";
    GetDataBuilder dataBuilder = new GetDataBuilderImpl(null, null, null, null, false) {
      @Override
      public byte[] forPath(String path) throws Exception {
        return ruleStr.getBytes("UTF-8");
      }
    };
    when(client.getData()).thenReturn(dataBuilder);

    RuleConfigSource source = new ZookeeperRuleConfigSource(client, new YamlRuleConfigParser());
    UniformRuleConfigMapping mapping = source.load();
    assertNull(mapping);
    mapping = source.load();
    assertNull(mapping);
  }

  @Test(expectedExceptions = {RuntimeException.class}, expectedExceptionsMessageRegExp = ".*init.*")
  public void testLoad_withZkClientException() throws Exception {
    CuratorFramework client = Mockito.mock(CuratorFramework.class);
    doThrow(new RuntimeException()).when(client).start();

    RuleConfigSource source =
        new ZookeeperRuleConfigSource(client, new YamlRuleConfigParser());
    source.load();
  }

  @Test(expectedExceptions = {RuntimeException.class}, expectedExceptionsMessageRegExp = ".*init.*")
  public void testLoad_withZkNodeCacheException() throws Exception {
    CuratorFramework client = Mockito.mock(CuratorFramework.class);
    doNothing().when(client).start();

    RuleConfigSource source = new ZookeeperRuleConfigSource(client, new YamlRuleConfigParser());
    source.load();
  }

  @Test(expectedExceptions = {RuntimeException.class},
      expectedExceptionsMessageRegExp = ".*address.*")
  public void testLoad_withEmptyAddrOrPath() {
    RuleConfigSource source = new ZookeeperRuleConfigSource("", "", new YamlRuleConfigParser());
    source.load();
  }

}
