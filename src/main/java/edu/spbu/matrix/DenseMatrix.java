package edu.spbu.matrix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Плотная матрица
 */
public class DenseMatrix implements Matrix {
    public double[][] data;
    /**
     * загружает матрицу из файла
     * @param fileName
     */
    public DenseMatrix(String fileName) {
        try {
            File f = new File(fileName);
            Scanner input = new Scanner(f);
            String[] line;
            ArrayList<Double[]> a = new ArrayList<>();
            Double[] tmp = {};
            while (input.hasNextLine()) {
                line = input.nextLine().split(" ");
                tmp = new Double[line.length];
                for (int i=0; i<tmp.length; i++) {
                    tmp[i] = Double.parseDouble(line[i]);
                }
                a.add(tmp);
            }
            double[][] result = new double[a.size()][tmp.length];
            for (int i=0; i<result.length; i++) {
                for (int j=0; j<result[0].length; j++) {
                    result[i][j] = a.get(i)[j];
                }
            }
            data = result;
        } catch(IOException e) {
            e.printStackTrace();
        }

    }

    public DenseMatrix(double[][] r) {
        data = r;
    }

    public double[][] transpose() {
        double[][] dT = new double[data[0].length][data.length];
        for (int i=0; i<data[0].length; i++ ) {
            for (int j=0; j<data.length; j++ ) {
                dT[i][j] = data[j][i];
            }
        }
        return dT;
    }

    /**
     * многопоточное умножение матриц
     *
     * @param o
     * @return
     */
     public DenseMatrix dmul(Matrix o) {
         DenseMatrix d = (DenseMatrix)o;
         class Dispatcher {
             int value = 0;
             public int next() {
                 synchronized (this) {
                     return value++;
                 }
             }
         }

         double[][] result = new double[data.length][d.data[0].length];
         double[][] dT = d.transpose();
         Dispatcher dispatcher = new Dispatcher();

         class MultRow implements Runnable {
             Thread thread;

             public MultRow(String s) {
                 thread = new Thread(this, s);
                 thread.start();
             }
             public void run() {
                 int i;
                 while ((i = dispatcher.next()) < data.length) {
                     for (int j = 0; j < dT.length; j++) {
                         for (int k = 0; k < dT[0].length; k++) {
                             result[i][j] += data[i][k] * dT[j][k];
                         }
                     }
                 }
             }
         }

         MultRow one = new MultRow("1");
         MultRow two = new MultRow("2");
         MultRow three = new MultRow("3");
         MultRow four = new MultRow("4");

         try {
             one.thread.join();
             two.thread.join();
             three.thread.join();
             four.thread.join();

         } catch (InterruptedException e){
             e.printStackTrace();
         }

         return new DenseMatrix(result);
     }

    /**
     * сравнивает с обоими вариантами
     * @param o
     * @return
     */
    public boolean equals(Object o) {
        boolean y = true;
        if (o instanceof DenseMatrix) {
            DenseMatrix tmp = (DenseMatrix)o;
            if (data.length == tmp.data.length && data[0].length == tmp.data[0].length) {
                for (int i = 0; i<data.length; i++) {
                    for (int j=0; j<data[0].length; j++) {
                        if (data[i][j] != tmp.data[i][j]) {
                            y = false;
                        }
                    }
                }
            } else {
                y = false;
            }
        } else if (o instanceof SparseMatrix) {
            SparseMatrix tmp = (SparseMatrix)o;
            if (data.length == tmp.row && data[0].length == tmp.col) {
                for (int i = 0; i<tmp.row; i++) {
                    if (tmp.map.containsKey(i)) {
                        for (int j = 0; j<tmp.col; j++) {
                            if (tmp.map.get(i).containsKey(j)) {
                                if (tmp.map.get(i).get(j) != data[i][j]) {
                                    y = false;
                                }
                            } else {
                                if (data[i][j] != 0) {
                                    y = false;
                                }
                            }
                        }
                    } else {
                        for (int j = 0; j < tmp.col; j++) {
                            if (data[i][j] != 0) {
                                y = false;
                            }
                        }
                    }
                }
            } else {
                y = false;
            }
        }
        return y;
    }
}
