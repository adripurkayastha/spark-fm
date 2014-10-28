package de.kp.spark.fm.source
/* Copyright (c) 2014 Dr. Krusche & Partner PartG
* 
* This file is part of the Spark-FM project
* (https://github.com/skrusche63/spark-fm).
* 
* Spark-FM is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* Spark-FM is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* Spark-FM. 
* 
* If not, see <http://www.gnu.org/licenses/>.
*/

import org.apache.spark._
import org.apache.spark.SparkContext._

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import de.kp.spark.fm.{Configuration,SparseVector}

class FileSource(@transient sc:SparkContext) extends Source(sc) {

  val input = Configuration.file()
  
  /**
   * Read data from file system: it is expected that the lines with
   * the respective text file are already formatted in the SPMF form
   */
  override def connect(params:Map[String,String] = Map.empty[String,String]):RDD[(Int,(Double,SparseVector))] = {
    
    val num_partitions = sc.broadcast(params("num_partitions").toInt)
    val randomizedDS = sc.textFile(input).map(line => {
    
      val ix = new java.util.Random().nextInt(num_partitions.value)
      
      val parts = line.split(',')
      
      val target   = parts(0).toDouble
      val features = parts(1).trim().split(' ').map(_.toDouble)

      (ix, (target,buildSparseVector(features)))
   
    })
    
    val partitioner = new Partitioner() {
      
      def numPartitions = num_partitions.value
      def getPartition(key: Any) = key.asInstanceOf[Int]
      
    }
    
    randomizedDS.partitionBy(partitioner)
   
  }
  
}