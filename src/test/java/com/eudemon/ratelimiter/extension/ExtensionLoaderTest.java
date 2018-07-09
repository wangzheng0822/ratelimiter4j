package com.eudemon.ratelimiter.extension;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.eudemon.ratelimiter.extension.ExtensionLoader;

import java.util.List;

@Test
public class ExtensionLoaderTest {

  public void testGetExtensionList() {
    List<InterfaceA4Test> extensions = ExtensionLoader.getExtensionList(InterfaceA4Test.class);
    Assert.assertNotNull(extensions);
    Assert.assertEquals(extensions.size(), 2);
    Assert.assertTrue(extensions.get(0) instanceof ClassA4Test);
    Assert.assertTrue(extensions.get(1) instanceof ClassB4Test);

    List<InterfaceA4Test> extensions2 = ExtensionLoader.getExtensionList(InterfaceA4Test.class);
    Assert.assertNotNull(extensions2);
    Assert.assertEquals(extensions2.size(), 2);
    Assert.assertTrue(extensions2.get(0) instanceof ClassA4Test);
    Assert.assertTrue(extensions2.get(1) instanceof ClassB4Test);

    Assert.assertSame(extensions.get(0), extensions2.get(0));
    Assert.assertSame(extensions.get(1), extensions2.get(1));
  }

  public void testGetExtensionList_returnUnSingletonObject() {
    List<InterfaceA4Test> extensions =
        ExtensionLoader.getExtensionList(InterfaceA4Test.class, false);
    Assert.assertNotNull(extensions);
    Assert.assertEquals(extensions.size(), 2);
    Assert.assertTrue(extensions.get(0) instanceof ClassA4Test);
    Assert.assertTrue(extensions.get(1) instanceof ClassB4Test);

    List<InterfaceA4Test> extensions2 =
        ExtensionLoader.getExtensionList(InterfaceA4Test.class, false);
    Assert.assertNotNull(extensions2);
    Assert.assertEquals(extensions2.size(), 2);
    Assert.assertTrue(extensions2.get(0) instanceof ClassA4Test);
    Assert.assertTrue(extensions2.get(1) instanceof ClassB4Test);

    Assert.assertNotSame(extensions.get(0), extensions2.get(0));
    Assert.assertNotSame(extensions.get(1), extensions2.get(1));
  }

  public void testGetExtension() {
    InterfaceA4Test extension = ExtensionLoader.getExtension(InterfaceA4Test.class);
    Assert.assertNotNull(extension);
    Assert.assertTrue(extension instanceof ClassA4Test);
  }

}
