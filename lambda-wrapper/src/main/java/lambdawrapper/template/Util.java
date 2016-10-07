package lambdawrapper.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Created by ahl on 10/7/16.
 */
public class Util {

  public static String escapeJavaScript(String str) {
    return StringEscapeUtils.escapeJavaScript(str);
  }
}
