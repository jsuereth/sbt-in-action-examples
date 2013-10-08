---
layout: scalastyle
title: "Chapter 6 - Process & I/O"
---


6. The I/O and Process Libraries (Difficulty: 2, Importance: , M)

6.1 Working with Files
6.2 Working with Streams
6.3 Mappings - ZIP / JAR
6.4 Logging
6.5 Process - Add switches depending upon operating system
6.6 Summary

Objectives:

* File Mappings
- files ** filter
- files.**.get
- files ** filter x relativeTo(dir)
* sbt.IO library
- tmp files
- streams
- copy/move/etc
* sbt.Process library
* sbt.Logger?

So far we're up and running with sbt, and we've got a basic build, along with our tests to make sure that our code works. However, we've still got a couple of issues with our build. Our integration tests aren't quite as slick as we would like - we still need to start the Play application manually. To be fully integrated in our build, we should be starting the server before running the tests and stopping it afterwards. And then we've still got the problem of packaging. We'll need to package the application so that it can be deployed to another server for system testing and eventually production. Actually, we will do the packaging first, then we can run our integration tests against the package that we will deploy. This way we're testing what we will eventually deploy.

Packaging using processes

In the previous chapters, we saw an example of how to use the sbt Process library, the task that retrieved the git head revision hash:

    val gitHeadCommitSha = taskKey[String]("Determines the current git commit SHA")

    gitHeadCommitSha := Process("git rev-parse HEAD").lines.head

You can see from this that it is pretty simple to define and run a new Process from sbt. So can we do our packaging in the same way? The answer is yes, but with a pretty important caveat that we'll come to. Our job is to take all of our classes, along with all of the classes in our dependencies and write them into a single big jar, which we can deploy and simply run from the command line. This is just one approach of many that we can take (for instance a single jar with the dependencies listed in the MANIFEST.MF, or using a plugin).

The first thing we need to do is to define a target directory to copy into:

    val dependentJarDirectory = settingKey[File]("location of the unpacked dependent jars")

    dependentJarDirectory := target.value / "dependent-jars"

Here, we're defining a directory called dependent-jars which we'll copy everything into. Note that the directory is inside our target directory, so that it will be cleaned when we do an sbt clean - if we're generating files then this is good practice. We need a task to create the directory:

    val createDependentJarDirectory = taskKey[Unit]("create the dependent-jars directory")

    createDependentJarDirectory in ThisBuild := Process(s"mkdir ${dependentJarDirectory.value}") !

So far so good. Now, we take the contents of our dependencies and copy them into this directory. But how do we know what our dependencies are? Well, we could use libraryDependencies. This contains a list of our dependencies. However, this isn't actually useful in our case - we need the location of the files, so that we can unpack them. We will use the classpath - this contains the physical location of the jars that we will use. The classpath is a sequence of either JARs or directores, so we'll need to cope with both of them. If we have a JAR, we can extract all of the files within using unzip - a JAR is just a ZIP file. If it is a directory, we need to copy the directory. Let's define a method to do this:

    def unpack(target: File, f: File) = {
      if (f.isDirectory) Process(s"xcopy /E /Y ${f.getAbsolutePath} ${target.getAbsolutePath}") !
      else Process(s"unzip -n -qq -d ${target.getAbsolutePath} ${f.getAbsolutePath}") !
    }

and finally a task which calls this method:

    val unpackJars = taskKey[Seq[_]]("unpacks a dependent jars into target/dependent-jars")

    unpackJars in ThisBuild := {Build.data((dependencyClasspath in Runtime).value).map ( f => unpack(dependentJarDirectory.value, f))}

We run this, and it works.

xxx


However, most of you at this point will have seen the basic flaw in what we've done. If you've been following along running the code, chances are, it won't have worked for you. This is because the above code is very specific to the machine on which we're running. In fact, for most builds, the above code would be unacceptable. Taking the example of the copy - we're using xcopy, which is a command only available on Windows systems. Even the unzip command that we've used may not be available, or may not be the same version with the same parameters on somebody elses machine. If you recall from chapter 1, this is the reason why ant was created in the first place, because make suffered from this sort of problem.

