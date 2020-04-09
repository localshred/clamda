# (= 'clamda (+ 'clojure 'ramda))

[![cljdoc badge](https://cljdoc.org/badge/localshred/clamda)](https://cljdoc.org/d/localshred/clamda/CURRENT)
[![CircleCI](https://circleci.com/gh/localshred/clamda/tree/master.svg?style=svg)](https://circleci.com/gh/localshred/clamda/tree/master)

`clamda` is `clojure.core` on Ramda steroids.

Clojure is an utterly brilliant programming language that has fundamentally changed the way I think about writing software.

[Ramda](https://ramdajs.com) is a fantastic functional library for the JavaScript/Node community, and though I am _loving_
Clojure, I do miss `Ramda`. Like... a lot.

## `clojure.core` warts

Clojure is a fantastic functional language with an incredible core library. But some of the design choices
and naming leave me wanting a more consistent way to work with the core utilities. The downside of _some_
of `clojure.core` include:

+ Reliance on variadic techniques which are in stark contrast to data-last/currying. (See `assoc`, `update`, et. al)
+ Weird naming imbalances (e.g. `every?` vs `some`, not to be confused with `some?` which is `(complement nil?)`...)

## Ramda saves the day?

Yes, I'm telling you Clojure could learn a few things from a scrappy JS library that I have really grown to love.
`Ramda` has a pretty good API where:

+ Most functions are curried and the data to be operated on comes last.
+ Solid functional primitives for replacing logic flows like `ifElse`, `when`, `tryCatch`, etc.
+ Advanced primitives like `applySpec` and `evolve` for functional map/object construction.

---

I wrote `clamda` to fill in some missing gaps in the clojure core API, and I hope you find them useful. Some things I did:

+ Provided currying primitives `curry`, `curry-n`, and even a `defcurry` macro.
+ Wrote pretty much the rest of clamda using `defcurry`.
+ Used "clojurized" `Ramda` fn names (like `pathSatisfies` -> `path-satisfies`, or `pathEq` -> `pathEq`).
+ Used "clojure-y" names where it seemed a better fit for layering on top of existing names (like `>`, `>=`, `if`, `when`, etc).
+ Wrap existing clojure fns that had the arguments in the "wrong" order, and curried the fn (like `select-keys`, now called `pick`).
+ Made thread-frist core fns curried/thread-last (see `assoc`, `assoc-in`, `update`, `update-in`).

You'll also notice that I haven't included everything from `Ramda`, at least not yet. My guess is that there are plenty of fns
in `Ramda` that won't need translation simply because Clojure already supports (or negates the need for) some of them.

And before you get all mad about Rich being "Right About Everything", I don't really care. Rich is brilliant and made an amazing
language, but I like curried/data-last fns more than I like variadic fns and macro thread rewriting of argument passing.
Both approaches have a lot of value and I plan on continuin to write variadic functions (sometimes with `curry-n` to really
turn up the juice). I know, salty. How do you know you won't like it until you try it though...

## Clojars Deployment

1. Update `<version>` in `pom.xml`
1. Make a commit for the version bump.
1. `./bin/swap` to go into "maven mode" directory structure.
1. `mvn deploy` to push to clojars.
1. `./bin/swap` back out of "maven mode".
1. Go verify on [clojars](https://clojars.org/localshred/clamda) and [cljdoc.org](https://cljdoc.org/d/localshred/clamda/0.1.0/doc/readme).
