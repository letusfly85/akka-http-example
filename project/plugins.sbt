logLevel := sbt.Level.Info

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6")

addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.5.0")

//mysql
libraryDependencies += "mysql" % "mysql-connector-java"  % "5.1.33"
addSbtPlugin("org.scalikejdbc" %% "scalikejdbc-mapper-generator" % "3.0.+")

//flyway
addSbtPlugin("org.flywaydb" % "flyway-sbt" % "4.2.0")
resolvers += "Flyway" at "https://flywaydb.org/repo"
