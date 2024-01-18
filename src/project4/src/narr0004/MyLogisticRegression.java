package narr0004;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyLogisticRegression {
    private static double learningRate = 0.01;
    private static int iterations = 10000;
    private static double[] weights;
    private static double bias;

    static {
        MyLogisticRegression model = new MyLogisticRegression(learningRate, iterations);
        String csvFilePath = "narr0004/ship_data.csv";
        model.loadAndTrain(csvFilePath);
        System.out.println("Model Trained");
    }
    /**
     * the MyLogisticRegression class.
     * Initialize the learning rate and number of iterations for the training process.
     * @param learningRate It's for the gradient descent algorithm.
     * @param iterations It's for run the training process.
     */
    private MyLogisticRegression(double learningRate, int iterations) {
        MyLogisticRegression.learningRate = learningRate;
        MyLogisticRegression.iterations = iterations;
    }
    /**
     * Loads the training data and initiates the training process.
     * @param csvFilePath CSV file containing training data.
     */
    public void loadAndTrain(String csvFilePath) {
        List<double[]> featureList = new ArrayList<>();
        List<Double> labelList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                double[] features = new double[values.length - 1];
                for (int i = 0; i < values.length - 1; i++) {
                    features[i] = Double.parseDouble(values[i]);
                }
                double label = Double.parseDouble(values[values.length - 1]);

                featureList.add(features);
                labelList.add(label);
            }
        } catch (IOException e) {
            System.err.println("Error in reading the file: " + csvFilePath);
            e.printStackTrace();
            return;
        }
        double[][] X_train = featureList.toArray(new double[0][]);
        double[] y_train = new double[labelList.size()];
        for (int i = 0; i < labelList.size(); i++) {
            y_train[i] = labelList.get(i);
        }
        train(X_train, y_train);
    }

    /**
     * Trains the logistic regression model by using the training data.
     * @param X  features of training data.
     * @param y labels of training data.
     */
    private static void train(double[][] X, double[] y) {
        int nSamples = X.length;
        int nFeatures = X[0].length;
        weights = new double[nFeatures];
        for (int i = 0; i < nFeatures; i++) {
            weights[i] = 0;
        }
        bias = 0;

        for (int i = 0; i < iterations; i++) {
            double[] predictions = new double[nSamples];
            for (int j = 0; j < nSamples; j++) {
                predictions[j] = sigmoid(dotProduct(X[j], weights) + bias);
            }
            double db = 0;
            double[] dw = new double[nFeatures];
            for (int k = 0; k < nSamples; k++) {
                double error = predictions[k] - y[k];
                db = db + error / nSamples;
                for (int j = 0; j < nFeatures; j++) {
                    dw[j] = dw[j]  + (error * X[k][j]) / nSamples;
                }
            }
            for (int j = 0; j < nFeatures; j++) {
                weights[j] = weights[j] - learningRate * dw[j];
            }
            bias = bias - learningRate * db;
        }
    }

    /**
     * Predicts the label for given features using the trained model.
     * @param features features for which the prediction need to be made.
     * @return predicted.
     */
    public static int predict(double[] features) {
        double prediction = sigmoid(dotProduct(features, weights) + bias);
        return prediction > 0.5 ? 1 : 0;
    }

    /**
     * Applies the sigmoid function to a given value.
     * @param z  sigmoid function will apply to the value.
     * @return The sigmoid of the given value.
     */
    private static double sigmoid(double z) {
        return 1 / (1 + Math.exp(-z));
    }

    /**
     * Calculates the dot product of two vectors.
     * @param a  first vector.
     * @param b  second vector.
     * @return The dot product of the two vectors.
     */
    private static double dotProduct(double[] a, double[] b) {
        double product = 0;
        for (int i = 0; i < a.length; i++) {
            product += a[i] * b[i];
        }
        return product;
    }
}
