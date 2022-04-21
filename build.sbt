name := "DistribAlgo"

version := "1.0"

scalaVersion := "2.13.5"

resolvers += "titech.c.coord" at "https://xdefago.github.io/ScalaNeko/sbt-repo/"

libraryDependencies += "titech.c.coord" %% "scalaneko" % "0.22.0"

fork in run := true
