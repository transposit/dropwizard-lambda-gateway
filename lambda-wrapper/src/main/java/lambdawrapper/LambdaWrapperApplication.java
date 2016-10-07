package lambdawrapper;

import com.amazonaws.services.cloudfront.model.InvalidArgumentException;
import com.amazonaws.util.StringInputStream;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lambdawrapper.config.GordonConfig;
import lambdawrapper.config.GordonIntegrationResponse;
import lambdawrapper.resources.LambdaExecutor;
import lambdawrapper.resources.LambdaWrapper;
import lambdawrapper.template.Input;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.SystemLogChute;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
                  final Environment environment) throws Exception {
    LambdaWrapper lambdaWrapper = new LambdaWrapper();
    ResourceConfig resourceConfig = environment.jersey().getResourceConfig().register(lambdaWrapper);

    InputStream inputStream = LambdaWrapperApplication.class.getClassLoader().getResourceAsStream("settings.yml");
    ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    //yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    GordonConfig config = yamlMapper.readValue(inputStream, GordonConfig.class);

    // Velocity setup.
    Velocity.init();

    final VelocityContext velocityContext = new VelocityContext();

    velocityContext.put("name", "Velocity");
    velocityContext.put("project", "Jakarta");

    velocityContext.put("input", Input.class);


    // Parse settings.yml file
    // XXX do we need to parse all of them?
    Map<String, String> methods = new HashMap<String, String>();

    config.lambdas.forEach((name, c) -> {
      methods.put(name, c.handler);
    });

    config.apigateway.forEach((name, api) -> {
      api.resources.forEach((path, resource) -> {
        if (!methods.containsKey(resource.integration.lambda)) {
          throw new IllegalArgumentException("no such lambda specified: " + resource.integration.lambda);
        }
      });
    });

    config.apigateway.forEach((name, api) -> {
      api.resources.forEach((path, resource) -> {
        try {
          Resource.Builder resourceBuilder = Resource.builder();
          resourceBuilder.path(path);
          ResourceMethod.Builder methodBuilder = resourceBuilder.addMethod(resource.methods);
          String lambdaName = resource.integration.lambda;
          LambdaExecutor executor = new LambdaExecutor(configuration.dev, configuration.hotLoad,
                  methods.get(lambdaName), lambdaName);
          //if (c.requestType != null) methodBuilder.consumes(c.requestType);
          //if (c.responseType != null) methodBuilder.produces(c.responseType);
          methodBuilder.handledBy(new Inflector<ContainerRequestContext, Object>() {
            @Override
            public Object apply(ContainerRequestContext containerRequestContext) {
              try {
                InputStream entityStream = containerRequestContext.getEntityStream();

                MultivaluedMap<String, String> headers = containerRequestContext.getUriInfo().getQueryParameters();
                /*
                if (c.passthroughQueryParams) {
                  MultivaluedMap<String, String> headers = containerRequestContext.getUriInfo().getQueryParameters();
                  Map<String, Object> requestJson = c.requestType != null && c.requestType.equals("application/json") ?
                          mapper.readValue(entityStream, new TypeReference<Map<String, Object>>() {
                          }) : new HashMap<>();
                  requestJson.put("querystring", headers);
                  entityStream = new ByteArrayInputStream(mapper.writeValueAsBytes(requestJson));
                }
                */

                if (resource.request_templates != null && !resource.request_templates.isEmpty()) {
                  String contentType = containerRequestContext.getHeaderString(HttpHeaders.CONTENT_TYPE);
                  if (contentType == null)
                    contentType = "application/json";

                  String template = resource.request_templates.get(contentType);
                  if (template != null) {
                    StringWriter w = new StringWriter();
                    Velocity.evaluate(velocityContext, w, "velocity", template);
                    System.out.println(w.toString());
                    entityStream = new StringInputStream(w.toString());
                  }
                }

                // XXX AWS API Gateway catches exceptions and packages them up for consumption by the response
                // integration layer; we can do that if/when we need it.
                String result = executor.run(entityStream);

                if (resource.integration.responses != null && !resource.integration.responses.isEmpty()) {
                  Map<String, Object> body = parseResult(result);
                  String errorMessage = (String) body.getOrDefault("errorMessage", "");
                  Optional<GordonIntegrationResponse> response = resource.integration.responses.stream()
                          .filter(resp -> Pattern.matches(resp.pattern, errorMessage))
                          .findFirst();

                  if (response.isPresent()) {
                    final Response.ResponseBuilder r = Response.status(response.get().code);
                    response.get().parameters.forEach((key, value) -> {
                      r.header(path("method.response.header", key),
                              (String) body.get(path("integration.response.body", value)));
                    });
                    return r.build();
                  }
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
    });
  }

  private String path(String prefix, String value) {
    String[] ps = prefix.split("\\.");
    String[] vs = value.split("\\.");

    if (ps.length >= vs.length)
      throw new InvalidArgumentException("too few components in " + value);
    if (ps.length + 1 != vs.length)
      throw new InvalidArgumentException("too many components in " + value);

    for (int i = 0; i < ps.length; i++) {
      if (!ps[i].equals(vs[i]))
        throw new InvalidArgumentException(value + " does not match prefix " + prefix);
    }

    return vs[vs.length - 1];
  }

  private Map<String, Object> parseResult(String result) throws IOException {
    return mapper.readValue(result, new TypeReference<Map<String, Object>>() {
    });
  }
}
