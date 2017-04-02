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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.zip.ZipFile;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.util.MathArrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.signbit.tools.atomizer.ClassRef;

public class ABC
{
   private static final Logger logger = LoggerFactory.getLogger(ABC.class);

   final private ArrayList<ClassRef> classRefs;

   private ArrayList<ABCluster> clusterCandidates;
   private int populationSize;

   private RandomDataGenerator rdg;

   public ABC(final Collection<ClassRef> classSet, double bias, int popSize)
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
      clusterCandidates = new ArrayList<>(populationSize * 2);
      for (int ii = 0; ii < populationSize; ii ++)
      {
         clusterCandidates.add(new ABCluster(classRefs, rdg, bias));
      }
      for (int ii = 0; ii < populationSize; ii ++)
      {
         int selector = rdg.nextInt(0, populationSize - 1);
         clusterCandidates.add(populationSize + ii, clusterCandidates.get(ii).mutate(classRefs.get(selector)));
      }
      Collections.sort(clusterCandidates);
   }

   public void step()
   {
      /*
       * generate new population
       */
      for (int ii = 0; ii < populationSize; ii ++)
      {
         int selector = rdg.nextInt(0, populationSize - 1);
         clusterCandidates.set(populationSize + ii, clusterCandidates.get(ii).mutate(classRefs.get(selector)));
      }

      /*
       * sort the candidates; so the best ones are at the beginning of the pool
       */
      Collections.sort(clusterCandidates);
   }

   public static void main(String[] args) throws IOException
   {
      final long startTimeNano = System.nanoTime();

      ZipFile zipFile = new ZipFile(args[0]);

      Map<String, ClassRef> allClasses = ClassRef.loadClasses(zipFile);

      ClassRef.resolveDependencies(allClasses);

      final long classLoadEndTimeNano = System.nanoTime();
      logger.info("Class graph loaded in {}ms", (classLoadEndTimeNano - startTimeNano) / 1000000);

      // TODO: read input parameters
      int POPULATION_SIZE = 1024;
      int RUN_CYCLE_COUNT = 256;
      double BIAS = 0.33;

      ABC abc = new ABC(allClasses.values(), BIAS, POPULATION_SIZE);

      final long clusterInitializationEndTimeNano = System.nanoTime();
      logger.info("Initial clusters created in in {}ms", (clusterInitializationEndTimeNano - classLoadEndTimeNano) / 1000000);

      for (int ii = 0; ii < RUN_CYCLE_COUNT; ii ++)
      {
         final long stepStartTime = System.nanoTime();
         abc.step();
         final long stepEndTime = System.nanoTime();

         int bestScore = abc.clusterCandidates.get(0).getScore();
         int worstScore = abc.clusterCandidates.get(abc.populationSize - 1).getScore();

         logger.info("Step {} completed in {}ms; best score {}, worst score {}", ii, (stepEndTime - stepStartTime) / 1000000, bestScore, worstScore);
      }
   }
}
