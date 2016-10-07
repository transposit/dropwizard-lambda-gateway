# Dropwizard Lambda gateway
This is a simple [dropwizard](http://www.dropwizard.io/) container for local development execution of labmdas.


## Configure your Lambdas
Create a file `<your project>/src/main/resources/settings.yml` that specifies details about your labmda and API Gateway configuration. This configuration is borrowed from [Gordon](https://github.com/jorgebastida/gordon) which can then be used for deployment into AWS.

Below is an example of a `settings.yml` file or see `https://github.com/kmonkeyjam/dropwizard-lambda-gateway/blob/master/example/src/main/resources/settings.yml`.

```
lambdas:
    test:
        runtime: java8
        handler: ExampleLambda::handlePost

    test2:
        runtime: java8
        handler: ExampleLambda
    
apigateway:
    api:
        description: my apis
        resources:
            /test:
                methods: POST
                integration:
                    lambda: test
            /test2:
                methods: GET
                integration:
                    lambda: test2
            /redirect:
                methods: GET
                responses:
                  - code: "302"
                    parameters:
                        method.response.header.Location: True
                integration:
                    lambda: redirect
                    responses:
                      - pattern: ""
                        code: "302"
                        parameters:
                            method.response.header.Location:
                                integration.response.body.location
```

## Building and running lambda-wrapper locally
From within the lambda-wrapper directory:

Build: `mvn install`

Run as prod or dev: `java -jar target/lambda-wrapper-0.0.1-SNAPSHOT.jar server dev.yml`

## Embedding lambdas for development and easier debugging
The `example` project shows a simple maven project configured to load the lambdas locally as a maven dependency.  Notice in the config you can leave out the `jarLoc` to load the class from the main jar.  

If you use Paw (https://luckymarmot.com/paw) for easy testing, you can use the `test.paw` file as a convenience.

