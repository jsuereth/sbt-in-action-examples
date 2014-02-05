import com.typesafe.sbt.SbtGit._
import complete.DefaultParsers._
import complete.Parser

name := "preowned-kittens"

versionWithGit

git.baseVersion := "0.1"

val checkNoLocalChanges = taskKey[Unit]("checks to see if we have local git changes.  Fails if we do.")

checkNoLocalChanges := {
  // TODO - implement.
  val dir = baseDirectory.value
  val changes = Process("git diff-index --name-only HEAD --", dir) !! streams.value.log
  if(!changes.isEmpty) {
    val changeMsg = changes.split("[\r\n]+").mkString(" - ","\n - ","\n")
    sys.error("Git changes were found: \n" + changeMsg)
  }
}

val integrationTests = taskKey[Unit]("runs integration tests.")

integrationTests := streams.value.log.info("Integration tests successful")

def releaseParser(state: State): Parser[String] = {
   val version = (Digit ~ chars(".0123456789").*) map {
    case (first, rest) => (first +: rest).mkString
   }
   val complete = (chars("v") ~ token(version, "<version number>")) map {
    case (v, num) => v + num
   }
   Space ~> complete  
}

def releaseAction(state: State, version: String): State = {
    ("all test integrationTests" ::
    s"git tag ${version}" ::
    "reload" ::
    "publish" ::
    state)
}

val releaseHelp = Help("release",
  "release <version>" -> "Runs the release script for a given version number",
  """|Runs our release script.  This will:
     |1. Run all the tests.
     |2. Tag the git repo with the version number.
     |3. Reload the build with the new version number from the git tag
     |4. publish all the artifacts""".stripMargin
)

val releaseCommand = Command("release", releaseHelp)(releaseParser)(releaseAction)


commands += releaseCommand