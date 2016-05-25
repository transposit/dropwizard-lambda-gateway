package lambdawrapper.resources;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

/**
 * Created by tina on 5/25/16.
 */
public class FakeContext implements Context {
  private String functionName;

  public FakeContext(String functionName) {
    this.functionName = functionName;
  }

  @Override
  public String getAwsRequestId() {
    return "test-id";
  }

  @Override
  public String getLogGroupName() {
    return "test-group";
  }

  @Override
  public String getLogStreamName() {
    return "test-stream";
  }

  @Override
  public String getFunctionName() {
    return functionName;
  }

  @Override
  public CognitoIdentity getIdentity() {
    return null;
  }

  @Override
  public ClientContext getClientContext() {
    return null;
  }

  @Override
  public int getRemainingTimeInMillis() {
    return 0;
  }

  @Override
  public int getMemoryLimitInMB() {
    return 0;
  }

  @Override
  public LambdaLogger getLogger() {
    return System.out::println;
  }
}
