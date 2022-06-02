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
    -o|--output)
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
    -c|--config-file)
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
    --set-tint-safe)
    TINT_SAFE=1
    shift
    ;;
    --set-tint-not-safe)
    TINT_SAFE=0
    shift
    ;;
    --set-naga-safe)
    NAGA_SAFE=1
    shift
    ;;
    --set-naga-not-safe)
    NAGA_SAFE=0
    shift
    ;;
    -u|--use-jar)
    USE_JAR=1
    shift
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