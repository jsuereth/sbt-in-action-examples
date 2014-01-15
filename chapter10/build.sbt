import com.typesafe.sbt.SbtGit._

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

val releaseScript = Command.single("release", 
                                   ("release <version>", "Runs the release script for a given version number"), 
                                   """|Runs our release script.  This will:
                                      |1. Run all the tests.
                                      |2. Tag the git repo with the version number.
                                      |3. Reload the build with the new version number from the git tag
                                      |4. publish all the artifacts""".stripMargin) {
  (state, releaseVersion) =>
    // Here we make the list of things we want to do:
    "checkNoLocalChanges" ::
    "test" ::
    s"git tag v${releaseVersion}" ::
    "reload" ::
    "publish" ::
    state
}

commands += releaseScript
