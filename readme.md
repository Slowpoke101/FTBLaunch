FTB Launcher
====

Note: some classes used in this project come from the community, and as such are under other open source licenses.
please see the links in the headers of those java files for more information about the source & the open source license used.
FTB's code uses the following license:
##The license
Copyright 2012-2014 FTB Launcher Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

##Compiling
* Download the source from this repo, either with git or as a zip/tarball.
* Install Eclipse or IDEA
* you will need project lombok integration installed see http://projectlombok.org/download.html for more info on its IDE integration. Gradle adds this to the classpath for you in the IDE for IDE building
* To build a jar/exe using gradle run 'gradlew build' to generate the .exe version add 'launch4j' to the end after installing launch4j
* To generate IDE project with dependencies using gradle run 'gradlew eclipse' or 'gradlew idea' after running a build.
* With Eclipse/idea, import generated project/module and build as usual.

##Pull Request Standards
* Indent with spaces(4)
* Avoid trailing whitespace
* If using eclipse: Use formatter located at eclipse_formatter.xml
* If using Intellij IDEA use the eclipse formatter(there is a plugin for this)
* Sign-off your commits
* Avoid merge commits in pull requests
* Squash your commits - have at most one commit per major change

##Updating your fork
Before submitting a pull request, you should ensure that your fork is up to date.
To do this, run these commands:

    git remote add upstream git://github.com/Slowpoke101/FTBLaunch.git
    git pull --rebase upstream master
    git push --force origin <branch_name>

The first command is only necessary the first time. If you have issues merging, you will need to get a merge tool such as [P4Merge](http://perforce.com/product/components/perforce_visual_merge_and_diff_tools).
Once it is set up, run `git mergetool`. Once all conflicts are fixed, run `git rebase --continue`, and `git push --force origin <branch_name>`.
