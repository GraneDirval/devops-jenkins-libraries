import groovy.json.JsonSlurper
import hudson.model.*

def call(String jiraIssueKey, String whitelistedDestination) {

    println "Retrieveing opened pull requests..."
    def openedPullRequestsList = fetchOpenedPullRequestIds()

    LinkedHashMap result = [];
    for (id in openedPullRequestsList) {

        print "Retrieveing data for pull request: " + id + "... ";
        def pullRequest = fetchPullRequestData(id);
        def destinationReference = pullRequest.pullRequestTargets.destinationReference[0];

        if (destinationReference != whitelistedDestination) {
            println "Pull request destination `${destinationReference}` is not `${whitelistedDestination}` branch. Ignoring."
            continue;
        }

        def attributes = [
                pullRequest.pullRequestTargets.sourceReference[0],
                pullRequest.pullRequestTitle
        ]

        println "Trying to match Issue key with attributes of Pull Request ${pullRequest.pullRequestId}...";


        for (attribute in attributes) {
            def pattern = jiraIssueKey
            def expression = (attribute =~ pattern)

            if (expression.find()) {
                println "`$attribute`  matched `$pattern`.";

                def pullRequestData = [
                        'PULL_REQUEST_ID': pullRequest.pullRequestId,
                        'SOURCE_COMMIT'  : pullRequest.pullRequestTargets.sourceCommit[0],
                ]

                result = pullRequestData;

                break;
            } else {
                println "`$attribute`  does not match `$pattern`. Ignored";
            }
        }
    }

    return result;

}

def fetchPullRequestData(id) {

    def prInfoShellScript = "aws codecommit get-pull-request --pull-request-id $id"
    def prResult = prInfoShellScript.execute().text;
    def parsedPrInfo = new JsonSlurper().parseText(prResult);

    return parsedPrInfo.pullRequest;
}

def fetchOpenedPullRequestIds() {
    def shellScript = "aws codecommit list-pull-requests --repository-name webstore --pull-request-status OPEN"
    def result = shellScript.execute().text;
    def parsedInfo = new JsonSlurper().parseText(result);
    return parsedInfo.pullRequestIds;
}