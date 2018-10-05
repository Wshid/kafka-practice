### MEMO(on kafka-test wiki)
- docker를 사용한다.
- 3개의 노드를 사용한다.(node1, node2, node3)
- 각 브로커(노드)에 zookeeper와 kafka를 설치한다.
- docker 노드 구축 명령어
```
    docker run --name node<X> -v path/nodeX:/root ubuntu

    # 컨테이너 실행
    docker start node1 node2 node3

    # container bash로 접속
    docker exec -it node1 /bin/bash
```