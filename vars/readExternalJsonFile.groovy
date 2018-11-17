import groovy.json.JsonSlurperClassic

def call(filePath) {

    def content = ("cat $filePath".execute().text.trim())
    return new JsonSlurperClassic().parseText(content);
}