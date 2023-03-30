#!/bin/sh
apiStatus=`git status -s | wc -l`
if [ $apiStatus -ne 0 ]; then
   echo "This repository has changes and we won't be able to auto upversion. Please commit or stash your changes and try again"
   exit 1
fi

oldver=`cat gradle.properties  | grep "projectVersion=" | cut -f 2 -d "="`
charLen=`echo $oldver | grep -i snapshot | wc -c`
if [[ $charLen -eq "0" ]]
   then
       echo "Current version is not a snapshot. Are you SURE it's time to upversion??"
       echo "You will have to do this one manually"
       exit 1
fi

finalVer=`echo $oldver | sed 's/-SNAPSHOT//g'`
cat gradle.properties | sed "s/$oldver/$finalVer/g" > gradle.properties2; 
mv gradle.properties2 gradle.properties

commits=`git lg | grep -n -m 1 "Upversion to " |sed  's/\([0-9]*\).*/\1/' | tail -n 1`

fixesString=`git log --color --graph --pretty=format:'%s' --abbrev-commit | head -n $commits | grep -i "fixes #" | cut -c 1-2 --complement | sed 's/#\([0-9]*\)/\<a href="https:\/\/github.com\/redhat-developer\/intellij-rsp\/issues\/\1"\>#\1\<\/a\>/g' | awk '{ print "      <li>" $0 "</li>";}'`
changelogString="  <h3>$finalVer</h3>\n  <ul>\n$fixesString\n</ul>\n"
cat src/main/resources/META-INF/plugin.xml  | head -n 11 > tmpPluginXml
printf "$changelogString" >> tmpPluginXml
cat src/main/resources/META-INF/plugin.xml  | tail -n +12 >> tmpPluginXml
mv tmpPluginXml src/main/resources/META-INF/plugin.xml

git commit -a -m "Upversion to $finalVer for release" --signoff
git push origin master
git tag v$finalVer
git push origin v$finalVer


newLastSegment=`echo $finalVer | cut -f 3 -d "." | awk '{ print $0 + 1;}' | bc`
newverPrefix=`echo $finalVer | cut -f 1,2 -d "."`
newver=$newverPrefix.$newLastSegment-SNAPSHOT
cat gradle.properties | sed "s/$finalVer/$newver/g" > gradle.properties2; 
mv gradle.properties2 gradle.properties
git commit -a -m "Move to $newver" --signoff
git push origin master





