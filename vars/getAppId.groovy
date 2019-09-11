def call(PULLREQUEST_ID, APP_PREFIX){

  if(APP_PREFIX){
    APP_ID = "PR-" + PULLREQUEST_ID + "-" + APP_PREFIX
  }else{
    APP_ID = "PR-" + PULLREQUEST_ID

  }
  return APP_ID;

}