#!/bin/sh
repoOwnerAndName=redhat-developer/intellij-rsp
curBranch=`git rev-parse --abbrev-ref HEAD`
ghtoken=`cat ~/.keys/gh_access_token`
argsPassed=$#
echo "args: " $argsPassed
if [ "$argsPassed" -eq 1 ]; then
	debug=1
	echo "YOU ARE IN DEBUG MODE. Changes will NOT be pushed upstream"
else
	echo "The script is live. All changes will be pushed, deployed, etc. Live."
	debug=0
fi
read -p "Press enter to continue"


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

echo ""
echo "These are the commits for the release"
commits=`git lg | grep -n -m 1 "Upversion to " |sed  's/\([0-9]*\).*/\1/' | tail -n 1`
commitMsgs=`git log --color --pretty=format:'%h - %s' --abbrev-commit | head -n $commits`
echo "$commitMsgs"
read -p "Press enter to continue"


cleanVer=`echo $oldver | sed 's/-SNAPSHOT//g'`
finalVer=$cleanVer.Final
cat gradle.properties | sed "s/$oldver/$finalVer/g" > gradle.properties2; 
mv gradle.properties2 gradle.properties

commits=`git lg | grep -n -m 1 "Upversion to " |sed  's/\([0-9]*\).*/\1/' | tail -n 1`
fixesString=`git log --color --graph --pretty=format:'%s' --abbrev-commit | head -n $commits | grep -i "fixes #" | cut -c 1-2 --complement | sed 's/#\([0-9]*\)/\<a href="https:\/\/github.com\/redhat-developer\/intellij-rsp\/issues\/\1"\>#\1\<\/a\>/g' | awk '{ print "      <li>" $0 "</li>";}'`
changelogString="  <h3>$finalVer</h3>\n  <ul>\n$fixesString\n</ul>\n"
cat src/main/resources/META-INF/plugin.xml  | head -n 11 > tmpPluginXml
printf "$changelogString" >> tmpPluginXml
cat src/main/resources/META-INF/plugin.xml  | tail -n +12 >> tmpPluginXml
mv tmpPluginXml src/main/resources/META-INF/plugin.xml

echo "Committing and pushing to master"
git commit -a -m "Upversion to $finalVer for release" --signoff
if [ "$debug" -eq 0 ]; then
	git push origin $curBranch
else 
	echo git push origin $curBranch
fi


echo "Need to run a build"
read -p "Press enter to continue"

./gradlew buildPlugin
echo "Did it succeed?"
read -p "Press enter to continue"


echo ""
echo "Tagging and pushing to origin"
git tag v$finalVer
if [ "$debug" -eq 0 ]; then
	git push origin v$finalVer
else 
	echo git push origin v$finalVer
fi


echo ""
echo "It's time to go kick a real build"
read -p "Press enter to continue"

echo "Now build again with release flag"
read -p "Press enter to continue"

echo "Time to do github release"
read -p "Press enter to continue"


echo "Making a release on github for $finalVer"
commitMsgsClean=`git log --color --pretty=format:'%s' --abbrev-commit | head -n $commits | awk '{ print " * " $0;}' | awk '{printf "%s\\\\n", $0}' | sed 's/"/\\"/g'`
createReleasePayload="{\"tag_name\":\"v$finalVer\",\"target_commitish\":\"$curBranch\",\"name\":\"$finalVer\",\"body\":\"Release of $finalVer:\n\n"$commitMsgsClean"\",\"draft\":false,\"prerelease\":false,\"generate_release_notes\":false}"

if [ "$debug" -eq 0 ]; then
	curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $ghtoken"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  https://api.github.com/repos/$repoOwnerAndName/releases \
	  -d "$createReleasePayload" | tee createReleaseResponse.json
else 
	echo curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $ghtoken"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  https://api.github.com/repos/$repoOwnerAndName/releases \
	  -d "$createReleasePayload"
fi

echo "Please go verify the release looks correct. We will add the asset next"
read -p "Press enter to continue"

assetUrl=`cat createReleaseResponse.json | grep assets_url | cut -c 1-17 --complement | rev | cut -c3- | rev | sed 's/api.github.com/uploads.github.com/g'`
rm createReleaseResponse.json
zipFileName=`ls -t -1 build/distributions/ | grep ".zip$" | head -n 1`
zipFileNameSafe=`echo $zipFileName | sed 's/ /_/g'`
zipLoc=build/distributions/$zipFileName
echo "Running command to add artifact to release: "
	echo curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $ghtoken"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  -H "Content-Type: application/octet-stream" \
	  "$assetUrl?name=$zipFileNameSafe" \
	  --data-binary "@$zipLoc"
if [ "$debug" -eq 0 ]; then
	curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $ghtoken"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  -H "Content-Type: application/octet-stream" \
	  "$assetUrl?name=$zipFileNameSafe" \
	  --data-binary "@$zipLoc"
fi
echo ""
echo "Please go verify the release looks correct and the distribution was added correctly."
read -p "Press enter to continue"



newLastSegment=`echo $finalVer | cut -f 3 -d "." | awk '{ print $0 + 1;}' | bc`
newverPrefix=`echo $finalVer | cut -f 1,2 -d "."`
newver=$newverPrefix.$newLastSegment-SNAPSHOT
cat gradle.properties | sed "s/$finalVer/$newver/g" > gradle.properties2; 
mv gradle.properties2 gradle.properties
git commit -a -m "Move to $newver" --signoff
git push origin master





