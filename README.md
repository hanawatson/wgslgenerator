# wgslgenerator

This tool can be used to generate random shaders in [WebGPU Shading Language](https://gpuweb.github.io/gpuweb/wgsl/). It
can be used with [wgslrunner](https://github.com/hanawatson/wgslrunner) to faciliate the testing of
the [Dawn](https://dawn.googlesource.com/dawn/) and [wgpu](https://github.com/gfx-rs/wgpu) WebGPU APIs, as well as their
respective WGSL compilers [Tint](https://dawn.googlesource.com/tint) and [naga](https://github.com/gfx-rs/naga).

## Prerequisites

wgslgenerator can be cloned using:

```
$ git clone https://github.com/hanawatson/wgslgenerator
```

## Usage instructions

wgslgenerator can be used by running its associated shell script, `wgslgenerator.sh`. Several flags may be specified.
| Flag | Meaning | Default value |
| ---- | ------- | ------------- |
| `-o <argument>`, `--output-file <argument>` | The path that the generated shader should be saved to - must end in `.wgsl` and be in an existing, writeable directory | None - if unspecified, the shader will be printed to standard output |
| `-c <argument>`, `--config-file <argument>` | The path of the configuration JSON file that should be used (more details in the corresponding section below) | None - if unspecified, the default configuration will be used |
| `-s <argument>`, `--seed <argument>` | Specifies a seed to provide to the random generator to aid reproduction of interesting shaders - must be a signed 64-bit integer | None - if unspecified, a random seed will be produced internally |
| `-r`, `--randomize-output-file` | Enables randomization of created filenames by appending the random seed associated with the generated shader to it, e.g. `filename.wgsl` -> `filename12345.wgsl` | Disabled |
| `--set-tint-(safe/not-safe)` | Enables/disables safety toggle for Tint, e.g. prevents generation of code known to be unsupported by Tint | Enabled |
| `--set-naga-(safe/not-safe)` | Enables/disables safety toggle for naga, e.g. prevents generation of code known to be unsupported by naga | Enabled |
| `j`, `--use-jar` | Enables usage of the standalone `wgslgenerator.jar` jar (see the following README section) | Disabled |

Notes:

- if no output filepath is provided but the `-r` flag is enabled, the output will be a file consisting of just the
  associated seed and the `.wgsl` extension.
- the random seed for a shader is recorded as a comment at the start of any outputted WGSL code, regardless of if
  the `-r` flag has been enabled or not.
  
## Standalone jar

wgslgenerator can be compiled into a standalone 'fat jar' that can then be run on its own. To build the jar, the following should be run:

```
$ ./gradlew jar
```

The jar will then be built and outputted to the `build/libs` directory. It can be used by placing `wgslgenerator.jar` and `wgslgen.sh` in the same directory (i.e. both at top-level) and then specifying the `--use-jar` flag when running `wgslgen.sh`. All other flags and behaviors of `wgslgen.sh` remain the same (as above) regardless of if the `--use-jar` flag is passed.

As with the full Gradle project, `wgslgenerator.jar` should not be run without the helper script, to avoid argument expectation-related errors. No other files (e.g. those in the `resources` directory) are required to run the jar.

## JSON configuration

wgslgenerator can be configured using a JSON file that conforms to its expected config input. Default configuration
files can be found in the `src/main/kotlin/resources` directory, and can be used as demonstrations of the required
format of the file.
Several values within the file are deemed as probabilities, but in reality there is no requirement to have the values be
less than 1; this is to avoid having to lower every other probability if, for instance, the user wishes to have one
type/expression/statement appear much more frequently than others.
Additionally, care should be taken with some values (e.g. maximums such as `max_statements_in_body`, probabilities of
generating more of something such as `generate_parentheses_around_expression`), as setting them too high may cause
extremely long runtimes or terminations due to overflow.

If used with wgslrunner, some wgslgenerator configuration options can be useful in disabling/suppressing parts of the
generator that generate code that may trigger bugs in the tools tested by wgslrunner. Configuration files can be directly
passed to wgslrunner: more about this can be found in the wgslrunner documentation.

## Requirements

- JDK with Java version >= 1.8