Callout:

It is very important to consider the machines on which the build will be run if you're calling out to the command line to execute a process. This obviously also includes the continuous integration system. For a project which is restricted to a well defined team (a personal project which noone else will use, a company team which only uses Windows machines), then it may be acceptable to push to the command line. If you are guaranteed to have access to the command line tool in question, this may also be acceptable. For our example from chapter 1, using git, this would be a completely reasonable thing to assume that the end developer had if the project was held in a git repository. I think it's fairly safe to assume that you will have a JVM installed as well. We'll see later on the best way to use these. For a real project, you'll have to use your judgement - there are often better alternatives.

So, how do we get around this problem? Well, there are a couple of options. The first is to write your own file manipulation methods, or use a library which is designed for file handling, such as Apache Commons IO. However, sbt has a lot of built in methods available by default in the sbt.IO package. And these are designed to be OS independent. Let's start with the createDependentJarDirectory task:

    createDependentJarDirectory in ThisBuild := {sbt.IO.createDirectory(dependentJarDirectory.value)}

This is fairly self-explanatory, this creates the directory, if it doesn't exist already. And for the more complex unpack method, we use two methods, copyDirectory and unzip, which do what you would expect:

    def unpack(target: File, f: File) = {
      val ignores = List("meta-inf", "license")
      if (f.isDirectory) {println("f=" + f); sbt.IO.copyDirectory(f, target) }
      else sbt.IO.unzip(f, target, filter = new NameFilter { def accept(name: String) = !ignores.exists(i => name.toLowerCase().startsWith(i)) && !new File(name).exists })
    }

The NameFilter ensures that we only copy the first file of any particular name into the resultant directory. So the resulting directory should reflect the classpath.  TODO Add explanation for the ignores. Finally, we need to tie this altogether into a uber jar. Now here, we could reasonably use the 'jar' command, because this is usually part of the Java JDK distribution, and we're fairly sure that you will have that. However, there is a small (tiny?) chance that the options won't be the same between versions, so we'll stick to using the sbt.IO package.

    val createUberJar = taskKey[File]("create jar which we will run")

    createUberJar in ThisBuild := { create (dependentJarDirectory.value, target.value / "build.jar"); target.value / "build.jar"}

    def create(dir: File, buildJar: File) = {
      val files = (dir ** "*").get.filter(d => d != dir)
      val filesWithPath = files.map(x => (x, x.relativeTo(dir).get.getPath))
      sbt.IO.zip(filesWithPath, buildJar)
    }

This method requires some explanation. First, we get a list of all of the files within the dir directory (1). The ** gives a recursive list. This is very similar to the Ant globbing. We want all of the files within the dependent jars directory, except for the top level directory itself (2). So files is a Seq[File]. However, sbt.IO.zip requires a Seq[(File, String)] where the String is the name within the zip. For instance (file("/foobar/sbt-in-action-examples/target/dependent-jars/com/foo/bar/Foo.class"), "com/foo/bar/Foo.class"). For each file that is returns by the **, we need to retrieve the path name relative to the dependent-jars directory. This we achieve with the relativeTo(dir) method, which returns an Option[File]. Finally we call the zip to create the jar jar, which we can run from the command line:

    $ java -jar target/build.jar

Note also that the createUberJar task returns the created jar, so that we can use it subsequently.

Callout for other **

Callout for other methods such as relativeTo


Now, especially if you've been following along, you'll have noticed one or two things. The first is that our solution isn't really optimal. If, for instance, we add a new class to our src, then we have to manually compile, and then manually run createDependentJarDirectory, then unpackJars and then createUberJar. We need to chain these together so that I can just call createUberJar, and it will do the rest. We will do this using task dependencies. Also, you'll have noticed that if we remove a class, it will still get included in the final jar because we never remove the old class files. This second problem is more difficult, and can be solved in one of two ways. We could try to work out which files need to be removed from the dependent jars directory. This is non-trivial, and is beyond the scope of this book. The second, easier option is to remove the dependent jars directory every time we do the build. This takes longer for each build, but is safer, and is guaranteed to work. So we need another task, to delete the dependent jars directory:

    val deleteDependentJarsDirectory = taskKey[Unit]("delete the dependent jars directory")

    deleteDependentJarsDirectory in ThisBuild := { sbt.IO.delete(dependentJarDirectory.value) }

