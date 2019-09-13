import groovy.json.JsonOutput

def call(filePath, content) {
    def json = JsonOutput.toJson(content)
    sh "echo '$json'>$filePath"
    json = null;
}