/**
 * The MIT License
 *
 * Copyright (c) 2013 Pablo R. Mier <pablo.rodriguez.mier@usc.es>.
 * Centro de Investigación en Tecnoloxías da Información (CITIUS) http://citius.usc.es
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * Original source: https://github.com/pablormier/parallel-loops
 */

package net.ftb.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.*;

import net.ftb.data.Settings;
import net.ftb.log.Logger;

/**
 * <p>This class contains some useful classes and methods to parallelize code in an
 * easy and fluent way. Parallel class is self-contained to enable an easier
 * integration and reuse in different java projects. Simple example:</p>
 * <pre>
 *     {@code Parallel.ForEach(elements, new Parallel.Action<String>() {
 *              public void doAction(String s) {
 *                  System.out.println("Processing element " + s + " on thread " + Thread.currentThread().getName());
 *              }
 *            });
 *     }
 * </pre>
 *
 * <p>The ForEach function can be used as a parallel map function to transform a collection of elements
 * in parallel. For example, suppose we want to convert a list of words to upper case:</p>
 * <pre>
 *     {@code Collection<String> upperCaseWords = new ForEach<Integer,String>(elements)
 *                  .apply(new Function<String>(){
 *                       public Integer apply(String element){
 *                             return element.toUpperCase();
 *                       }
 *                   }).values();
 *     }
 * </pre>
 *
 * @author Pablo Rodríguez Mier <pablo.rodriguez.mier@usc.es>
 */
public final class Parallel
{

    private Parallel()
    {
        throw new RuntimeException("Use Parallel static methods");
    }

    /**
     * First-order function interface
     *
     * @param <E> Element to transform
     * @param <V> Result of the transformation
     * @author Pablo Rodríguez Mier
     */

    public static interface F<E, V>
    {
        /**
         * Apply a function over the element e.
         *
         * @param e Input element
         * @return transformation result
         */
        V apply (E e);
    }

    /**
     * Action class can be used to define a concurrent task that does not return
     * any value after processing the element.
     *
     * @param <E> Element processed within the action
     */
    public static abstract class Action<E> implements F<E, Void>
    {

        /**
         * This method is final and cannot be overridden. It applies the action
         * implemented by {@link Action#doAction(Object)}.
         */
        public final Void apply (E element)
        {
            doAction(element);
            return null;
        }

        /**
         * Defines the action that will be applied over the element. Every
         * action must implement this method.
         *
         * @param element element to process
         */
        public abstract void doAction (E element);
    }

    /**
     * This class provides some useful methods to handle the execution of a
     * collection of tasks.
     *
     * @param <V> value of the task
     */
    public static class TaskHandler<V>
    {
        private Collection<Future<V>> runningTasks = new LinkedList<Future<V>>();
        private ExecutorService executorService;

        public TaskHandler(ExecutorService executor, Iterable<Callable<V>> tasks, boolean poolSizeCheck, int maxPool, int sleep)
        {

            this.executorService = executor;
            for (Callable<V> task : tasks)
            {
                while (poolSizeCheck && (((ThreadPoolExecutor) executor).getTaskCount() - ((ThreadPoolExecutor) executor).getCompletedTaskCount()) > maxPool)
                {
                    if (Settings.getSettings().getDebugLauncher())
                    {
                        Logger.logDebug("system time: " + System.currentTimeMillis());
                        Logger.logDebug("task count: " + (((ThreadPoolExecutor) executor).getTaskCount() - ((ThreadPoolExecutor) executor).getCompletedTaskCount()));
                    }

                    try
                    {
                        Thread.sleep(sleep);
                    }
                    catch (Exception e)
                    {
                    }
                }
                runningTasks.add(executor.submit(task));
            }
        }

        public TaskHandler(ExecutorService executor, Iterable<Callable<V>> tasks)
        {

            this.executorService = executor;
            for (Callable<V> task : tasks)
            {
                runningTasks.add(executor.submit(task));
            }
        }

        /**
         * Get the current tasks (futures) that are being executed.
         *
         * @return Collection of futures
         * @see Future
         */
        public Collection<Future<V>> tasks ()
        {
            return this.runningTasks;
        }

        /**
         * This function is equivalent to
         * {@link ExecutorService#awaitTermination(long, TimeUnit)}
         *
         * @see ExecutorService#awaitTermination(long, TimeUnit)
         */
        public boolean wait (long timeout, TimeUnit unit) throws InterruptedException
        {
            return this.executorService.awaitTermination(timeout, unit);
        }

        /**
         * Retrieves the result of the transformation of each element (the value
         * of each Future). This function blocks until all tasks are terminated.
         *
         * @return a collection with the results of the elements transformation
         * @throws InterruptedException
         * @throws ExecutionException
         */
        public Collection<V> values () throws InterruptedException, ExecutionException
        {
            Collection<V> results = new LinkedList<V>();
            for (Future<V> future : this.runningTasks)
            {
                V result = future.get();
                if (result != null)
                    results.add(future.get());
            }
            return results;
        }

