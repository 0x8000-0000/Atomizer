#!/bin/sh

java -cp ${the.classpath}:${project.build.directory}/${project.build.finalName}.${project.packaging} net.signbit.tools.atomizer.export.VisualizeClasses $1 $2
