import groovy.json.JsonSlurperClassic

HashMap call(String apiKey) {
    def response = "https://slack.com/api/users.list?token=$apiKey&pretty=1".toURL().text
    def parsedInfo = new JsonSlurperClassic().parseText(response)

    HashMap profiles = []
    for (item in parsedInfo.members) {
        profiles.put(item.profile.email, item)
    }

    return profiles;
}