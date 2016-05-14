# Setting up Convox 
- convox install --stack-name "gateway" --instance-count "1" --region "us-west-2" ~/Dropbox/convox_credentials.csv
- convox apps create

# Building and releasing new version
- mvn install (lambda-wrapper)
- convox deploy

# Running locally
- Proxy port 9000 in VBox on OS X
- convox start
