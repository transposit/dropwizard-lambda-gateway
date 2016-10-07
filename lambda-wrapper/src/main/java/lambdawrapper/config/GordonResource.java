package lambdawrapper.config;

import java.util.List;
import java.util.Map;

/**
 * Created by ahl on 10/5/16.
 */
public class GordonResource {
  public String methods; // XXX this probably needs to be a list or something
  public GordonIntegration integration;
  public List<GordonMethodResponses> responses;
  public Map<String, String> request_templates;
}
