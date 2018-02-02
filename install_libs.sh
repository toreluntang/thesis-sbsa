sudo apt-get update
wget -qO - https://packages.confluent.io/deb/4.0/archive.key | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://packages.confluent.io/deb/4.0 stable main"
sudo apt-get update && sudo apt-get install confluent-platform-oss-2.11
sudo apt-get install librdkafka1
sudo apt-get install librdkafka-dev
sudo apt-get install libssl-dev
sudo apt-get install liblz4-dev
sudo apt-get install libsasl2-dev
sudo apt-get install python3-pip
pip3 install --upgrade pip
pip3 install confluent-kafka
sudo pip3 install numpy scipy
sudo pip3 install tensorflow
sudo pip3 install h5py
sudo pip3 install keras
sudo pip3 install confluent-kafka
