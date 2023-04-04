name := "DistribAlgo"

version := "1.0"

scalaVersion := "2.13.10"

resolvers += "titech.c.coord" at "https://xdefago.github.io/ScalaNeko/sbt-repo/"

libraryDependencies += "titech.c.coord" %% "scalaneko" % "0.23.0"

run / fork := true
