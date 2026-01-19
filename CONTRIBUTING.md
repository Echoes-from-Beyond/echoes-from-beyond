# Contributing

## Pull Requests
Pull requests are welcome! If possible, please allow edits from maintainers, as it makes minor changes easier.

Commit messages should adhere to [conventional commits guidelines](https://gist.github.com/qoomon/5dfcdf8eec66a051ecd85625518cfd13).

If you're fixing a bug, please create an issue first and reference it in your pull request. Bug fixes should _not_ contain breaking changes or introduce new features!

If you're adding a new feature, you don't _need_ to create an issue first, but it's HIGHLY recommended to do so in order to find out if your feature will be accepted before you make it!

Our CI system detects invalid code style and will fail the build. Before you commit, make sure you run `./gradlew spotlessApply`. Also see the [style section](#style).

By contributing, you agree to license the contributed code under the [GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html#license-text). Please do not submit code under different licenses.

## Style

We use the Spotless plugin to enforce [google-java-format](https://github.com/google/google-java-format). This automatically identifies and fixes many "small" formatting errors across our Java codebase.

However, not all aspects of our style are (or can be) automatically enforced. These are outlined below.

While you should _try_ to adhere to these guidelines, don't stress about getting everything right, especially if it's your first time contributing. We don't decline contributions because of style violations, but we might ask you to make changes.

Note: the words MUST, MUST NOT, SHOULD, SHOULD NOT, and MAY are intended to be interpreted as per [RFC 2119](https://www.rfc-editor.org/rfc/rfc2119).

### General
* One SHOULD use `Optional` instead of returning `null`, except when:
  * There is a significant and _demonstrable_ performance degradation caused by returning `Optional` compared to returning `null`.
  * APIs that are designed to "mimic" or wrap Java APIs, like `Map#get`, MAY return `null` instead of an empty `Optional`.
* One MUST NOT annotate anything with `@NotNull`!
  * Instead, one SHOULD apply `@NullMarked` to every class file.
    * One exception is when "unknown nullability" is actually desired throughout the entire file.
* One MUST use `jspecify` annotations instead of `jetbrains` where possible.
* One MUST use explicit nullchecks to validate untrusted input, such as deserialized data from a file.
* One SHOULD NOT use explicit nullchecks (like `Objects#requireNonNull`) in cases where implicit ones are equivalent.
  * One MAY use explicit nullchecks in cases where implicit ones would have lesser or no effect on nullness detection.
* One SHOULD limit external dependencies. 
  * For example, small utility methods, even if they "duplicate" code found in external packages, are preferred.

### Testing
* Non-trivial logic SHOULD be unit tested.
* No individual unit test SHOULD take much more than half a second to complete on a relatively modern desktop computer.
  * If long-running tests are needed, they MUST be annotated with `@Disabled` so they don't slow down CI builds.
* Since our testing suite has multithreading enabled, tests MUST NOT share state in a non-threadsafe manner.
* Code to be tested SHOULD provide an API to facilitate testing whenever necessary.
  * Such an API MUST be documented both with explicit Javadoc and annotated with `@VisibleForTesting`.
* Tests MUST be deterministic.
  * Tests MUST NOT perform file IO (aside from console output on failure), network access, or random number generation.
    * If the code to be tested does any of these things as a matter of course, the code to be tested SHOULD provide testing-only APIs that allow the tests to be run in a deterministic way.
  * Tests MUST NOT be platform-dependent.

### Documentation & Commenting
* One SHOULD NOT use multiline implementation comments.
  * This does not apply to Javadoc.
  * This excludes the license header.
* One SHOULD add implementation comments liberally for _complex_ code, or code that is especially bug-prone.
* One MAY use `// TODO` comments when reasonable.
* One SHOULD link to existing code for further reading.
* One SHOULD wrap comment lines at 100 characters.