# Source Release - Source code release plugin for gradle

The plugin will do the following

* Run tests
* Ensure git is clean
* Bump version in `build.gradle`
* Commit the change
* Tag the commit with 'v$version'
* Merge current branch into /release
* Push /release
