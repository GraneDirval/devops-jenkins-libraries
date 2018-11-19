def call(appConfigFile) {
  def buildVariables;
  def defaultBillingApiHost = 'http://billing.playwing.com/api'
  if (checkIfFileExists(appConfigFile)) {
    echo "File exists and no need to rewrite it"
    buildVariables = readExternalJsonFile appConfigFile
  } else {
    echo "Rewriting file $appConfigFile"
    buildVariables = [
        BILLING_API_HOST      : defaultBillingApiHost,
        DROP_DB_ON_EACH_COMMIT: true,
    ]
    writeToExternalJsonFile(appConfigFile, buildVariables)
  }
  return buildVariables;
}