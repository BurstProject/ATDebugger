name:= "ATDebugger"

version := "1.0"

scalaVersion := "2.11.5"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.9" withSources() withJavadoc()

libraryDependencies += "org.scalafx" %% "scalafx" % "2.2.76-R11" withSources() withJavadoc()

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)

libraryDependencies += "org.scalafx" %% "scalafxml-core-sfx2" % "0.2.2" withSources() withJavadoc()

//libraryDependencies += "org.scalafx" %% "scalafxml-core" % "0.2.1" withSources() withJavadoc()

fork in run := true

incOptions := incOptions.value.withNameHashing(false)

jfxSettings

JFX.mainClass := Some("atdebugger.ATDebugger")

JFX.devKit := JFX.jdk("/path/to/jdk")

EclipseKeys.withSource := true
