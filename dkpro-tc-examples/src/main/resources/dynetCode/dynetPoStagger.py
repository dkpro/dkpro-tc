from sys import argv
from collections import Counter, defaultdict
from itertools import count
import random

import dynet as dy
import numpy as np

if  __name__ =='__main__':

	# argv[1] is the constant to which dynet reachts to set the seed value
	print("Seed: ", argv[2])
	# argv[3] is the constant to which dynet reachts to set the working memory
	print("Memory: ", argv[4])	
	print("Train Data: ", argv[5])
	print("Train Label: ", argv[6])
	print("Test Data: ", argv[7])	
	print("Test Label: ", argv[8])	
	print("Embedding: ", argv[9])	
	print("Result-Out: ", argv[10])	

	trainSeq = argv[5]
	trainLabel = argv[6]
	testSeq = argv[7]
	testLabel = argv[8]	
	embedding = argv[9]
	prediction = argv[10]

class Vocab:
    def __init__(self, w2i=None):
        if w2i is None: w2i = defaultdict(count(0).next)
        self.w2i = dict(w2i)
        self.i2w = {i:w for w,i in w2i.items()}
    @classmethod
    def from_corpus(cls, corpus):
        w2i = defaultdict(count(0).__next__)
        for sent in corpus:
            [w2i[word] for word in sent]
        return Vocab(w2i)

    def size(self): return len(self.w2i.keys())

def load_embeddings_file(file_name, sep=" ",lower=False):
    """
    load embeddings file
    """
    emb={}
    for line in open(file_name, errors='ignore', encoding='utf-8'):
        try:
            fields = line.strip().split(sep)
            vec = [float(x) for x in fields[1:]]
            word = fields[0]
            if lower:
                word = word.lower()
            emb[word] = vec
        except ValueError:
            print("Error converting: {}".format(line))

    print("loaded pre-trained embeddings (word->emb_vec) size: {} (lower: {})".format(len(emb.keys()), lower))
    return emb, len(emb[word])

def read(data, labels):
    """
    Read a POS-tagged file where each line is of the form "word1/tag2 word2/tag2 ..."
    Yields lists of the form [(word1,tag1), (word2,tag2), ...]
    """
    sents = []
    
    f = open(data)
    sentences=f.readlines()
    f.close()
    
    f = open(labels)
    labels=f.readlines()
    f.close()
    
    for seq, labels in zip(sentences,labels):
        s = seq.strip().split()
        l = labels.strip().split()
        
        sent=[]
        for se, le in zip(s,l):
        	sent.append((se,le))
        sents.append(sent)
    
    return sents


train=list(read(trainSeq, trainLabel))
dev=list(read(testSeq, testLabel))
words=[]
tags=[]
chars=set()
wc=Counter()
for sent in (train+dev):
    for w,p in sent:
        words.append(w)
        tags.append(p)
        chars.update(w)
        wc[w]+=1
words.append("_UNK_")
chars.add("<*>")

vw = Vocab.from_corpus([words]) 
vt = Vocab.from_corpus([tags])
vc = Vocab.from_corpus([chars])
UNK = vw.w2i["_UNK_"]

nwords = vw.size()
ntags  = vt.size()
nchars  = vc.size()

# DyNet Starts

model = dy.Model()
trainer = dy.AdagradTrainer(model)

NUM_LAYERS = 1

embeddings, emb_dim = load_embeddings_file(embedding)
# init model parameters and initialize them
WORDS_LOOKUP = model.add_lookup_parameters((nwords, emb_dim))
CHARS_LOOKUP = model.add_lookup_parameters((nchars, 20))
init = 0
UNK_vec = np.random.rand(emb_dim)

notfound = found= 0.0
for word in vw.w2i.keys():
    # for those words we have already in w2i, update vector, otherwise add to w2i (since we keep data as integers)
    if word in embeddings.keys():
        found+=1
        #print("found ["+word+"] in w2i")
        WORDS_LOOKUP.init_row(vw.w2i[word], embeddings[word])
    else:
        notfound+=1
        WORDS_LOOKUP.init_row(vw.w2i[word], UNK_vec)

print("did not find embeddings for %.1f percent of the vocabulary " % (notfound/(found+notfound)*100))

#WORDS_LOOKUP = model.add_lookup_parameters((nwords, 128))
#CHARS_LOOKUP = model.add_lookup_parameters((nchars, 20))
p_t1  = model.add_lookup_parameters((ntags, 30))

# MLP on top of biLSTM outputs 100 -> 32 -> ntags
pH = model.add_parameters((32, 50*2))
pO = model.add_parameters((ntags, 32))

char_emb_dim=50

# word-level LSTMs
# input dimension is word-vector+fwd.CharVector+bckwd.CharVector-Length
fwdRNN = dy.LSTMBuilder(NUM_LAYERS, emb_dim+(char_emb_dim*2), 50, model) # layers, in-dim, out-dim, model
bwdRNN = dy.LSTMBuilder(NUM_LAYERS, emb_dim+(char_emb_dim*2), 50, model)


