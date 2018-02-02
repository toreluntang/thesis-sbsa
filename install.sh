myip="$(dig +short myip.opendns.com @resolver1.opendns.com)"
mkdir docker
mv docker-compose-single-broker.yml docker/docker-compose-single-broker.yml
mkdir flink
mkdir kafka
mkdir jobs
sh docker-install.sh
wget -P flink http://mirrors.dotsrc.org/apache/flink/flink-1.3.2/flink-1.3.2-bin-hadoop27-scala_2.11.tgz
wget -P kafka http://mirrors.dotsrc.org/apache/kafka/1.0.0/kafka_2.11-1.0.0.tgz
wget -P jobs https://www.dropbox.com/s/ouzq8s2029lph1b/flink-jobs.tar.gz?dl=0
tar zxvf flink/flink-1.3.2-bin-hadoop27-scala_2.11.tgz -C flink
rm -fr flink/flink-1.3.2-bin-hadoop27-scala_2.11.tgz
tar zxvf kafka/kafka_2.11-1.0.0.tgz -C kafka
rm -fr kafka/kafka_2.11-1.0.0.tgz
tar zxvf jobs/flink-jobs.tar.gz\?dl\=0 -C jobs
rm -fr jobs/flink-jobs.tar.gz\?dl\=0
apt-get install default-jdk
echo "********************************CMDS********************************"
echo "server ip is: ${myip}"
echo "nano flink/flink-1.3.2/conf/flink-conf.yaml"
echo "nano flink/flink-1.3.2/conf/slaves"
echo "nano docker/docker-compose-single-broker.yml"
echo "docker swarm init"
echo "docker stack deploy -c docker/docker-compose-single-broker.yml kk"
echo "./flink/flink-1.3.2/bin/start-cluster.sh"
echo "./flink/flink-1.3.2/bin/taskmanager.sh start-foreground"
echo "./flink/flink-1.3.2/bin/flink run -d -p 1 -m ${myip}:6123 jobs/simplejob-preprocess-1.0-SNAPSHOT.jar --bootstrap.servers localhost:9092 --topic-in part1 --topic-out flink-classify-in"
echo "./flink/flink-1.3.2/bin/flink run -d -p 1 -m ${myip}:6123 jobs/simplejob-classify-1.0-SNAPSHOT.jar --bootstrap.servers localhost:9092 --topic-in flink-classify-in --topic-out pp"
echo "============================WEEEHOOO================================"
