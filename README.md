# Tabitha

[![Build Status](https://badge.buildkite.com/bd90a813b49a2777b80079355157fbaadf74ea44aa5c7ddd86.svg)](https://buildkite.com/widen/tabitha)
![Maven Central](https://img.shields.io/maven-central/v/com.widen.oss/tabitha-core)

Tabular data reading, writing, and processing library for JVM languages.

This library is currently under active development and is pre-release.

Made with :heart: by Widen.

## Features

- **Immutable data types:** Data is always immutable, which allows methods to be zero-copy operations.
- **Lazy evaluation:** Data is streamed lazily, allowing for complex data processing and producing while using constant memory.
- **Multi-threaded processing:** Input data can be consumed and processed fast and efficiently using a parallel executor.
- **Multiple formats:** Tabitha supports input and output in multiple formats, including CSV, TSV, XLSX, and XLS.

## Installation

Tabitha comes in several modules; a core module, which provides the API, and plugin modules that provide specific file format support. The core module provides no format support out of the box, so you'll likely want to include at least one plugin.

With Gradle:

```
// Tabitha API
compile 'com.widen:tabitha-core:0.4.0'
// Support for CSV, TSV, etc
compile 'com.widen:tabitha-delimited:0.4.0'
// Support for XLSX and XLS
compile 'com.widen:tabitha-excel:0.4.0'
```

Other dependency managers should be similar.

## Documentation

Documentation will be available in this repository, but is not yet written. For now, check the source for interesting comments that may help explain usage. Ideally, the class and method names should be pretty clear as to what they do.

## Examples

Check out the [`examples`](examples) directory for some simple examples of how you can use Tabitha.

## License

Available under the MIT license. See [the license file](LICENSE.md) for details.
