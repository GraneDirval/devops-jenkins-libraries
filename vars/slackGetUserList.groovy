import groovy.json.JsonSlurperClassic

HashMap call(String apiKey) {
    def response = "https://slack.com/api/users.list?token=$apiKey&pretty=1".toURL().text
    def parsedInfo = new JsonSlurperClassic().parseText(response)

    HashMap profiles = []


    for (item in parsedInfo.members) {

        def emails = resolvePossibleEmails(item.profile.email)

        for (email in emails){

            println "Resolved email $email";
            profiles.put(email, item)
        }

    }


    return profiles;
}

def resolvePossibleEmails(email){

    def emailDomains = [
        'ama.us',
        'playwing.net',
        'playwing.com',
        'origin-data.com'
    ]

    def pattern = '(.*)@.*'
    def expression = (email =~ pattern)

    def userEmailPrefix;
    if (expression.find()) {
        userEmailPrefix = expression.group(1);
    }
    if(!userEmailPrefix){
        return [];
    }

    def emails = [];
    for (domain in emailDomains){
        emails << "$userEmailPrefix@$domain".trim()
    }
    return emails;
}