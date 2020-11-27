/**
 * Copyright (C) 2020-2021. Ricky Galliani. All Rights Reserved.
 * Email: pjgalliani@gmail.com
 */

package votedperceptron

import example.Example
import perceptron.Perceptron

import scala.annotation.tailrec
import scala.util.Random

class VotedPerceptron(var weights: List[(List[Double], Double)] = List[(List[Double], Double)](),
                      val maxEpochs: Int = MaxEpochs) {

  private val random = new Random

  /**
   * Implements the Voted perceptron.Perceptron learning algorithm:
   * - http://curtis.ml.cmu.edu/w/courses/index.php/Voted_Perceptron
   */
  def train(examples: List[Example]): List[(List[Double], Double)] = {
    val numExamples = examples.length
    if (numExamples == 0) throw new IllegalStateException("No training examples passed.")

    @tailrec
    def trainEpoch(epoch: Int,
                   curWeights: List[Double],
                   pocketWeightVotes: WeightVotes): List[(List[Double], Double)] = {
      if (epoch >= maxEpochs) {
        pocketWeightVotes.getFinalWeights
      }
      else {
        val randExampleInds = random.shuffle((0 until numExamples).toList)
        var curVotes = 0 // Number of examples this set of weights classifies correctly
        var stillPerfect = true
        while (stillPerfect && curVotes < numExamples) {
          val example = examples(randExampleInds(curVotes))
          val prediction = Perceptron.predict(curWeights, example.X)
          stillPerfect = prediction == example.y
          curVotes = if (stillPerfect) curVotes + 1 else curVotes
        }
        if (stillPerfect) { // current weights linearly separate the dataset
          List((curWeights, 1.0))
        }
        else {
          // Update current weights: w = w + x * y where (x, y) is a random misclassified example
          val mistakes = Perceptron.misclassifiedExamples(curWeights, examples)
          val randomMistake = mistakes(random.nextInt(mistakes.length))
          val dw = (List(1.0) ::: randomMistake.X).map(_ * randomMistake.y)
          val newWeights = curWeights.zip(dw).map { case (w, d) => w + d }
          // Add current weights to set of past weights
          val newWeightVotes = {
            if (curVotes == 0) {
              pocketWeightVotes
            }
            else {
              WeightVotes(curWeights :: pocketWeightVotes.weights, curVotes :: pocketWeightVotes.votes)
            }
          }
          trainEpoch(epoch + 1, newWeights, newWeightVotes)
        }
      }
    }

    // Training for the first time, initialize weights to 0.0 as only "voting" weights
    val modDim = examples.head.X.length + 1
    val initialWeights = List.fill(modDim)(0.0)
    val pocketWeightVotes = WeightVotes()
    weights = List((initialWeights, 1.0))
    trainEpoch(epoch = 1, curWeights = initialWeights, pocketWeightVotes = pocketWeightVotes)
  }

  def predict(X: List[Double]): Int = VotedPerceptron.predict(weights, X)

  def predictBatch(Xs: List[List[Double]]): List[Int] = Xs.map(predict)

}

object VotedPerceptron {

  def predict(weights: List[(List[Double], Double)], x: List[Double]): Int = {
    val score = weights.map { case (w, v) => Perceptron.predict(w, x) * v }.sum
    if (score >= 0) 1 else -1
  }

}