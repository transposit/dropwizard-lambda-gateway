package lambdawrapper;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lambdawrapper.resources.LambdaWrapper;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class LambdaWrapperApplication extends Application<LambdaWrapperConfiguration> {
  public static void main(final String[] args) throws Exception {
    new LambdaWrapperApplication().run(args);
  }

  @Override
  public String getName() {
    return "LambdaWrapper";
  }

  @Override
  public void initialize(final Bootstrap<LambdaWrapperConfiguration> bootstrap) {
    // TODO: application initialization
  }

  @Override
  public void run(final LambdaWrapperConfiguration configuration,
                  final Environment environment) throws GeneralSecurityException, IOException {
    LambdaWrapper authWrapper = new LambdaWrapper();
    environment.jersey().register(authWrapper);
  }
}
