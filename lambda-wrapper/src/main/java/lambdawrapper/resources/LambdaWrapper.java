package lambdawrapper.resources;

import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tina on 11/6/15.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class LambdaWrapper {
  private static ObjectMapper mapper = new ObjectMapper();

  public LambdaWrapper() throws GeneralSecurityException, IOException {
  }

  @GET
  @Path("/status")
  public Response getStatus() throws IOException {
    Map<String, Object> result = new HashMap<>();
    InputStream stream = LambdaWrapper.class.getClassLoader().getResourceAsStream("git.json");
    result.put("git", mapper.readValue(stream, ObjectNode.class));
    return Response.ok(result).build();
  }

}
