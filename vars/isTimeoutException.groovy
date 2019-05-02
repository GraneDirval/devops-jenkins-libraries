def call(Exception err) {
  return 'SYSTEM' == err.getCauses()[0].getUser().toString()
}