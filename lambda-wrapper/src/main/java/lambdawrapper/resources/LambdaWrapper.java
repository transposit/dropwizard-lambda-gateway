package lambdawrapper.resources;

import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;

/**
 * Created by tina on 11/6/15.
 */
@Path("/lambda")
@Consumes("application/json")
@Produces(MediaType.APPLICATION_JSON)
public class LambdaWrapper {
  private AWSLambdaClient client = new AWSLambdaClient();
  private static ObjectMapper mapper = new ObjectMapper();

  public LambdaWrapper() throws GeneralSecurityException, IOException {
    client.setEndpoint("https://lambda.us-west-2.amazonaws.com");
  }

  @POST
  @Timed
  @Path("/venues")
  public Response execute(Object req, @HeaderParam("Host") String host) throws IOException, ExecutionException, InterruptedException {
    String functionName = host.contains("staging") ? "venues-staging" : "venues";
    String s = invokeLambda(functionName, req);
    return Response.ok(s).build();
  }

  @GET
  @Path("/health")
  public Response executeHealth() {
    return Response.ok().build();
  }

  @POST
  @Timed
  @Path("/users")
  public Response executeUsers(Object req, @HeaderParam("Host") String host) throws IOException, ExecutionException, InterruptedException {
    String s = invokeLambda(host.contains("staging") ? "users-staging" : "users", req);
    return Response.ok(s).build();
  }

  private String invokeLambda(String functionName, Object payload) throws JsonProcessingException {
    InvokeRequest request = new InvokeRequest();
    request.setFunctionName(functionName);
    request.setPayload(mapper.writeValueAsString(payload));
    InvokeResult result = client.invoke(request);
    return new String(result.getPayload().array());
  }
}
