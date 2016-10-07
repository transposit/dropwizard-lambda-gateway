package lambdawrapper.template;

import com.jayway.jsonpath.JsonPath;

/**
 * Created by ahl on 10/6/16.
 */
public class Input {
  public static final ThreadLocal<String> body = new ThreadLocal<String>;
  public static String json(String path) {
    return JsonPath.read(body, path);
  }
}
