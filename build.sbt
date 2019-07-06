name := "uniswag-backend"
version := "1.0"
scalaVersion := "2.11.8"

lazy val skinnyMicroVersion = "1.1.+"
lazy val jettyVersion = "9.2.15.v20160210"

libraryDependencies ++= Seq(
  "org.skinny-framework" %% "skinny-micro"        % skinnyMicroVersion,
  "org.skinny-framework" %% "skinny-micro-server" % skinnyMicroVersion,
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "com.typesafe.play" % "play-json_2.11" % "2.5.4",
  "com.github.cb372" %% "scalacache-caffeine" % "0.9.1",
  "com.google.code.findbugs" % "jsr305" % "1.3.+" // TODO: https://github.com/google/guava/issues/1095
)

fork in run := true

cancelable in Global := true

enablePlugins(JavaServerAppPackaging)
