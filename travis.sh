#!/bin/bash

ideaVersion="2017.1"

travisCache=".cache"

if [ ! -d ${travisCache} ]; then
    echo "Create cache" ${travisCache}
    mkdir ${travisCache}
fi

function download {

    url=$1
    basename=${url##*[/|\\]}
    cachefile=${travisCache}/${basename}

    if [ ! -f ${cachefile} ]; then
        wget $url -P ${travisCache};
    else
        echo "Cached file `ls -sh $cachefile` - `date -r $cachefile +'%Y-%m-%d %H:%M:%S'`"
    fi

    if [ ! -f ${cachefile} ]; then
        echo "Failed to download: $url"
        exit 1
    fi
}

# Unzip IDEA

if [ -d ./idea  ]; then
    rm -rf idea
    echo "created idea dir"
fi

# Download main idea folder
download "http://download.jetbrains.com/idea/ideaIU-${ideaVersion}.tar.gz"
tar zxf ${travisCache}/ideaIU-${ideaVersion}.tar.gz -C .

# Move the versioned IDEA folder to a known location
ideaPath=$(find . -name 'idea-IU*' | head -n 1)
mv ${ideaPath} ./idea

if [ -d ./plugins ]; then
    rm -rf plugins
    mkdir plugins
    echo "created plugin dir"
fi

download "http://phpstorm.espend.de/files/proxy/phpstorm-${ideaVersion}-php.zip"
unzip -qo $travisCache/phpstorm-${ideaVersion}-php.zip -d ./plugins

# Run the tests
if [ "$1" = "-d" ]; then
    ant -d -f build-test.xml -Didea.build=./idea
else
    ant -f build-test.xml -Didea.build=./idea
fi

# Was our build successful?
stat=$?

if [ "${TRAVIS}" != true ]; then
    ant -f build-test.xml -q clean

    if [ "$1" = "-r" ]; then
        rm -rf idea
        rm -rf plugins
    fi
fi

# Return the build status
exit ${stat}
