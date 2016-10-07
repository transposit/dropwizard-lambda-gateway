package lambdawrapper.resources;

import com.amazonaws.services.lambda.runtime.Context;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

/**
 * Created by tina on 5/24/16.
 */
public class LambdaExecutor {
  private String methodName;
  private String lambdaName;

  public LambdaExecutor(String methodName, String lambdaName) {
    this.lambdaName = lambdaName;
    this.methodName = methodName;
  }

  public String run(InputStream input)
          throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      String[] parts = this.methodName.split("::");
      Class<?> aClass = Class.forName(parts[0]);
      Method method = aClass.getMethod(parts[1], InputStream.class, OutputStream.class, Context.class);
      method.invoke(null, input, outputStream, new FakeContext(this.lambdaName));
    return new String(outputStream.toByteArray());
  }
}
