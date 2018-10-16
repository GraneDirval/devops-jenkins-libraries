Boolean call(String idOrKey, String transitionName){

    def result = jiraGetIssueTransitions idOrKey: idOrKey


    print result;
    for (transition in result.data.transitions){

        print transition;
        if (transition.name == transitionName){
            jiraTransitionIssue idOrKey: idOrKey, input: [transition: [id: transition.id]]
            return  true;
        }
    }

    return  false;
}