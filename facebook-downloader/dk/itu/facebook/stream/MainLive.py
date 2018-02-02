import facebook
import time
import argparse
import json
from confluent_kafka import Producer
from keras.preprocessing.text import Tokenizer


def check_int(val):
    try:
        ival = int(val)
    except ValueError:
        raise argparse.ArgumentParser("Value {} from input has to be an integer.".format(val))
    return ival


def preprocessing_tokenize(X, num_words=1000):
    _tokenizer = Tokenizer(num_words=num_words,
                           filters='!"#$%&()*+,-./:;<=>?@[\\]^_`{|}~\t\n',
                           lower=True,
                           split=" ",
                           char_level=False)

    _tokenizer.fit_on_texts(X)
    _X = _tokenizer.texts_to_matrix(X)

    return _X


def get_tokenized_data(json_message):
    try:
        news_text = json_message['message'] + json_message['name']
    except:
        news_text = ""
    return preprocessing_tokenize([news_text])


if __name__ == "__main__":

    parser = argparse.ArgumentParser()
    parser.add_argument('--token', dest='fb_app_token',
                        help='The facebook API key, which can be fetched from https://developers.facebook.com/tools/explorer/145634995501895/')
    parser.add_argument('--page_id', dest='fb_page_id',
                        help='The page id from facebook. From https://www.facebook.com/FoxNews/ the page id is FoxNews.')
    parser.add_argument('--server', dest='kafka_bootstrap_server', help='The kafka server')
    parser.add_argument('--topic', dest='kafka_topic', help='The topic the message should be produced as')
    parser.add_argument('--sleep', dest='sleep_time', default=300,
                        help='Sleep between each request to facebook for the newest post', type=check_int)

    args = parser.parse_args()

    fb_app_token = args.fb_app_token
    fb_page_id = args.fb_page_id
    kafka_bootstrap_server = args.kafka_bootstrap_server
    kafka_topic = args.kafka_topic
    sleep_time = args.sleep_time

    p = Producer({'bootstrap.servers': kafka_bootstrap_server})

    graph = facebook.GraphAPI(access_token=fb_app_token, version='2.10')

    previous_id = 0

    while 1:
        print("Fetching newest post")
        post_and_reactions = graph.get_object(fb_page_id,
                                              fields='username,posts.limit(1){message,description,caption,name,reactions.type(LIKE).limit(0).summary(true).as(LIKE),reactions.type(LOVE).limit(0).summary(true).as(LOVE),reactions.type(WOW).limit(0).summary(true).as(WOW),reactions.type(HAHA).limit(0).summary(true).as(HAHA),reactions.type(SAD).limit(0).summary(true).as(SAD),reactions.type(ANGRY).limit(0).summary(true).as(ANGRY), created_time}')

        current_id = post_and_reactions['posts']['data'][0]['id']
        print("Newest post has id {} and previous post has id {}".format(current_id, previous_id))

        if current_id != previous_id:
            preprocessed_data = get_tokenized_data(post_and_reactions['posts']['data'][0])
            post_and_reactions['posts']['data'][0]['tokenized_data'] = preprocessed_data.tolist()

            previous_id = current_id
            p.produce(kafka_topic, json.dumps(post_and_reactions['posts']['data'][0]))
            print("Message has been produced")

        p.flush()
        print("Sleeping for {} seconds".format(sleep_time))
        time.sleep(sleep_time)
