/* Sum.Java - last edited 4/11/2014 by C. Herbert
 * This application uses the Java Fork/Join Framework to achieve true parallel
 * processing combined with time-sharing mutitasking. The purpose of the code 
 * is to demonstrate parallel processing in Java.
 * 
 * The code sums an array of 1,000 randomly generated intergers (1 <= a[i] <= 100)
 * with a 10 millisecond time delay added to each sum operation. The Sum class 
 * extends the RecursiveTask class, recursively splits the array into parts of 
 * at most 50 elements assigned to separate processes to be summed iteratively.
 * 
 * It uses a ForJoinPool object to allow the user to set the maximum number of 
 * allowable concurrent processes. Hence, it is a hybrid recursive/iterative algorithm.
 *
 * Without the 10 millisecond time delay, processing times are trivial, 
 * even for large arrays (less than 1/100 sec. for 1 million elements).
 */
 
package sum;

import java.util.concurrent.*;
import java.util.Scanner;

// class for managing ForkJoinPool settings
class Globals {

    static int processes = 1;   // set default number of processes to 1
    static ForkJoinPool fjPool; // ForkJoinPool object variable
} // end class Globals
//*****************************************************************************

 class Sum extends RecursiveTask<Long> {
    
    // set constant to switch to iterative sequential processes at n = 50
    static final int SEQUENTIAL_THRESHOLD = 50;
    int low;        // low (left) end of dataset
    int high;       // high (right end of dataset
    long[] array;

    // Sum constructor lo and hi establish section of array for this Sum object
    Sum(long[] arr, int lo, int hi) {
        array = arr;
        low = lo;
        high = hi;
    } // end Sum constructor
    //****************************************************************

    // the compute method is the hybrid summation algorithm
    protected Long compute() {

        // if below threshold, computer iterative sum 
        if (high - low < SEQUENTIAL_THRESHOLD) {
            long sum = 0;
            // place add a random value to the array and add it to the sum
            for (int i = low; i < high; ++i) {
                sum = sum + array[i];
                // sleep for 10 milliseconds to delay operation
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } // end try catch
                
            }  //end for
            return sum;
        } // end if
        
        // else perform recursion 
        else {
            
            // find midpoint
            int mid = low + (high - low) / 2;
            // find sum of left half
            Sum left = new Sum(array, low, mid);
            // find sum of left half
            Sum right = new Sum(array, mid, high);
            
            //separate into different processes, then join results
            left.fork();
            long rightAns = right.compute();
            long leftAns = left.join();
            return leftAns + rightAns;
        } // end else
    } // end  compute()

    // the sumArray method ivokes processes from the pool of processes
    static long sumArray(long[] array) {
        return Globals.fjPool.invoke(new Sum(array, 0, array.length));
    }  // end sumArray()
 	 //**********************************************************************************

    /* The main method asks the user to set the maximum number of processes that will be
     * allowed to run concurrently.  It casn exceed the number of processors
     * because of time-sharing mutitasking as well as parallel processing.
     */
    public static void main(String[] args) {

        // variable to hold the sum of the values in the array
        long sum = 0;

        Scanner kb = new Scanner(System.in);

        System.out.println("Enter the maximum number of concurrent processes for this code:");
        Globals.processes = kb.nextInt();

        //set the maximum number of processes;
        Globals.fjPool = new ForkJoinPool(Globals.processes);

        // declare a long array and load it with random values
        long[] myArray = new long[1000];
        for (int i = 0; i < myArray.length; ++i) 
                myArray[i] = (long) (Math.random() * 100 + 1);

        // get the start time in nanoseconds
        long startTime = System.nanoTime();

        // sum the array
        sum = sumArray(myArray);

        // get the end time in nanoseconds
        long endTime = System.nanoTime();

        // calculate elapsed time in nanoseconds
        long duration = endTime - startTime;

        // print the sum of the array
        System.out.printf("the sum of the values in the array is: %-,12d%n", sum);

        // print the elapsed time in seconds   (nanaoseconds/ 1 billion)
        System.out.printf("The algorithm took %12.8f seconds.%n", (double) duration / 1.0e+09);

    } // end main
} // end class Sum
