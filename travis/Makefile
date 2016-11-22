DEPS_DIR="travis/dependencies"

SBT_TRAVIS_TASKS=';clean;clean-files;test'

test: $(DEPS_DIR)
	cd .. && sbt -Dsbt.ivy.home=${DEPS_DIR}/.ivy2 \
	                             -Dsbt.global.base=${DEPS_DIR}/.sbtboot \
	                             -Dsbt.boot.directory=${DEPS_DIR}/.boot \
	                             ${SBT_TRAVIS_TASKS}

$(DEPS_DIR):
	mkdir -p $@

.PHONY: test