# char-level LSTMs
cFwdRNN = dy.LSTMBuilder(NUM_LAYERS, 20, char_emb_dim, model)
cBwdRNN = dy.LSTMBuilder(NUM_LAYERS, 20, char_emb_dim, model)

def word_rep(w, cf_init, cb_init):
    pad_char = vc.w2i["<*>"]
    char_ids = [pad_char] + [vc.w2i[c] for c in w] + [pad_char]
    char_embs = [CHARS_LOOKUP[cid] for cid in char_ids]
    fw_exps = cf_init.transduce(char_embs)
    bw_exps = cb_init.transduce(reversed(char_embs))

    return dy.concatenate([ WORDS_LOOKUP[vw.w2i[w]], fw_exps[-1], bw_exps[-1] ])

    # if wc[w] > 5:
    #     w_index = vw.w2i[w]
    #     return WORDS_LOOKUP[w_index]
    # else:
    #     pad_char = vc.w2i["<*>"]
    #     char_ids = [pad_char] + [vc.w2i[c] for c in w] + [pad_char]
    #     char_embs = [CHARS_LOOKUP[cid] for cid in char_ids]
    #     fw_exps = cf_init.transduce(char_embs)
    #     bw_exps = cb_init.transduce(reversed(char_embs))
    #     return dy.concatenate([ fw_exps[-1], bw_exps[-1] ])

def build_tagging_graph(words):
    dy.renew_cg()
    # parameters -> expressions
    H = dy.parameter(pH)
    O = dy.parameter(pO)

    # initialize the RNNs
    f_init = fwdRNN.initial_state()
    b_init = bwdRNN.initial_state()

    cf_init = cFwdRNN.initial_state()
    cb_init = cBwdRNN.initial_state()

    # get the word vectors. word_rep(...) returns a 128-dim vector expression for each word.
    wembs = [word_rep(w, cf_init, cb_init) for w in words]
    wembs = [dy.noise(we,0.2) for we in wembs] # optional

    # feed word vectors into biLSTM
    fw_exps = f_init.transduce(wembs)
    bw_exps = b_init.transduce(reversed(wembs))
# OR
#    fw_exps = []
#    s = f_init
#    for we in wembs:
#        s = s.add_input(we)
#        fw_exps.append(s.output())
#    bw_exps = []
#    s = b_init
#    for we in reversed(wembs):
#        s = s.add_input(we)
#        bw_exps.append(s.output())

    # biLSTM states
    bi_exps = [dy.concatenate([f,b]) for f,b in zip(fw_exps, reversed(bw_exps))]

    # feed each biLSTM state to an MLP
    exps = []
    for x in bi_exps:
        r_t = O*(dy.tanh(H * x))
        exps.append(r_t)

    return exps

def sent_loss(words, tags):
    vecs = build_tagging_graph(words)
    errs = []
    for v,t in zip(vecs,tags):
        tid = vt.w2i[t]
        err = dy.pickneglogsoftmax(v, tid)
        errs.append(err)
    return dy.esum(errs)

def tag_sent(words):
    vecs = build_tagging_graph(words)
    vecs = [dy.softmax(v) for v in vecs]
    probs = [v.npvalue() for v in vecs]
    tags = []
    for prb in probs:
        tag = np.argmax(prb)
        tags.append(vt.i2w[tag])
    return zip(words, tags)

def evaluate():
    good_sent = bad_sent = good = bad = 0.0
    gold_out = []
    pred_out = []
    words_out = []
    for sent in dev:
        words = [w for w, t in sent]
        golds = [t for w, t in sent]
        tags = [t for w, t in tag_sent(words)]

        gold_out.append(golds)
        pred_out.append(tags)
        words_out.append(words)
        if tags == golds:
            good_sent += 1
        else:
            bad_sent += 1
        for go, gu in zip(golds, tags):
            if go == gu:
                good += 1
            else:
                bad += 1
    print("Accuracy: ", good / (good + bad) * 100, good_sent / (good_sent + bad_sent) * 100)
    return words_out, gold_out, pred_out


num_tagged = cum_loss = 0
for ITER in range(1):
    random.shuffle(train)
    for i,s in enumerate(train,1):
        if i > 0 and i % 500 == 0:   # print status
            #trainer.status()
            #print(cum_loss / num_tagged)
            cum_loss = num_tagged = 0
            num_tagged = 0
        #if i % 10000 == 0 or i == len(train)-1: # eval on dev
        #    evaluate()
        # train on sent
        words = [w for w,t in s]
        golds = [t for w,t in s]

        loss_exp =  sent_loss(words, golds)
        cum_loss += loss_exp.scalar_value()
        num_tagged += len(golds)
        loss_exp.backward()
        trainer.update()
    print ("epoch %r finished" % ITER)
    evaluate()
    trainer.update_epoch(1.0)
print("Finish")
w,g,p = evaluate()

with open(prediction, mode="w") as out:
    out.write("#Gold\tPrediction\n")
    for w_sent, g_sent,p_sent in zip(w,g,p):
        assert(len(p_sent) == len(g_sent) == len(w_sent))
        for i in range(0, len(p_sent)):
            out.write(g_sent[i] + "\t" + p_sent[i]+"\n")
        out.write("\n")




