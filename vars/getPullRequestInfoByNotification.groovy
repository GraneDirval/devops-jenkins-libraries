import com.cloudbees.groovy.cps.NonCPS
import groovy.json.JsonSlurper


def call(String sqs_body, String awsProfileName) {

    def parsed = new JsonSlurper().parseText(sqs_body)

    String NOTIFICATION_TYPE = '';
    String PULL_REQUEST_ID = '';

    def eventTypeExpression = (parsed.Message =~ /Event: (.*?)\./)
    if(eventTypeExpression.find()){

        def result = eventTypeExpression.group(1)

        if(result == 'Updated'){
            NOTIFICATION_TYPE = 'UPDATE'
        }

        if(result == 'Created'){
            NOTIFICATION_TYPE = 'CREATE'
        }
    }

    def prNameExpression = (parsed.Message =~ /Pull request name: (\d+)\./)
    if (prNameExpression.find()) {
        PULL_REQUEST_ID = prNameExpression.group(1)
    }

    if (PULL_REQUEST_ID == '' || NOTIFICATION_TYPE == '') {
        throw new Exception("""
            Cannot parse data from Notification Message. Seems like format changed or unsupported message type.\n
            `${parsed.message}`            
        """);
    }


    def pullRequest = retrieveDataFromAws(PULL_REQUEST_ID, awsProfileName)

    def params = [
        'PULL_REQUEST_TITLE'                : pullRequest.title,
        'PULL_REQUEST_STATUS'               : pullRequest.pullRequestStatus,
        'PULL_REQUEST_SOURCE_COMMIT'        : pullRequest.pullRequestTargets.sourceCommit[0],
        'PULL_REQUEST_SOURCE_REFERENCE'     : pullRequest.pullRequestTargets.sourceReference[0],
        'PULL_REQUEST_DESTINATION_COMMIT'   : pullRequest.pullRequestTargets.destinationCommit[0],
        'PULL_REQUEST_DESTINATION_REFERENCE': pullRequest.pullRequestTargets.destinationReference[0],
        'PULL_REQUEST_IS_MERGED'            : pullRequest.pullRequestTargets.mergeMetadata.isMerged
    ]

    params.put('PULL_REQUEST_ID', PULL_REQUEST_ID);
    params.put('NOTIFICATION_TYPE', NOTIFICATION_TYPE);
    params.put('APP_ID', "PR-$PULL_REQUEST_ID");


    return params;
}

@NonCPS
static retrieveDataFromAws(String PULL_REQUEST_ID, String awsProfileName) {

    GString shellScript = "aws codecommit get-pull-request --pull-request-id $PULL_REQUEST_ID --profile $awsProfileName"
    def result = (shellScript.execute().text)
    def parsedInfo = new JsonSlurper().parseText(result)
    def pullRequest = parsedInfo.pullRequest

    return pullRequest;
}
