package com.eudemon.ratelimiter.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.error.YAMLException;

import com.eudemon.ratelimiter.exception.ConfigurationResolveException;
import com.eudemon.ratelimiter.utils.YamlUtils;

public class YamlUtilsTest {

  private static final String VALID_YAML_STRING_1 = "persons:\n" +
      "- name: zheng\n" +
      "  age: 20\n" +
      "  male: true\n" +
      "- name: xiaoli\n" +
      "  age: 19\n" +
      "  male: false";

  private static final String VALID_YAML_STRING_2 = "persons:\n" +
      "- name: zheng\n" +
      "  age: 20\n" +
      "- name: xiaoli\n" +
      "  age: 19\n" +
      "  male: false";

  private static final String INVALID_YAML_STRING_1 = "persons:\n" +
      "- name: zheng\n" +
      "  age: 20\n" +
      "  male: true\n" +
      "  other: other-v\n" +
      "- name: xiaoli\n" +
      "  age: 19\n" +
      "  male: false";

  @Test
  public void testParse_withString() {
    MappingClass4Test result =
        YamlUtils.parse(VALID_YAML_STRING_1, MappingClass4Test.class);
    Assert.assertNotNull(result);
    print(result);

    result = YamlUtils.parse(VALID_YAML_STRING_2, MappingClass4Test.class);
    Assert.assertNotNull(result);
    print(result);
  }

  @Test
  public void testParse_withEmptyString() {
    MappingClass4Test result =
        YamlUtils.parse("", MappingClass4Test.class);
    Assert.assertNull(result);

    result = YamlUtils.parse((String) null, MappingClass4Test.class);
    Assert.assertNull(result);
  }

  @Test(expectedExceptions = {ConfigurationResolveException.class})
  public void testParse_withInvalidString() throws YAMLException {
    YamlUtils.parse(INVALID_YAML_STRING_1, MappingClass4Test.class);
  }

  @Test
  public void testParse_withInputStream() {
    InputStream inpustream = new ByteArrayInputStream(VALID_YAML_STRING_1.getBytes());
    MappingClass4Test result = YamlUtils.parse(inpustream, MappingClass4Test.class);
    Assert.assertNotNull(result);
    print(result);

    inpustream = new ByteArrayInputStream(VALID_YAML_STRING_2.getBytes());
    result = YamlUtils.parse(inpustream, MappingClass4Test.class);
    Assert.assertNotNull(result);
    print(result);
  }

  @Test(expectedExceptions = {ConfigurationResolveException.class})
  public void testParse_withInvalidInputStream() throws YAMLException {
    InputStream inpustream = new ByteArrayInputStream(INVALID_YAML_STRING_1.getBytes());
    YamlUtils.parse(inpustream, MappingClass4Test.class);
  }

  private void print(MappingClass4Test yamlMappingClass4Test) {
    for (MappingClass4Test.MappingClassUnit4Test unit : yamlMappingClass4Test.getPersons()) {
      System.out.println(unit.toString());
    }
    System.out.println();
  }

}
