# Lambda Execution framework
This is a simple dropwizard container that can be configured to run either a local lambda jar or invoke a lambda on aws.

## Building and running lambda-wrapper locally
You can 

## Example GatwayConfig.json for lambda-wrapper/src/main/resources

```
[
  {
    "path": "/test",
    "jarLoc": "/Users/.../test-0.0.1-SNAPSHOT.jar",
    "className": "TestMain",
    "method": "handle",
    "deployedFunction": "test-lambda"
  }
]
```

## Setting up Convox 
- convox install --stack-name "gateway" --instance-count "2" --region "us-west-2" ~/Dropbox/convox_credentials.csv
- convox apps create

## Building and releasing new version
- from within the lambda-wrapper directory, run `mvn package`
- convox deploy

## Running locally
- Proxy port 443 in VBox on OS X
- convox start

