import groovy.json.JsonSlurper

@NonCPS
def call (APP_ID){

  GString shellScript = "cat /var/app/${APP_ID}/config/review.json"

  def result = (shellScript.execute().text)

  if(!result){
    return null;
  }

  def reviewInfo = new JsonSlurper().parseText(result)

  result = null;

  return reviewInfo
}