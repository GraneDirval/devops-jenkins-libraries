def call(String service, String command, parameters) {

  String shellScript = "aws $service $command";

  for (item in parameters) {
    shellScript += " --$item.key $item.value"
  }

  def result = (shellScript.execute().text)

  print(result);

  if (!result) {
    return null;
  }

  def parsedInfo = new JsonSlurperClassic().parseText(result)

  return parsedInfo;
}