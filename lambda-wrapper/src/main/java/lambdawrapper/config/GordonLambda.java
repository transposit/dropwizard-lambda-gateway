package lambdawrapper.config;

import java.util.List;

/**
 * Created by ahl on 10/5/16.
 */
public class GordonLambda {
  public String runtime;
  public String handler;
  public String code; // Used to locate other settings.yml files.
  public List<String> build; // Irrelevant in this runtime context;
  public String description;
  public String role;
}
