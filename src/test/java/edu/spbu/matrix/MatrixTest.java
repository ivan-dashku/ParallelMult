package edu.spbu.matrix;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MatrixTest
{
    @Test
    public void dmulDD() {
        Matrix m1 = new DenseMatrix("m1.txt");
        Matrix m2 = new DenseMatrix("m2.txt");
        Matrix expected = new DenseMatrix("result.txt");
        Matrix m3 = m1.dmul(m2);
        Assert.assertTrue(m3.equals(expected));
    }

    @Test
    public void dmulSS() {
        Matrix m1 = new SparseMatrix("m1.txt");
        Matrix m2 = new SparseMatrix("m2.txt");
        Matrix expected = new SparseMatrix("result.txt");
        Matrix m3 = m1.dmul(m2);
        Assert.assertTrue(m3.equals(expected));
    }
}

