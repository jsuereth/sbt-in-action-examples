val gitHeadCommitSha = settingKey[String]("Determines the current git commit SHA")

gitHeadCommitSha := Process("git rev-parse HEAD").lines.head

version := "1.0-" + gitHeadCommitSha.value
