
# Contributing

## Create pull request
PR are always welcome, even if they only contain small fixes like typos, or a few lines of code. If there will be a significant effort, please document it as an issue and get a discussion going before starting to work on it.

Please submit a PR broken down into small changes bit by bit. A PR consisting of a lot of features and code changes may be hard to review. It is recommended to submit PRs in an incremental fashion.

Before submitting your PR, please ensure that your code adheres to the project's coding standards and that all tests pass.

### Required Pre-submission Checks

The CI pipeline runs tests on multiple JDK versions (8, 11, 17, 21, 25) and operating systems (Ubuntu, Windows, macOS). Please run the following Maven commands locally to verify your changes:

**1. Standard Build Test:**
```bash
./mvnw -V --no-transfer-progress -Pgen-javadoc -Pgen-dokka clean package
```

**2. Reflect Mode Test:**
```bash
./mvnw -V --no-transfer-progress -Dfastjson2.creator=reflect clean package
```

Or use the shortcut (if available):
```bash
mvn validate
mvn test
```

**Note:** Your PR should not break any existing tests. If your changes affect multiple modules, please ensure all related tests pass in both standard and reflect modes.

This [Wiki](https://github.com/alibaba/fastjson2/wiki) contains information about scenarios structure, design and api documents, how to use, how to run it, and more.

Note: If you split your pull request to small changes, please make sure any of the changes goes to master will not break anything. Otherwise, it can not be merged until this feature complete.

## Report issues
It is a great way to contribute by reporting an issue. Well-written and complete bug reports are always welcome! Please open an issue and follow the template to fill in required information.

Before opening any issue, please look up the existing issues to avoid submitting a duplication.
If you find a match, you can "subscribe" to it to get notified on updates. If you have additional helpful information about the issue, please leave a comment.

When reporting issues, always include:

* Which version you are using.
* Steps to reproduce the issue.
* Snapshots or log files if needed

Because the issues are open to the public, when submitting files, be sure to remove any sensitive information, e.g. username, password, IP address, and company name. You can
replace those parts with "REDACTED" or other strings like "****".

