# wgslgenerator

## Usage instructions
A shell script is provided that should be used to generate WGSL code. Three flags may be specified:
| Flag | Meaning | Default value |
| ---- | ------- | ------------- |
| `-o, --output-file <argument>` | Specifies a filepath to create a file at and write generated code to. The file must end in `.wgsl`, be in an existing directory, and there must not be an existing file at the location. | Standard output |
| `-r, --randomize-output-file` | Enables randomization of created filenames by appending the random seed associated with the generated shader to it, e.g. `filename.wgsl` -> `filename12345.wgsl`. | Disabled |
| `-s, --seed <argument>` | Specifies a seed to provide to the random generator to aid reproduction of interesting shaders. The seed must be a Long number (signed 64-bit integer). | Random seed produced internally |

Notes:
- if no output filepath is provided but the `-r` flag is enabled, the output will be a file consisting of just the associated seed and the `.wgsl` extension.
- the random seed for a shader is recorded as a comment at the start of any outputted WGSL code, regardless of if the `-r` flag has been enabled or not.
