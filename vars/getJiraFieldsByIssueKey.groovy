def call(String JIRA_ISSUE_KEY) {

  def response = jiraGetIssue idOrKey: JIRA_ISSUE_KEY
  def jiraFields = response.data.fields

  return  jiraFields
}