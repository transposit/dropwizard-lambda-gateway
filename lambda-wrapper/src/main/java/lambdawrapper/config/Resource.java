package lambdawrapper.config;

import java.util.List;
import java.util.Map;

/**
 * Created by ahl on 10/5/16.
 */
public class Resource {
  public String methods; // XXX this probably needs to be a list or something
  public Integration integration;
  public List<MethodResponses> responses;
  public Map<String, String> request_templates;
}
