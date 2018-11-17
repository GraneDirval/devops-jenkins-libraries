import groovy.json.JsonSlurperClassic

def call(filePath) {
    def content = sh(script: "cat $filePath", returnStdout: true).trim()
    return new JsonSlurperClassic().parseText(content);
}