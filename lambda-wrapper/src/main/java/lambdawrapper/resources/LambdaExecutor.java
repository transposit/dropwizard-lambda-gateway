package lambdawrapper.resources;

import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by tina on 5/24/16.
 */
public class LambdaExecutor {
  private AWSLambdaClient client;
  private Method methodInstance;
  private Boolean dev;
  private Boolean hotLoad;
  private String methodName;
  private String lambdaName;

  public LambdaExecutor(Boolean dev, boolean hotLoad, String methodName, String lambdaName)
          throws MalformedURLException, ClassNotFoundException, NoSuchMethodException {
    this.dev = dev;
    this.hotLoad = hotLoad;
    this.lambdaName = lambdaName;
    this.methodName = methodName;
    if (dev && !hotLoad) {
      methodInstance = getMethodInstance(getUrlClassLoader());
    } else {
      client = new AWSLambdaClient();
      client.setEndpoint("https://lambda.us-west-2.amazonaws.com");
    }
  }

  private Method getMethodInstance(URLClassLoader ucl) throws MalformedURLException, NoSuchMethodException, ClassNotFoundException {
    String[] parts = this.methodName.split("::");
    Class<?> aClass = ucl == null ? Class.forName(parts[0]) : Class.forName(parts[0], true, ucl);
    return aClass.getMethod(parts[1], InputStream.class, OutputStream.class, Context.class);
  }

  private URLClassLoader getUrlClassLoader() throws MalformedURLException {
    return null;
    /*
    if (config.jarLoc == null) return null;
    File jarFile = new File(config.jarLoc);
    URL fileURL = jarFile.toURI().toURL();
    String jarURL = "jar:" + fileURL + "!/";
    URL urls[] = {new URL(jarURL)};
    return new URLClassLoader(urls);
    */
  }

  private void runLocal(InputStream inputStream, OutputStream outputStream) {
    try {
      URLClassLoader ucl = getUrlClassLoader();
      Method method = hotLoad ? getMethodInstance(ucl) : this.methodInstance;
      method.invoke(null, inputStream, outputStream, new FakeContext(this.lambdaName));
      if (ucl != null) ucl.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String invokeLambda(InputStream input) throws IOException {
    InvokeRequest request = new InvokeRequest();
    request.setFunctionName(this.lambdaName);
    request.setPayload(IOUtils.toString(input));
    InvokeResult result = client.invoke(request);
    return new String(result.getPayload().array());
  }

  public String run(InputStream input) {
    if (dev) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      runLocal(input, outputStream);
      return new String(outputStream.toByteArray());
    } else {
      try {
        return invokeLambda(input);
      } catch (IOException e) {
        e.printStackTrace();
        return e.getMessage();
      }
    }
  }
}
