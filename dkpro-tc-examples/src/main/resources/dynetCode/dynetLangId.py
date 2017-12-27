# coding=utf-8
from sys import argv
from collections import Counter, defaultdict
from itertools import count
import random
import argparse

import dynet as dy
import numpy as np

# Rather primitive example for a language identification using word-level information
# meant as show case

documentLength=40
embDimension=25  

if  __name__ =='__main__':
	parser = argparse.ArgumentParser(description='Demo')
	parser.add_argument('--trainData', nargs=1, required=True)
	parser.add_argument('--trainOutcome', nargs=1, required=True)
	parser.add_argument('--testData', nargs=1, required=True)
	parser.add_argument('--testOutcome', nargs=1, required=True)    
	parser.add_argument('--embedding', nargs=1, required=False)    
	parser.add_argument('--maxLen', nargs=1, required=True)
	parser.add_argument('--predictionOut', nargs=1, required=True)
	parser.add_argument('--seed', nargs=1, required=True)    
	
	parser.add_argument('--dynet-seed', nargs=1, required=False)    
	parser.add_argument('--dynet-mem', nargs=1, required=False)    
	parser.add_argument('--dynet-devices', nargs=1, required=False)    
	parser.add_argument('--dynet-autobatch', nargs=1, required=False)    
	
	args = parser.parse_args()
	np.random.seed(int(args.seed[0]))
	trainSeq = args.trainData[0]
	trainLabel = args.trainOutcome[0]
	testSeq = args.testData[0]
	testLabel = args.testOutcome[0]
	prediction = args.predictionOut[0]

def read(data, labels):        
    f = open(data)
    sentences=f.readlines()
    f.close()
    
    f = open(labels)
    labels=f.readlines()
    f.close()
    
    sents = []
    l = labels[0].split()
    i=0
    for seq in sentences:
       sents.append((int(l[i]), list(map(int,seq.strip().split()))))
       i+=1
#        list(map(int, results))   
    return sents
    
    
train=list(read(trainSeq, trainLabel))
dev=list(read(testSeq, testLabel))

maxToken = (max([v for e in train for v in e[1]]))

# create parameter collection
m = dy.ParameterCollection()

# add parameters to parameter collection
pW = m.add_parameters((embDimension,embDimension*documentLength))
pB = m.add_parameters(embDimension)
lookup = m.add_lookup_parameters((maxToken+1, embDimension))

# create trainer 
trainer = dy.SimpleSGDTrainer(m)

def create_network_return_loss(inputs, expected_output):
    '''
    inputs is a list of numbers
    '''
    dy.renew_cg()
    W = dy.parameter(pW) # from parameters to expressions
    b = dy.parameter(pB)
    
    if(len(inputs) > documentLength):
       inputs = inputs[0:documentLength]
    
    emb_vectors = [lookup[i] for i in inputs]
    
    while(len(emb_vectors) < documentLength):
        pad = dy.vecInput(embDimension)
        pad.set(np.zeros(embDimension))
        emb_vectors.append(pad)
    
    net_input = dy.concatenate(emb_vectors)
    net_output = dy.softmax( (W*net_input) + b)
    loss = -dy.log(dy.pick(net_output, expected_output))
    return loss
    
def create_network_return_best(inputs):
    '''
    inputs is a list of numbers
    '''
    dy.renew_cg()
    W = dy.parameter(pW)
    b = dy.parameter(pB)

    if(len(inputs) > documentLength):
       inputs = inputs[0:documentLength]    
    
    emb_vectors = [lookup[i] for i in inputs]
    
    while(len(emb_vectors) < documentLength):
        pad = dy.vecInput(embDimension)
        pad.set(np.zeros(embDimension))
        emb_vectors.append(pad)    
    
    net_input = dy.concatenate(emb_vectors)
    net_output = dy.softmax( (W*net_input) + b)
    return np.argmax(net_output.npvalue())
    
for epoch in range(0,5): #increase iterations for better results
    random.shuffle(train)
    for tupel in train:
        loss = create_network_return_loss(tupel[1], tupel[0])
        loss.value() # need to run loss.value() for the forward prop
        loss.backward()
        trainer.update()

results = []        
for tupel in dev:
    p = create_network_return_best(tupel[1])
    results.append((tupel[0],p))

with open(prediction, mode='w') as out:
    out.write('#Gold\tPrediction\n')
    for e in results:
        out.write(str(e[0]) + '\t' + str(e[1])+'\n')
    out.write('\n')
        



