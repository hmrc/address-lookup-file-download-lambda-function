
# address-lookup-file-download-lambda-function

A lambda function that downloads files from Ordnance Survey.

__credstash__:
    
    role: address_lookup

    user: address_lookup_password

    password: address_lookup_password
    
These have been added to integration so far.

### Testing

#### Unit tests

    sbt clean test

### Run locally using `aws sam local`

- Install `aws-sam-cli` using the instructions [here](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html)
- Run `sam build` to build the project
- Run `sam local invoke` to invoke the lambda function locally

**Note**: This Lambda function is intended to run in AWS and retrieve the 
Ordnance Survey API key from Secrets Manager. However, it can also be 
executed locally by setting the ORDNANCE_SURVEY_API_KEY environment 
variable in the template.yaml file to the Ordnance Survey API key, 
which is available in the txm-secrets GIT repository.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
