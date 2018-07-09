package com.eudemon.ratelimiter.rule;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.eudemon.ratelimiter.exception.InvalidUrlException;
import com.eudemon.ratelimiter.rule.ApiLimit;
import com.eudemon.ratelimiter.rule.AppUrlRateLimitRule;

@Test
public class AppUrlRateLimitRuleTest {

  private static ApiLimit l1 = new ApiLimit("/", 100, 20);
  private static ApiLimit l2 = new ApiLimit("/user", 90, 10);
  private static ApiLimit l3 = new ApiLimit("/user/lender", 80, 10);
  private static ApiLimit l4 = new ApiLimit("/user/borrower/lpd", 70, 10);
  private static ApiLimit l5 = new ApiLimit("/user/{username:(^[a-zA-Z]*$)}/lpd", 60, 10);
  private static ApiLimit l6 = new ApiLimit("/user/{actorId:(^[0-9]*$)}/lpd", 50, 10);
  private static ApiLimit l7 = new ApiLimit("/wallet/{walletId}", 40, 10);

  public void testAddLimitRule_and_getLimitInfo() throws InvalidUrlException {
    AppUrlRateLimitRule appUrlRateLimitRule = new AppUrlRateLimitRule();
    appUrlRateLimitRule.addLimitInfo(l1);
    appUrlRateLimitRule.addLimitInfo(l2);
    appUrlRateLimitRule.addLimitInfo(l3);
    appUrlRateLimitRule.addLimitInfo(l4);
    appUrlRateLimitRule.addLimitInfo(l5);
    appUrlRateLimitRule.addLimitInfo(l6);
    appUrlRateLimitRule.addLimitInfo(l7);

    ApiLimit actualInfo = appUrlRateLimitRule.getLimitInfo("/");
    assertEqualsLimitInfo(actualInfo, l1);

    actualInfo = appUrlRateLimitRule.getLimitInfo("/user");
    assertEqualsLimitInfo(actualInfo, l2);

    actualInfo = appUrlRateLimitRule.getLimitInfo("/user/lender");
    assertEqualsLimitInfo(actualInfo, l3);

    actualInfo = appUrlRateLimitRule.getLimitInfo("/user/hello");
    assertEqualsLimitInfo(actualInfo, l2);

    actualInfo = appUrlRateLimitRule.getLimitInfo("/user/lender/what");
    assertEqualsLimitInfo(actualInfo, l3);

    actualInfo = appUrlRateLimitRule.getLimitInfo("/user/borrower/lpd");
    assertEqualsLimitInfo(actualInfo, l4);

    actualInfo = appUrlRateLimitRule.getLimitInfo("/user/borrower/hello");
    assertEqualsLimitInfo(actualInfo, l2);

    actualInfo = appUrlRateLimitRule.getLimitInfo("/user/123/lpd");
    assertEqualsLimitInfo(actualInfo, l6);

    actualInfo = appUrlRateLimitRule.getLimitInfo("/user/123");
    assertEqualsLimitInfo(actualInfo, l2);

    actualInfo = appUrlRateLimitRule.getLimitInfo("/user/123/hello");
    assertEqualsLimitInfo(actualInfo, l2);

    actualInfo = appUrlRateLimitRule.getLimitInfo("/user/hello/lpd");
    assertEqualsLimitInfo(actualInfo, l5);

    actualInfo = appUrlRateLimitRule.getLimitInfo("/user/hello/what");
    assertEqualsLimitInfo(actualInfo, l2);

    actualInfo = appUrlRateLimitRule.getLimitInfo("/wallet/123");
    assertEqualsLimitInfo(actualInfo, l7);

    actualInfo = appUrlRateLimitRule.getLimitInfo("/wallet/hello");
    assertEqualsLimitInfo(actualInfo, l1);

    actualInfo = appUrlRateLimitRule.getLimitInfo("/wallet/123/hello");
    assertEqualsLimitInfo(actualInfo, l7);
  }

  public void testAddLimitRule_and_getLimitInfo_withEmptyRule() throws InvalidUrlException {
    AppUrlRateLimitRule rule = new AppUrlRateLimitRule();
    ApiLimit info = rule.getLimitInfo("");
    Assert.assertNull(info);
    info = rule.getLimitInfo(null);
    Assert.assertNull(info);
    info = rule.getLimitInfo("/");
    Assert.assertNull(info);
    info = rule.getLimitInfo("/user/lender");
    Assert.assertNull(info);
  }

  public void testAddLimitRule_and_getLimitInfo_ruleOnlyContainsRootPath()
      throws InvalidUrlException {
    AppUrlRateLimitRule rule = new AppUrlRateLimitRule();
    rule.addLimitInfo(l1);

    ApiLimit actualInfo = rule.getLimitInfo("/");
    assertEqualsLimitInfo(actualInfo, l1);

    actualInfo = rule.getLimitInfo("/user");
    assertEqualsLimitInfo(actualInfo, l1);
  }

  public void testAddLimitRule_withDifferentOrder() throws InvalidUrlException {
    AppUrlRateLimitRule rule = new AppUrlRateLimitRule();
    rule.addLimitInfo(l3);
    rule.addLimitInfo(l2);

    ApiLimit actualInfo = rule.getLimitInfo("/");
    Assert.assertNull(actualInfo);

    actualInfo = rule.getLimitInfo("/user");
    assertEqualsLimitInfo(actualInfo, l2);

    actualInfo = rule.getLimitInfo("/user/lender");
    assertEqualsLimitInfo(actualInfo, l3);
  }

  public void testAddLimitRule_withDuplicatedLimitInfos() throws InvalidUrlException {
    AppUrlRateLimitRule rule = new AppUrlRateLimitRule();
    rule.addLimitInfo(l3);
    rule.addLimitInfo(l3);

    ApiLimit actualInfo = rule.getLimitInfo("/user/lender");
    assertEqualsLimitInfo(actualInfo, l3);
  }

  @Test(expectedExceptions = {InvalidUrlException.class})
  public void testAddLimitRule_withInvalidLimitInfo() throws InvalidUrlException {
    AppUrlRateLimitRule rule = new AppUrlRateLimitRule();
    rule.addLimitInfo(new ApiLimit("invalid url", 10, 10));
  }

  @Test(expectedExceptions = {InvalidUrlException.class})
  public void testGetLimitInfo_withInvalidUrl() throws InvalidUrlException {
    AppUrlRateLimitRule rule = new AppUrlRateLimitRule();
    rule.addLimitInfo(l1);
    rule.addLimitInfo(l2);
    rule.getLimitInfo("invalid url");
  }

  // test thread-safe
  public void testClassThreadSafe() {
    // TODO
  }

  private void assertEqualsLimitInfo(ApiLimit actualInfo, ApiLimit expectedInfo) {
    Assert.assertNotNull(actualInfo);
    Assert.assertEquals(actualInfo.getApi(), expectedInfo.getApi());
    Assert.assertEquals(actualInfo.getLimit(), expectedInfo.getLimit());
  }

}
