import groovy.json.JsonSlurper
import hudson.model.*

LinkedHashMap call(String jiraIssueKey, String whitelistedDestination, String awsProfileName, String repoName) {

    println "Retrieveing opened pull requests..."
    def openedPullRequestsList = fetchOpenedPullRequestIds(awsProfileName, repoName)

    def result = ['result':false];
    for (id in openedPullRequestsList) {

        print "Retrieveing data for pull request: " + id + "... ";
        def pullRequest = fetchPullRequestData(id, awsProfileName);
        def destinationReference = pullRequest.pullRequestTargets.destinationReference[0];

        if (destinationReference != whitelistedDestination) {
            println "Pull request destination `${destinationReference}` is not `${whitelistedDestination}` branch. Ignoring."
            continue;
        }

        def attributes = [
                pullRequest.pullRequestTargets.sourceReference[0],
                pullRequest.title
        ]

        println "Trying to match Issue key with attributes of Pull Request ${pullRequest.pullRequestId}...";


        for (attribute in attributes) {
            def pattern = jiraIssueKey
            def expression = (attribute =~ pattern)

            if (expression.find()) {
                println "`$attribute`  matched `$pattern`.";

                return [
                    'result' : true,
                    'PULL_REQUEST_ID': pullRequest.pullRequestId,
                    'SOURCE_COMMIT'  : pullRequest.pullRequestTargets.sourceCommit[0],
                    'SOURCE_REFERENCE': pullRequest.pullRequestTargets.sourceReference[0]
                ]
            } else {
                println "`$attribute`  does not match `$pattern`. Ignored";
            }
        }
    }

    return result;

}

def fetchPullRequestData(id, awsProfileName) {

    def prInfoShellScript = "aws codecommit get-pull-request --pull-request-id $id --profile $awsProfileName"
    def prResult = prInfoShellScript.execute().text;
    def parsedPrInfo = new JsonSlurper().parseText(prResult);

    return parsedPrInfo.pullRequest;
}

def fetchOpenedPullRequestIds(repoName, awsProfileName) {
    def shellScript = "aws codecommit list-pull-requests --repository-name $repoName --pull-request-status OPEN --profile $awsProfileName"
    def result = shellScript.execute().text;
    def parsedInfo = new JsonSlurper().parseText(result);
    return parsedInfo.pullRequestIds;
}