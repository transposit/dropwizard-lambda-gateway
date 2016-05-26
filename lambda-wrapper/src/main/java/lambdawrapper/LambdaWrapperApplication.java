package lambdawrapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lambdawrapper.resources.LambdaConfig;
import lambdawrapper.resources.LambdaExecutor;
import lambdawrapper.resources.LambdaWrapper;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.List;

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
    LambdaWrapper lambdaWrapper = new LambdaWrapper();
    ResourceConfig resourceConfig = environment.jersey().getResourceConfig().register(lambdaWrapper);

    InputStream inputStream = LambdaWrapperApplication.class.getClassLoader().getResourceAsStream("GatewayConfig.json");
    List<LambdaConfig> configs = new ObjectMapper().readValue(inputStream, new TypeReference<List<LambdaConfig>>() {
    });
    configs.stream().forEach(c -> {
      try {
        Resource.Builder resourceBuilder = Resource.builder();
        resourceBuilder.path(c.path);
        ResourceMethod.Builder methodBuilder = resourceBuilder.addMethod(c.methodType);
        LambdaExecutor executor = new LambdaExecutor(configuration.dev, c, configuration.hotLoad);
        if (c.requestType != null) methodBuilder.consumes(c.requestType);
        methodBuilder.produces(c.responseType)
                .handledBy(new Inflector<ContainerRequestContext, Object>() {
                  @Override
                  public Object apply(ContainerRequestContext containerRequestContext) {
                    return executor.run(containerRequestContext.getEntityStream());
                  }
                });
        resourceConfig.registerResources(resourceBuilder.build());
      } catch (NoSuchMethodException | MalformedURLException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    });
  }
}
