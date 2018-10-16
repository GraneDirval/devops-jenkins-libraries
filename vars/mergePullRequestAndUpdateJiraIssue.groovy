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
        return;
    }


    Boolean isMerged = false;
    Boolean isMergeable = checkIfMergeable(target.repositoryName, target.destinationCommit, target.sourceCommit)

    if (isMergeable) {

        println "PR is mergeable. Merging."
        isMerged = executeAWSCliCommand("codecommit", "merge-pull-request-by-fast-forward", [
                "pull-request-id": pullRequestId,
                "repository-name": repositoryName
        ])

        if (isMerged) {

            println "Merge was successful."

            def branchName = extractBranchName(sourceReference);

            if(branchName){
                executeAWSCliCommand("codecommit", "delete-branch", [
                        "branch-name"    : branchName,
                        "repository-name": repositoryName
                ])
                println "Branch $branchName was deleted"
            }

            jiraComment body: "Successfully merged PR-$pullRequestId", issueKey: jiraIssueKey

        }
    }

    if (!isMergeable) {
        println "Pull Request #$pullRequestId cannot be merged. Performing Jira changes"

        jiraComment body: "Cannot merge PR-$pullRequestId.\nPlease resolve branch conflicts.", issueKey: jiraIssueKey

        jiraTransitionIssueByName(jiraIssueKey, "Merge Failed")

    } else if (isMergeable && !isMerged) {
        println "Error has been occured during merging of pull request #$pullRequestId"
    }


}

def extractBranchName(String reference){
    def expression = (sourceReference =~ /refs\/heads\/(.*)/)
    if (expression.find()) {
        return expression.group(1)
    }else{
        return  null;
    }
}

Boolean checkIfMergeable(String repositoryName, String destinationCommit, String sourceCommit) {

    def parsedInfo = executeAWSCliCommand("codecommit", "get-merge-conflicts", [
            "repository-name"             : repositoryName,
            "destination-commit-specifier": destinationCommit,
            "source-commit-specifier"     : sourceCommit,
            "merge-option"                : "FAST_FORWARD_MERGE",
    ])

    print parsedInfo;

    return parsedInfo.mergeable;
}


def executeAWSCliCommand(String service, String command, parameters) {

    String shellScript = "aws $service $command";

    for (item in parameters) {
        shellScript += " --$item.key $item.value"
    }

    def result = (shellScript.execute().text)

    if (!result) {
        return null;
    }

    def parsedInfo = new JsonSlurperClassic().parseText(result)

    return parsedInfo;
}