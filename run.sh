#!/bin/bash
#
# The Insight Data Engineering Code Challenge 2015-07-02
#
# Accepts a first argument specifying one of:
#   java, perl, python, go, clean, or help
#
# Daniel Lamblin


usage() {
    echo -e <<END
\n${usage_line}
\thelp\tprints this help.
\tjava\truns word count and running median tweet statistics tool in java.
\tperl\truns word count and running median tweet statistics tool in perl.
\toneliner\truns word count and running median tweet stats a in perl oneliner.
\tpython\truns word count and running median tweet statistics tool in python.
\t      \t[python not-yet-implemented]
\tgo\truns word count and running median tweet statistics tool in go.

END
    exit 0
}

makedirs() {
    for dir; do
        if [[ ! -e "${dir}" ]]; then
            echo "Making directory ${dir}" >&2
            mkdir "${dir}"
        fi
    done
}

main() {
    # Variables and configuration
    local usage_line="Usage: $0 {help|java|oneliner|perl|python|go|clean}\n"
    local command="${1:-python}"

    local dir_in='tweet_input'
    local dir_out='tweet_output'

    local gradle_cmd='./gradlew'
    local java_project_name='InsightDataEngineeringCodingChallenge'
    local java_bin_path="./build/install/${java_project_name}/bin"
    local java_tweetstat_cmd="${java_bin_path}/tweetStats"

    local perl_tweetstat_cmd="./src/perl/tweetStats.pl"

    local line_tweetstat_cmd="./src/perl/non-scalable_10min_oneliner.pl"

    local python_tweetstat_cmd="./src/python/tweetStats.py"

    local go_tweetstat_cmd="go run ./src/golang/tweetStats/tweetStats.go"

    #Start processing
    case ${command} in
        help)   usage ;;
        --help) usage ;;
        h)      usage ;;
        -h)     usage ;;
    esac

    echo "You chose ${command}: from java, oneliner, perl, python, go," \
         "clean, or help." >&2

    case ${command} in
        java)
            echo "While updating from v1.1 to v2.0 the Java solution has broken." >&2
            exit
            makedirs "${dir_in}" "${dir_out}"
            echo "Good choice; see all the source in src/java/{main,test}/lamblin" >&2
            if [[ ! -x ${java_tweetstat_cmd} ]]; then
                echo "Installing: ${java_tweetstat_cmd}" >&2
                ${gradle_cmd} installApp
            fi
            echo "Running tweet stats for word count and running median." >&2
            ${java_tweetstat_cmd} -i "${dir_in}" -o "${dir_out}/wc_result.txt"
            ;;
        oneliner)
            makedirs "${dir_in}" "${dir_out}"
            echo "The Perl one-liner outputs all results to STDOUT, ft2.txt then ff1.txt." >&2
            echo "See also the slightly better \"perl\"; and this source in ./src/perl/*.pl" >&2
            local perl_cmd=$(which perl)
            if [[ $? -ne 0 ]]; then
                echo "Actually, it seems you might not have perl installed." >&2
            else
                echo "Running tweet stats for word count and running median." >&2
                ${perl_cmd} ${line_tweetstat_cmd} "${dir_in}"/*
            fi
            ;;
        perl)
            makedirs "${dir_in}" "${dir_out}"
            echo "See also the \"oneliner\" perl; and this source in src/perl" >&2
            echo "Running tweet stats for word count and running median." >&2
            ${perl_tweetstat_cmd} -o "${dir_out}" "${dir_in}"/*
            ;;
        python)
            makedirs "${dir_in}" "${dir_out}"
            echo "The Python solution uses multiple worker processes." >&2
            local py_cmd=$(which python3)
            if [[ $? -ne 0 ]]; then
                echo "Actually, it seems you might not have python3 installed." >&2
            else
                echo "Running tweet stats for word count and running median." >&2
                ${python_tweetstat_cmd} -o "${dir_out}" "${dir_in}"/*
            fi
            ;;
        go)
            makedirs "${dir_in}" "${dir_out}"
            local go_cmd=$(which go)
            if [[ $? -ne 0 ]]; then
                echo "Actually, it seems you might not have go installed." >&2
            else
              echo "I assume you have go installed. You may need to set your GOPATH" >&2
              echo "Running tweet stats for word count and running median." >&2
              cat "${dir_in}"/* | ${go_tweetstat_cmd} -o "${dir_out}"
            fi
            ;;
        clean)
            echo "Clean isn't a language, but I am going to" \
                 "clean up some files, mostly related to the Java build." >&2
            ${gradle_cmd} clean
            rm "${dir_in}/"pg*txt
            ;;
        *)
            echo "That \"{$command}\" doesn't actually make sense to run.sh." >&2
            echo -e ${usage_line}
            exit 1
    esac
}
main $@
