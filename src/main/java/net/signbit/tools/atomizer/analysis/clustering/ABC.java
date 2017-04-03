/*
 * Copyright 2017 Florin Iucha <florin@signbit.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.signbit.tools.atomizer.analysis.clustering;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipFile;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.util.MathArrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.signbit.tools.atomizer.ClassRef;
import net.signbit.tools.atomizer.PackageRef;

public class ABC
{
   private static final Logger logger = LoggerFactory.getLogger(ABC.class);

   private final ArrayList<ClassRef> classRefs;

   private ABCluster[] clusters;
   private int populationSize;

   private RandomDataGenerator rdg;

   private static int POPULATION_SIZE = 1024;
   private static int RUN_CYCLE_COUNT = 256;

   private static final double BIAS_MIN = 0.05;
   private static final double BIAS_MAX = 0.45;

   public ABC(final Collection<ClassRef> classSet, int popSize)
   {
      rdg = new RandomDataGenerator();

      /*
       * shuffle the input class set and store in classRefs
       */
      ArrayList<ClassRef> support = new ArrayList<>(classSet);

      int[] indices = MathArrays.sequence(classSet.size(), 0, 1);
      MathArrays.shuffle(indices);

      classRefs = new ArrayList<>(classSet.size());
      for (int ii: indices)
      {
         classRefs.add(support.get(ii));
      }

      /*
       * initialize the population
       */
      populationSize = popSize;
      clusters = new ABCluster[populationSize * 2];
      for (int ii = 0; ii < populationSize; ii ++)
      {
         // TODO: consider use a normally distributed bias
         double bias = rdg.nextUniform(BIAS_MIN, BIAS_MAX);
         clusters[ii] = new ABCluster(classRefs, rdg, bias);
      }
      for (int ii = 0; ii < populationSize; ii ++)
      {
         int selector = rdg.nextInt(0, populationSize - 1);
         clusters[populationSize + ii] = clusters[ii].mutate(classRefs.get(selector));
      }

      Arrays.parallelSort(clusters);
   }

   public void performBigStep()
   {
      /*
       * generate new population
       */
      for (int ii = 0; ii < populationSize; ii++)
      {
         int selector = rdg.nextInt(0, populationSize - 1);
         clusters[populationSize + ii] = clusters[ii].mutateByPackage(classRefs.get(selector));
      }

      selectBestClusters();
   }

   public void performStep()
   {
      // TODO: use more CPUs

      /*
       * generate new population
       */
      for (int ii = 0; ii < populationSize; ii++)
      {
         int selector = rdg.nextInt(0, populationSize - 1);
         clusters[populationSize + ii] = clusters[ii].mutate(classRefs.get(selector));
      }

      selectBestClusters();
   }

   private void selectBestClusters()
   {
      /*
       * sort the candidates; so the best ones are at the beginning of the pool
       */
      Arrays.parallelSort(clusters);

      int ii = 0;
      while (ii < populationSize)
      {
         if (0 == clusters[ii].getScore())
         {
            /*
             * eliminate trivial clusters
             */
            double bias = rdg.nextUniform(BIAS_MIN, BIAS_MAX);
            clusters[ii] = new ABCluster(classRefs, rdg, bias);
         }
         else if (clusters[ii].equals(clusters[ii + 1]))
         {
            /*
             * eliminate duplicates
             */
            double bias = rdg.nextUniform(BIAS_MIN, BIAS_MAX);
            clusters[ii] = new ABCluster(classRefs, rdg, bias);
         }

         ii ++;
      }
   }

   public static void main(String[] args) throws IOException
   {
      final long startTimeNano = System.nanoTime();

      ZipFile zipFile = new ZipFile(args[0]);

      Map<String, ClassRef> allClasses = ClassRef.loadClasses(zipFile);

      allClasses = ClassRef.filterInnerClasses(allClasses);
      PackageRef.filterInnerClasses();
      ClassRef.resolveDependencies(allClasses);

      final long classLoadEndTimeNano = System.nanoTime();
      logger.info("Class graph loaded in {}ms", (classLoadEndTimeNano - startTimeNano) / 1000000);

      // TODO: read input parameters
      POPULATION_SIZE = Integer.valueOf(args[1]);
      RUN_CYCLE_COUNT = Integer.valueOf(args[2]);

      ABC abc = new ABC(allClasses.values(), POPULATION_SIZE);

      final long clusterInitializationEndTimeNano = System.nanoTime();
      logger.info("Initial {} clusters created in in {}ms", POPULATION_SIZE, (clusterInitializationEndTimeNano - classLoadEndTimeNano) / 1000000);

      for (int ii = 0; ii < RUN_CYCLE_COUNT; ii ++)
      {
         final long stepStartTime = System.nanoTime();
         abc.performBigStep();
         final long stepEndTime = System.nanoTime();

         int bestScore = abc.clusters[0].getScore();
         int worstScore = abc.clusters[abc.populationSize - 1].getScore();

         logger.info("Package step {} completed in {}ms; best score {}, worst score {}", ii, (stepEndTime - stepStartTime) / 1000000, bestScore, worstScore);
      }

      for (int ii = 0; ii < RUN_CYCLE_COUNT; ii ++)
      {
         final long stepStartTime = System.nanoTime();
         abc.performStep();
         final long stepEndTime = System.nanoTime();

         int bestScore = abc.clusters[0].getScore();
         int worstScore = abc.clusters[abc.populationSize - 1].getScore();

         logger.info("Class step {} completed in {}ms; best score {}, worst score {}", ii, (stepEndTime - stepStartTime) / 1000000, bestScore, worstScore);
      }

      StringBuilder sb = new StringBuilder();
      sb.append("Population size: ");
      sb.append(POPULATION_SIZE);
      sb.append('\n');
      sb.append("Cycles: ");
      sb.append(RUN_CYCLE_COUNT);
      sb.append('\n');
      sb.append("Best cluster: score ");
      sb.append(abc.clusters[0].getScore());
      sb.append('\n');

      FileWriter writer = new FileWriter(args[3]);
      writer.append(sb.toString());
      abc.clusters[0].writeTo(writer);
      writer.close();
   }
}
