to run from sources use
mvn -Ddpc_web.indexPath=<path to the index> jetty:run

eg: mvn -Ddpc_web.indexPath=../index-dpc/ jetty:run

to run with stemmer on
$ mvn  -Ddpc_web.useStemmer=true jetty:run

