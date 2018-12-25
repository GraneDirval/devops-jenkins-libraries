import groovy.json.JsonSlurperClassic

HashMap call(String apiKey) {
    def response = "https://slack.com/api/users.list?token=$apiKey&pretty=1".toURL().text
    def parsedInfo = new JsonSlurperClassic().parseText(response)

    HashMap profiles = []

    def emailDomains = [
        'ama.us',
        'playwing.net',
        'playwing.com',
        'origin-data.com'
    ]

    for (item in parsedInfo.members) {

        def pattern = '(.*)@.*'
        def expression = (item.profile.email =~ pattern)

        def userEmailPrefix;
        if (expression.find()) {
            userEmailPrefix = expression.group(1);
        }
        if(!userEmailPrefix){
            continue;
        }

        for (domain in emailDomains){
            def email = "$userEmailPrefix@$domain".trim()
            profiles.put(email, item)

            println "Resolved email $email";

        }

    }


    return profiles;
}