#!/bin/bash
set -eEuo pipefail

################################################################################
# util functions
################################################################################

# NOTE: $'foo' is the escape sequence syntax of bash
readonly ec=$'\033'      # escape char
readonly eend=$'\033[0m' # escape end
readonly nl=$'\n'        # new line

colorEcho() {
    local color=$1
    shift

    # if stdout is the console, turn on color output.
    [ -t 1 ] && echo "${ec}[1;${color}m$*${eend}" || echo "$*"
}

redEcho() {
    colorEcho 31 "$@"
}

yellowEcho() {
    colorEcho 33 "$@"
}

blueEcho() {
    colorEcho 36 "$@"
}

logAndRun() {
    local simple_mode=false
    [ "$1" = "-s" ] && {
        simple_mode=true
        shift
    }

    if $simple_mode; then
        echo "Run under work directory $PWD : $*"
        "$@"
    else
        blueEcho "Run under work directory $PWD :$nl$*"
        time "$@"
    fi
}

die() {
    redEcho "Error: $*" 1>&2
    exit 1
}

################################################################################
# biz logic
################################################################################

# shellcheck disable=SC2154
[ $# -ne 1 ] && die "need only 1 argument for version!$nl${nl}usage:$nl  $0 1.x.y"
readonly bump_version="$1"

# adjust current dir to project root dir
cd "$(dirname "$(readlink -f "$0")")/.."

# bump pom version
./mvnw \
    org.codehaus.mojo:versions-maven-plugin:2.10.0:set \
    -DgenerateBackupPoms=false \
    -DnewVersion="$bump_version"
