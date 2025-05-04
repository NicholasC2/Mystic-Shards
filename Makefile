ifneq (,$(wildcard ./.env))
    include .env
    export
endif

install:
	del target\Mystic-Shards-*.jar
	mvn package
	copy target\Mystic-Shards-*.jar $(MINECRAFT_PLUGIN_PATH)

check-env:
	echo MINECRAFT_PLUGIN_PATH: $(MINECRAFT_PLUGIN_PATH)