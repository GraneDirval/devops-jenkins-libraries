Boolean call() {

  def currentDay = getCurrentDayOfWeek();

  if ((currentDay == 'Saturday') || (currentDay == 'Sunday')) {
    return false;
  }

  Calendar c = Calendar.instance

  c.timeZone = TimeZone.getTimeZone("Europe/Kiev")

  def hour = c.get(Calendar.HOUR_OF_DAY)

  if (hour < 10 && hour > 19) {
    return false;
  }

  return true;

}