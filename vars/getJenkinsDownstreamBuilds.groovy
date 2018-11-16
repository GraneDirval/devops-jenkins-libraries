def call(build, Bool isFailedOnly) {
  def downstreamBuilds = [];

  for (job in Hudson.instance.getAllItems(hudson.model.Job)) {
    for (run in job.getBuilds()) {
      def cause = run.getCause(Cause.UpstreamCause)
      if (cause && cause.pointsTo(build.getRawBuild())) {

        if (isFailedOnly) {
          if (build.result == "FAILURE") {
            downstreamBuilds << run;
          }

        } else {
          downstreamBuilds << run;
        }
        println "Downstream for " + build.getFullDisplayName() + " is " + run.getFullDisplayName()
      }
    }
  }

  return downstreamBuilds;
}