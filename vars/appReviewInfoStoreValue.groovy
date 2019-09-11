@NonCPS
def call(APP_ID, key, value){

  def reviewInfo = appReviewInfoRetrieve(APP_ID);

  def data;

  if(!reviewInfo){
    data = getDefaultValue();
  }else{
    data = reviewInfo;
  }


  print data;

  data[key] = value

  print data;

  GString path = "/var/app/${APP_ID}/config/review.json"

  writeToExternalJsonFile(path, data);

  reviewInfo = null;
  data = null;
  path = null;

}


def getDefaultValue(){

  def defaults = [
      isReviewOk: false,
      selectedReviewer: null
  ]

  return defaults;

}