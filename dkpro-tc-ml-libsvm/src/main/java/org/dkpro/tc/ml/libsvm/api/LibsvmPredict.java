/*******************************************************************************
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.dkpro.tc.ml.libsvm.api;

import libsvm.*;
import java.io.*;
import java.util.*;

public class LibsvmPredict
{
    private svm_print_interface svm_print_null = new svm_print_interface()
    {
        public void print(String s)
        {
        }
    };

    private svm_print_interface svm_print_stdout = new svm_print_interface()
    {
        public void print(String s)
        {
            System.out.print(s);
        }
    };

    private svm_print_interface svm_print_string = svm_print_stdout;

    void info(String s)
    {
        svm_print_string.print(s);
    }

    private double atof(String s)
    {
        return Double.valueOf(s).doubleValue();
    }

    private int atoi(String s)
    {
        return Integer.parseInt(s);
    }

    public void predict(BufferedReader input, DataOutputStream output, svm_model model,
            int predict_probability)
                throws IOException
    {
        int correct = 0;
        int total = 0;
        double error = 0;
        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;

        int svm_type = svm.svm_get_svm_type(model);
        int nr_class = svm.svm_get_nr_class(model);
        double[] prob_estimates = null;

        if (predict_probability == 1) {
            if (svm_type == svm_parameter.EPSILON_SVR || svm_type == svm_parameter.NU_SVR) {
                info("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="
                        + svm.svm_get_svr_probability(model) + "\n");
            }
            else {
                int[] labels = new int[nr_class];
                svm.svm_get_labels(model, labels);
                prob_estimates = new double[nr_class];
                output.writeBytes("labels");
                for (int j = 0; j < nr_class; j++)
                    output.writeBytes(" " + labels[j]);
                output.writeBytes("\n");
            }
        }
        while (true) {
            String line = input.readLine();
            if (line == null)
                break;

            StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

            double target = atof(st.nextToken());
            int m = st.countTokens() / 2;
            svm_node[] x = new svm_node[m];
            for (int j = 0; j < m; j++) {
                x[j] = new svm_node();
                x[j].index = atoi(st.nextToken());
                x[j].value = atof(st.nextToken());
            }

            double v;
            if (predict_probability == 1
                    && (svm_type == svm_parameter.C_SVC || svm_type == svm_parameter.NU_SVC)) {
                v = svm.svm_predict_probability(model, x, prob_estimates);
                output.writeBytes(v + " ");
                for (int j = 0; j < nr_class; j++)
                    output.writeBytes(prob_estimates[j] + " ");
                output.writeBytes("\n");
            }
            else {
                v = svm.svm_predict(model, x);
                output.writeBytes(v + "\n");
            }

            if (v == target)
                ++correct;
            error += (v - target) * (v - target);
            sumv += v;
            sumy += target;
            sumvv += v * v;
            sumyy += target * target;
            sumvy += v * target;
            ++total;
        }
        if (svm_type == svm_parameter.EPSILON_SVR || svm_type == svm_parameter.NU_SVR) {
            info("Mean squared error = " + error / total + " (regression)\n");
            info("Squared correlation coefficient = "
                    + ((total * sumvy - sumv * sumy) * (total * sumvy - sumv * sumy))
                            / ((total * sumvv - sumv * sumv) * (total * sumyy - sumy * sumy))
                    + " (regression)\n");
        }
        else
            info("Accuracy = " + (double) correct / total * 100 + "% (" + correct + "/" + total
                    + ") (classification)\n");
    }

    private void exit_with_help()
    {
        System.err.print(
                "usage: svm_predict [options] test_file model_file output_file\n" + "options:\n"
                        + "-b probability_estimates: whether to predict probability estimates, 0 or 1 (default 0); one-class SVM not supported yet\n"
                        + "-q : quiet mode (no outputs)\n");
        System.exit(1);
    }

    public void main(String argv[])
        throws IOException
    {
        int i, predict_probability = 0;
        svm_print_string = svm_print_stdout;

        // parse options
        for (i = 0; i < argv.length; i++) {
            if (argv[i].charAt(0) != '-')
                break;
            ++i;
            switch (argv[i - 1].charAt(1)) {
            case 'b':
                predict_probability = atoi(argv[i]);
                break;
            case 'q':
                svm_print_string = svm_print_null;
                i--;
                break;
            default:
                throw new IllegalArgumentException("Unknown option: " + argv[i - 1] + "\n");
            }
        }
        if (i >= argv.length - 2)
            exit_with_help();
        BufferedReader input = new BufferedReader(new FileReader(argv[i]));
        DataOutputStream output = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(argv[i + 2])));
        svm_model model = svm.svm_load_model(argv[i + 1]);
        if (model == null) {
            input.close();
            output.close();
            throw new IOException("can't open model file " + argv[i + 1] + "\n");
        }
        if (predict_probability == 1) {
            if (svm.svm_check_probability_model(model) == 0) {
                input.close();
                output.close();
                throw new IllegalArgumentException("Model does not support probabiliy estimates\n");
            }
        }
        else {
            if (svm.svm_check_probability_model(model) != 0) {
                info("Model supports probability estimates, but disabled in prediction.\n");
            }
        }
        predict(input, output, model, predict_probability);
        input.close();
        output.close();

    }
}
