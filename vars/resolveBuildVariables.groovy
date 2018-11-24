def call(appConfigFile) {
    def buildVariables;
    def defaultBillingApiHost = 'http://billing.playwing.com/api'
    def defaultVariables = [
            BILLING_API_HOST      : defaultBillingApiHost,
            DROP_DB_ON_EACH_COMMIT: true,
            DB_UPDATE_TYPE        : 'STAGE_DB_WITH_NEW_MIGRATIONS'
    ]


    if (checkIfFileExists(appConfigFile)) {
        echo "File exists and no need to rewrite it"
        buildVariables = readExternalJsonFile appConfigFile
        buildVariables = defaultVariables + buildVariables;
    } else {
        echo "Rewriting file $appConfigFile"
        buildVariables = defaultVariables;
        writeToExternalJsonFile(appConfigFile, buildVariables)
    }
    return buildVariables;
}