        public void shutdown ()
        {
            executorService.shutdown();
        }
    }

    /**
     * Class to generate a parallelized version of the for each loop.
     * @param <E> elements to iterate over.
     * @param <V> processed element type result.
     */
    public static class ForEach<E, V> implements F<F<E, V>, TaskHandler<V>>
    {
        // Source elements
        private Iterable<E> elements;
        private boolean poolSizeCheck = false;
        private int sleep = 0;
        private int maxTasksInPool = 0;
        //
        // Default executor will run tasks in four threads
        private ExecutorService executor = Executors.newFixedThreadPool(4);

        public ForEach(Iterable<E> elements)
        {
            this.elements = elements;
        }

        /**
         * Configure Pool Size.
         * Will limit speed of the apply().
         *
         * @param tasks Size of the tasks in pool
         * @param sleepTime Time to sleep(in ms) before trying add more tasks in pool
         * @return a ForEach instance
         */
        public ForEach<E, V> configurePoolSize (int tasks, int sleepTime)
        {
            poolSizeCheck = true;
            sleep = sleepTime;
            maxTasksInPool = tasks;
            return this;
        }

        /**
         * Configure the number of available threads that will be used. Note
         * that this configuration has no effect if a custom executor
         * {@link ForEach#customExecutor(ExecutorService)} is provided.
         *
         * @param threads number of threads to use
         * @return a ForEach instance
         */
        public ForEach<E, V> withFixedThreads (int threads)
        {
            this.executor = Executors.newFixedThreadPool(threads);
            return this;
        }

        /**
         * Set a custom executor service
         *
         * @param executor ExecutorService to use
         * @return the instance of ForEach configured with the new executor
         *         service.
         */
        public ForEach<E, V> customExecutor (ExecutorService executor)
        {
            this.executor = executor;
            return this;
        }

        /**
         *
         * Encapsulates the ForEach instance into a Callable that retrieves a
         * TaskHandler with the invoked tasks. Example:
         *
         * <pre>
         *     {@code Collection<Double> numbers = new Collection<Double>(...);
         *       Callable<TaskHandler<V>> forEach = new ForEach<Double, String>(numbers)
         *              .prepare(new F<Double, String>() {
         *                  String apply(Double e) {
         *                      return e.toString();
         *                  }
         *              });
         *     forEach.call().values();
         *     }
         * </pre>
         *
         * @param f
         * @return
         */
        public Callable<TaskHandler<V>> prepare (final F<E, V> f)
        {
            return new Callable<Parallel.TaskHandler<V>>()
            {
                public TaskHandler<V> call () throws Exception
                {
                    return new ForEach<E, V>(elements).apply(f);
                }
            };
        }

        public TaskHandler<V> apply (F<E, V> f)
        {
            return new TaskHandler<V>(executor, map(elements, f), poolSizeCheck, maxTasksInPool, sleep);
        }

        private Iterable<Callable<V>> map (final Iterable<E> elements, final F<E, V> f)
        {
            return new Iterable<Callable<V>>()
            {
                @Override
                public Iterator<Callable<V>> iterator ()
                {
                    return new Iterator<Callable<V>>()
                    {
                        Iterator<E> it = elements.iterator();

                        @Override
                        public boolean hasNext ()
                        {
                            return it.hasNext();
                        }

                        @Override
                        public Callable<V> next ()
                        {
                            final E e = it.next();
                            return new Callable<V>()
                            {
                                public V call () throws Exception
                                {
                                    return f.apply(e);
                                }
                            };
                        }

                        @Override
                        public void remove ()
                        {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            };
        }

    }

    /**
     * InterruptedExceptions occurred during the execution will be thrown as
     * RuntimeExceptions. To handle these interruptions, use new For.Each
     * instead of this static method.
     *
     * @param elements
     * @param task
     */
    public static <A, V> Collection<V> ForEach (Iterable<A> elements, F<A, V> task)
    {
        try
        {
            TaskHandler<V> loop = new ForEach<A, V>(elements).apply(task);
            Collection<V> values = loop.values();
            loop.executorService.shutdown();
            return values;
        }
        catch (Exception e)
        {
            throw new RuntimeException("ForEach method exception. " + e.getMessage());
        }
    }

    /**
     * Perform a parallel iteration, similar to {@code for(i=from;i<to;i++)}
     * but launching one thread per iteration.
     *
     * @param from   starting index
     * @param to     upper bound
     * @param action the action to perform in each iteration
     */
    public static void For (final long from, final long to, final Action<Long> action)
    {

        ForEach(new Iterable<Long>()
        {
            public Iterator<Long> iterator ()
            {
                return new Iterator<Long>()
                {
                    private long current = from;

                    public boolean hasNext ()
                    {
                        return current < to;
                    }

                    public Long next ()
                    {
                        return current++;
                    }

                    public void remove ()
                    {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        }, action);
    }

}
