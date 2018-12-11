def call(string jobName, Boolean inProgressOnly = false) {

  for (item in Jenkins.instance.items) {
    if (jobName.equals(item.name)) {

      def builds = [];

      for (build in item.getBuilds()) {
        if (inProgressOnly) {
          if (build.isInProgress())
            builds << build
        } else {
          builds << build
        }
      }

      return builds;
    }
  }
}