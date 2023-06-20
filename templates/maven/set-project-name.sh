#!/bin/bash

if [ $# != 1 ]; then
	echo Usage: "$0" "<projectname>"
	exit 1
fi

projectname="$1"

find . \( -type d -name .git -prune \) -o -type f -print0 | xargs -0 sed -i s/EXERCISE""NAME/"$projectname"/
while IFS= read -r -d $'\0' file; do
	file_replaced=$(echo "$file" | sed s/EXERCISE""NAME/"$projectname"/)
	mv "$file" "$file_replaced"
done < <(find . \( -type d -name .git -prune \) -o -type f -name "*EXERCISE""NAME*" -print0)
