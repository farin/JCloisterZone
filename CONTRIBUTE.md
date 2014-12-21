# Contributing to [JCloisterZone](http://jcloisterzone.com/)



## I found a bug. How to report it?

Sorry for that. The preferred way is to [look through the existing issues](https://github.com/farin/JCloisterZone/issues) on the project's GitHub pages. If the bug has not already been reported you can create a new issue. Alternatively send an email. Attaching a saved game and `error.log` (if created) may be helpful.



## In which ways can I contribute?

I am glad for any help — suggestions, localizations, design or programming contributions, beta testing or donations are welcomed. Just get in touch!



## I'm a programmer, can I change and improve the code?

Yes, the help would be much appreciated!


### Code style
Regarding code style like indentation and whitespace, **follow the conventions you see used in the source already.**


### Running and modifying the code

Build your own version from source by reading [BUILDING.md](https://github.com/farin/JCloisterZone/blob/master/BUILDING.md).



### Submitting pull requests

1. Create a new branch, please don't work in your `master` branch directly.
1. Add failing tests for the change you want to make. Run `mvn test` to see the tests fail.
1. Fix stuff.
1. Run `mvn test` to see if the tests pass. Repeat steps 2-4 until done.
1. Update the documentation to reflect any changes.
1. Push to your fork and submit a pull request.
