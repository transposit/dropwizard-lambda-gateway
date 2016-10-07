package lambdawrapper.template;

import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ahl on 10/6/16.
 */
public class Input {
  public static final ThreadLocal<ContainerRequestContext> context = new ThreadLocal<ContainerRequestContext>();

  public static String json(String path) {

    String body = "";

    try {
      body = IOUtils.toString(context.get().getEntityStream());
    } catch (IOException ioe) {}

    if (body.equals(""))
      body = "{}";
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(JsonPath.read(body, path));
    } catch (Exception e) {
      System.out.println(e);
      return "{}";
    }
  }

  private static <T> Map<String, T> multiToSingle(MultivaluedMap<String, T> in) {
    HashMap<String, T> out = new HashMap<String, T>();

    in.forEach((key, lst) -> {
      out.put(key, lst.get(0));
    });

    return out;
  }

  public static Map<String, Object> params() {
    System.out.println("$input.params()");

    MultivaluedMap<String, String> querystring = context.get().getUriInfo().getQueryParameters();
    MultivaluedMap<String, String> header = context.get().getHeaders();

    HashMap<String, Object> out = new HashMap<String, Object>();
    out.put("querystring", multiToSingle(querystring));
    out.put("header", multiToSingle(header));

    return out;
  }

  public static Object params(String key) {
    // The order is path, querystring, or header

    // TODO path

    List<String> lst = context.get().getUriInfo().getQueryParameters().getOrDefault(key,
            context.get().getHeaders().get(key));

    if (lst != null && !lst.isEmpty())
      return lst.get(0);
    return null;
  }
}