So how do we declare that one task depends on another? This is simple: you can define the inputs to a task by simply referencing the value inside the task. So if we want crereateUberJar to depend on unpackJars, we must simply use the value:

    createUberJar in ThisBuild := {
      val unpack: Seq[_] = unpackJars.value
      create (dependentJarDirectory.value, target.value / "build.jar")
    }

and it's as simple as that. When the build.sbt is being compiled, it will read these dependencies and construct a dependency tree to determine which task can be executed when.

Callout: When tasks collide: parallel execution

sbt tasks are executed in parallel by default. Therefore, if you have task A which depends on task B and task C, sbt will try to execute tasks B and C at the same time. This can be trivially proven using the following definitions:

    def sleep(s: String) = { val sleep=new java.util.Random().nextInt(5000); Thread.sleep(sleep); println(s"${s} ${sleep}"); }

    val taskA = taskKey[String]("taskA")

    val taskB = taskKey[String]("taskB")

    val taskC = taskKey[String]("taskC")

    taskA := { val b = taskB.value; val c = taskC.value; "taskA" }

    taskB := { sleep("taskB"); "taskB" }

    taskC := { sleep("taskC"); "taskC" }

    > taskA
    taskC 1943
    taskB 2157
    [success] Total time: 2 s, completed 22-Sep-2013 17:19:31

Clearly, the tasks are being executed in parallel. Therefore, if B also depends on C, you need to explicitly declare this in the definition of task B, otherwise your build could fail. 

In our example, we've got createUberJar which depends upon unpackJars. If we were naively to add createDependentJarDirectory to our dependencies for createUberJar, it is possible that we would end up with builds sometimes failing because the directory didn't exist when we try and unpack the jars into it. unpackJars should have the dependency on createDependentJarsDirectory. If necessary, you can declare the dependency in both tasks - the task will only be executed once. We will complete the dependency tree later, after we can run the jar.

If you do declare that B also depends on C, then the tasks will be executed serially, not in parallel.

    taskB := { val c = taskC.value; sleep("taskB"); "taskB" }

    > taskA
    taskC 3576
    taskB 3067
    [success] Total time: 7 s, completed 22-Sep-2013 17:25:38

sbt also checks for cycles in the list of tasks to execute. If you have:

    val taskA = taskKey[String]("taskA")

    taskA := { val b = taskB.value; "taskA" }

    val taskB = taskKey[String]("taskB")

    taskB := { val a = taskA.value; "taskB" }

