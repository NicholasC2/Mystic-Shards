install:
	del target\Mystic-Shards-*.jar
	mvn package
	copy target\Mystic-Shards-*.jar "C:\Users\nicca\Documents\Minecraft\Java\Servers\Testing Server\plugins"