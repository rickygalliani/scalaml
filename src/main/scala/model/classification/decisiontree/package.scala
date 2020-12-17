/**
 * Copyright (C) 2020-2021. Ricky Galliani. All Rights Reserved.
 * Email: pjgalliani@gmail.com
 */

package model.classification

package object decisiontree {
  val LogLevelSeed: Int = 51
  val TrainSeed: Int = 71
  val MaxDepth: Int = 5
  val EntropyEpsilon: Double = 0.01
  val NumThresholds = 3
  val Thresholds: List[Double] = (1 to NumThresholds).map(x => 1.0 * x / (NumThresholds + 1)).toList
}