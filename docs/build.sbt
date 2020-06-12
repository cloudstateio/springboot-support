
lazy val docs = project
  .in(file("."))
  .enablePlugins(CloudstateParadoxPlugin)
  .settings(
    deployModule := "springboot",
    paradoxProperties in Compile ++= Map(
      "cloudstate.springboot.version" -> "2.2.4.RELEASE",
      "cloudstate.springboot.lib.version" -> { if (isSnapshot.value) previousStableVersion.value.getOrElse("0.0.0") else version.value },
      "extref.cloudstate.base_url" -> "https://cloudstate.io/docs/core/current/%s",
      "snip.base.base_dir" -> s"${(baseDirectory in ThisBuild).value.getAbsolutePath}/../",
    )
  )