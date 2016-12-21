package edu.spbu.matrix;

import java.util.Objects;

/**
 *
 */
public interface Matrix
{
    boolean equals(Object o);

    /**
     * многопоточное умножение матриц
     * @param o
     * @return
     */
    Matrix dmul(Matrix o);

}
