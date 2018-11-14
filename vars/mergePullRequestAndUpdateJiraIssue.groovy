import groovy.json.JsonSlurperClassic

def call(String pullRequestId, String jiraIssueKey) {

    def data = executeAWSCliCommand("codecommit", "get-pull-request", ["pull-request-id": pullRequestId]);
    def pullRequest = data.pullRequest
    def target = pullRequest.pullRequestTargets[0];

    String status = pullRequest.pullRequestStatus;
    String repositoryName = target.repositoryName;
    String sourceReference = target.sourceReference;

    if (status != 'OPEN') {
        println "Request status is not valid. Expected 'OPEN', actual '$status'"
        return 'invalid_status';
    }


    Boolean isMergeable = checkIfMergeable(target.repositoryName, target.destinationReference, target.sourceReference)

    if (!isMergeable) {

        println "Pull Request #$pullRequestId cannot be merged. Performing Jira changes"
        jiraComment body: "Cannot merge PR-$pullRequestId.\nPlease resolve branch conflicts.", issueKey: jiraIssueKey
        jiraTransitionIssueByName(jiraIssueKey, "Merge Failed")

        return 'is_not_mergeable';
    }

    println "PR is mergeable. Merging."
    isMerged = executeAWSCliCommand("codecommit", "merge-pull-request-by-fast-forward", [
            "pull-request-id": pullRequestId,
            "repository-name": repositoryName
    ])


    if (!isMerged) {
        println "Error has been occured during merging of pull request #$pullRequestId"
        jiraComment body: "Cannot merge PR-$pullRequestId.\nPlease see Jenkins log to see whats was wrong", issueKey: jiraIssueKey
        jiraTransitionIssueByName(jiraIssueKey, "Merge Failed")

        return 'merge_error';
    }


    println "Merge was successful."

    def branchName = extractBranchName(sourceReference);
    if (branchName) {
        executeAWSCliCommand("codecommit", "delete-branch", [
                "branch-name"    : branchName,
                "repository-name": repositoryName
        ])
        println "Branch $branchName was deleted"
    }

    jiraComment body: "Successfully merged PR-$pullRequestId", issueKey: jiraIssueKey

    return 'success'

}

def extractBranchName(String reference) {
    def expression = (reference =~ /refs\/heads\/(.*)/)
    if (expression.find()) {
        return expression.group(1)
    } else {
        return null;
    }
}

def checkIfMergeable(String repositoryName, String destinationCommit, String sourceCommit) {

    def params = [
            "repository-name"             : repositoryName,
            "destination-commit-specifier": destinationCommit,
            "source-commit-specifier"     : sourceCommit,
            "merge-option"                : "FAST_FORWARD_MERGE",
    ];

    def parsedInfo = executeAWSCliCommand("codecommit", "get-merge-conflicts", params)

    println parsedInfo;

    return parsedInfo.mergeable;
}


def executeAWSCliCommand(String service, String command, parameters) {

    String shellScript = "aws $service $command";

    for (item in parameters) {
        shellScript += " --$item.key $item.value"
    }

    def result = (shellScript.execute().text)

    print(result);

    if (!result) {
        return null;
    }

    def parsedInfo = new JsonSlurperClassic().parseText(result)

    return parsedInfo;
}