Then sbt will give you

    [error] Cyclic reference involving
    [error]    {file:/C:/code/sbt/sbt-in-action-examples/chapter6/}root/*:taskB
    [error]    {file:/C:/code/sbt/sbt-in-action-examples/chapter6/}root/*:taskA
    [error] Use 'last' for the full log.


Coming back to our build example, sometimes it is useful to know what is going on in a task, especially if the task fails. sbt provides a standard logging framework to enable us to add output to trace th build. We've already come across this in chapter TODO, where we had XXX problems, and used the last command to find out the problem. Adding this to our tasks is easy: we just use streams.value.log. This is reference to the streams task, which provides per-task logging and I/O via a Streams instance.

    createUberJar in ThisBuild := {
      val unpack: Seq[_] = unpackJars.value
      val log = streams.value.log
      create (log, dependentJarDirectory.value, target.value / "build.jar")
    }

    def create(log: Logger, dir: File, buildJar: File) = {
      val files = (dir ** "*").get.filter(_ != dir)
      val filesWithPath = files.map(x => (x, x.relativeTo(dir).get.getPath))
      filesWithPath.foreach(fp => log.debug("copying " + fp._1 + " zip(" + fp._2 + ")"))
      sbt.IO.zip(filesWithPath, buildJar)
    }

Note that we have to use streams.value from the task itself, we can't reference it in the create method.

TODO Callout: sbt and scala macros - referencing value.

We rerun the task, and no extra output appears - has it actually logged anything? The answer is yes, but as you'll recall from chapter TODO, the amount of output that appears on the screen is configurable. All of the output is still available. We can use last to make it appear:

> last
...
[debug] copying C:\code\sbt\sbt-in-action-examples\chapter6\target\dependent-jars\public\stylesheets zip(public\stylesheets)
[debug] copying C:\code\sbt\sbt-in-action-examples\chapter6\target\dependent-jars\public\images\favicon.png zip(public\images\favicon.png)
[debug] copying C:\code\sbt\sbt-in-action-examples\chapter6\target\dependent-jars\public\stylesheets\main.css zip(public\stylesheets\main.css)
[success] Total time: 11 s, completed 23-Sep-2013 16:33:58

You can also specify the last command, such as:

> last compile

This will give the output of the last compile command

This is particularly useful if a command fails, you can always ask for the complete output post-execution. There are five levels of logging we can use, debug, info, warn and error. By default, all of these will appear in the stream which is displayed when you use the last command. However, by default, sbt only displays the warn & error levels on the console. We can change these by using the TODO setting.

Running our build with our integration tests

Now that we have a jar, we can create a task to be able to run the jar from within sbt. Now, if we were uncertain that everyone who will run our build will have the unzip command, which is why we started to use sbt.IO.unzip, then we can be absolutely certain that everyone who has sbt has Java installed on their machine. You can't run sbt without it.

The first and most obvious thing that we can try is to run the java process using the Process method

    val runUberJar = taskKey[Int]("run the uber jar")

    runUberJar := {
      val uberJar = createUberJar.value
      Process("java -jar " + uberJar.getAbsolutePath) !
    }

Note that we're ensuring that createUberJar has been run. This works, although it assumes that java is on the path of your shell (it has to be available, but there is no guarantee that it is on the path of your shell). We could replace the java with the javaHome setting:


    // TODO fix this
    runUberJar := {
      val uberJar = createUberJar.value
      Process((javaHome.value / "bin" / "java").getAbsolutePath + " -jar " + uberJar.getAbsolutePath) !
    }

This improves things, but is still not perfect. However, sbt (from 0.13 onwards) has a better solution - it has an API specifically for running java: the Fork API. This is the general style:

    val options = ForkOptions(...)
    val arguments: Seq[String] = ...
    val mainClass: String = ...
    val exitCode: Int = Fork.java(options, mainClass +: arguments)

So in our case, this becomes a lot simpler:


    runUberJar := {
      val uberJar = createUberJar.value
      val options = ForkOptions()
      streams.value.log.error("uberJar.getAbsolutePath=" + uberJar.getAbsolutePath)
      val arguments = Seq("-cp", uberJar.getAbsolutePath)
      val mainClass = "foo.NewGlobal"
      val exitCode: Int = Fork.java(options, mainClass +: arguments)
    }

This works, but doesn't really.

----

You will recall from chapter 5 that sbt can run tests within the sbt JVM, or it can fork a new one. In our case, we

---


Say something about returning the values of the tasks 
Next: eliminate the copy of the jars?
Otherwise, what can we do about streams?
And we need to run the jar using java fork & then run the integration tests
Add in the rest of the dependencies
Make sure all return types are correct

http://www.scala-sbt.org/0.13.0/docs/Detailed-Topics/Paths.html


  // selects all directories under "src"
def srcDirs(base: File) = ( (base / "src") ** "*") filter { _.isDirectory }

  // selects archives (.zip or .jar) that are selected by 'somePathFinder'
def archivesOnly(base: PathFinder) = base filter ClasspathUtilities.isArchive


Looking at this, why do we need to package for integration tests? We could just set the classpath and start another java process?

Also, versioning in target?

