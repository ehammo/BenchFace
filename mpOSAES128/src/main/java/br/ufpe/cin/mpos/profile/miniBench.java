package br.ufpe.cin.mpos.profile;

import android.util.Log;

/**
 * Created by eduar on 07/02/2017.
 */

public class MiniBench {

    long startTime;
    long endTime;
    double elapsedTime;
    double emptyLoopTime;
    double MFLOPS;
    double s = 0.5877852522;
    double c = 0.8090169943;
    double x0 = 0.5;
    double y0 = 0.5;
    double x = 0.0;
    double y = 0.0;
    double dx = 0.0;
    double dy = 0.0;
    double x1, y1;
    long num_iterations = 100000000;
    int num_operations_per_loop = 10;

    public long clock() {
        return System.nanoTime();
    }

    public double start() {
        Log.d("teste", "miniBenchmarking start");
        //testing an empty loop time
        startTime = clock();
        for (int i = 0; i < num_iterations; i++) {
        } // do nothing
        endTime = clock();
        emptyLoopTime = ((double) endTime - startTime) / 1000000000.0;
        Log.d("teste", "Empty Loop: " + emptyLoopTime + " seconds for " + num_iterations + " iterations\n");
//testing filled loop time
        startTime = clock();
        for (int i = 0; i < num_iterations; i++) {
            dx = x - x0;
            dy = y - y0;
            x1 = x0 + dx * c - dy * s;
            y1 = y0 + dx / s + dy * c;
            x = x1;
            y = y1;
        }
        endTime = clock();
        elapsedTime = ((double) endTime - startTime) / 1000000000.0;
        ;
        Log.d("teste", "Floating-Point Loop: " + elapsedTime + " seconds for " + num_iterations + " iterations\n");
        MFLOPS = (1 / 1000000.0) * num_operations_per_loop * num_iterations / (double) (elapsedTime - emptyLoopTime);
        Log.d("teste", "Elapsed Time: " + elapsedTime + "\n");
        Log.d("teste", "MFlops: " + MFLOPS + "\n");
        Log.d("teste", "miniBenchmarking end");
        return MFLOPS;
    }

    /*

    /*
    *
unsigned long num_iterations = 1000000000;
int num_operations_per_loop = 10;
int i = 0;
//testing an empty loop time
startTime = clock();
for (i = 0; i < num_iterations; i++) {
} // do nothing
endTime = clock();
emptyLoopTime = ((double)endTime - startTime)/CLOCKS_PER_SEC;;
printf("Empty Loop: %f seconds for %lu iterations\n", emptyLoopTime, num_iterations);
//testing filled loop time
startTime = clock();
for (i = 0; i < num_iterations; i++) {
dx = x - x0;
dy = y - y0;
x1 = x0 + dx * c;
y1 = y0 + dx / s;
x = x1;
y = y1;
}
endTime = clock();
elapsedTime = ((double)endTime - startTime)/CLOCKS_PER_SEC;;
printf("Floating-Point Loop: %f seconds for %lu iterations\n", elapsedTime,
num_iterations);
MFLOPS = (1 / 1000000.0) * num_operations_per_loop * num_iterations / (double)
(elapsedTime - emptyLoopTime);
printf("Elapsed Time: %f\n", elapsedTime);
printf("MFLOPS: %f\n", MFLOPS);
fprintf(elapsedMflops, "%f\n", elapsedTime);
fprintf(Mflops, "%f\n", MFLOPS);
}
    *
    * */

}
