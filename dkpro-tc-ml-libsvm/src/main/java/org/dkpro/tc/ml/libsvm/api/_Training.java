/*******************************************************************************
 * Copyright 2019
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;
import static java.nio.charset.StandardCharsets.UTF_8;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_print_interface;
import libsvm.svm_problem;

public class _Training
{
    private svm_parameter param; // set by parse_command_line
    private svm_problem prob; // set by read_problem
    private svm_model model;
    private String input_file_name; // set by parse_command_line
    private String model_file_name; // set by parse_command_line
    private String error_msg;

    private static svm_print_interface svm_print_null = new svm_print_interface()
    {
        public void print(String s)
        {
        }
    };

    private static void exit_with_help()
    {
        System.out.print("Usage: svm_train [options] training_set_file [model_file]\n"
                + "options:\n" + "-s svm_type : set type of SVM (default 0)\n"
                + "	0 -- C-SVC		(multi-class classification)\n"
                + "	1 -- nu-SVC		(multi-class classification)\n" + "	2 -- one-class SVM\n"
                + "	3 -- epsilon-SVR	(regression)\n" + "	4 -- nu-SVR		(regression)\n"
                + "-t kernel_type : set type of kernel function (default 2)\n"
                + "	0 -- linear: u'*v\n" + "	1 -- polynomial: (gamma*u'*v + coef0)^degree\n"
                + "	2 -- radial basis function: exp(-gamma*|u-v|^2)\n"
                + "	3 -- sigmoid: tanh(gamma*u'*v + coef0)\n"
                + "	4 -- precomputed kernel (kernel values in training_set_file)\n"
                + "-d degree : set degree in kernel function (default 3)\n"
                + "-g gamma : set gamma in kernel function (default 1/num_features)\n"
                + "-r coef0 : set coef0 in kernel function (default 0)\n"
                + "-c cost : set the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default 1)\n"
                + "-n nu : set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)\n"
                + "-p epsilon : set the epsilon in loss function of epsilon-SVR (default 0.1)\n"
                + "-m cachesize : set cache memory size in MB (default 100)\n"
                + "-e epsilon : set tolerance of termination criterion (default 0.001)\n"
                + "-h shrinking : whether to use the shrinking heuristics, 0 or 1 (default 1)\n"
                + "-b probability_estimates : whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0)\n"
                + "-wi weight : set the parameter C of class i to weight*C, for C-SVC (default 1)\n"
                + "-q : quiet mode (no outputs)\n");
        System.exit(1);
    }

    public void run(String argv[]) throws Exception
    {
        parse_command_line(argv);
        read_problem();
        error_msg = svm.svm_check_parameter(prob, param);

        if (error_msg != null) {
            throw new Exception(error_msg);
        }

        model = svm.svm_train(prob, param);
        svm.svm_save_model(model_file_name, model);
    }

    private static double atof(String s)
    {
        double d = Double.valueOf(s).doubleValue();
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            throw new IllegalArgumentException("NaN or Infinity in input\n");
        }
        return d;
    }

    private static int atoi(String s)
    {
        return Integer.parseInt(s);
    }

    private void parse_command_line(String argv[])
    {
        int i;
        svm_print_interface print_func = null; // default printing to stdout

        param = new svm_parameter();
        // default values
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.degree = 3;
        param.gamma = 0; // 1/num_features
        param.coef0 = 0;
        param.nu = 0.5;
        param.cache_size = 100;
        param.C = 1;
        param.eps = 1e-3;
        param.p = 0.1;
        param.shrinking = 1;
        param.probability = 0;
        param.nr_weight = 0;
        param.weight_label = new int[0];
        param.weight = new double[0];

        // parse options
        for (i = 0; i < argv.length; i++) {
            if (argv[i].charAt(0) != '-')
                break;
            if (++i >= argv.length)
                exit_with_help();
            switch (argv[i - 1].charAt(1)) {
            case 's':
                param.svm_type = atoi(argv[i]);
                break;
            case 't':
                param.kernel_type = atoi(argv[i]);
                break;
            case 'd':
                param.degree = atoi(argv[i]);
                break;
            case 'g':
                param.gamma = atof(argv[i]);
                break;
            case 'r':
                param.coef0 = atof(argv[i]);
                break;
            case 'n':
                param.nu = atof(argv[i]);
                break;
            case 'm':
                param.cache_size = atof(argv[i]);
                break;
            case 'c':
                param.C = atof(argv[i]);
                break;
            case 'e':
                param.eps = atof(argv[i]);
                break;
            case 'p':
                param.p = atof(argv[i]);
                break;
            case 'h':
                param.shrinking = atoi(argv[i]);
                break;
            case 'b':
                param.probability = atoi(argv[i]);
                break;
            case 'q':
                print_func = svm_print_null;
                i--;
                break;
            case 'w':
                ++param.nr_weight; {
                int[] old = param.weight_label;
                param.weight_label = new int[param.nr_weight];
                System.arraycopy(old, 0, param.weight_label, 0, param.nr_weight - 1);
            }

            {
                double[] old = param.weight;
                param.weight = new double[param.nr_weight];
                System.arraycopy(old, 0, param.weight, 0, param.nr_weight - 1);
            }

                param.weight_label[param.nr_weight - 1] = atoi(argv[i - 1].substring(2));
                param.weight[param.nr_weight - 1] = atof(argv[i]);
                break;
            default:
                throw new IllegalArgumentException("Unknown option: " + argv[i - 1] + "\n");
            }
        }

        svm.svm_set_print_string_function(print_func);

        // determine filenames

        if (i >= argv.length)
            exit_with_help();

        input_file_name = argv[i];

        if (i < argv.length - 1)
            model_file_name = argv[i + 1];
        else {
            int p = argv[i].lastIndexOf('/');
            ++p; // whew...
            model_file_name = argv[i].substring(p) + ".model";
        }
    }

    // read in a problem (in svmlight format)

    private void read_problem() throws IOException
    {
        try (BufferedReader fp = new BufferedReader(
                new InputStreamReader(new FileInputStream(input_file_name), UTF_8))) {
            Vector<Double> vy = new Vector<Double>();
            Vector<svm_node[]> vx = new Vector<svm_node[]>();
            int max_index = 0;

            while (true) {
                String line = fp.readLine();
                if (line == null)
                    break;

                StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

                vy.addElement(atof(st.nextToken()));
                int m = st.countTokens() / 2;
                svm_node[] x = new svm_node[m];
                for (int j = 0; j < m; j++) {
                    x[j] = new svm_node();
                    x[j].index = atoi(st.nextToken());
                    x[j].value = atof(st.nextToken());
                }
                if (m > 0)
                    max_index = Math.max(max_index, x[m - 1].index);
                vx.addElement(x);
            }

            prob = new svm_problem();
            prob.l = vy.size();
            prob.x = new svm_node[prob.l][];
            for (int i = 0; i < prob.l; i++)
                prob.x[i] = vx.elementAt(i);
            prob.y = new double[prob.l];
            for (int i = 0; i < prob.l; i++)
                prob.y[i] = vy.elementAt(i);

            if (param.gamma == 0 && max_index > 0)
                param.gamma = 1.0 / max_index;

            if (param.kernel_type == svm_parameter.PRECOMPUTED)
                for (int i = 0; i < prob.l; i++) {
                    if (prob.x[i][0].index != 0) {
                        throw new IllegalArgumentException(
                                "Wrong kernel matrix: first column must be 0:sample_serial_number\n");
                    }
                    if ((int) prob.x[i][0].value <= 0 || (int) prob.x[i][0].value > max_index) {
                        throw new IllegalArgumentException(
                                "Wrong input format: sample_serial_number out of range\n");
                    }
                }

        }
    }
}
