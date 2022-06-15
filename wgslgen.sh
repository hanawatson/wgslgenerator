set -e

# default config values
SEED=
OUTPUT_FILE=
RANDOMIZE_OUTPUT_FILE=0
CONFIG_FILE=
TINT_SAFE=1
NAGA_SAFE=1
USE_JAR=0

while [ $# -gt 0 ]; do
  case "${1}" in
    -h|--help)
    echo "
    USAGE: ./wgslgen.sh [OPTIONS]

    OPTIONS:
        -h, --help                                              Print help message
        -o, --output-shader <OUTPUT_SHADER>                     Path the generated shader should be written to
                                                                [default: none - shader printed to stdout]
        -c, --input-config <INPUT_CONFIG>                       Path of the config file that should be passed [default:
                                                                none - internal default config used]
        -s, --seed <SEED>                                       Seed (signed 64-bit integer) to provide to internal
                                                                random generator [default: none - random seed generated]
        -j, --use-jar                                           Use the standalone wgslgenerator jar, which must be
                                                                located in the top-level wgslgenerator directory
                                                                [default: disabled]
        -(r/R), --(enable/disable)-randomize-output-file        Enable/disable output logging if any test fails
                                                                [default: enabled]
        -(t/T), --set-tint-(safe/not-safe)                      Enable/disable output logging if all tests pass
                                                                [default: disabled]
        -(n/N), --set-naga-(safe/not-safe)                      Enable/disable printing error output to the console if
                                                                any tests fail [default: disabled]"
    exit 0
    ;;
    -s|--seed)
    if [ ! "${2}" ]; then
      echo "Error: no seed was provided."
      exit 1
    else
      SEED="${2}"
    fi
    shift
    shift
    ;;
    -o|--output-shader)
    if [ ! "${2}" ]; then
      echo "Error: no output file path was provided."
      exit 1
    else
      OUTPUT_FILE="${2}"
    fi
    # validate that the provided output path ends in .wgsl
    if [ "${OUTPUT_FILE##*.}" != wgsl ]; then
      echo "Error: provided output file is not a WGSL file."
      exit 1
    elif [ -f "${OUTPUT_FILE}" ] && [ ! -w "${OUTPUT_FILE}" ]; then
      echo "Error: existing output file at provided path cannot be written to."
      exit 1
    fi
    shift
    shift
    ;;
    -c|--input-config)
    if [ ! "${2}" ]; then
      echo "Error: no config file was provided."
      exit 1
    else
      CONFIG_FILE="${2}"
    fi
    if [ ! -f "${CONFIG_FILE}" ]; then
      echo "Error: provided config file does not exist."
      exit 1
    # validate that the provided config file ends in .json
    elif [ "${CONFIG_FILE##*.}" != json ]; then
      echo "Error: provided config file is not a JSON file."
      exit 1
    fi
    shift
    shift
    ;;
    -r|--enable-randomize-output-file)
    RANDOMIZE_OUTPUT_FILE=1
    shift
    ;;
    -R|--disable-randomize-output-file)
    RANDOMIZE_OUTPUT_FILE=0
    shift
    ;;
    -t|--set-tint-safe)
    TINT_SAFE=1
    shift
    ;;
    -T|--set-tint-not-safe)
    TINT_SAFE=0
    shift
    ;;
    -n|--set-naga-safe)
    NAGA_SAFE=1
    shift
    ;;
    -N|--set-naga-not-safe)
    NAGA_SAFE=0
    shift
    ;;
    -j|--use-jar)
    USE_JAR=1
    shift
    ;;
    *)
    echo "Error: unrecognised argument provided."
    exit 1
    ;;
  esac
done

if [ "${USE_JAR}" -eq 1 ]; then
  java -jar wgslgenerator.jar "${RANDOMIZE_OUTPUT_FILE}" "${TINT_SAFE}" "${NAGA_SAFE}" \
  "conf:${CONFIG_FILE}" "out:${OUTPUT_FILE}" "seed:${SEED}"
else
  ./gradlew run --args="${RANDOMIZE_OUTPUT_FILE} ${TINT_SAFE} ${NAGA_SAFE} \
  conf:${CONFIG_FILE} out:${OUTPUT_FILE} seed:${SEED}" -quiet
fi