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
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.util.MathArrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;

import net.signbit.tools.atomizer.ClassRef;
import net.signbit.tools.atomizer.PackageRef;

public class ABC
{
   private static final Logger logger = LoggerFactory.getLogger(ABC.class);

   private final ArrayList<ClassRef> classRefs;

   private ArrayList<ABCluster> clusters;
   private int populationSize;

   private HashSet<ABCluster> disconnectedSets;

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

      disconnectedSets = new HashSet<>();

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
      clusters = new ArrayList<ABCluster>(populationSize);
      for (int ii = 0; ii < populationSize; ii ++)
      {
         // TODO: consider use a normally distributed bias
         double bias = rdg.nextUniform(BIAS_MIN, BIAS_MAX);
         clusters.add(new ABCluster(classRefs, rdg, bias));
      }
   }

   public void performBigStep()
   {
      /*
       * generate new population
       */
      for (int ii = 0; ii < populationSize; ii++)
      {
         int selector = rdg.nextInt(0, classRefs.size() - 1);
         clusters.get(ii).scheduleElement(classRefs.get(selector));
      }

      Stream<ABCluster> mutatedElementsStream =
            clusters.parallelStream().map(ABCluster::mutateByPackageUsingScheduled);

      selectBestClusters(mutatedElementsStream);
   }

   public void performStep()
   {
      // TODO: use more CPUs

      /*
       * generate new population
       */
      for (int ii = 0; ii < populationSize; ii++)
      {
         int selector = rdg.nextInt(0, classRefs.size() - 1);
         clusters.get(ii).scheduleElement(classRefs.get(selector));
      }

      Stream<ABCluster> mutatedElementsStream = clusters.parallelStream()
            .map(ABCluster::mutateByClassUsingScheduled);

      selectBestClusters(mutatedElementsStream);
   }

   private void selectBestClusters(Stream<ABCluster> mutatedElementsStream)
   {
      clusters = new ArrayList<>(Stream.concat(clusters.stream(), mutatedElementsStream)
            .sorted()
            .limit(POPULATION_SIZE)
            .collect(toList()));

      int ii = 0;
      final int limit = populationSize - 1;
      while (ii < limit)
      {
         if (clusters.get(ii).isDegenerate())
         {
            /*
             * eliminate trivial clusters
             */
            double bias = rdg.nextUniform(BIAS_MIN, BIAS_MAX);
            clusters.set(ii, new ABCluster(classRefs, rdg, bias));
         }
         else if (0 == clusters.get(ii).getScore())
         {
            /*
             * found two non-connected components; need to extract the
             * smaller one, and continue cleaving the bigger component
             */
            disconnectedSets.add(clusters.get(ii));

            double bias = rdg.nextUniform(BIAS_MIN, BIAS_MAX);
            clusters.set(ii, new ABCluster(classRefs, rdg, bias));
         }
         else if (clusters.get(ii).equals(clusters.get(ii + 1)))
         {
            /*
             * eliminate duplicates
             */
            double bias = rdg.nextUniform(BIAS_MIN, BIAS_MAX);
            clusters.set(ii, new ABCluster(classRefs, rdg, bias));
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

         int bestScore = abc.clusters.get(0).getScore();
         int worstScore = abc.clusters.get(abc.populationSize - 1).getScore();

         logger.info("Package step {} completed in {}ms; best score {}, worst score {}", ii, (stepEndTime - stepStartTime) / 1000000, bestScore, worstScore);
      }

      for (int ii = 0; ii < RUN_CYCLE_COUNT; ii ++)
      {
         final long stepStartTime = System.nanoTime();
         abc.performStep();
         final long stepEndTime = System.nanoTime();

         int bestScore = abc.clusters.get(0).getScore();
         int worstScore = abc.clusters.get(abc.populationSize - 1).getScore();

         logger.info("Class step {} completed in {}ms; best score {}, worst score {}", ii, (stepEndTime - stepStartTime) / 1000000, bestScore, worstScore);
      }

      StringBuilder sb = new StringBuilder();
      sb.append("Population size: ");
      sb.append(POPULATION_SIZE);
      sb.append('\n');

      sb.append("Cycles: ");
      sb.append(RUN_CYCLE_COUNT);
      sb.append('\n');

      sb.append("Number of distinct disconnected clusters: ");
      sb.append(abc.disconnectedSets.size());
      sb.append('\n');

      sb.append("Best cluster: score ");
      sb.append(abc.clusters.get(0).getScore());
      sb.append('\n');

      FileWriter writer = new FileWriter(args[3]);
      writer.append(sb.toString());
      for (int ii = 0; ii < 5; ii ++)
      {
         writer.append("Cluster #");
         writer.append(Integer.toString(ii));
         writer.append('\n')
         abc.clusters.get(ii).writeTo(writer);
         writer.append("----------------------------");
      }
      writer.close();
   }
}
