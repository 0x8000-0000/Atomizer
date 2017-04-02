Potential dependencies:

   <!-- http://watchmaker.uncommons.org/
        Apache 2.0 -->
   <dependency>
      <groupId>org.uncommons.watchmaker</groupId>
      <artifactId>watchmaker-framework</artifactId>
      <version>0.7.1</version>
   </dependency>

   <!-- http://moeaframework.org/ 
        LGPL 3.0 -->
   <dependency>
      <groupId>org.moeaframework</groupId>
      <artifactId>moeaframework</artifactId>
      <version>2.12</version>
   </dependency>

Algorithm:

   A population member is a bi-partite clustering of the set S into A and B.
      Compute the number of dependencies from A to B and the number of dependencies from B to A.
      The lower of the two numbers is the fitness score of the member.

   To create the initial population:
      Randomly divide the set of classes in two groups: A and B

   Mutations:
      Variant 1:
         Moving an element from A to B
         Moving an element from B to A
         Swapping elements between A and B
      Variant 2:
         Select a random element from set S and change its cluster membership
