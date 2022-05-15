# default config values
OUTPUT_FILE="NULL"
RANDOMIZE_OUTPUT_FILE=0
SEED="NULL"

for arg in "$@"
do
  case $arg in
    -o|--output)
    OUTPUT_FILE="$2"
    shift
    shift
    ;;
    -r|--randomize-output-file)
    RANDOMIZE_OUTPUT_FILE=1
    shift
    ;;
    -s|--seed)
    SEED="$2"
    shift
    shift
    ;;
  esac
done

./gradlew run --args="$OUTPUT_FILE $RANDOMIZE_OUTPUT_FILE $SEED"