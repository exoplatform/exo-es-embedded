# elasticsearch embedded add-on

This add-on embedded an ElasticSearch in eXo Platform Server.

## Installing

1. Build the add-on with Maven
```
$ mvn clean package
```
1. Copy ```elasticsearch-embedded/packaging/target/elasticsearch-embedded-packaging-1.0.x-SNAPSHOT.zip``` to ```$PLATFORM_HOME/addons```
1. Add the following entry in $PLATFORM_HOME/addons/local.json:
```
[
 {
 "id": "exo-elasticsearch-embedded",
 "version": "1.0.x-SNAPSHOT",
 "name": "elasticsearch embedded Add-on",
 "description": "The elasticsearch embedded Add-on",
 "downloadUrl": "file://./elasticsearch-embedded-packaging-1.0.x-SNAPSHOT.zip",
 "vendor": "eXo platform",
 "license": "LGPLv3",
 "supportedDistributions": ["community","enterprise"],
 "supportedApplicationServers": ["tomcat","jboss"]
 }
]
```
1. Install the addon:
```
$ ./addon install exo-elasticsearch-embedded --snapshots
```
1. Start PLF

## Testing

After starting eXo Platform you can test as follow:

```
$ curl -v localhost:9200
$ curl -XPUT 'http://localhost:9200/blog/user/tclement' -d '{ "name" : "Thibault Clement" }'
$ curl -v localhost:9200/blog
```

## Install Plugins
The elasticsearch embedded add-on is packaged with two elasticsearch plugin:
. https://github.com/Asquera/elasticsearch-http-basic to add authentication to ES
. https://github.com/mobz/elasticsearch-head to browse and interact with your ES cluster

To install it you just need to unzip the ```$PLATFORM_HOME/elasticsearch-plugins.zip``` and restart the server

After installation of plugins, only localhost request or username/password request can access.

### Testing elasticsearch-http-basic plugin

**Authorized**

| Request | Response Code      | Reason |
|-------------------------------------------------------------|-------|----------------------------------------------|
| ```$ curl -v localhost:9200``` | 200 | localhost is configured as whitelisted ip |
| ```$ curl -XPUT 'http://localhost:9200/blog/user/tclement' -d '{ "name" : "Thibault Clement" }'``` | 200 | localhost is configured as whitelisted ip |
| ```$ curl -v --user root:gtn no_localhost:9200/blog``` | 200 | root/gtn has set in configuration |

**Not Authorized**

| Request | Response Code      | Reason|
|-------------------------------------------------------------|-------|----------------------------------------------|
| ```$ curl -v no_localhost:9200``` | 200 | return "{\"OK\":{}}" although Unauthorized |
| ```$ curl -XPUT 'no_localhost:9200/blog/user/tclement' -d '{ "name" : "Thibault Clement" }'``` | 401 | Unauthorized as not localhost |
| ```$ curl -v --user root:notgtn no_localhost:9200/blog``` | 401 | Unauthorized as wrong password |

_DISCLAIMER: elasticsearch-http-basic is working only on tomcat PLF distributions_

### Testing head plugin

http://localhost:9200/_plugin/head/

For more documentation about head plugin you can take a look to official documentation: http://mobz.github.io/elasticsearch-head/
