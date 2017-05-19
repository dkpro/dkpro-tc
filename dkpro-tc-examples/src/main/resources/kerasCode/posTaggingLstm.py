from sys import argv
import numpy as np
from keras.preprocessing import sequence
from keras.models import Sequential
from keras.layers import Dense, Activation, Embedding, TimeDistributed, Bidirectional, Convolution1D
from keras.layers import LSTM
from keras.utils import np_utils

EMBEDDING_DIM=-1
np.set_printoptions(threshold=np.nan)

def numpyizeVector(vec):
	vout=[]
	file = open(vec, 'r')
	for l in file.readlines():
		l = l.strip()
		l = l[1:-1] # strip the brackets
		v = [int(x) for x in l.split()]
		vout.append(v)
		#vout.append(np.fromstring(l, dtype=int, sep=' '))
	file.close()
	return vout
	
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
		id, vector = e.split("\t")
		matrix[int(id)]=np.asarray(vector.split(" "), dtype='float32')
	return matrix, dim

def runExperiment(trainVec, trainOutcome, testVec, testOutcome, embedding, longest_sequence, predictionOut):	

	trainVecNump = numpyizeVector(trainVec)
	trainOutcome = numpyizeVector(trainOutcome)
	print(trainOutcome[0])
	
	testVecNump = numpyizeVector(testVec)
	testOutcome = numpyizeVector(testOutcome)
	
	embeddings,dim = loadEmbeddings(embedding)
	EMBEDDING_DIM = dim
	
	x_train = sequence.pad_sequences(trainVecNump, maxlen=longest_sequence)
	y_train = sequence.pad_sequences(trainOutcome, maxlen=longest_sequence)
	x_test = sequence.pad_sequences(testVecNump, maxlen=longest_sequence)
	
	y_test = testOutcome
	maxLabel = max(x for s in trainOutcome+testOutcome for x in s) + 1
		
	y_train = np.array([np_utils.to_categorical(s, maxLabel) for s in y_train])
	print(y_train.shape)
	print(y_train[0].shape)	

	vocabSize = max(x for s in trainVecNump+testVecNump for x in s)

	print("Embedding dim: ", embeddings.shape)
	print("Train data dim: ", x_train.shape)	
	print("Train label dim: ", y_train.shape)
	model = Sequential()
	model.add(Embedding(output_dim=embeddings.shape[1], input_dim=embeddings.shape[0],
                       input_length=x_train.shape[1], weights=[embeddings], trainable=False))
	model.add(Convolution1D(128, 5, padding='same', activation='relu'))	
	model.add(Bidirectional(LSTM(EMBEDDING_DIM, return_sequences=True)))
	model.add(TimeDistributed(Dense(maxLabel)))
	model.add(Activation('softmax'))

	# try using different optimizers and different optimizer configs
	model.compile(loss='categorical_crossentropy',
              optimizer='rmsprop',
              metrics=['accuracy'])

	model.fit(x_train, y_train, epochs=3, shuffle=True)

	prediction = model.predict_classes(x_test)

	predictionFile = open(predictionOut, 'w')
	predictionFile.write("#Gold\tPrediction\n")
	for i in range(0, len(prediction)):
		predictionEntry = prediction[i]
		for j in range(0, len(y_test[i])):
			if y_test[i][j]==0:
				break #we reached the padded area - zero is reserved
			predictionFile.write(str(y_test[i][j]) +"\t" + str(predictionEntry[j])+ "\n")
		predictionFile.write("\n")
	predictionFile.close()


if  __name__ =='__main__':
	runExperiment(argv[1], argv[2], argv[3], argv[4], argv[5], int(argv[6]), argv[7])
