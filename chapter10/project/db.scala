import sbt._
import Keys._


/** This helper represents how we will execute database statements. */
trait DatabaseHelper {
  def runQuery(sql: String, log: Logger): Unit
  def tables: List[String]
}


object DatabaseHelper {
  
  import complete.DefaultParsers._
  import complete.{TokenCompletions, Completions, Parser, Completion}
    def localFile(base: File): Parser[File] = {
      val completions = TokenCompletions.fixed { (seen, level) =>
        val fileNames = for {
          file <- IO.listFiles(base) 
          name <- IO.relativize(base, file).toSeq
          if name startsWith seen
        } yield Completion.token(seen, name drop seen.length)
        
        Completions.strict(fileNames.toSet)
      }
      val fileName: Parser[String] = 
        token(NotFileSeparator.* map (_.mkString), completions)
      fileName map (n => new File(base, n))
    }
    
    //TODO -  Ok now for the recursive crazy parser...
    
    
  // TODO - Platform specific, or ignore that junk?
  val NotFileSeparator = charClass(x => x != java.io.File.separatorChar, "non-file-separator character")
  
  
  val sqlScriptFileDirectory = settingKey[File]("Directory for SQL scripts")
  val sqlFileParser: State => Parser[File] = { state =>
    val extracted = Project extract state
    val bd = extracted get sqlScriptFileDirectory
    Space ~> localFile(bd)
  }
  val dbRunScriptFile = inputKey[Unit]("Runs SQL scripts from a directory.")
  val dbHelper = taskKey[DatabaseHelper]("")
  
  val dbSettings: Seq[Setting[_]] = Seq(
    dbRunScriptFile := {
      val file = sqlFileParser.parsed
      val sql = IO.read(file)
      val db = dbHelper.value
      val log = streams.value.log
      val statements = sql split ";" map (_.replaceAll("[\r\n]", "")) map (_.trim) 
      for {
        stmt <- statements
        if !stmt.isEmpty
        _ = log.info(s"Executing [$stmt]...")
      } db.runQuery(stmt, log)
    },
    sqlScriptFileDirectory := sourceDirectory.value / "sql"
  )
  
}