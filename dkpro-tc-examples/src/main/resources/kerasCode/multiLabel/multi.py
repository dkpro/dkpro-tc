from sys import argv
import numpy as np
from keras.preprocessing import sequence
from keras.models import Sequential
from keras.layers import Dense, Activation, Embedding, TimeDistributed, Bidirectional, Convolution1D
from keras.layers import LSTM, Flatten, Conv1D, MaxPooling1D
from keras.utils import np_utils

EMBEDDING_DIM=-1
np.set_printoptions(threshold=np.nan)

def numpyizeVector(vec):
	vout=[]
	file = open(vec, 'r')
	for l in file.readlines():
		l = l.strip()
		v = [int(x) for x in l.split()]
		vout.append(v)
		#vout.append(np.fromstring(l, dtype=int, sep=' '))
	file.close()
	return vout
	
def numpyizeOutcomeVector(vec):
	file = open(vec, 'r')
	vecs=[]
	for l in file.readlines():
		l = l.strip()
		v = [int(x) for x in l.split()]
		vecs.append(v)
	file.close()
	return vecs	
	
def loadEmbeddings(emb):
	matrix = {}	
	f = open(emb, 'r')
	embData = f.readlines()
	f.close()
	dim = len(embData[0].split())-1
	matrix = np.zeros((len(embData)+1, dim))	
	for e in embData:
		e = e.strip()
		if not e:
			continue
		idx = e.find(" ")
		id = e[:idx]
		vector = e[idx+1:]
		matrix[int(id)]=np.asarray(vector.split(" "), dtype='float32')
	return matrix, dim

def prepareOutcomes(rawOutcomes, dimX, dimY):
	y_ = np.zeros((dimX, dimY))
	for i in range(0, len(rawOutcomes)):
		inner = y_[i]
		for j in range(0, len(rawOutcomes[i])):
			if rawOutcomes[i][j] > 0:
				idx = rawOutcomes[i][j]
				inner[idx]=1
			y_[i]=inner
	return y_
	

def runExperiment(trainVec, trainOutcome, testVec, testOutcome, embedding, longest_sequence, predictionOut):	

	trainVecNump = numpyizeVector(trainVec)
	trainOutcome = numpyizeOutcomeVector(trainOutcome)
	
	testVecNump = numpyizeVector(testVec)
	testOutcome = numpyizeOutcomeVector(testOutcome)
	
	embeddings,dim = loadEmbeddings(embedding)
	EMBEDDING_DIM = dim
	
	x_train = sequence.pad_sequences(trainVecNump, maxlen=longest_sequence)
	x_test = sequence.pad_sequences(testVecNump, maxlen=longest_sequence)
	
	y_test = testOutcome
	maxLabel = max(x for s in trainOutcome+testOutcome for x in s) + 1 # label start at zero 
	
	y_train = prepareOutcomes(trainOutcome, x_train.shape[0], maxLabel)

	print("X  : ", x_train.shape)
	print("Y  : ", y_train.shape)
	print("EMB: ", embeddings.shape)

	vocabSize = max(x for s in trainVecNump+testVecNump for x in s)

	print("Building arbitrary model")
	model = Sequential()
	model.add(Embedding(output_dim=embeddings.shape[1], input_dim=embeddings.shape[0], input_length=x_train.shape[1], weights=[embeddings], trainable=False))
	model.add(Conv1D(16,
                 5,
                 padding='valid',
                 activation='tanh',
                 strides=1))
	model.add(MaxPooling1D(pool_size=4))	
	model.add(LSTM(128, return_sequences=True))
	model.add(LSTM(128, go_backwards=True))	
	model.add(Dense(maxLabel))
	model.add(Activation('sigmoid')) #sigmoid helps 

	model.compile(loss='binary_crossentropy',
              optimizer='rmsprop')

	model.fit(x_train, y_train, epochs=10, shuffle=True)

	preds = model.predict(x_test)
	#preds[preds>=0.5] = 1
	#preds[preds<0.5] = 0
	
	y_test = prepareOutcomes(testOutcome, x_test.shape[0], maxLabel)
	
	predictionFile = open(predictionOut, 'w')
	predictionFile.write("#Gold\tPrediction\n")
	for i in range(0, len(preds)):
		for j in range(0, len(y_test[i])):
			predictionFile.write(str(y_test[i][j]))
			if j+1 < len(y_test[i]):
				predictionFile.write(" ")
		predictionFile.write("\t")		
		for j in range(0, len(preds[i])):
			predictionFile.write(str(preds[i][j]))
			if j+1 < len(preds[i]):
				predictionFile.write(" ")					
		predictionFile.write("\n")					

	predictionFile.close()


if  __name__ =='__main__':
	runExperiment(argv[1], argv[2], argv[3], argv[4], argv[5], int(argv[6]), argv[7])
