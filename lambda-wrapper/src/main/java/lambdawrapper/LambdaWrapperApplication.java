package lambdawrapper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LambdaWrapperApplication extends Application<LambdaWrapperConfiguration> {
  public static ObjectMapper mapper = new ObjectMapper();

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
        if (c.responseType != null) methodBuilder.produces(c.responseType);
        methodBuilder.handledBy(new Inflector<ContainerRequestContext, Object>() {
                  @Override
                  public Object apply(ContainerRequestContext containerRequestContext) {
                    try {
                      InputStream entityStream = containerRequestContext.getEntityStream();

                      if (c.passthroughQueryParams) {
                        MultivaluedMap<String, String> headers = containerRequestContext.getUriInfo().getQueryParameters();
                        Map<String, Object> requestJson = c.requestType != null && c.requestType.equals("application/json") ?
                                mapper.readValue(entityStream, new TypeReference<Map<String, Object>>() {
                                }) : new HashMap<>();
                        requestJson.put("querystring", headers);
                        entityStream = new ByteArrayInputStream(mapper.writeValueAsBytes(requestJson));
                      }
                      String result = executor.run(entityStream);
                      if (c.redirect) {
                        Map<String, Object> resultMap = parseResult(result);
                        String location = (String) resultMap.get("location");
                        return Response.temporaryRedirect(URI.create(location)).build();
                      }
                      return result;
                    } catch (IOException e) {
                      return Response.serverError();
                    }
                  }
        });
        resourceConfig.registerResources(resourceBuilder.build());
      } catch (NoSuchMethodException | MalformedURLException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    });
  }

  private Map<String, Object> parseResult(String result) throws IOException {
    return mapper.readValue(result, new TypeReference<Map<String, Object>>() {
    });
  }
}
