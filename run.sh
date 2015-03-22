#!/bin/bash
#
# The Insight Data Engineering Code Challenge 2015-03-17
#
# Accepts a first argument specifying one of:
#   java, perl, python, go, getxts, clean, or help
#
# Daniel Lamblin


usage() {
    echo -e <<END
\n${usage_line}
\thelp\tprints this help.
\tgetxts\tfetches gutenberg books for ${dir_in}.
\tjava\truns word count and running median in java.
\tperl\truns word count and running median in perl.     [not-yet-implemented]
\tpython\truns word count and running median in python. [not-yet-implemented]
\tgo\truns word count and running median in go.         [not-yet-implemented]

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
    local usage_line="Usage: $0 {help|getxtsjava|perl|python|go|clean}\n"
    local command="${1:-java}"

    local dir_in='wc_input'
    local dir_out='wc_output'

    local gradle_cmd='./gradlew'
    local java_project_name='InsightDataEngineeringCodingChallenge'
    local java_bin_path="./build/install/${java_project_name}/bin"
    local java_wc_cmd="${java_bin_path}/wordCount"
    local java_med_cmd="${java_bin_path}/runningMedianWordsPerLine"

    #Start processing
    case ${command} in
        help)   usage ;;
        --help) usage ;;
        h)      usage ;;
        -h)     usage ;;
        getxts)
            makedirs "${dir_in}" "${dir_out}"
            echo -e "\n\t!WARNING!\tThe Gutenberg Project" \
                    "may ban you for 24h if you run this too much." \
                    "[5 second pause]" >&2
            sleep 5;
            echo "Adding some files to input directory." >&2

            local curl_cmd=$(which curl)
            local curl_status=$?
            local curl_arg='-s -o'
            local wget_cmd=$(which wget)
            local wget_status=$?
            local wget_arg='-q -O'

            local gutenberg_cache_base='http://www.gutenberg.org/cache/epub/'
            local shakespeare="${gutenberg_cache_base}100/pg100.txt"
            local ed_al_poe_1="${gutenberg_cache_base}2147/pg2147.txt"
            local ed_al_poe_2="${gutenberg_cache_base}2148/pg2148.txt"
            local whaley_tale="${gutenberg_cache_base}2489/pg2489.txt"
            local c_dickens_1="${gutenberg_cache_base}98/pg98.txt"
            local c_dickens_2="${gutenberg_cache_base}1400/pg1400.txt"
            local c_dickens_3="${gutenberg_cache_base}730/pg730.txt"
            local c_dickens_4="${gutenberg_cache_base}46/pg46.txt"

            if [[ ${curl_status} -ne 0 ]]; then
                if [[ ${wget_status} -ne 0 ]]; then
                    echo "We don't seem to have wget nor curl available" \
                         "to grab some texts." >&2
                    GET_CMD='echo'
                    GET_ARG='- Planned to fetch:'
                else
                    GET_CMD="${wget_cmd}"
                    GET_ARG="${wget_arg}"
                fi
            else
                GET_CMD="${curl_cmd}"
                GET_ARG="${curl_arg}"
            fi

            for url in ${shakespeare} ${ed_al_poe_1} ${ed_al_poe_2} \
                       ${whaley_tale} ${c_dickens_1} ${c_dickens_2} \
                       ${c_dickens_3} ${c_dickens_4}; do
                name=$(basename "${url}")
                if [[ ! -f "${dir_in}/${name}" ]]; then
                    echo "  Adding ${name}" >&2
                    "${GET_CMD}" ${GET_ARG} "${dir_in}/${name}" "${url}" >&2
                fi
            done
            exit 0
            ;;
    esac

    echo "You chose ${command}: from java, perl, python, go" \
         "(and getxts, clean, or help)." >&2

    case ${command} in
        java)
            makedirs "${dir_in}" "${dir_out}"
            echo "Good choice" >&2
            if [[ ! -x ${java_wc_cmd} || ! -x ${java_med_cmd} ]]; then
                echo "Installing: ${java_wc_cmd} and ${java_med_cmd}" >&2
                ${gradle_cmd} installApp
            fi
            ${java_wc_cmd} -i "${dir_in}" -o "${dir_out}/wc_result.txt"
            ${java_med_cmd} -i "${dir_in}" -o "${dir_out}/med_result.txt"
            ;;
        perl)
            makedirs "${dir_in}" "${dir_out}"
            echo "Not yet implemented" >&2
            ;;
        python)
            makedirs "${dir_in}" "${dir_out}"
            echo "Not yet implemented" >&2
            ;;
        go)
            makedirs "${dir_in}" "${dir_out}"
            echo "Not yet implemented" >&2
            ;;
        clean)
            echo "Clean isn't a language, but I am going to" \
                 "clean up some files." >&2
            ${gradle_cmd} clean
            rm "${dir_in}/"pg*txt
            ;;
        *)
            echo "That doesn't actually make sense to run.sh." >&2
            echo -e ${usage_line}
            exit 1
    esac
}
main $@