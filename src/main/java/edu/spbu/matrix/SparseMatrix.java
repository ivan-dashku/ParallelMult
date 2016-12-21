package edu.spbu.matrix;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Разряженная матрица
 */
public class SparseMatrix implements Matrix
{
    public ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Double>> map;
    public int row;
    public int col;
    /**
     * загружает матрицу из файла
     * @param fileName
     */
    public SparseMatrix(String fileName) {
        col = 0;
        row = 0;
        ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Double>> result = new ConcurrentHashMap<>();
        try {
            File f = new File(fileName);
            Scanner input = new Scanner(f);
            String[] line = {};
            ConcurrentHashMap<Integer, Double> tmp = new ConcurrentHashMap<>();

            while (input.hasNextLine()) {
                tmp = new ConcurrentHashMap<>();
                line = input.nextLine().split(" ");
                for (int i=0; i<line.length; i++) {
                    if (line[i]!="0") {
                        tmp.put(i, Double.parseDouble(line[i]));
                    }
                }
                if (tmp.size()!=0) {
                    result.put(row++, tmp);
                }
            }
            col = line.length;
            map = result;
        } catch(IOException e) {
            e.printStackTrace();
        }

    }

    public SparseMatrix(ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Double>> map, int row, int col) {
        this.map = map;
        this.row = row;
        this.col = col;
    }

    public SparseMatrix transpose() {
        ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Double>> result = new ConcurrentHashMap<>();
        for (ConcurrentHashMap.Entry<Integer, ConcurrentHashMap<Integer, Double>> row : map.entrySet()){
            for (HashMap.Entry<Integer, Double> elem : row.getValue().entrySet()) {
                if (!result.containsKey(elem.getKey())) {
                    result.put(elem.getKey(), new ConcurrentHashMap<>());
                }
                result.get(elem.getKey()).put(row.getKey(), elem.getValue());
            }
        }
        return new SparseMatrix(result, col, row);
    }

    public void toFile(String filename) {
        try {
            PrintWriter w = new PrintWriter(filename);
            for (int i = 0; i<row; i++) {
                if (map.containsKey(i)) {
                    for (int j = 0; j<col; j++) {
                        if (map.get(i).containsKey(j)) {
                            w.print(map.get(i).get(j));
                        } else {
                            w.print((double)0);
                        }
                    }
                } else {
                    for (int j = 0; j < col; j++) {
                        w.print((double)0);
                    }
                    w.println();
                }
            }
            w.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }




    /**
     * многопоточное умножение матриц
     *
     * @param o
     * @return
     */
    @Override
    public Matrix dmul(Matrix o){
        SparseMatrix s = (SparseMatrix)o;

        SparseMatrix sT = s.transpose();

        ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Double>> result = new ConcurrentHashMap<>();
        double sum = 0;
        class MultRow implements Runnable {
            Thread thread;
            ConcurrentHashMap<Integer, Double> tmp = new ConcurrentHashMap<>();

            public MultRow(String s) {
                thread = new Thread(this, s);
                thread.start();
            }

            public void run() {
                for(ConcurrentHashMap.Entry<Integer, ConcurrentHashMap<Integer,Double>> i : map.entrySet()) {
                    double sum = 0;
                    if (map.containsKey(i.getKey())) {
                        tmp = new ConcurrentHashMap<>();
                        for (int j = 0; j < sT.row; j++) {
                            if (sT.map.containsKey(j)) {
                                for (int k = 0; k < sT.col; k++) {
                                    if (sT.map.get(j).containsKey(k) && i.getValue().containsKey(k)) {
                                        sum += sT.map.get(j).get(k) * i.getValue().get(k);
                                    }
                                }
                                if (sum != 0) {
                                    tmp.put(j, sum);
                                }
                                sum = 0;
                            }
                        }
                        if (tmp.size() != 0) {
                            result.put(i.getKey(), tmp);
                        }
                    }
                }
            }
        }

        MultRow one = new MultRow("one");
        MultRow two = new MultRow("two");
        MultRow three = new MultRow("three");
        MultRow four = new MultRow("four");

        try {
            one.thread.join();
            two.thread.join();
            three.thread.join();
            four.thread.join();
        } catch( InterruptedException e) {
            e.printStackTrace();
        }

        return new SparseMatrix(result, row, s.col);
    }



    /**
     * спавнивает с обоими вариантами
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        boolean y = true;
        if (o instanceof DenseMatrix) {
            DenseMatrix tmp = (DenseMatrix)o;
            if (tmp.data.length == row && tmp.data[0].length == col) {
                for (int i = 0; i<row; i++) {
                    if (map.containsKey(i)) {
                        for (int j = 0; j<col; j++) {
                            if (map.get(i).containsKey(j)) {
                                if (map.get(i).get(j) != tmp.data[i][j]) {
                                    y = false;
                                }
                            } else {
                                if (tmp.data[i][j] != 0) {
                                    y = false;
                                }
                            }
                        }
                    } else {
                        for (int j = 0; j < col; j++) {
                            if (tmp.data[i][j] != 0) {
                                y = false;
                            }
                        }
                    }
                }
            } else {
                y = false;
            }
        } else if (o instanceof SparseMatrix) {
            SparseMatrix tmp = (SparseMatrix) o;
            if (tmp.col == col && tmp.row == row) {
                for (int i = 0; i<row; i++) {
                    if (map.containsKey(i) && tmp.map.containsKey(i))  {
                        for (int j = 0; j<col; j++) {
                            if (map.get(i).containsKey(j) && tmp.map.get(i).containsKey(j)) {
                                if (map.get(i).get(j).doubleValue() != tmp.map.get(i).get(j).doubleValue()) {
                                    y = false;
                                }
                            } else if (map.get(i).containsKey(j) || tmp.map.get(i).containsKey(j)) {
                                y = false;
                            }
                        }
                    } else if (map.containsKey(i) || tmp.map.containsKey(i)) {
                        y = false;
                    }
                }
            } else {
                y = false;
            }
        }
        return y;
    }
}
