test: test.shadow
	./bin/test

test.shadow:
	shadow-cljs compile test

test.watch:
	./bin/test --watch

.PHONY: test
