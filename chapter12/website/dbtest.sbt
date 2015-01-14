// Database testing build settings.

val dbLocation = settingKey[File]("The location of the testing database.")

dbLocation := target.value / "database"

val dbHelper = taskKey[DatabaseHelper]("typesafehub/reactive-platform-service")

dbHelper := derby((fullClasspath in Compile).value, dbLocation.value)

val dbListTables = taskKey[List[String]]("Prints out all available tables in the database.")

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

val dbEvolutionTest = inputKey[Unit]("Tests a database evolution")

DatabaseEvolutionTesting.evolutionsDirectoryDefaultSetting

dbEvolutionTest := {
  //val cmd = DatabaseEvolutionTesting.parser.parsed
  //val db = dbHelper.value
  //val log = streams.value.log
  //DatabaseEvolutionTesting.runCommand(cmd, db, log)
  val cmd = DatabaseEvolutionTesting.oldParser.parsed
  println(cmd)
}

