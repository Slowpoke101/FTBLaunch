FTB Launcher
====

##The license
Copyright 2012 FTB Launcher Contributors

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
* Install maven or Eclipse
* If using maven, run `mvn` in the directory you installed it to.
* With Eclipse, open as a project and build as usual

##Pull Request Standards
* Indent with tabs
* Avoid trailing whitespace
* Sign-off your commits
* Avoid merge commits in pull requests
* Squash your commits - have at most one commit per major change

##Updating your fork
Before submitting a pull request, you should ensure that your fork is up to date.
To do this, run these commands:

    git remote add upstream git://github.com/Slowpoke101/FTBLaunch.git
    git pull --rebase upstream master
    git push --force origin <branch_name>

The first command is only necessary the first time. If you have issues merging, you will need to get a merge tool such as [P4Merge]().
Once it is set up, run `git mergetool`. Once all conflicts are fixed, run `git rebase --continue`, and `git push --force origin <branch_name>`.