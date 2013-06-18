name := "preowned-kittens"

libraryDependencies += "org.apache.derby" % "derby" % "10.10.1.1"

val dbLocation = settingKey[File]("The location of the testing database.")

dbLocation := target.value / "database"

val dbHelper = taskKey[DatabaseHelper]("")

dbHelper := derby((fullClasspath in Compile).value, dbLocation.value)

val dbListTables = taskKey[List[String]]("")

dbListTables := dbHelper.value.tables


val dbQuery = inputKey[Unit]("Runs a query against the database and prints the result")

val queryParser = {
  import complete.DefaultParsers._
  token(any.* map (_.mkString), "<sql>")
}

dbQuery := {
  val query = queryParser.parsed
  val db = dbHelper.value
  val log = streams.value.log
  db.runQuery(query, log)
}

