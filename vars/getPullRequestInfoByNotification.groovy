import com.cloudbees.groovy.cps.NonCPS
import groovy.json.JsonSlurper


def call(String sqs_body) {

    def parsed = new JsonSlurper().parseText(sqs_body)

    def createPRexpression = (parsed.Message =~ /.*Pull Request ID as (\d+) and title as (.*)\..*/)
    def updatePRexpression = (parsed.Message =~ /.*updated the following PullRequest (\d+)*/)

    String NOTIFICATION_TYPE = '';
    String PULL_REQUEST_ID = '';

    if (createPRexpression.find()) {
        PULL_REQUEST_ID = createPRexpression.group(1)
        NOTIFICATION_TYPE = 'CREATE'
    }

    if (updatePRexpression.find()) {
        PULL_REQUEST_ID = updatePRexpression.group(1)
        NOTIFICATION_TYPE = 'UPDATE'
    }


    if (PULL_REQUEST_ID == '' || NOTIFICATION_TYPE == '') {
        throw new Exception('Cannot parse data from Notification Message. Seems like format changed or unsupported message type.');
    }


    def pullRequest = retrieveDataFromAws(PULL_REQUEST_ID)

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
static retrieveDataFromAws(String PULL_REQUEST_ID) {

    GString shellScript = "aws codecommit get-pull-request --pull-request-id $PULL_REQUEST_ID"
    def result = (shellScript.execute().text)
    def parsedInfo = new JsonSlurper().parseText(result)
    def pullRequest = parsedInfo.pullRequest

    return pullRequest;
}
