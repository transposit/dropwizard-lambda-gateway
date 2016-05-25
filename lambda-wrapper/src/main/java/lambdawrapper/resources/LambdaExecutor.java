package lambdawrapper.resources;

import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.util.IOUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
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
  private Method method;
  private String functionName;
  private Boolean dev;

  public LambdaExecutor(Boolean dev, LambdaConfig config) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException {
    this.dev = dev;
    if (dev) {
      File jarFile = new File(config.jarLoc);
      URL fileURL = jarFile.toURI().toURL();
      String jarURL = "jar:" + fileURL + "!/";
      URL urls[] = {new URL(jarURL)};
      URLClassLoader ucl = new URLClassLoader(urls);
      method = Class.forName(config.className, true, ucl)
              .getMethod(config.method, InputStream.class, OutputStream.class, Context.class);
    } else {
      this.functionName = config.deployedFunction;
      client = new AWSLambdaClient();
      client.setEndpoint("https://lambda.us-west-2.amazonaws.com");
    }
  }

  private void runLocal(InputStream inputStream, OutputStream outputStream) {
    try {
      method.invoke(null, inputStream, outputStream, null);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String invokeLambda(InputStream input) throws IOException {
    InvokeRequest request = new InvokeRequest();
    request.setFunctionName(functionName);
    request.setPayload(IOUtils.toString(input));
    InvokeResult result = client.invoke(request);
    return new String(result.getPayload().array());
  }

  public Object run(InputStream input) {
    if (dev) {
      return new StreamingOutput() {
        @Override
        public void write(OutputStream outputStream) throws IOException, WebApplicationException {
          runLocal(input, outputStream);
        }
      };
    } else {
      try {
        return invokeLambda(input);
      } catch (IOException e) {
        e.printStackTrace();
        return Response.ok(e.getMessage()).build();
      }
    }
  }
}
