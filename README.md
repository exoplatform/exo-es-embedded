# elasticsearch embedded add-on

This add-on embedded an ElasticSearch in eXo Platform Server.

Only localhost node can access the cluster and only localhost request to the node are accepted by default.

## Installing

1. Build the add-on with Maven
```
$ mvn clean package
```
2. Copy ```exo-es-embedded/packaging/target/exo-es-embedded-packaging-1.0.x-SNAPSHOT.zip``` to ```$PLATFORM_HOME/addons```
3. Add the following entry in $PLATFORM_HOME/addons/local.json:
```
[
 {
 "id": "exo-es-embedded",
 "version": "1.0.x-SNAPSHOT",
 "name": "elasticsearch embedded Add-on",
 "description": "The elasticsearch embedded Add-on",
 "downloadUrl": "file://./exo-es-embedded-packaging-1.0.x-SNAPSHOT.zip",
 "vendor": "eXo platform",
 "license": "LGPLv3",
 "supportedDistributions": ["community","enterprise"],
 "supportedApplicationServers": ["tomcat","jboss"]
 }
]
```
4. Install the addon:
```
$ ./addon install exo-es-embedded --snapshots
```
5. Start PLF

## Install Plugins

_Only working on Tomcat distribtution._

After installation of plugins you need to restart the server

### Head

Unzip https://github.com/mobz/elasticsearch-head/archive/master.zip to ```$PLATFORM_HOME/es/plugins/head/_site```

After restart you can access it from http://localhost:9200/_plugin/head/

For more documentation about head plugin you can take a look to official documentation: http://mobz.github.io/elasticsearch-head/

### Marvel

Unzip http://download.elasticsearch.org/elasticsearch/marvel/marvel-latest.zip to ```$PLATFORM_HOME/es/plugins/marvel```

After restart you can access it from http://localhost:9200/_plugin/marvel/kibana/index.html

For more documentation about marvel plugin you can take a look to official documentation: https://www.elastic.co/products/marvel

### Others

Like Head or Marvel just unzip the plugin in ```$PLATFORM_HOME/es/plugins/[plugin-name]``` and restart the server

## Testing

After starting eXo Platform you can test as follow:

```
$ curl -v localhost:9200
$ curl -XPUT 'http://localhost:9200/blog/user/tclement' -d '{ "name" : "Thibault Clement" }'
$ curl -v localhost:9200/blog
```