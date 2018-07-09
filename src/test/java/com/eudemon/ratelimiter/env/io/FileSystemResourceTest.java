package com.eudemon.ratelimiter.env.io;

import org.testng.annotations.Test;

import com.eudemon.ratelimiter.env.io.FileSystemResource;
import com.eudemon.ratelimiter.env.io.Resource;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Test
public class FileSystemResourceTest {

  private static final String OK_PATH = "src/test/resources/ratelimiter-env.yaml";

  public void testGetPath() {
    Resource resource = new FileSystemResource(OK_PATH);
    String actualPath = resource.getPath();
    assertEquals(OK_PATH, actualPath);
  }

  public void testGetExtension() {
    Resource resource = new FileSystemResource(OK_PATH);
    String actualExtension = resource.getExtension();
    assertEquals("yaml", actualExtension);

    resource = new FileSystemResource("no-extension");
    actualExtension = resource.getExtension();
    assertNull(actualExtension);
  }

  public void testGetDescription() {
    Resource resource = new FileSystemResource(OK_PATH);
    String description = resource.getDescription();
    assertNotNull(description);
    assertTrue(description.length() > 0);
  }

  public void testExists() {
    Resource resource = new FileSystemResource(OK_PATH);
    boolean actualExisted = resource.exists();
    assertTrue(actualExisted);

    resource = new FileSystemResource("src/test/resources/no-exsiting.yaml");
    actualExisted = resource.exists();
    assertFalse(actualExisted);
  }

  public void testGetInputStream() {
    Resource resource = new FileSystemResource(OK_PATH);
    try {
      resource.getInputStream();
    } catch (IOException e) {
      fail();
    }
  }

  @Test(expectedExceptions = {IOException.class})
  public void testGetInputStream_withNotExistingPath() throws IOException {
    Resource resource = new FileSystemResource("no-existing.yaml");
    resource.getInputStream();
  }

}
