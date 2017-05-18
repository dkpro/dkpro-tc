#
# Copyright 2017
# Ubiquitous Knowledge Processing (UKP) Lab
# Technische Universität Darmstadt
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see http://www.gnu.org/licenses/.



# This is an adapted demo test case from the Keras project (https://keras.io)


'''Train a recurrent convolutional network on the IMDB sentiment
classification task.

Gets to 0.8498 test accuracy after 2 epochs. 41s/epoch on K520 GPU.
'''
from __future__ import print_function

from sys import argv
from keras.preprocessing import sequence
from keras.models import Sequential
from keras.layers import Dense, Dropout, Activation
from keras.layers import Embedding
from keras.layers import LSTM
from keras.layers import Conv1D, MaxPooling1D
from keras.datasets import imdb
import numpy as np

# Embedding
max_features = 20000
#maxlen = 100
embedding_size = 128

# Convolution
kernel_size = 5
filters = 64
pool_size = 4

# LSTM
lstm_output_size = 70

# Training
batch_size = 30
epochs = 2

'''
Note:
batch_size is highly sensitive.
Only 2 epochs are needed as the dataset is very small.
'''

def numpyizeDataVector(vec):
	trainVecNump=[]
	file = open(vec, 'r')
	for l in file.readlines():
		l = l.strip()
		l = l[1:-1] # strip the brackets
		trainVecNump.append(np.fromstring(l, dtype=int, sep=' '))
	file.close()
	return trainVecNump
	
def numpyizeOutcomeVector(vec):
	file = open(vec, 'r')
	v=""
	for l in file.readlines():
		l = l.strip()
		l = l[1:-1] # strip the brackets
		v=np.fromstring(l, dtype=int, sep=' ')
	file.close()
	return v
	


def runExperiment(trainVec, trainOutcome, testVec, testOutcome, embedding, maximumLength):

	trainVecNump = numpyizeDataVector(trainVec)
	trainOutcome = numpyizeOutcomeVector(trainOutcome)
	
	testVecNump = numpyizeDataVector(testVec)
	testOutcome = numpyizeOutcomeVector(testOutcome)

	x_train = sequence.pad_sequences(trainVecNump, maxlen=int(maximumLength))
	x_test = sequence.pad_sequences(testVecNump, maxlen=int(maximumLength))
	
	y_train = trainOutcome
	y_test = testOutcome
		
	print(x_train.shape)
	print(x_test.shape)	

	print('Build model...')

	model = Sequential()
	model.add(Embedding(max_features, embedding_size, input_length=int(maximumLength)))
	model.add(Dropout(0.25))
	model.add(Conv1D(filters,
                 kernel_size,
                 padding='valid',
                 activation='relu',
                 strides=1))
	model.add(MaxPooling1D(pool_size=pool_size))
	model.add(LSTM(lstm_output_size))
	model.add(Dense(1))
	model.add(Activation('sigmoid'))

	model.compile(loss='binary_crossentropy',
              optimizer='adam',
              metrics=['accuracy'])

	print('Train...')
	model.fit(x_train, y_train,
          batch_size=batch_size,
          epochs=epochs,
          validation_data=(x_test, y_test))
	score, acc = model.evaluate(x_test, y_test, batch_size=batch_size)
	print('Test score:', score)
	print('Test accuracy:', acc)


if  __name__ =='__main__':
	runExperiment(argv[1], argv[2], argv[3], argv[4], argv[5], argv[6])