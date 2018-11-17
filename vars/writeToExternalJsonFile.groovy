import groovy.json.JsonOutput

def call(filePath, content) {
    def json = JsonOutput.toJson(content)

    def shellScript = "echo '$json'>$filePath"
    shellScript.execute()

    String resolvedContent = ("cat $filePath".execute().text)
    print resolvedContent
}