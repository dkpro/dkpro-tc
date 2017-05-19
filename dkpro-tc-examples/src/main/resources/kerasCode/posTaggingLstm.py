from sys import argv
import numpy as np
from keras.preprocessing import sequence
from keras.models import Sequential
from keras.layers import Dense, Activation, Embedding, TimeDistributed, Bidirectional
from keras.layers import LSTM
from keras.utils import np_utils

EMBEDDING_DIM=64

def numpyizeVector(vec):
	vout=[]
	file = open(vec, 'r')
	for l in file.readlines():
		l = l.strip()
		l = l[1:-1] # strip the brackets
		vout.append(np.fromstring(l, dtype=int, sep=' '))
	file.close()
	return vout
	

def runExperiment(trainVec, trainOutcome, testVec, testOutcome, embedding, longest_sequence, predictionOut):	

	trainVecNump = numpyizeVector(trainVec)
	trainOutcome = numpyizeVector(trainOutcome)
	
	testVecNump = numpyizeVector(testVec)
	testOutcome = numpyizeVector(testOutcome)

	x_train = sequence.pad_sequences(trainVecNump, maxlen=int(longest_sequence))
	x_test = sequence.pad_sequences(testVecNump, maxlen=int(longest_sequence))
	
	y_test = testOutcome
	maxLabel = max(x for s in trainOutcome+testOutcome for x in s) + 1
	y_train = np.array([np_utils.to_categorical(seq, maxLabel) for seq in trainOutcome])

	vocabSize = max(x for s in trainVecNump+testVecNump for x in s)

	model = Sequential()
	model.add(Embedding(vocabSize+1, EMBEDDING_DIM))
	model.add(Bidirectional(LSTM(EMBEDDING_DIM, return_sequences=True)))
	model.add(TimeDistributed(Dense(maxLabel)))
	model.add(Activation('tanh'))

# try using different optimizers and different optimizer configs
	model.compile(loss='categorical_crossentropy',
              optimizer='adam',
              metrics=['accuracy'])

	model.fit(x_train, y_train, epochs=2)

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
