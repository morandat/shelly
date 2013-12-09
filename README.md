# Shelly

A simple annotation based language to describe command lines and interactive shell.
This language look like [Cliche][] with more powerful constructs.

## Features



## Annotations

`@Group`
 : Declare a group of commands
 
`@Command`
 : Declare a command

`@Option`
 : Declare an option

`@Context`
 : Declare a group of options/commands

`@Description`
 : Add a description to any item

`@Default`
 : Mark a command as a default one.

## Dependencies

This code has no other dependencies than JDK 1.7 (used for multi-catch blocks and diamond operator) for normal user.
During test phase, the test-ng framework and hamcrest are also used.

## Exemple

```java

class Test {
}
```

## TODO

- [x] Boolean options
- [ ] Stop parse when -- is found
- [ ] Error handler
- [ ] Clean way of doing meta objects


[Cliche]: https://code.google.com/p/cliche/