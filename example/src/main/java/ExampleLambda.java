import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by tina on 5/24/16.
 */
public class ExampleLambda {
  private static ObjectMapper mapper = new ObjectMapper();
  public static void handlePost(InputStream inputStream, OutputStream outputStream, Context context)
          throws IOException, ExecutionException, InterruptedException {
    Map<String, String> input = mapper.readValue(inputStream, new TypeReference<Map<String, String>>() {
    });
    input.put("foo", "bar");
    context.getLogger().log("Testing out the system logger");
    mapper.writeValue(outputStream, input);
  }

  public static void handleGet(InputStream inputStream, OutputStream outputStream, Context context)
          throws IOException, ExecutionException, InterruptedException {
    Writer writer = new OutputStreamWriter(outputStream);
    MustacheFactory mf = new DefaultMustacheFactory();
    Mustache mustache = mf.compile(new StringReader("Hello World!"), "example");
    mustache.execute(writer, new HashMap<>());
    writer.flush();
  }

  public static void handleRedirect(InputStream inputStream, OutputStream outputStream, Context context)
          throws IOException, ExecutionException, InterruptedException {
    Map<String, String> input = new HashMap<>();
    input.put("location", "http://www.github.com");
    mapper.writeValue(outputStream, input);
  }

  public static void handleQueryParamPassthrough(InputStream inputStream, OutputStream outputStream, Context context)
          throws IOException, ExecutionException, InterruptedException {
    Map<String, Object> params = new HashMap<>();
    mapper.writeValue(outputStream, params);
  }
}
