### docker
---
- 실행방법
    ```
    docker build -t kafka-practice:1.0.0
    docker-compose up
    ```
- DockerFile의 경우 일종의 클래스
    - 이미지를 구축
    - 기존 이미지에서 customize하여 사용할 수 있음
- docker-compose
    - 일종의 팩토리?
    - 도커 인스턴스를 생성하는 부분
- DockerFile [Blog](http://blog.naver.com/PostView.nhn?blogId=alice_k106&logNo=220646382977&parentCategoryNo=7&categoryNo=&viewDate=&isShowPopularPosts=true&from=search)
    - FROM
        - 기반 이미지 지정
        ```
        FROM ubuntu:14.04
        ```
    - RUN
        - 명령어 입력
        - bash 쉘에서의 명령어와 유사
        ```
        RUN mkdir /abcde
        RUN echo "Hello, World!"    
        ```
    - ADD
        - build 명령 중에, HOST fs에서 파일을 가져오는 것
        - Image에 파일을 더함(ADD)
        - ADD <host fs의 파일>:<추가될 이미지 디렉토리>
        ```
        ADD test.txt /
        ```
    - CMD / ENTRYPOINT
        - build로 이미지 생성, run시 효력
        - ENTRYPOINT를 두어, 해당 컨테이너 실행시, 바로 실행 수 있도록 한다.
        ```
        ENTRYPOINT /entrypoint.sh
        ```

    - docker build -t <Image Name>:<Version>
        - \-t 옵션을 주어, 이미지 이름 및 버전 지정이 가능
- docker volume local
    - docker에서 dirver : local로 두었을 때, 실제 위치는
        ```
        /Users/wshid/Library/Containers/com.docker.docker/Data/vms/0
        ```
        - 해당 디렉터리에 위치하는 tty에 ```screen tty```로 접속한다.
        - ```/var/lib/docker``` 하단에 다양한 정보가 기록되는 것을 확인할 수 있다.
    - screen 종료시, Ctrl + A + D 를 누르면 된다.
- Dockerfile none 제거하기
    - [refer](https://www.slipp.net/questions/536)
    - untagged 이미지 조회
        ```
        docker images -f "dangling=true" -q
        ```
    - docker rmi 명령어 연결
        ```
        docker rmi $(docker images -f "dangling=true" -q)
        ```
- docker stop all
    ```
    docker stop $(docker ps -aq)
    ```
- Dockerfile
    - 기본적으로, /root 하단에 mkdir이 불가능하다.
        - DockerFile에서 root를 만진 후, volumes 설정때문에 발생하는 오류일 수 있다.
        - 맞다 volumes에서 로드하기 때문에 발생하는 에러였음
        - 더군다나 bash에 관련된 문제도 해결되었다.
            - 초기 로그인시 깨지는 현상 해결
        - 디렉터리를 겹치지 않게 해주어야 할 듯 하다.
- docker-compose up / all의차이
    - https://stackoverflow.com/questions/41461304/acess-bash-docker-compose-with-centos-image
    - docker-compose up의 경우 지속되는 서비스를,
    - run의 경우 일시 서비스를 의미한다.
        - 하지만 run의 경우, 실행시마다 새로운 컨테이너를 실행시키는 것으로 보인다.
    - docker-compose.yml에서 tty 옵션을 켜주도록 한다.
- docker add hosts
    - 특정 네트워크 하단에 설정을 하면 된다.
    - docker-compose.yml
        ```
        #origin
        networks:
            - kafkanet

        #new
        networks:
            kafkanet:
                aliases:
                - peter-zk001
                - peter-kafka001
        ```
    - 결과
        ```
        wshid-MacBook-Pro:kafka_docker wshid$ docker-compose up
        Recreating 3dd3658a806e_node1 ... done
        Recreating 04cd7976ae5d_node2 ... done
        Recreating a17c3c54f966_node3 ... done
        Attaching to node2, node1, node3
        ```
        - Recreating 되어 제대로 먹는 것을 확인할 수 있다.
        - 내부에서 **ping**이나 **ssh**로 노드 연결을 확인할 수 있었다.
    