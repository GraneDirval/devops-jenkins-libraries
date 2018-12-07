String call() {

  def date = new Date()
  Calendar calendar = Calendar.getInstance();
  calendar.setTime(date);
  def day = calendar.get(Calendar.DAY_OF_WEEK);


  def map = [
      1: "Sunday",
      2: "Monday",
      3: "Tuesday",
      4: "Wednesday",
      5: "Thursday",
      6: "Friday",
      7: "Saturday"]

  return map[day];

}