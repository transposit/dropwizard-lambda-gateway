package lambdawrapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.dropwizard.Configuration;

public class LambdaWrapperConfiguration extends Configuration {
  @JsonInclude
  public Boolean dev;
}
