# Releasing

1. Wait for any running [Travis builds](https://travis-ci.com/github/cloudstateio/springboot-support/builds) to complete.

2. Create an annotated tag (with `git tag -a vX.Y.Z`) and then [release](https://github.com/cloudstateio/springboot-support/releases) for the next version.

3. Travis will start a [build](https://travis-ci.com/github/cloudstateio/springboot-support/builds) and publish to Bintray, and then sync to Maven Central.


## Snapshots

Configuration in `.mvn/snapshot.jgitver.config.xml` provides snapshot versions like those used with other Cloudstate projects (that use sbt-dynver plugin), such as `0.5.1-7-abcd1234`.

To publish a versioned snapshot to https://bintray.com/cloudstateio/snapshots, use the `deployment/deploy-snapshot.sh` script, with BINTRAY_USERNAME and BINTARY_PASSWORD environment variables set.

```
BINTRAY_USERNAME={username} BINTARY_PASSWORD={apikey} deployment/deploy-snapshot.sh
```

Note: the repository must not be dirty when using the above configuration. Commit before releasing a versioned snapshot.
