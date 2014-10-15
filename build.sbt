name := "teamstats"

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-directives" % "0.8.2",
  "net.databinder" %% "unfiltered-filter" % "0.8.2",
  "net.databinder" %% "unfiltered-jetty" % "0.8.2",
  "net.databinder" %% "unfiltered-specs2" % "0.8.2" % "test"
)

libraryDependencies += "net.liftweb" %% "lift-json" % "3.0-M1"

resolvers ++= Seq(
  "java m2" at "http://download.java.net/maven/2"
)
