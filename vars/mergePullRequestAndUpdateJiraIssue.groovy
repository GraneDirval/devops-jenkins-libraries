import groovy.json.JsonSlurper

def call(String pullRequestId, String jiraIssueKey) {

    def data = executeAWSCliCommand("aws codecommit get-pull-request --pull-request-id $pullRequestId");

    def pullRequest = data.pullRequest
    def target = pullRequest.pullRequestTargets[0];

    String status = pullRequest.pullRequestStatus;
    String repositoryName = target.repositoryName;


    if (status != 'OPEN') {
        println "Request status is not valid. Expected 'OPEN', actual '$status'"
        return;
    }


    Boolean isMerged = false;
    Boolean isMergeable = checkIfMergeable(target.repositoryName, target.destinationCommit, target.sourceCommit)

    if (isMergeable) {

        isMerged = executeAWSCliCommand("aws codecommit merge-pull-request-by-fast-forward --pull-request-id $pullRequestId --repository-name $repositoryName")

        if (isMerged) {
            print('delete brnach')
            /*executeAWSCliCommand("aws codecommit delete-branch --branch-name $BRANCH_NAME --repository-name $repositoryName")*/

            jiraComment body: "Successfully merged PR-$pullRequestId", issueKey: jiraIssueKey

        }
    }

    if (!isMergeable) {
        println "Pull Request #$pullRequestId cannot be merged. Performing Jira changes"

        jiraComment body: "Cannot merge PR-$pullRequestId.\nPlease resolve branch conflicts.", issueKey: jiraIssueKey

        jiraTransitionIssueByName (jiraIssueKey, "Merge Failed")

    } else if (isMergeable && !isMerged) {
        println "Error has been occured during merging of pull request #$pullRequestId"
    }


}

Boolean checkIfMergeable(String repositoryName, String destinationCommit, String sourceCommit) {

    String shellScript = "aws codecommit get-merge-conflicts " +
            "--repository-name $repositoryName " +
            "--destination-commit-specifier $destinationCommit " +
            "--source-commit-specifier $sourceCommit " +
            "--merge-option FAST_FORWARD_MERGE"

    def parsedInfo = executeAWSCliCommand(shellScript)

    return parsedInfo.mergeable;
}


def executeAWSCliCommand(String shellScript) {

    def result = (shellScript.execute().text)

    if (!result) {
        return null;
    }

    def parsedInfo = new JsonSlurper().parseText(result)

    return parsedInfo;
}