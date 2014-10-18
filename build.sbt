name := "teamstats"

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-directives" % "0.8.2",
  "net.databinder" %% "unfiltered-filter" % "0.8.2",
  "net.databinder" %% "unfiltered-jetty" % "0.8.2",
  "net.databinder" %% "unfiltered-specs2" % "0.8.2" % "test"
)

libraryDependencies += "net.liftweb" %% "lift-json" % "3.0-M1"

libraryDependencies ++= Seq (
  "org.mongodb" %% "casbah" % "2.7.3",
  "com.novus" %% "salat" % "1.9.9"
)

resolvers ++= Seq(
  "java m2" at "http://download.java.net/maven/2",
  "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases",
  "repo.novus rels" at "http://repo.novus.com/releases/"
)

