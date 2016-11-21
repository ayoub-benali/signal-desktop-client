MAKEFILE_DIR=$(shell dirname $(firstword $(MAKEFILE_LIST)))
DEPS_DIR="${MAKEFILE_DIR}/dependencies"

SBT_TRAVIS_TASKS=';clean;test'

test: $(DEPS_DIR)
	cd $(MAKEFILE_DIR)/.. && sbt -Dsbt.ivy.home=${DEPS_DIR}/.ivy2 \
	                             -Dsbt.global.base=${DEPS_DIR}/.sbtboot \
	                             -Dsbt.boot.directory=${DEPS_DIR}/.boot \
	                             ${SBT_TRAVIS_TASKS}

$(DEPS_DIR):
	mkdir -p $@

.PHONY: test
