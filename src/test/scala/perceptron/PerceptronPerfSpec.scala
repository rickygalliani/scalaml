/**
 * Copyright (C) 2020-2021. Ricky Galliani. All Rights Reserved.
 * Email: pjgalliani@gmail.com
 */

package perceptron

import example.Example
import org.scalameter.api._
import utility.TestUtility

object PerceptronPerfSpec extends Bench.LocalTime {
  
  val MinSize: Int = 10
  val MaxSize: Int = 100
  val StepSize: Int = 5
  val NumOutliers: Int = MaxSize / MinSize

  val TestSizes: Gen[Int] = Gen.range("numExamples")(MinSize, MaxSize, StepSize)

  val LinearlySeparableExamples: List[Example] = TestUtility.generateBinaryExamples(MaxSize, 0)
  val LinearlyInseparableExamples: List[Example] = TestUtility.generateBinaryExamples(MaxSize, NumOutliers)

  performance of "Linearly Inseparable Case: perceptron.Perceptron" in {
    measure method "train" in {
      using(TestSizes) in { numExamples =>
        val p = new Perceptron()
        val examples = LinearlyInseparableExamples.take(numExamples)
        p.train(examples)
      }
    }
  }

  performance of "Linearly Separable Case: perceptron.Perceptron" in {
    measure method "train" in {
      using(TestSizes) in { numExamples =>
        val p = new Perceptron()
        val examples = LinearlySeparableExamples.take(numExamples)
        p.train(examples)
      }
    }
  }

}