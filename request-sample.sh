#!/bin/bash
# send a request to local akka http sever.

for index in `seq 1 100`
do
    for id in 1 2 3 4 5 6 7 8 9 10
    do
        echo "{\"userId\":\"${id}-${index}\",\"key\":\"1\"}" |
        curl -H "Content-type: application/json"  \
            -XPOST localhost:8080/api/v1/sample -d @-
            #-XPOST localhost:8080/api/v1/reflect -d @-
            #-XPOST localhost:8080/api/v1/one -d @-
    done
done

