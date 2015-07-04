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
    local command="${1:-java}"

    local dir_in='tweet_input'
    local dir_out='tweet_output'

    local gradle_cmd='./gradlew'
    local java_project_name='InsightDataEngineeringCodingChallenge'
    local java_bin_path="./build/install/${java_project_name}/bin"
    local java_tweetstat_cmd="${java_bin_path}/tweetCount"

    local perl_tweetstat_cmd="./src/main/perl/wordCount.pl"

    local line_tweetstat_cmd="./non-scalable_10min_oneliner1.pl"

    local go_tweetstat_cmd="go run ./src/golang/wordcount/wordcount.go"

    #Start processing
    case ${command} in
        help)   usage ;;
        --help) usage ;;
        h)      usage ;;
        -h)     usage ;;
    esac

    echo "You chose ${command}: from java, oneliner, perl, python, go, " \
         "clean, or help." >&2

    case ${command} in
        java)
            echo "While updating from v1.1 to v2.0 the Java solution has broken." >&2
            exit
            makedirs "${dir_in}" "${dir_out}"
            echo "Good choice; see all the source in src/{main,test}/java/lamblin" >&2
            if [[ ! -x ${java_tweetstat_cmd} || ! -x ${java_md_cmd} ]]; then
                echo "Installing: ${java_tweetstat_cmd} and ${java_md_cmd}" >&2
                ${gradle_cmd} installApp
            fi
            echo "Running word count." >&2
            ${java_tweetstat_cmd} -i "${dir_in}" -o "${dir_out}/wc_result.txt"
            echo "Running median word count per line." >&2
            ${java_md_cmd} -i "${dir_in}" -o "${dir_out}/med_result.txt"
            ;;
        oneliner)
          echo "While updating from v1.1 to v2.0 the Perl oneline solution has broken." >&2
          exit
            makedirs "${dir_in}" "${dir_out}"
            echo "See also the slightly better \"perl\"; and this source in ./*.pl" >&2
            local perl_cmd=$(which perl)
            if [[ $? -ne 0 ]]; then
                echo "Actually, it seems you might not have perl installed." >&2
            else
                echo "Running word count." >&2
                ${perl_cmd} ${line_tweetstat_cmd} "${dir_in}"/* > "${dir_out}/wc_result.txt"
                echo "Running median word count per line. (This is naively slow)" >&2
                ${perl_cmd} ${line_md_cmd} "${dir_in}"/* > "${dir_out}/med_result.txt"
            fi
            ;;
        perl)
          echo "While updating from v1.1 to v2.0 the Perl solution has broken." >&2
          exit
            makedirs "${dir_in}" "${dir_out}"
            echo "See also the \"oneliner\" perl; and this source in src/main/perl" >&2
            echo "Running word count." >&2
            ${perl_tweetstat_cmd} "${dir_in}"/* > "${dir_out}/wc_result.txt"
            echo "Running median word count per line." >&2
            ${perl_md_cmd} "${dir_in}"/* > "${dir_out}/med_result.txt"
            ;;
        python)
            makedirs "${dir_in}" "${dir_out}"
            echo "Not yet implemented; aw. I had under a week and java is so verbose" >&2
            ;;
        go)
          echo "While updating from v1.1 to v2.0 the Go solution has broken." >&2
          exit
            makedirs "${dir_in}" "${dir_out}"
            echo "I assume you have go installed. You may need to set your GOPATH" >&2
            echo "Running word count." >&2
            cat "${dir_in}"/* | ${go_tweetstat_cmd} > "${dir_out}/wc_result.txt"
            echo "Running median word count per line." >&2
            cat "${dir_in}"/* | ${go_md_cmd} > "${dir_out}/med_result.txt"
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
